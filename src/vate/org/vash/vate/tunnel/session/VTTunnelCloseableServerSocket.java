package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

public class VTTunnelCloseableServerSocket extends ServerSocket implements Closeable
{
  private final ServerSocket serverSocket;
  
  public VTTunnelCloseableServerSocket(ServerSocket serverSocket) throws IOException
  {
    this.serverSocket = serverSocket;
  }
  
  public Socket accept() throws IOException
  {
    return serverSocket.accept();
  }
  
  public void bind(SocketAddress endpoint) throws IOException
  {
    serverSocket.bind(endpoint);
  }
  
  public void bind(SocketAddress endpoint, int backlog) throws IOException
  {
    serverSocket.bind(endpoint, backlog);
  }
  
  public boolean isBound()
  {
    return serverSocket.isBound();
  }
  
  public boolean isClosed()
  {
    return serverSocket.isClosed();
  }
  
  public void close() throws IOException
  {
    serverSocket.close();
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    serverSocket.setSoTimeout(timeout);
  }
  
  public int getLocalPort()
  {
    return serverSocket.getLocalPort();
  }
  
  public InetAddress getInetAddress()
  {
    return serverSocket.getInetAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return serverSocket.getLocalSocketAddress();
  }
}