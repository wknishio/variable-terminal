package org.vash.vate.client.graphicsmode;

import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.task.VTTask;

public class VTGraphicsModeClient extends VTTask
{
  private VTGraphicsModeClientSessionHandler sessionHandler;
  
  public VTGraphicsModeClient(VTClientSession session)
  {
    super(session.getExecutorService());
    this.sessionHandler = new VTGraphicsModeClientSessionHandler(new VTGraphicsModeClientSession(session));
    // this.setStopped(true);
  }
  
  public boolean isFinished()
  {
    return sessionHandler.isFinished();
  }
  
  public void setFinished(boolean finished)
  {
    sessionHandler.setFinished(finished);
  }
  
  public boolean isStopped()
  {
    return sessionHandler.isStopped();
  }
  
  public void setStopped(boolean stopped)
  {
    sessionHandler.setStopped(stopped);
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