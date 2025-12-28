package org.vash.vate.server.filetransfer;

public class VTFileTransferServerSessionHandler implements Runnable
{
  private VTFileTransferServer server;
  private VTFileTransferServerSession session;
  
  public VTFileTransferServerSessionHandler(VTFileTransferServer server, VTFileTransferServerSession session)
  {
    this.server = server;
    this.session = session;
  }
  
  public VTFileTransferServer getServer()
  {
    return server;
  }
  
  public VTFileTransferServerSession getSession()
  {
    return session;
  }
  
  public void run()
  {
    try
    {
      session.startSession();
      session.getTransaction().run();
      session.endSession();
    }
    catch (Throwable e)
    {
      
    }
  }
}