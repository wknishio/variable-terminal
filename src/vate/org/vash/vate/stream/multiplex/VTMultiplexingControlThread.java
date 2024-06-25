package org.vash.vate.stream.multiplex;

import java.util.concurrent.ExecutorService;

public class VTMultiplexingControlThread implements Runnable
{
  private final VTMultiplexingConnection connection;
  private final ExecutorService executorService;
  // private int tunnelType = VTTunnelConnection.TUNNEL_TYPE_TCP;
  
  public VTMultiplexingControlThread(VTMultiplexingConnection connection, ExecutorService executorService)
  {
    this.connection = connection;
    this.executorService = executorService;
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
          executorService.hashCode();
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