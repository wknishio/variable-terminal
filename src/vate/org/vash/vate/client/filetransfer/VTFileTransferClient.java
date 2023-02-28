package org.vash.vate.client.filetransfer;

import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.task.VTTask;

public class VTFileTransferClient extends VTTask
{
  private VTClientConnection connection;
  private VTClientSession session;
  private VTFileTransferClientSessionHandler handler;
  
  public VTFileTransferClient(VTClientSession session)
  {
    this.connection = session.getConnection();
    this.session = session;
    this.handler = new VTFileTransferClientSessionHandler(this, new VTFileTransferClientSession(this));
  }
  
  public VTClientConnection getConnection()
  {
    return connection;
  }
  
  public VTClientSession getSession()
  {
    return session;
  }
  
  /*
   * public void setConnection(VTClientConnection connection) { this.connection
   * = connection; }
   */
  
  public VTFileTransferClientSessionHandler getHandler()
  {
    return handler;
  }
  
  public void setHandler(VTFileTransferClientSessionHandler handler)
  {
    this.handler = handler;
  }
  
  /*
   * public String[] split(String[] splitCommand, String command) { int x, y, z;
   * y = 0; z = 0; for (x = 0; x < command.length(); x++) { if
   * (command.charAt(x) == '|') { y++; } } splitCommand = new String[++y]; y =
   * 0; for (x = 0; x <= command.length(); x++) { if ((x == command.length()) ||
   * (command.charAt(x) == '|')) { splitCommand[y++] = command.substring(z, x);
   * z = ++x; } } return splitCommand; }
   */
  
  public void run()
  {
    handler.run();
  }
}