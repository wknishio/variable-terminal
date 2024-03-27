package org.vash.vate.tunnel.connection;

import java.util.concurrent.ExecutorService;

import org.vash.vate.task.VTTask;

public class VTTunnelConnectionHandler extends VTTask
{
  private VTTunnelConnection connection;
  private VTTunnelConnectionControlThread control;
  
  public VTTunnelConnectionHandler(VTTunnelConnection connection, ExecutorService executor)
  {
    this.connection = connection;
    this.control = new VTTunnelConnectionControlThread(connection, executor);
  }
  
  /*
   * public void setTunnelType(int tunnelType) {
   * this.thread.setTunnelType(tunnelType); }
   */
  
  public VTTunnelConnection getConnection()
  {
    return connection;
  }
  
  public void task()
  {
    try
    {
      connection.start();
      control.run();
      connection.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}