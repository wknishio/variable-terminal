package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import org.vash.vate.VT;
import org.vash.vate.socket.VTAuthenticatedProxySocketFactory;
import org.vash.vate.socket.VTProxy;

public class VTTunnelRemoteSocketFactory extends VTAuthenticatedProxySocketFactory
{
  private VTTunnelChannelRemoteSocketBuilder builder;
  
  private int channelType = VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
  
  private static VTProxy proxyNone = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT, "", "");
  
  public VTTunnelRemoteSocketFactory(VTTunnelChannelRemoteSocketBuilder builder)
  {
    this.builder = builder;
  }
  
  public void setChannelType(int channelType)
  {
    this.channelType = channelType | VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
  }
  
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port, proxyNone);
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    return builder.connect(channelType, host.getHostAddress(), port, proxyNone);
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port, proxyNone);
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    return builder.connect(channelType, host.getHostAddress(), port, proxyNone);
  }
  
  public Socket createSocket(String host, int port, Socket proxyConnection, VTProxy... proxies) throws IOException, UnknownHostException
  {
    if (proxies != null && proxies.length >= 1)
    {
      VTProxy proxy = proxies[0];
      return builder.connect(channelType, host, port, proxy);
    }
    return builder.connect(channelType, host, port, proxyNone);
  }
}