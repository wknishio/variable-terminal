package org.vash.vate.server.console.shell;

import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

import org.vash.vate.VTSystem;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerShellOutputWriter extends VTTask
{
  //private int readed;
  private VTServerConnection connection;
  private VTServerSession session;
  private final byte[] buffer = new byte[VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES * 4];
  private CharsetDecoder decoder = VTSystem.getStrictCharsetDecoder(null);
  private final byte[] nullCommandFilter = new byte[] {};
  private volatile byte[] firstCommandFilter = nullCommandFilter;
  private volatile byte[] secondCommandFilter = nullCommandFilter;
  
  public VTServerShellOutputWriter(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void setShellEncoding(String shellEncoding)
  {
    try
    {
      decoder = VTSystem.getStrictCharsetDecoder(shellEncoding);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void setCommandFilter(String commandFilter, String encoding)
  {
    firstCommandFilter = nullCommandFilter;
    secondCommandFilter = nullCommandFilter;
    if (connection.getSilent())
    {
      return;
    }
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
    if (firstCommandFilter == nullCommandFilter || secondCommandFilter == nullCommandFilter)
    {
      try
      {
        firstCommandFilter = (commandFilter + "\r\n").getBytes();
        secondCommandFilter = (commandFilter + "\n").getBytes();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void task()
  {
    String utf = null;
    int offset = 0;
    int length = 0;
    //String encoding = null;
    while (!isStopped())
    {
      try
      {
        offset = 0;
        length = session.getShellInputStream().read(buffer, 0, buffer.length);
        if (length > 0 && !isStopped())
        {
          if (!connection.getSilent())
          {
            if (firstCommandFilter.length > 0 && length >= firstCommandFilter.length && VTArrayComparator.arrayEquals(firstCommandFilter, buffer, 0, firstCommandFilter.length))
            {
              offset = firstCommandFilter.length;
            }
            else if (secondCommandFilter.length > 0 && length >= secondCommandFilter.length && VTArrayComparator.arrayEquals(secondCommandFilter, buffer, 0, secondCommandFilter.length))
            {
              offset = secondCommandFilter.length;
            }
            if (length - offset > 0)
            {
              utf = null;
              try
              {
                utf = decoder.decode(ByteBuffer.wrap(buffer, 0, length)).toString();
              }
              catch (Throwable t)
              {
                
              }
              if (utf != null && utf.length() > 0)
              {
                connection.getShellWriter().writeUTF(utf);
                connection.getShellWriter().flush();
              }
              else
              {
                connection.getShellWriter().writeData(buffer, 0, length - offset);
                connection.getShellWriter().flush();
              }
            }
            firstCommandFilter = nullCommandFilter;
            secondCommandFilter = nullCommandFilter;
          }
          else
          {
            connection.getShellWriter().writeData(buffer, 0, length - offset);
            connection.getShellWriter().flush();
          }
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