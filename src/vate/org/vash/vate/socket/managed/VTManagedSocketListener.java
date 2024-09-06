package org.vash.vate.socket.managed;

public interface VTManagedSocketListener
{
  public void connected(VTManagedSocket socket);
  public void disconnected(VTManagedSocket socket);
}