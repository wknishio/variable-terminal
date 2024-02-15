package org.vash.vate.socket;

import java.io.Closeable;
import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.ServerSocketChannel;

public class VTServerSocket extends ServerSocket implements Closeable
{
  private final ServerSocket serverSocket;
  
  public VTServerSocket(ServerSocket serverSocket) throws IOException
  {
    this.serverSocket = serverSocket;
  }
  
  public Socket accept() throws IOException
  {
    Socket socket = serverSocket.accept();
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
    //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
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
  
  public int getSoTimeout() throws IOException
  {
    return serverSocket.getSoTimeout();
  }
  
  public int getReceiveBufferSize() throws SocketException
  {
    return serverSocket.getReceiveBufferSize();
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    return serverSocket.getReuseAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return serverSocket.getLocalSocketAddress();
  }
  
  public int getLocalPort()
  {
    return serverSocket.getLocalPort();
  }
  
  public InetAddress getInetAddress()
  {
    return serverSocket.getInetAddress();
  }
  
  public ServerSocketChannel getChannel()
  {
    return serverSocket.getChannel();
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    serverSocket.setSoTimeout(timeout);
  }
  
  public void setReceiveBufferSize(int size) throws SocketException
  {
    serverSocket.setReceiveBufferSize(size);
  }
  
  public void setReuseAddress(boolean reuse) throws SocketException
  {
    serverSocket.setReuseAddress(reuse);
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    serverSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
  }
}