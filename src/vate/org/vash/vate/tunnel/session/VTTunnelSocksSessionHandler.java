package org.vash.vate.tunnel.session;

import org.vash.vate.VT;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.proxy.server.VTSocksHttpProxyAuthenticatorNone;
import org.vash.vate.proxy.server.VTSocksHttpProxyAuthenticatorUsernamePassword;
import org.vash.vate.proxy.server.VTSocksMultipleUserValidation;
import org.vash.vate.proxy.server.VTSocksProxyServer;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelSocksSessionHandler extends VTTunnelSessionHandler
{
  private static final int socksBufferSize = VT.VT_STANDARD_BUFFER_SIZE_BYTES;
  private final VTTunnelChannel channel;
  private final VTTunnelSession session;
  private final VTSocksMultipleUserValidation validation;
  private final VTProxy proxy;
  private final VTRemoteSocketFactory socketFactory;
  private final int connectTimeout;
  private final String bind;
  
  //public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  //{
    //super(session, channel);
    //this.session = session;
    //this.channel = channel;
  //}
  
  public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel, String username, String password, String bind, int connectTimeout, VTRemoteSocketFactory socketFactory, VTProxy proxy)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
    this.proxy = proxy;
    this.socketFactory = socketFactory;
    this.connectTimeout = connectTimeout;
    this.bind = bind;
    if (username != null && password != null && username.length() > 0 && password.length() > 0)
    {
      this.validation = new VTSocksMultipleUserValidation(new String[] {username}, new String[] {password});
    }
    else
    {
      this.validation = null;
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
          VTSocksProxyServer.setUDPTimeout(VT.VT_PING_LIMIT_MILLISECONDS);
          VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorUsernamePassword(validation, channel.getConnection().getNonces(), channel.getConnection().getRandom(), channel.getConnection().getExecutorService(), bind, connectTimeout, socketFactory, proxy), session.getSocket(), channel.getConnection().getExecutorService(), false, false, bind, connectTimeout, socketFactory, proxy);
          socksServer.setDatagramSocketFactory(channel.getConnection().createRemoteSocketFactory(channel));
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
          VTSocksProxyServer.setUDPTimeout(VT.VT_PING_LIMIT_MILLISECONDS);
          VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorNone(channel.getConnection().getExecutorService(), bind, connectTimeout, socketFactory, proxy), session.getSocket(), channel.getConnection().getExecutorService(), false, false, bind, connectTimeout, socketFactory, proxy);
          socksServer.setDatagramSocketFactory(channel.getConnection().createRemoteSocketFactory(channel));
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
      close();
    }
  }
}