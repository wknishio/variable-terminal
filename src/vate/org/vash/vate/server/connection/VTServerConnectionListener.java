package org.vash.vate.server.connection;

public interface VTServerConnectionListener
{
  public void connectionStarted(VTServerConnection connection);
  public void connectionFinished(VTServerConnection connection);
}