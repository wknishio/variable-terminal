package org.vash.vate.socket.allocated;

import java.util.concurrent.ExecutorService;

public class VTAllocatedSocketManagerControlThread implements Runnable
{
  private final VTAllocatedSocketManager connection;
  private final ExecutorService executorService;
  // private int tunnelType = VTTunnelConnection.TUNNEL_TYPE_TCP;
  
  public VTAllocatedSocketManagerControlThread(VTAllocatedSocketManager connection)
  {
    this.connection = connection;
    this.executorService = connection.getExecutorService();
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