package org.vash.vate.client.console.remote;

import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.task.VTTask;

public class VTClientRemoteConsoleReader extends VTTask
{
  //private static final int resultBufferSize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  // private int available;
  //private int readChars;
  //private final char[] resultBuffer = new char[resultBufferSize];
  // private VTClient client;
  private VTClientSession session;
  private VTClientConnection connection;
  
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
        VTMainConsole.print(connection.getResultReader().readUTF());
        VTMainConsole.flush();
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