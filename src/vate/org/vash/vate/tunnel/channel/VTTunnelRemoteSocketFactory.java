package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.vash.vate.socket.VTAuthenticatedProxySocketFactory;
import org.vash.vate.socket.VTProxy;

public class VTTunnelRemoteSocketFactory extends VTAuthenticatedProxySocketFactory
{
  private final VTTunnelChannelRemoteSocketBuilder socketBuilder;
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  
  public VTTunnelRemoteSocketFactory(VTTunnelChannelRemoteSocketBuilder socketBuilder)
  {
    this.socketBuilder = socketBuilder;
  }
  
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    return socketBuilder.connect(host, port, PROXY_NONE);
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    return socketBuilder.connect(host.getHostName(), port, PROXY_NONE);
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    return socketBuilder.connect(host, port, PROXY_NONE);
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    return socketBuilder.connect(host.getHostName(), port, PROXY_NONE);
  }
  
  public Socket createSocket(String host, int port, Socket proxyConnection, VTProxy... proxies) throws IOException, UnknownHostException
  {
    if (proxies != null && proxies.length >= 1)
    {
      VTProxy proxy = proxies[0];
      return socketBuilder.connect(host, port, proxy);
    }
    return socketBuilder.connect(host, port, PROXY_NONE);
  }
}