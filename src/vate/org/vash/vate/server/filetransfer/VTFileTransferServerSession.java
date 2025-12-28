package org.vash.vate.server.filetransfer;

public class VTFileTransferServerSession
{
  private VTFileTransferServer server;
  private VTFileTransferServerTransaction transaction;
  
  public VTFileTransferServerSession(VTFileTransferServer server)
  {
    this.server = server;
    this.transaction = new VTFileTransferServerTransaction(this);
  }
  
  public VTFileTransferServer getServer()
  {
    return server;
  }
  
  public VTFileTransferServerTransaction getTransaction()
  {
    return transaction;
  }
  
  public void startSession()
  {
    try
    {
      server.getConnection().resetFileTransferStreams();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
  
  public void endSession()
  {
    try
    {
      server.getConnection().closeFileTransferStreams();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}