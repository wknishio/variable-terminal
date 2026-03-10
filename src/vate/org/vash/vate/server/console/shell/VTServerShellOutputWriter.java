package org.vash.vate.server.console.shell;

import org.vash.vate.VTSystem;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerShellOutputWriter extends VTTask
{
  private int readed;
  private VTServerConnection connection;
  private VTServerSession session;
  private final byte[] nullCommandFilter = new byte[] {};
  private final byte[] buffer = new byte[VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES * 4];
  private volatile byte[] firstCommandFilter = nullCommandFilter;
  private volatile byte[] secondCommandFilter = nullCommandFilter;
  
  public VTServerShellOutputWriter(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void setCommandFilter(String commandFilter, String encoding)
  {
    if (encoding != null && encoding.length() > 0)
    {
      try
      {
        firstCommandFilter = (commandFilter + "\r\n").getBytes(encoding);
        secondCommandFilter = (commandFilter + "\n").getBytes(encoding);
      }
      catch (Throwable t)
      {
        
      }
    }
    else
    {
      firstCommandFilter = (commandFilter + "\r\n").getBytes();
      secondCommandFilter = (commandFilter + "\n").getBytes();
    }
  }
  
  public void task()
  {
    String data = null;
    int offset = 0;
    while (!isStopped())
    {
      try
      {
        offset = 0;
        readed = session.getShellInputStream().read(buffer, 0, buffer.length);
        if (readed > 0 && !isStopped())
        {
          if (firstCommandFilter.length > 0 && readed >= firstCommandFilter.length && VTArrayComparator.arrayEquals(firstCommandFilter, buffer, 0, firstCommandFilter.length))
          {
            offset = firstCommandFilter.length;
          }
          else if (secondCommandFilter.length > 0 && readed >= secondCommandFilter.length && VTArrayComparator.arrayEquals(secondCommandFilter, buffer, 0, secondCommandFilter.length))
          {
            offset = secondCommandFilter.length;
          }
          if (readed - offset > 0)
          {
            data = null;
            try
            {
              if (session.getShellEncoding() != null)
              {
                data = new String(buffer, offset, readed - offset, session.getShellEncoding());
              }
              else
              {
                data = new String(buffer, offset, readed - offset);
              }
            }
            catch (Throwable t)
            {
              
            }
            if (data != null && data.length() > 0)
            {
              connection.getResultWriter().writeUTF(data);
              connection.getResultWriter().flush();
            }
          }
          firstCommandFilter = nullCommandFilter;
          secondCommandFilter = nullCommandFilter;
        }
        else
        {
          setStopped(true);
          break;
        }
      }
      catch (Throwable e)
      {
        setStopped(true);
        break;
      }
    }
    session.getShellProcessor().stopShell();
  }
}