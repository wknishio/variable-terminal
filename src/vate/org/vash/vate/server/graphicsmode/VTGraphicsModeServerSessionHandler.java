package org.vash.vate.server.graphicsmode;

// import org.vash.vate.terminal.VTTerminal;

public class VTGraphicsModeServerSessionHandler implements Runnable
{
  private VTGraphicsModeServerSession session;

  public VTGraphicsModeServerSessionHandler(VTGraphicsModeServerSession session)
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
      // VTTerminal.setSystemOut();
      // VTTerminal.setSystemErr();
      // e.printStackTrace();
    }
    try
    {
      session.getSession().getConnection().resetGraphicsModeStreams();
    }
    catch (Throwable e)
    {
      // VTTerminal.setSystemOut();
      // VTTerminal.setSystemErr();
      // e.printStackTrace();
    }
    System.runFinalization();
    System.gc();
  }
}