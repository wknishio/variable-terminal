package org.vash.vate.socket.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import org.vash.vate.VT;
import org.vash.vate.tunnel.channel.VTTunnelChannelRemoteSocketBuilder;

public class VTTunnelRemoteSocketFactory extends VTAuthenticatedProxySocketFactory
{
  private VTTunnelChannelRemoteSocketBuilder builder;
  
  private int channelType = VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT;
  
  public VTTunnelRemoteSocketFactory(VTTunnelChannelRemoteSocketBuilder builder)
  {
    this.builder = builder;
  }
  
  public void setChannelType(int channelType)
  {
    this.channelType = channelType | VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT;
  }
  
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port, Proxy.Type.DIRECT, "", 0, "", "");
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    return builder.connect(channelType, host.getHostAddress(), port, Proxy.Type.DIRECT, "", 0, "", "");
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port, Proxy.Type.DIRECT, "", 0, "", "");
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    return builder.connect(channelType, host.getHostAddress(), port, Proxy.Type.DIRECT, "", 0, "", "");
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port, Proxy.Type.DIRECT, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port) throws IOException
  {
    return builder.connect(channelType, host.getHostName(), port, Proxy.Type.DIRECT, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port, Proxy.Type.DIRECT, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    return builder.connect(channelType, host.getHostName(), port, Proxy.Type.DIRECT, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
}