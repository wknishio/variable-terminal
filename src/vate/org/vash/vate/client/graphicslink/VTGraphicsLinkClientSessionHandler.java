package org.vash.vate.client.graphicslink;

public class VTGraphicsLinkClientSessionHandler implements Runnable
{
  private VTGraphicsLinkClientSession session;
  
  public VTGraphicsLinkClientSessionHandler(VTGraphicsLinkClientSession session)
  {
    this.session = session;
  }
  
  public boolean isFinished()
  {
    return session.isFinished();
  }
  
  public void setFinished(boolean finished)
  {
    session.setFinished(finished);
  }
  
  public boolean isStopped()
  {
    return session.isStopped();
  }
  
  public void setStopped(boolean stopped)
  {
    session.setStopped(stopped);
  }
  
  public boolean isReadOnly()
  {
    return session.isReadOnly();
  }
  
  public void setReadOnly(boolean readOnly)
  {
    session.setReadOnly(readOnly);
  }
  
  /*
   * public void setHighQuality(boolean highQuality) {
   * session.setHighQuality(highQuality); }
   */
  
  public void run()
  {
    try
    {
      session.getSession().getConnection().resetGraphicsLinkStreams();
      if (session.verifySession())
      {
        session.receiveInitialScreenSize();
        session.startSession();
        session.waitSession();
        session.tryStopThreads();
        session.waitThreads();
        session.endSession();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    System.runFinalization();
    System.gc();
  }
}