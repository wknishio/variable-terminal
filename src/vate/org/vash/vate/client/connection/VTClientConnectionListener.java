package org.vash.vate.client.connection;

public interface VTClientConnectionListener
{
  public void connectionStarted(VTClientConnection connection);
  public void connectionFinished(VTClientConnection connection);
}