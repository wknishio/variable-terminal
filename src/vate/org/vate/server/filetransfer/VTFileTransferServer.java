package org.vate.server.filetransfer;

import org.vate.server.connection.VTServerConnection;
import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTFileTransferServer extends VTTask
{
  private VTServerConnection connection;
  private VTServerSession session;
  private VTFileTransferServerSessionHandler handler;

  public VTFileTransferServer(VTServerSession session)
  {
    this.connection = session.getConnection();
    this.session = session;
    this.handler = new VTFileTransferServerSessionHandler(this, new VTFileTransferServerSession(this));
  }

  public VTServerConnection getConnection()
  {
    return connection;
  }

  public VTServerSession getSession()
  {
    return session;
  }

  public VTFileTransferServerSessionHandler getHandler()
  {
    return handler;
  }

  public void run()
  {
    handler.run();
  }
}