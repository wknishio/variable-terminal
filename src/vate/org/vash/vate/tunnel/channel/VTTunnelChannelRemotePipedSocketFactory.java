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
  
  public Socket pipeSocket(String bind, int type, boolean originator) throws IOException
  {
    return socketBuilder.pipeSocket(bind, type, originator);
  }
  
  public Socket pipeSocket(String bind, int type, boolean originator, OutputStream out) throws IOException
  {
    return socketBuilder.pipeSocket(bind, type, originator, out);
  }
}