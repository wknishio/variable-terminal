package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VT;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelBindSocketListener implements Runnable
{
  private ServerSocket serverSocket;
  private VTTunnelChannel channel;
  @SuppressWarnings("unused")
  private ExecutorService threads;
  private static final String SESSION_SEPARATOR = "\f\b";
  private static final char SESSION_MARK = 'C';
  private volatile boolean closed = false;
  
  public VTTunnelChannelBindSocketListener(VTTunnelChannel channel, ExecutorService threads)
  {
    this.channel = channel;
    this.threads = threads;
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
        Socket socket = null;
        InputStream socketInputStream = null;
        OutputStream socketOutputStream = null;
        try
        {
          socket = listen();
          socketInputStream = socket.getInputStream();
          socketOutputStream = socket.getOutputStream();
        }
        catch (Throwable t)
        {
          
        }
        if (socketInputStream != null && socketOutputStream != null)
        {
          int channelType = channel.getChannelType();
          int tunnelType = channel.getTunnelType();
          //TODO: will start a local socks/http proxy server that connects remotely using new socket factory
          session = new VTTunnelSession(channel.getConnection(), socket, socketInputStream, socketOutputStream, true);
          handler = new VTTunnelSessionHandler(session, channel);
          VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
          if (output != null)
          {
            int outputNumber = output.number();
            session.setOutputNumber(outputNumber);
            session.setTunnelOutputStream(output);
            //session.getTunnelOutputStream().open();
            if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
            {
              String host = channel.getRedirectHost();
              int port = channel.getRedirectPort();
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
            }
            else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              String socksUsername = channel.getSocksUsername();
              String socksPassword = channel.getSocksPassword();
              if (socksUsername == null || socksPassword == null)
              {
                socksUsername = "*";
                socksPassword = "*" + SESSION_SEPARATOR + "*";
              }
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + socksUsername + SESSION_SEPARATOR + socksPassword).getBytes("UTF-8"));
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
          //TODO: will start a local socks/http proxy server that connects remotely using new socket factory
        }
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    //closed = true;
  }
  
  private Socket listen()
  {
    Socket socket = null;
    try
    {
      socket = serverSocket.accept();
      socket.setTcpNoDelay(true);
      //socket.setSendBufferSize(1024 * 64);
      //socket.setReceiveBufferSize(1024 * 64);
      //socket.setSoLinger(true, 5);
      socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
      //socket.getInputStream();
      //socket.getOutputStream();
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}