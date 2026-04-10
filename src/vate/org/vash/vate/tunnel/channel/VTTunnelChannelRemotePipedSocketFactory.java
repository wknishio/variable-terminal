package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;

public class VTTunnelChannelRemotePipedSocketFactory extends VTRemotePipedSocketFactory
{
  private final VTTunnelChannelRemoteSocketBuilder socketBuilder;
  private final int type;
  
  public VTTunnelChannelRemotePipedSocketFactory(VTTunnelChannelRemoteSocketBuilder socketBuilder, int type)
  {
    this.socketBuilder = socketBuilder;
    this.type = type;
  }
  
  public Socket requestPipe(String bind) throws IOException
  {
    return socketBuilder.pipeSocket(true, bind, type);
  }
  
  public Socket requestPipe(String bind, OutputStream out) throws IOException
  {
    return socketBuilder.pipeSocket(true, bind, type, out);
  }
  
  public Socket respondPipe(String bind) throws IOException
  {
    return socketBuilder.pipeSocket(false, bind, 0);
  }
  
  public Socket respondPipe(String bind, OutputStream out) throws IOException
  {
    return socketBuilder.pipeSocket(false, bind, 0, out);
  }
}