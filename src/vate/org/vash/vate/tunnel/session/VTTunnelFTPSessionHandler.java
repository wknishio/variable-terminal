package org.vash.vate.tunnel.session;

import java.io.File;

import org.vash.vate.VT;
import org.vash.vate.ftp.server.VTFTPAuthenticator;
import org.vash.vate.ftp.server.VTFTPNativeFileSystem;
import org.vash.vate.ftp.server.VTFTPServer;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.socket.remote.VTRemoteClientSocketFactory;
import org.vash.vate.socket.remote.VTRemoteServerSocketFactory;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelFTPSessionHandler extends VTTunnelSessionHandler
{
  private final VTTunnelChannel channel;
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
    this.channel = channel;
    this.proxy = proxy;
    this.socketFactory = socketFactory;
    this.connectTimeout = connectTimeout;
    this.bind = bind;
    if (username != null && password != null && username.length() > 0 && password.length() > 0)
    {
      this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new File("/").getAbsoluteFile()), username, password);
    }
    else
    {
      this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new File("/").getAbsoluteFile()), null, null);
    }
  }
  
  public VTTunnelSession getSession()
  {
    return session;
  }
  
  @SuppressWarnings("all")
  public void run()
  {
    VTFTPServer ftpserver = null;
    try
    {
      VTRemoteClientSocketFactory clientFactory = new VTRemoteClientSocketFactory(socketFactory, connectTimeout, 0, bind, proxy);
      VTRemoteServerSocketFactory serverFactory = new VTRemoteServerSocketFactory(socketFactory, 0, 0, bind);
      ftpserver = new VTFTPServer(validation, clientFactory, serverFactory, channel.getConnection().getExecutorService());
      ftpserver.setBufferSize(VT.VT_STANDARD_BUFFER_SIZE_BYTES);
      ftpserver.runConnection(session.getSocket());
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    finally
    {
      try
      {
        if (ftpserver != null)
        {
          ftpserver.endConnection();
        }
      }
      catch (Throwable t)
      {
        
      }
      close();
    }
  }
}