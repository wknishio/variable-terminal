package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;

public class VTTunnelChannelRemoteSocketFactory extends VTRemoteSocketFactory
{
  private final VTTunnelChannelRemoteSocketBuilder socketBuilder;
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  
  public VTTunnelChannelRemoteSocketFactory(VTTunnelChannelRemoteSocketBuilder socketBuilder)
  {
    this.socketBuilder = socketBuilder;
  }
  
  public Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy proxy) throws IOException
  {
    if (proxy != null)
    {
      return socketBuilder.connectSocket(bind, host, port, connectTimeout, dataTimeout, proxy);
    }
    return socketBuilder.connectSocket(bind, host, port, connectTimeout, dataTimeout, PROXY_NONE);
  }
  
  public Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    return socketBuilder.connectSocket(bind, host, port, connectTimeout, dataTimeout, PROXY_NONE);
  }
  
  public Socket acceptSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    return socketBuilder.acceptSocket(bind, host, port, connectTimeout, dataTimeout);
  }
  
  public ServerSocket bindSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    return socketBuilder.bindSocket(bind, host, port, connectTimeout, dataTimeout);
  }
  
  public void unbindSocket(String bind) throws IOException
  {
    socketBuilder.unbindSocket(bind);
  }
  
  public DatagramSocket createSocket(String bind, String host, int port, int dataTimeout) throws IOException
  {
    return socketBuilder.createSocket(bind, host, port, dataTimeout);
  }
}