package org.vash.vate.client.session;

public interface VTClientSessionListener
{
  public void sessionStarted(VTClientSession session);
  public void sessionFinished(VTClientSession session);
}
