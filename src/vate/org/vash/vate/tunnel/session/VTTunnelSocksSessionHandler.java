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
  
  //public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  //{
    //super(session, channel);
    //this.session = session;
    //this.channel = channel;
  //}
  
  public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel, String socksUsername, String socksPassword, VTProxy proxy, VTRemoteSocketFactory socketFactory, int connectTimeout)
  {
    super(session, channel);
    this.session = session;
    this.channel = channel;
    this.proxy = proxy;
    this.socketFactory = socketFactory;
    this.connectTimeout = connectTimeout;
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
          VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorUsernamePassword(validation, channel.getConnection().getExecutorService(), proxy, socketFactory, connectTimeout, null), session.getSocket(), channel.getConnection().getExecutorService(), false, true, proxy, socketFactory, connectTimeout);
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
          VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorNone(channel.getConnection().getExecutorService(), proxy, socketFactory, connectTimeout, null), session.getSocket(), channel.getConnection().getExecutorService(), false, true, proxy, socketFactory, connectTimeout);
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