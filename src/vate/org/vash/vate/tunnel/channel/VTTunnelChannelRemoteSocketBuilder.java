package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.Socket;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelPipedSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelRemoteSocketBuilder
{
  private VTTunnelChannel channel;
  private static final String SESSION_SEPARATOR = "\f\b";
  private static final char SESSION_MARK = 'C';
  
  public VTTunnelChannelRemoteSocketBuilder(VTTunnelChannel channel)
  {
    this.channel = channel;
  }
  
  public String toString()
  {
    return channel.toString();
  }
  
  public boolean equals(Object other)
  {
    return this.toString().equals(other.toString());
  }
  
  public VTTunnelChannel getChannel()
  {
    return channel;
  }
  
  public Socket connect(int channelType, String host, int port) throws IOException
  {
    VTTunnelSession session = null;
    VTTunnelSessionHandler handler = null;
    
    VTTunnelPipedSocket piped = new VTTunnelPipedSocket();
    session = new VTTunnelSession(channel.getConnection(), piped, piped.getInputStream(), piped.getOutputStream(), true);
    handler = new VTTunnelSessionHandler(session, channel);
    
    VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
    if (output != null)
    {
      piped.setOutputStream(output);
      session.setSocketInputStream(piped.getInputStream());
      session.setSocketOutputStream(piped.getOutputStream());
      int outputNumber = output.number();
      session.setOutputNumber(outputNumber);
      session.setTunnelOutputStream(output);
      channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port).getBytes("UTF-8"));
      channel.getConnection().getControlOutputStream().flush();
      boolean result = false;
      try
      {
        result = session.waitResult();
      }
      catch (Throwable t)
      {
        
      }
      if (result)
      {
        return piped;
      }
    }
    else
    {
      // cannot handle more sessions
      if (session != null)
      {
        session.close();
      }
    }
    return null;
  }
}