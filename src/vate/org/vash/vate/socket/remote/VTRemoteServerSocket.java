package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;
import java.util.UUID;

public class VTRemoteServerSocket extends ServerSocket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  private String host;
  private int port;
  private int connectTimeout = 0;
  private int dataTimeout = 0;
  private volatile boolean closed = false;
  private final String uuid;
  private ServerSocket remoteServerSocket;
  
  public VTRemoteServerSocket(VTRemoteSocketFactory remoteSocketFactory) throws IOException
  {
    super();
    this.remoteSocketFactory = remoteSocketFactory;
    this.uuid = UUID.randomUUID().toString();
  }
  
  public VTRemoteServerSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port) throws IOException
  {
    super();
    this.remoteSocketFactory = remoteSocketFactory;
    this.host = host;
    this.port = port;
    this.uuid = UUID.randomUUID().toString();
    bind(host, port);
  }
  
  public VTRemoteServerSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    super();
    this.remoteSocketFactory = remoteSocketFactory;
    this.host = host;
    this.port = port;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.uuid = UUID.randomUUID().toString();
    bind(host, port, connectTimeout, dataTimeout);
  }
  
  public void bind(SocketAddress endpoint) throws IOException
  {
    if (endpoint instanceof InetSocketAddress)
    {
      InetSocketAddress address = (InetSocketAddress) endpoint;
      bind(address.getHostName(), address.getPort());
    }
  }
  
  public void bind(SocketAddress endpoint, int backlog) throws IOException
  {
    if (endpoint instanceof InetSocketAddress)
    {
      InetSocketAddress address = (InetSocketAddress) endpoint;
      bind(address.getHostName(), address.getPort());
    }
  }
  
  public void bind(String host, int port) throws IOException
  {
    this.host = host;
    this.port = port;
    this.remoteServerSocket = remoteSocketFactory.bindSocket(uuid, host, port, connectTimeout, dataTimeout);
  }
  
  public void bind(String host, int port, int connectTimeout) throws IOException
  {
    this.host = host;
    this.port = port;
    this.connectTimeout = connectTimeout;
    this.remoteServerSocket = remoteSocketFactory.bindSocket(uuid, host, port, connectTimeout, dataTimeout);
  }
  
  public void bind(String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    this.host = host;
    this.port = port;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.remoteServerSocket = remoteSocketFactory.bindSocket(uuid, host, port, connectTimeout, dataTimeout);
  }
  
  public Socket accept() throws IOException
  {
    if (isClosed())
    {
      throw new IOException("VTRemoteServerSocket is closed");
    }
    if (remoteServerSocket != null)
    {
      return remoteServerSocket.accept();
    }
    return remoteSocketFactory.acceptSocket(uuid, host, port, connectTimeout, dataTimeout);
  }
  
  public boolean isBound()
  {
    return !closed;
  }
  
  public boolean isClosed()
  {
    return closed;
  }
  
  public int getLocalPort()
  {
    if (remoteServerSocket != null)
    {
      return remoteServerSocket.getLocalPort();
    }
    return port;
  }
  
  public InetAddress getInetAddress()
  {
    if (remoteServerSocket != null)
    {
      return remoteServerSocket.getInetAddress();
    }
    try
    {
      return InetAddress.getByName(host);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    if (remoteServerSocket != null)
    {
      return remoteServerSocket.getLocalSocketAddress();
    }
    try
    {
      return InetSocketAddress.createUnresolved(host, port);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  public int getSoTimeout()
  {
    return connectTimeout;
  }
  
  public void setSoTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }
  
  public void close() throws IOException
  {
    if (remoteServerSocket != null)
    {
      remoteServerSocket.close();
    }
    closed = true;
  }
}