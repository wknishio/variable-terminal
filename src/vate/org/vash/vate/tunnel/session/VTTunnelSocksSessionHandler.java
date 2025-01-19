package org.vash.vate.tunnel.session;

import org.vash.vate.VT;
import org.vash.vate.socket.proxy.VTProxy;
import org.vash.vate.socket.proxy.VTSocksHttpProxyAuthenticatorNone;
import org.vash.vate.socket.proxy.VTSocksHttpProxyAuthenticatorUsernamePassword;
import org.vash.vate.socket.proxy.VTSocksMultipleUserValidation;
import org.vash.vate.socket.proxy.VTSocksProxyServer;
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
  
  public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel, String socksUsername, String socksPassword, VTProxy proxy, VTRemoteSocketFactory socketFactory, int connectTimeout, String bind)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
    this.proxy = proxy;
    this.socketFactory = socketFactory;
    this.connectTimeout = connectTimeout;
    this.bind = bind;
    if (socksUsername != null && socksPassword != null && socksUsername.length() > 0 && socksPassword.length() > 0)
    {
      this.validation = new VTSocksMultipleUserValidation(new String[] {socksUsername}, new String[] {socksPassword});
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
          VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorUsernamePassword(validation, channel.getConnection().getNonces(), channel.getConnection().getRandom(), channel.getConnection().getExecutorService(), proxy, socketFactory, connectTimeout, bind), session.getSocket(), channel.getConnection().getExecutorService(), false, false, proxy, socketFactory, connectTimeout, bind);
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
          VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorNone(channel.getConnection().getExecutorService(), proxy, socketFactory, connectTimeout, bind), session.getSocket(), channel.getConnection().getExecutorService(), false, false, proxy, socketFactory, connectTimeout, bind);
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