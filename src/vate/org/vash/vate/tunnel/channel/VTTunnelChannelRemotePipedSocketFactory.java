package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.io.OutputStream;
import java.net.Socket;

import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;

public class VTTunnelChannelRemotePipedSocketFactory extends VTRemotePipedSocketFactory
{
  private final VTTunnelChannelRemoteSocketBuilder socketBuilder;
  
  public VTTunnelChannelRemotePipedSocketFactory(VTTunnelChannelRemoteSocketBuilder socketBuilder)
  {
    this.socketBuilder = socketBuilder;
  }
  
  public Socket requestPipe(String bind, int type) throws IOException
  {
    return socketBuilder.pipeSocket(true, bind, type);
  }
  
  public Socket requestPipe(String bind, int type, OutputStream out) throws IOException
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