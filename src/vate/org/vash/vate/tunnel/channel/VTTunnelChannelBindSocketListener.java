package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.socket.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelCloseableSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelBindSocketListener implements Runnable
{
  private final VTTunnelChannel channel;
  @SuppressWarnings("unused")
  private final ExecutorService executor;
  private ServerSocket serverSocket;
  private volatile boolean closed = false;
  private static final String SESSION_SEPARATOR = "\f";
  private static final char SESSION_MARK = '\b';
  
  public VTTunnelChannelBindSocketListener(VTTunnelChannel channel, ExecutorService executor)
  {
    this.channel = channel;
    this.executor = executor;
    this.closed = false;
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
  
  public void close() throws IOException
  {
    if (closed)
    {
      return;
    }
    try
    {
      if (serverSocket != null)
      {
        serverSocket.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      channel.close();
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    //channel.getConnection().removeChannel(this);
    closed = true;
  }
  
  public boolean bind()
  {
    try
    {
      if (serverSocket == null || serverSocket.isClosed())
      {
        serverSocket = new ServerSocket();
        // serverSocket.setReuseAddress(true);
      }
      // serverSocket.setReuseAddress(true);
      serverSocket.bind(channel.getBindAddress());
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public void remove()
  {
    channel.getConnection().removeBindListener(this);
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      while (!closed && !serverSocket.isClosed() && serverSocket.isBound())
      {
        VTTunnelSession session = null;
        VTTunnelSessionHandler handler = null;
        Socket acceptedSocket = null;
        InputStream socketInputStream = null;
        OutputStream socketOutputStream = null;
        
        try
        {
          acceptedSocket = accept();
          socketInputStream = acceptedSocket.getInputStream();
          socketOutputStream = acceptedSocket.getOutputStream();
        }
        catch (Throwable t)
        {
          
        }
        
        if (socketInputStream != null && socketOutputStream != null)
        {
          int tunnelType = channel.getTunnelType();
          int channelType = channel.getChannelType();
          int connectTimeout = channel.getConnectTimeout();
          int dataTimeout = channel.getDataTimeout();
          
          session = new VTTunnelSession(channel.getConnection(), true);
          session.setSocket(acceptedSocket);
          session.setSocketInputStream(socketInputStream);
          session.setSocketOutputStream(socketOutputStream);
          
          VTProxyType proxyType = channel.getProxy().getProxyType();
          String proxyHost = channel.getProxy().getProxyHost();
          int proxyPort = channel.getProxy().getProxyPort();
          String proxyUser = channel.getProxy().getProxyUser();
          String proxyPassword = channel.getProxy().getProxyPassword();
          
          String proxyTypeLetter = "G";
          
          if (proxyType == VTProxyType.GLOBAL)
          {
            proxyTypeLetter = "G";
          }
          else if (proxyType == VTProxyType.DIRECT)
          {
            proxyTypeLetter = "D";
          }
          else if (proxyType == VTProxyType.HTTP)
          {
            proxyTypeLetter = "H";
          }
          else if (proxyType == VTProxyType.SOCKS)
          {
            proxyTypeLetter = "S";
          }
          else if (proxyType == VTProxyType.ANY)
          {
            proxyTypeLetter = "A";
          }
          
          if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
          {
            proxyUser = "*";
            proxyPassword = "*" + SESSION_SEPARATOR + "*";
          }
          
          handler = new VTTunnelSessionHandler(session, channel);
          
          VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
          VTLinkableDynamicMultiplexedInputStream input = channel.getConnection().getInputStream(channelType, handler);
          
          if (output != null && input != null)
          {
            final int outputNumber = output.number();
            final int inputNumber = input.number();
            
            session.setTunnelOutputStream(output);
            session.setTunnelInputStream(input);
            session.getTunnelOutputStream().open();
            session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream(), new VTTunnelCloseableSocket(acceptedSocket));
            
            if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
            {
              String host = channel.getRedirectHost();
              int port = channel.getRedirectPort();
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
            }
            else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              String socksUsername = channel.getSocksUsername();
              String socksPassword = channel.getSocksPassword();
              if (socksUsername == null || socksPassword == null || socksUsername.length() == 0 || socksPassword.length() == 0)
              {
                socksUsername = "";
                socksPassword = "";
              }
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + socksUsername + SESSION_SEPARATOR + socksPassword + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
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
//          if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
//          {
//            handler = new VTTunnelSocksSessionHandler(session, channel, channel.getSocksUsername(), channel.getSocksPassword(), channel.getProxy(), channel.getConnection().createRemoteSocketFactory(channel));
//            executor.execute(handler);
//          }
//          else
//          {
//            
//          }
        }
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    //closed = true;
  }
  
  private Socket accept()
  {
    Socket socket = null;
    try
    {
      socket = serverSocket.accept();
      socket.setTcpNoDelay(true);
      socket.setKeepAlive(true);
      //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}