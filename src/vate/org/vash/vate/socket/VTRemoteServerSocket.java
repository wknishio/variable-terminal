package org.vash.vate.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VTRemoteServerSocket extends ServerSocket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  private String host;
  private int port;
  private int connectTimeout = 0;
  private int dataTimeout = 0;
  private volatile boolean closed = false;
  
  public VTRemoteServerSocket(VTRemoteSocketFactory remoteSocketFactory) throws IOException
  {
    super();
    this.remoteSocketFactory = remoteSocketFactory;
  }
  
  public VTRemoteServerSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port) throws IOException
  {
    super();
    this.remoteSocketFactory = remoteSocketFactory;
    this.host = host;
    this.port = port;
  }
  
  public VTRemoteServerSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    super();
    this.remoteSocketFactory = remoteSocketFactory;
    this.host = host;
    this.port = port;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
  }
  
  public void bind(String host, int port)
  {
    this.host = host;
    this.port = port;
  }
  
  public void bind(String host, int port, int connectTimeout)
  {
    this.host = host;
    this.port = port;
    this.connectTimeout = connectTimeout;
  }
  
  public void bind(String host, int port, int connectTimeout, int dataTimeout)
  {
    this.host = host;
    this.port = port;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
  }
  
  public Socket accept() throws IOException
  {
    if (isClosed())
    {
      throw new IOException("VTRemoteProxyServerSocket is closed");
    }
    return remoteSocketFactory.acceptSocket(host, port, connectTimeout, dataTimeout);
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
    return port;
  }
  
  public int getSoTimeout()
  {
    return connectTimeout;
  }
  
  public void setSoTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }
  
  public void close()
  {
    closed = true;
  }
}