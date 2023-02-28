package org.vash.vate.server.connection;

public class VTServerConnectionShutdownHook implements Runnable
{
  private VTServerConnection connection;
  
  public VTServerConnectionShutdownHook(VTServerConnection connection)
  {
    this.connection = connection;
  }
  
  public void run()
  {
    connection.closeSockets();
  }
}