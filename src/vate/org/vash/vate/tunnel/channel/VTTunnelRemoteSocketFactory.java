package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import org.vash.vate.socket.proxy.VTProxy;
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
      VTProxy proxy = proxies[0];
      return socketBuilder.connect(bind, host, port, connectTimeout, dataTimeout, proxy);
    }
    return socketBuilder.connect(bind, host, port, connectTimeout, dataTimeout, PROXY_NONE);
  }
  
  public Socket acceptSocket(String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    return socketBuilder.accept(host, port, connectTimeout, dataTimeout);
  }
  
  public DatagramSocket createSocket(String host, int port, int dataTimeout) throws IOException
  {
    return socketBuilder.create(host, port, dataTimeout);
  }
  
  public DatagramSocket createSocket(InetAddress address, int port, int dataTimeout) throws IOException
  {
    return socketBuilder.create(address, port, dataTimeout);
  }

}