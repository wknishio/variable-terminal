package org.vash.vate.server.graphicslink;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTGraphicsLinkServer extends VTTask
{
  private VTGraphicsLinkServerSessionHandler sessionHandler;
  
  public VTGraphicsLinkServer(VTServerSession session)
  {
    super(session.getExecutorService());
    this.sessionHandler = new VTGraphicsLinkServerSessionHandler(new VTGraphicsLinkServerSession(session));
    // this.setStopped(true);
  }
  
  public boolean isStopped()
  {
    return sessionHandler.isStopped();
  }
  
  public void setStopped(boolean stopped)
  {
    sessionHandler.setStopped(stopped);
    super.setStopped(stopped);
  }
  
  public boolean isReadOnly()
  {
    return sessionHandler.isReadOnly();
  }
  
  public void setReadOnly(boolean readOnly)
  {
    sessionHandler.setReadOnly(readOnly);
  }
  
  /*
   * public void setHighQuality(boolean highQuality) {
   * sessionHandler.setHighQuality(highQuality); }
   */
  
  public void task()
  {
    sessionHandler.run();
  }
}