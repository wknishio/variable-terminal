package org.vash.vate.tunnel.session;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketAddress;

import org.vash.vate.socket.remote.VTRemoteSocketFactory;

public class VTTunnelServerSocket extends ServerSocket
{
  private final VTRemoteSocketFactory factory;
  private final String bind;
  private final String host;
  private final int port;
  private int connectTimeout = 0;
  private int dataTimeout = 0;
  
  public VTTunnelServerSocket(VTRemoteSocketFactory factory, String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    super();
    this.factory = factory;
    this.bind = bind;
    this.host = host;
    this.port = port;
  }
  
  public Socket accept() throws IOException
  {
    return factory.acceptSocket(bind, host, port, connectTimeout, dataTimeout);
  }
  
  public int getLocalPort()
  {
    return port;
  }
  
  public void close() throws IOException
  {
    factory.unbindSocket(bind);
  }
  
  public InetAddress getInetAddress()
  {
    try
    {
      return InetAddress.getByName(host);
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  public void setSoTimeout(int timeout)
  {
    connectTimeout = timeout;
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return InetSocketAddress.createUnresolved(host, port);
  }
}