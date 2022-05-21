package org.vash.vate.server.filetransfer;

import org.vash.vate.VT;

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
      server.getConnection().getFileTransferControlDataOutputStream().write(VT.VT_FILE_TRANSFER_SESSION_STARTED);
      server.getConnection().getFileTransferControlDataOutputStream().flush();
      server.getConnection().getFileTransferControlDataInputStream().read();
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
      server.getConnection().getFileTransferControlDataOutputStream().write(VT.VT_FILE_TRANSFER_SESSION_FINISHED);
      server.getConnection().getFileTransferControlDataOutputStream().flush();
      server.getConnection().getFileTransferControlDataInputStream().read();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
  }
}