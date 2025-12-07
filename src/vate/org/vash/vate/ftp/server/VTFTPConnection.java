package org.vash.vate.ftp.server;

import java.io.IOException;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import vate.com.guichaguri.minimalftp.FTPConnection;

public class VTFTPConnection extends FTPConnection
{
  protected SocketFactory clientFactory;
  protected ServerSocketFactory serverFactory;
  protected ExecutorService executorService;
  
  public VTFTPConnection(VTFTPServer server, Socket con, int idleTimeout, int bufferSize) throws IOException
  {
    super(server, con, idleTimeout, bufferSize);
    clientFactory = server.clientFactory;
    serverFactory = server.serverFactory;
    executorService = server.executorService;
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
  
  protected void stop(boolean close) throws IOException
  {
    if(thread.isAlive() && !thread.isInterrupted())
    {
      thread.interrupt();
    }
    conHandler.onDisconnected();
    if(close)
    {
      con.close();
    }
  }
}