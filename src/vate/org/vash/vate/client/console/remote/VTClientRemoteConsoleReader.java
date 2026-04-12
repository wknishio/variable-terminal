package org.vash.vate.client.console.remote;

import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

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
  private final CharsetDecoder decoder = VTSystem.getStrictCharsetDecoder("UTF-8");
  private OutputStream commandOutputStream;
  
  public VTClientRemoteConsoleReader(VTClientSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void setCommandOutputStream(OutputStream stream)
  {
    this.commandOutputStream = stream;
  }
  
  public void task()
  {
    String utf = "";
    int length = 0;
    while (!isStopped())
    {
      try
      {
        //readChars = connection.getResultReader().read(resultBuffer, 0, resultBufferSize);
        utf = null;
        length = connection.getResultReader().readData(buffer);
        if (commandOutputStream != null)
        {
          try
          {
            commandOutputStream.write(buffer, 0, length);
            commandOutputStream.flush();
          }
          catch (Throwable t)
          {
            
          }
        }
        else
        {
          try
          {
            utf = decoder.decode(ByteBuffer.wrap(buffer, 0, length)).toString();
          }
          catch (Throwable t)
          {
            
          }
          if (utf != null && utf.length() > 0)
          {
            VTMainConsole.print(utf);
          }
          else
          {
            try
            {
              VTMainConsole.write(buffer, 0, length);
            }
            catch (Throwable t)
            {
              
            }
          }
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