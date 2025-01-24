package org.vash.vate.socket.ftpserver;

import java.io.File;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;

import com.guichaguri.minimalftp.FTPServer;
import com.guichaguri.minimalftp.api.IFTPListener;
import com.guichaguri.minimalftp.api.IUserAuthenticator;

public class VTFTPServer extends FTPServer
{
  protected SocketFactory clientFactory;
  protected ServerSocketFactory serverFactory;
  
  public VTFTPServer(IUserAuthenticator<File> auth, SocketFactory clientFactory, ServerSocketFactory serverFactory)
  {
    super(auth);
    this.clientFactory = clientFactory;
    this.serverFactory = serverFactory;
  }
  
  public InetAddress getAddress()
  {
    return null;
  }
  
  public void runConnection(Socket socket) throws IOException
  {
    VTFTPConnection con = createConnection(socket);
    synchronized(listeners) {
        for(IFTPListener l : listeners) {
            l.onConnected(con);
        }
    }
    synchronized(connections) {
        connections.add(con);
    }
    con.run();
  }
  
  public void endConnection() throws IOException
  {
    close();
  }
  
  protected VTFTPConnection createConnection(Socket socket) throws IOException
  {
    return new VTFTPConnection(this, socket, idleTimeout, 1024 * 8);
  }
}