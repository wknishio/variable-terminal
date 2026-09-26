package org.vash.vate.socket.managed;

public interface VTManagedSocketListener
{
  public void connectionStarted(VTManagedSocket socket);
  public void connectionFinished(VTManagedSocket socket);
  public void sessionStarted(VTManagedSocket socket);
  public void sessionFinished(VTManagedSocket socket);
}