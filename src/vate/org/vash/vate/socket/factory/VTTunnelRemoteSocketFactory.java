package org.vash.vate.socket.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.vash.vate.VT;
import org.vash.vate.tunnel.channel.VTTunnelChannelRemoteSocketBuilder;

public class VTTunnelRemoteSocketFactory extends SocketFactory
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
    return builder.connect(channelType, host, port);
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    return builder.connect(channelType, host.getHostAddress(), port);
  }
  
  public Socket createSocket(String host, int port, InetAddress arg2, int arg3) throws IOException, UnknownHostException
  {
    return builder.connect(channelType, host, port);
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress arg2, int arg3) throws IOException
  {
    return builder.connect(channelType, host.getHostAddress(), port);
  }
}