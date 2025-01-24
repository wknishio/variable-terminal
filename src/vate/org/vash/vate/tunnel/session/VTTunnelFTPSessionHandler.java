package org.vash.vate.tunnel.session;

import java.io.File;

import org.vash.vate.socket.ftpserver.VTFTPAuthenticator;
import org.vash.vate.socket.ftpserver.VTFTPServer;
import org.vash.vate.socket.ftpserver.VTFTPNativeFileSystem;
import org.vash.vate.socket.proxy.VTProxy;
import org.vash.vate.socket.remote.VTRemoteClientSocketFactory;
import org.vash.vate.socket.remote.VTRemoteServerSocketFactory;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelFTPSessionHandler extends VTTunnelSessionHandler
{
  //private final VTTunnelChannel channel;
  private final VTTunnelSession session;
  private final VTProxy proxy;
  private final VTRemoteSocketFactory socketFactory;
  private final int connectTimeout;
  private final String bind;
  private final VTFTPAuthenticator validation;
  
  //public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  //{
    //super(session, channel);
    //this.session = session;
    //this.channel = channel;
  //}
  
  public VTTunnelFTPSessionHandler(VTTunnelSession session, VTTunnelChannel channel, String username, String password, String bind, int connectTimeout, VTRemoteSocketFactory socketFactory, VTProxy proxy)
  {
    super(session, channel);
    this.session = session;
    //this.channel = channel;
    this.proxy = proxy;
    this.socketFactory = socketFactory;
    this.connectTimeout = connectTimeout;
    this.bind = bind;
    if (username != null && password != null && username.length() > 0 && password.length() > 0)
    {
      this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new File("/")), username, password);
    }
    else
    {
      this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new File("/")), null, null);
    }
  }
  
  public VTTunnelSession getSession()
  {
    return session;
  }
  
  @SuppressWarnings("all")
  public void run()
  {
    try
    {
      VTRemoteClientSocketFactory clientFactory = new VTRemoteClientSocketFactory(socketFactory, connectTimeout, 0, bind, proxy);
      VTRemoteServerSocketFactory serverFactory = new VTRemoteServerSocketFactory(socketFactory, 0, 0, bind);
      VTFTPServer ftpserver = new VTFTPServer(validation, clientFactory, serverFactory);
      ftpserver.startConnection(session.getSocket());
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    finally
    {
      close();
    }
  }
}