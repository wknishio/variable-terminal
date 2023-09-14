package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;

import org.vash.vate.socket.VTDefaultProxy;
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
  
  public Socket connect(int channelType, String host, int port, VTDefaultProxy proxy) throws IOException
  {
    return connect(channelType, host, port, proxy.getProxyType(), proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
  }
  
  public Socket connect(int channelType, String host, int port, Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) throws IOException
  {
    VTTunnelSession session = null;
    VTTunnelSessionHandler handler = null;
    
    String proxyTypeLetter = "D";
    if (proxyType == Proxy.Type.HTTP)
    {
      proxyTypeLetter = "H";
    }
    if (proxyType == Proxy.Type.SOCKS)
    {
      proxyTypeLetter = "S";
    }
    
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
      //session.getTunnelOutputStream().open();
      if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
      {
        proxyUser = "*";
        proxyPassword = "*" + SESSION_SEPARATOR + "*";
      }
      // request message sent
      channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
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
    throw new IOException("Failed to connect remotely to: host " + host + " port " + port + "");
  }
}