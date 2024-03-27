package org.vash.vate.stream.multiplex;

import java.util.concurrent.ExecutorService;

public class VTMultiplexingControlThread implements Runnable
{
  private final VTMultiplexingConnection connection;
  private final ExecutorService executor;
  // private int tunnelType = VTTunnelConnection.TUNNEL_TYPE_TCP;
  
  public VTMultiplexingControlThread(VTMultiplexingConnection connection, ExecutorService executor)
  {
    this.connection = connection;
    this.executor = executor;
  }
  
  public void run()
  {
    while (true)
    {
      try
      {
        String message = connection.getControlInputStream().readUTF();
        if (message != null && message.length() > 0)
        {
          // TODO: implement multiplexer
          executor.hashCode();
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        return;
      }
    }
  }
}