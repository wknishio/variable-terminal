package org.vash.vate.server.console.shell;

import org.vash.vate.VT;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerShellOutputWriter extends VTTask
{
  private static final int resultBufferSize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  private int readChars;
  private final char[] resultBuffer = new char[resultBufferSize];
  private VTServerConnection connection;
  private VTServerSession session;
  
  public VTServerShellOutputWriter(VTServerSession session)
  {
    this.session = session;
    this.connection = session.getConnection();
    this.stopped = false;
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }
  
  public void task()
  {
    // detectCharset();
    // shellInputStream = session.getShellInputStream();
    while (!stopped)
    {
      try
      {
        readChars = session.getShellOutputReader().read(resultBuffer, 0, resultBufferSize);
        if (readChars > 0 && !stopped)
        {
          connection.getResultWriter().write(resultBuffer, 0, readChars);
          connection.getResultWriter().flush();
        }
        else
        {
          stopped = true;
          break;
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        stopped = true;
        break;
      }
    }
    session.getShell().stopShell();
  }
}