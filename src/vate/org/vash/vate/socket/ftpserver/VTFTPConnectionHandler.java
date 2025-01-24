package org.vash.vate.socket.ftpserver;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;

import javax.net.ServerSocketFactory;
import javax.net.SocketFactory;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.SSLSocketFactory;

import com.guichaguri.minimalftp.FTPServer;
import com.guichaguri.minimalftp.handler.FTPConnectionHandler;

public class VTFTPConnectionHandler extends FTPConnectionHandler
{
  protected SocketFactory clientFactory;
  protected ServerSocketFactory serverFactory;
  
  public VTFTPConnectionHandler(VTFTPConnection connection, SocketFactory clientFactory, ServerSocketFactory serverFactory)
  {
    super(connection);
    this.clientFactory = clientFactory;
    this.serverFactory = serverFactory;
  }
  
  protected ServerSocket createPassiveServer(FTPServer server) throws IOException
  {
    return serverFactory.createServerSocket(0);
  }
  
  public Socket createDataSocket() throws IOException
  {
    if(passive && passiveServer != null)
    {
      if(secureData)
      {
        Socket acceptedSocket = passiveServer.accept();
        InetSocketAddress acceptedAddress = (InetSocketAddress) acceptedSocket.getRemoteSocketAddress();
        SSLSocketFactory sslfactory = con.getServer().getSSLContext().getSocketFactory();
        SSLSocket sslsocket = (SSLSocket)sslfactory.createSocket(acceptedSocket, acceptedAddress.getHostName(), acceptedAddress.getPort(), true);
        sslsocket.setUseClientMode(false);
        return sslsocket;
      }
      else
      {
        return passiveServer.accept();
      }
      
    }
    else if(secureData)
    {
      SSLSocketFactory sslfactory = con.getServer().getSSLContext().getSocketFactory();
      SSLSocket sslsocket = (SSLSocket)sslfactory.createSocket(clientFactory.createSocket(activeHost, activePort), activeHost, activePort, true);
      sslsocket.setUseClientMode(false);
      return sslsocket;
    }
    else
    {
      return clientFactory.createSocket(activeHost, activePort);
    }
  }
}