package org.vash.vate.client.session;

public interface VTClientSessionListener
{
  public void sessionCreated(VTClientSession session);
  public void sessionStarted(VTClientSession session);
  public void sessionFinished(VTClientSession session);
}
