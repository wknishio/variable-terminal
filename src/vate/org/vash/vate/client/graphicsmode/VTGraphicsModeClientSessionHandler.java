package org.vash.vate.client.graphicsmode;

public class VTGraphicsModeClientSessionHandler implements Runnable
{
  private VTGraphicsModeClientSession session;

  public VTGraphicsModeClientSessionHandler(VTGraphicsModeClientSession session)
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
    // VTTerminal.print("\nVT>Remote graphics link stopped!\nVT>");
  }
}