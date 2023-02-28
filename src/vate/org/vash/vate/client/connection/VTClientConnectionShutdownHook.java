package org.vash.vate.client.connection;

public class VTClientConnectionShutdownHook implements Runnable
{
  private VTClientConnection connection;
  
  public VTClientConnectionShutdownHook(VTClientConnection connection)
  {
    this.connection = connection;
  }
  
  public void run()
  {
    connection.closeSockets();
  }
}