package org.vate.tunnel.connection;

import java.util.concurrent.ExecutorService;

import org.vate.task.VTTask;

public class VTTunnelConnectionHandler extends VTTask
{
  private VTTunnelConnection connection;
  private VTTunnelConnectionControlThread thread;

  public VTTunnelConnectionHandler(VTTunnelConnection connection, ExecutorService threads)
  {
    this.connection = connection;
    this.thread = new VTTunnelConnectionControlThread(connection, threads);
  }

  /*
   * public void setTunnelType(int tunnelType) {
   * this.thread.setTunnelType(tunnelType); }
   */

  public VTTunnelConnection getConnection()
  {
    return connection;
  }

  public void run()
  {
    try
    {
      connection.start();
      thread.run();
      connection.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}