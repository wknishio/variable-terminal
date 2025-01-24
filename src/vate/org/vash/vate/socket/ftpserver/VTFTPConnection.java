package org.vash.vate.socket.ftpserver;

import java.io.IOException;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import com.guichaguri.minimalftp.FTPConnection;

public class VTFTPConnection extends FTPConnection
{
  protected SocketFactory clientFactory;
  protected ServerSocketFactory serverFactory;
  
  public VTFTPConnection(VTFTPServer server, Socket con, int idleTimeout, int bufferSize) throws IOException
  {
    super(server, con, idleTimeout, bufferSize);
    clientFactory = server.clientFactory;
    serverFactory = server.serverFactory;
  }
  
  public void start() throws IOException
  {
    this.conHandler = new VTFTPConnectionHandler(this, clientFactory, serverFactory);
    this.fileHandler = new VTFTPFileHandler(this);
    this.conHandler.registerCommands();
    this.fileHandler.registerCommands();
    this.conHandler.onConnected();
    this.thread = new ConnectionThread();
    this.thread.setDaemon(true);
    this.thread.start();
  }
  
  public void run() throws IOException
  {
    this.conHandler = new VTFTPConnectionHandler(this, clientFactory, serverFactory);
    this.fileHandler = new VTFTPFileHandler(this);
    this.conHandler.registerCommands();
    this.fileHandler.registerCommands();
    this.conHandler.onConnected();
    this.thread = new ConnectionThread();
    this.thread.setDaemon(true);
    this.thread.run();
  }
}