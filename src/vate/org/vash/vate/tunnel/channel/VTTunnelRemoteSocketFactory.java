package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;

public class VTTunnelRemoteSocketFactory extends VTRemoteSocketFactory
{
  private final VTTunnelChannelRemoteSocketBuilder socketBuilder;
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  
  public VTTunnelRemoteSocketFactory(VTTunnelChannelRemoteSocketBuilder socketBuilder)
  {
    this.socketBuilder = socketBuilder;
  }
  
  public Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException
  {
    if (proxies != null && proxies.length >= 1)
    {
      return socketBuilder.connectSocket(bind, host, port, connectTimeout, dataTimeout, proxies[0]);
    }
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
  
  public DatagramSocket createSocket(String host, int port, int dataTimeout) throws IOException
  {
    return socketBuilder.createSocket(host, port, dataTimeout);
  }
  
//  public DatagramSocket createSocket(InetAddress address, int port, int dataTimeout) throws IOException
//  {
//    return socketBuilder.create(address, port, dataTimeout);
//  }
}