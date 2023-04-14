package org.vash.vate.tunnel.session;

import org.vash.vate.VT;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

import net.sourceforge.jsocks.socks.ProxyServer;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;
import net.sourceforge.jsocks.socks.server.UserPasswordAuthenticator;

public class VTTunnelSocksSessionHandler extends VTTunnelSessionHandler
{
  private static final int socksBufferSize = VT.VT_STANDARD_DATA_BUFFER_SIZE;
  private VTTunnelChannel channel;
  private VTTunnelSession session;
  private VTTunnelSocksSingleUserValidation validation;
  
  public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
  }
  
  public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel, String socksUsername, String socksPassword)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
    this.validation = new VTTunnelSocksSingleUserValidation(socksUsername, socksPassword);
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
        ProxyServer socksServer = new ProxyServer(new UserPasswordAuthenticator(validation), session.getSocket(), true);
        socksServer.setPipeBufferSize(socksBufferSize);
        socksServer.run();
        // session.close();
      }
      else
      {
        ProxyServer socksServer = new ProxyServer(new ServerAuthenticatorNone(), session.getSocket(), true);
        socksServer.setPipeBufferSize(socksBufferSize);
        socksServer.run();
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