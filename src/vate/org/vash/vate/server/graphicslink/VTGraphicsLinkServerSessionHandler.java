package org.vash.vate.server.graphicslink;

// import org.vash.vate.terminal.VTTerminal;

public class VTGraphicsLinkServerSessionHandler implements Runnable
{
  private VTGraphicsLinkServerSession session;
  
  public VTGraphicsLinkServerSessionHandler(VTGraphicsLinkServerSession session)
  {
    this.session = session;
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
        session.sendInitialScreenSize();
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
//    System.runFinalization();
//    System.gc();
  }
}