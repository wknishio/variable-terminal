package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import org.vash.vate.socket.VTRemoteProxySocketFactory;
import org.vash.vate.socket.VTProxy;

public class VTTunnelRemoteSocketFactory extends VTRemoteProxySocketFactory
{
  private final VTTunnelChannelRemoteSocketBuilder socketBuilder;
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  
  public VTTunnelRemoteSocketFactory(VTTunnelChannelRemoteSocketBuilder socketBuilder)
  {
    this.socketBuilder = socketBuilder;
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
  
  public Socket acceptSocket(String host, int port, int timeout) throws IOException, UnknownHostException
  {
    return socketBuilder.accept(host, port, timeout);
  }
}