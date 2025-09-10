package org.vash.vate.server.console.shell;

import org.vash.vate.VTSystem;
import org.vash.vate.security.VTArrayComparator;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerShellOutputWriter extends VTTask
{
  private static final int resultBufferSize = VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES;
  private int readChars;
  private final char[] resultBuffer = new char[resultBufferSize];
  private final char[] nullCommandFilter = new char[0];
  private volatile char[] firstCommandFilter = nullCommandFilter;
  private volatile char[] secondCommandFilter = nullCommandFilter;
  private VTServerConnection connection;
  private VTServerSession session;
  
  public VTServerShellOutputWriter(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void setCommandFilter(String commandFilter)
  {
    firstCommandFilter = (commandFilter + "\r\n").toCharArray();
    secondCommandFilter = (commandFilter + "\n").toCharArray();
  }
  
  public void task()
  {
    // detectCharset();
    // shellInputStream = session.getShellInputStream();
    int offset = 0;
    while (!isStopped())
    {
      try
      {
        offset = 0;
        readChars = session.getShellOutputReader().read(resultBuffer, 0, resultBufferSize);
        if (readChars > 0 && !isStopped())
        {
          if (firstCommandFilter.length > 0 && readChars >= firstCommandFilter.length && VTArrayComparator.arrayEquals(firstCommandFilter, resultBuffer, 0, firstCommandFilter.length))
          {
            offset = firstCommandFilter.length;
            firstCommandFilter = nullCommandFilter;
            secondCommandFilter = nullCommandFilter;
          }
          else if (secondCommandFilter.length > 0 && readChars >= secondCommandFilter.length && VTArrayComparator.arrayEquals(secondCommandFilter, resultBuffer, 0, secondCommandFilter.length))
          {
            offset = secondCommandFilter.length;
            firstCommandFilter = nullCommandFilter;
            secondCommandFilter = nullCommandFilter;
          }
          else
          {
            firstCommandFilter = nullCommandFilter;
            secondCommandFilter = nullCommandFilter;
          }
          if (readChars - offset > 0)
          {
            connection.getResultWriter().write(resultBuffer, offset, readChars - offset);
            connection.getResultWriter().flush();
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