package org.vash.vate.server.session;

public interface VTServerSessionListener
{
  public void sessionCreated(VTServerSession session);
  public void sessionStarted(VTServerSession session);
  public void sessionFinished(VTServerSession session);
}