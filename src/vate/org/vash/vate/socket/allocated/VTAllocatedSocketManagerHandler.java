package org.vash.vate.socket.allocated;

import org.vash.vate.task.VTTask;

public class VTAllocatedSocketManagerHandler extends VTTask
{
  private VTAllocatedSocketManager connection;
  private VTAllocatedSocketManagerControlThread control;
  
  public VTAllocatedSocketManagerHandler(VTAllocatedSocketManager connection)
  {
    super(connection.getExecutorService());
    this.connection = connection;
    this.control = new VTAllocatedSocketManagerControlThread(connection);
  }
  
  /*
   * public void setTunnelType(int tunnelType) {
   * this.thread.setTunnelType(tunnelType); }
   */
  
  public VTAllocatedSocketManager getConnection()
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