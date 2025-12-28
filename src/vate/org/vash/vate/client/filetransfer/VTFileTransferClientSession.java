package org.vash.vate.client.filetransfer;

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
      client.getConnection().resetFileTransferStreams();
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
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}