package org.vash.vate.tunnel.session;

import java.io.File;

import org.vash.vate.VTSystem;
import org.vash.vate.filesystem.VTRootList;
import org.vash.vate.ftp.server.VTFTPAuthenticator;
import org.vash.vate.ftp.server.VTFTPNativeFileSystem;
import org.vash.vate.ftp.server.VTFTPServer;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.socket.remote.VTRemoteClientSocketFactory;
import org.vash.vate.socket.remote.VTRemoteServerSocketFactory;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.tunnel.channel.VTTunnelChannel;

public class VTTunnelFTPSessionHandler extends VTTunnelSessionHandler
{
  private final VTTunnelChannel channel;
  private final VTTunnelSession session;
  private final VTProxy proxy;
  //private final VTRemoteSocketFactory socketFactory;
  private final int connectTimeout;
  private final int dataTimeout;
  private final String bind;
  private final VTFTPAuthenticator validation;
  
  //public VTTunnelSocksSessionHandler(VTTunnelSession session, VTTunnelChannel channel)
  //{
    //super(session, channel);
    //this.session = session;
    //this.channel = channel;
  //}
  
  public VTTunnelFTPSessionHandler(VTTunnelChannel channel, VTTunnelSession session, String username, String password, String bind, int connectTimeout, int dataTimeout, VTProxy proxy)
  {
    super(channel, session);
    this.channel = channel;
    this.session = session;
    this.proxy = proxy;
    //this.socketFactory = socketFactory;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.bind = bind;
    if (username != null && password != null && username.length() > 0 && password.length() > 0)
    {
      if (VTReflectionUtils.detectWindows())
      {
        this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new VTRootList()), username, password);
      }
      else
      {
        this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new File("/").getAbsoluteFile()), username, password);
      }
    }
    else
    {
      if (VTReflectionUtils.detectWindows())
      {
        this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new VTRootList()), null, null);
      }
      else
      {
        this.validation = new VTFTPAuthenticator(new VTFTPNativeFileSystem(new File("/").getAbsoluteFile()), null, null);
      }
    }
  }
  
  public VTTunnelSession getSession()
  {
    return session;
  }
  
  public void run()
  {
    VTFTPServer ftpserver = null;
    try
    {
      VTRemoteSocketFactory socketFactory = channel.getConnection().createRemoteSocketFactory(channel);
      VTRemoteClientSocketFactory clientFactory = new VTRemoteClientSocketFactory(socketFactory, connectTimeout, dataTimeout, bind, proxy);
      VTRemoteServerSocketFactory serverFactory = new VTRemoteServerSocketFactory(socketFactory, connectTimeout, dataTimeout, bind);
      ftpserver = new VTFTPServer(validation, clientFactory, serverFactory, channel.getConnection().getExecutorService());
      ftpserver.setBufferSize(VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES);
      ftpserver.setTimeout(dataTimeout);
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