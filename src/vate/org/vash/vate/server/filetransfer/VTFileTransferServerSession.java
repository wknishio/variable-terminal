package org.vash.vate.server.filetransfer;

import org.vash.vate.VTSystem;

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
      server.getConnection().getFileTransferStartDataOutputStream().write(VTSystem.VT_FILE_TRANSFER_SESSION_STARTED);
      server.getConnection().getFileTransferStartDataOutputStream().flush();
      server.getConnection().getFileTransferStartDataInputStream().read();
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
      //server.getConnection().getFileTransferStartDataOutputStream().write(VT.VT_FILE_TRANSFER_SESSION_FINISHED);
      //server.getConnection().getFileTransferStartDataOutputStream().flush();
      //server.getConnection().getFileTransferStartDataInputStream().read();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}