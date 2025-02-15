package org.vash.vate.client.filetransfer;

import org.vash.vate.VT;

public class VTFileTransferClientSession
{
  private VTFileTransferClient client;
  private VTFileTransferClientTransaction transaction;
  
  public VTFileTransferClientSession(VTFileTransferClient client)
  {
    this.client = client;
    this.transaction = new VTFileTransferClientTransaction(this);
  }
  
  public VTFileTransferClient getClient()
  {
    return client;
  }
  
  public VTFileTransferClientTransaction getTransaction()
  {
    return transaction;
  }
  
  public void startSession()
  {
    try
    {
      client.getConnection().getFileTransferStartDataOutputStream().write(VT.VT_FILE_TRANSFER_SESSION_STARTED);
      client.getConnection().getFileTransferStartDataOutputStream().flush();
      client.getConnection().getFileTransferStartDataInputStream().read();
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
      client.getConnection().closeFileTransferStreams();
      //client.getConnection().getFileTransferStartDataOutputStream().write(VT.VT_FILE_TRANSFER_SESSION_FINISHED);
      //client.getConnection().getFileTransferStartDataOutputStream().flush();
      //client.getConnection().getFileTransferStartDataInputStream().read();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}