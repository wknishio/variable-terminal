package org.vash.vate.socket;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

public class VTRemoteProxyServerSocket extends ServerSocket
{
  private final VTRemoteProxySocketFactory socketFactory;
  private final String host;
  private final int port;
  private final int timeout;
  private volatile boolean closed = false;
  
  public VTRemoteProxyServerSocket(VTRemoteProxySocketFactory socketFactory, String host, int port, int timeout) throws IOException
  {
    super();
    this.socketFactory = socketFactory;
    this.host = host;
    this.port = port;
    this.timeout = timeout;
  }
  
  public Socket accept() throws IOException
  {
    if (isClosed())
    {
      throw new IOException("VTRemoteProxyServerSocket is closed");
    }
    return socketFactory.acceptSocket(host, port, timeout);
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
  
  public void close()
  {
    closed = true;
  }
}
