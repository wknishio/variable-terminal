package org.vash.vate.tunnel.session;

import org.vash.vate.VT;
import org.vash.vate.socket.VTDefaultProxy;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

import net.sourceforge.jsocks.socks.ProxyServer;

public class VTTunnelSocksSessionHandler extends VTTunnelSessionHandler
{
  private static final int socksBufferSize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  private VTTunnelChannel channel;
  private VTTunnelSession session;
  private VTTunnelSocksSingleUserValidation validation;
  private VTDefaultProxy proxy;
  
  //public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  //{
    //super(session, channel);
    //this.session = session;
    //this.channel = channel;
  //}
  
  public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel, String socksUsername, String socksPassword, VTDefaultProxy proxy)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
    this.proxy = proxy;
    if (socksUsername != null && socksPassword != null)
    {
      this.validation = new VTTunnelSocksSingleUserValidation(socksUsername, socksPassword);
    }
  }
  
  public VTTunnelSession getSession()
  {
    return session;
  }
  
  public void run()
  {
    try
    {
      if (validation != null)
      {
        
        try
        {
          ProxyServer socksServer = new ProxyServer(new VTTunnelSocksPlusHttpProxyAuthenticatorUsernamePassword(validation, proxy), session.getSocket(), false, true, proxy);
          socksServer.setPipeBufferSize(socksBufferSize);
          socksServer.run();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        // session.close();
      }
      else
      {
        try
        {
          ProxyServer socksServer = new ProxyServer(new VTTunnelSocksPlusHttpProxyAuthenticatorNone(proxy), session.getSocket(), false, true, proxy);
          socksServer.setPipeBufferSize(socksBufferSize);
          socksServer.run();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        // session.close();
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    finally
    {
      try
      {
        session.close();
      }
      catch (Throwable e)
      {
        //e.printStackTrace();
      }
      if (channel != null)
      {
        channel.removeSession(this);
      }
    }
  }
}