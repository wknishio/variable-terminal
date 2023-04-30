package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import org.vash.vate.VT;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelSocketListener implements Runnable
{
  private ServerSocket serverSocket;
  private VTTunnelChannel channel;
  private static final String SESSION_SEPARATOR = "\f\b";
  private static final char SESSION_MARK = 'C';
  private volatile boolean closed = false;
  
  public VTTunnelChannelSocketListener(VTTunnelChannel channel)
  {
//    try
//    {
//      this.serverSocket = new ServerSocket();
//    }
//    catch (Throwable e)
//    {
//      
//    }
    this.channel = channel;
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
      serverSocket.bind(channel.getBindAddress(), 0);
      return true;
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public void remove()
  {
    channel.getConnection().removeChannel(this);
  }
  
  public void run()
  {
    Thread.currentThread().setName(getClass().getSimpleName());
    try
    {
      while (!closed && !serverSocket.isClosed() && serverSocket.isBound())
      {
        Socket socket = listen();
        if (socket != null)
        {
          int channelType = channel.getChannelType();
          VTTunnelSession session = new VTTunnelSession(channel.getConnection(), socket, true);
          VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, channel);
          VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
          if (output != null)
          {
            int outputNumber = output.number();
            session.setOutputNumber(outputNumber);
            session.setTunnelOutputStream(output);
            if (channel.getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
            {
              String host = channel.getRedirectHost();
              int port = channel.getRedirectPort();
              // request message sent
              channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port).getBytes("UTF-8"));
              channel.getConnection().getControlOutputStream().flush();
            }
            else if (channel.getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
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