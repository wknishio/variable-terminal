package org.vash.vate.client.console.remote;

import org.vash.vate.VTSystem;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.task.VTTask;

public class VTClientRemoteConsoleReader extends VTTask
{
  private VTClientSession session;
  private VTClientConnection connection;
  private final byte[] buffer = new byte[VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES * 4];
  
  public VTClientRemoteConsoleReader(VTClientSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void task()
  {
    while (!isStopped())
    {
      try
      {
        //readChars = connection.getResultReader().read(resultBuffer, 0, resultBufferSize);
        String result = connection.getResultReader().readUTF(buffer);
        if (result.length() > 0)
        {
          VTMainConsole.print(result);
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        setStopped(true);
        break;
      }
    }
    setStopped(true);
    synchronized (session)
    {
      session.notifyAll();
    }
  }
}