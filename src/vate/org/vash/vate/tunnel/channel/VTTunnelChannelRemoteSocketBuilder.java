package org.vash.vate.tunnel.channel;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.proxy.client.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelDatagramSocket;
import org.vash.vate.tunnel.session.VTTunnelPipedSocket;
import org.vash.vate.tunnel.session.VTTunnelServerSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannelRemoteSocketBuilder
{
  private final VTTunnelChannel channel;
  private static final String SESSION_SEPARATOR = "\f";
  private static final char SESSION_MARK = '\b';
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  
  public VTTunnelChannelRemoteSocketBuilder(VTTunnelChannel channel)
  {
    this.channel = channel;
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
  
//  public Socket connect(int channelType, String host, int port, VTProxy proxy) throws IOException
//  {
//    return connect(channelType, host, port, proxy.getProxyType(), proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword(), null);
//  }
  
  public Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy proxy) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    if (bind ==  null)
    {
      bind = "";
    }
    if (proxy == null)
    {
      return connectSocket(bind, host, port, connectTimeout, dataTimeout, PROXY_NONE.getProxyType(), PROXY_NONE.getProxyHost(), PROXY_NONE.getProxyPort(), PROXY_NONE.getProxyUser(), PROXY_NONE.getProxyPassword());
    }
    return connectSocket(bind, host, port, connectTimeout, dataTimeout, proxy.getProxyType(), proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
  }
  
  public Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxyType proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    
    if (bind == null)
    {
      bind = "";
    }
    
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
    else if (proxyType == VTProxyType.PLUS)
    {
      proxyTypeLetter = "P";
    }
    
    VTTunnelSession session = null;
    VTTunnelSessionHandler handler = null;
    int channelType = channel.getChannelType();
    
    session = new VTTunnelSession(channel.getConnection(), true);
    VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(session);
    session.setSocket(pipedSocket);
    handler = new VTTunnelSessionHandler(session, channel);
    
    VTLinkableDynamicMultiplexedInputStream input = channel.getConnection().getInputStream(channelType, handler);
    VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
    
    if (output != null && input != null)
    {
      final int inputNumber = input.number();
      final int outputNumber = output.number();
      
      pipedSocket.setOutputStream(output);
      session.setSocketInputStream(pipedSocket.getInputStream());
      session.setSocketOutputStream(pipedSocket.getOutputStream());
      
      input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
      output.open();
      
      session.setTunnelInputStream(input);
      session.setTunnelOutputStream(output);
      
      if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
      {
        proxyUser = "*";
        proxyPassword = "*" + SESSION_SEPARATOR + "*";
      }
      // request message sent
      channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
      channel.getConnection().getControlOutputStream().flush();
      //System.out.println("sent.request:output=" + outputNumber);
      boolean result = false;
      try
      {
        result = session.waitResult();
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (result)
      {
        pipedSocket.setRemoteHost(session.getRemoteHost());
        pipedSocket.setRemotePort(session.getRemotePort());
        return pipedSocket;
      }
    }
    else
    {
      // cannot handle more sessions
    }
    if (handler != null)
    {
      handler.close();
    }
    throw new IOException("Failed to connect remotely using: host " + host + " port " + port + "");
  }
  
  public Socket acceptSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    
    if (bind == null)
    {
      bind = "";
    }
    
    String proxyTypeLetter = "A";
    String proxyHost = "";
    int proxyPort = 0;
    String proxyUser = "";
    String proxyPassword = "";
    
    if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
    {
      proxyUser = "*";
      proxyPassword = "*" + SESSION_SEPARATOR + "*";
    }
    
    VTTunnelSession session = null;
    VTTunnelSessionHandler handler = null;
    int channelType = channel.getChannelType();
    
    session = new VTTunnelSession(channel.getConnection(), true);
    VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(session);
    session.setSocket(pipedSocket);
    handler = new VTTunnelSessionHandler(session, channel);
    
    VTLinkableDynamicMultiplexedInputStream input = channel.getConnection().getInputStream(channelType, handler);
    VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
    
    if (output != null && input != null)
    {
      final int inputNumber = input.number();
      final int outputNumber = output.number();
      
      pipedSocket.setOutputStream(output);
      session.setSocketInputStream(pipedSocket.getInputStream());
      session.setSocketOutputStream(pipedSocket.getOutputStream());
      
      input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
      output.open();
      
      session.setTunnelInputStream(input);
      session.setTunnelOutputStream(output);
      
      if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
      {
        proxyUser = "*";
        proxyPassword = "*" + SESSION_SEPARATOR + "*";
      }
      // request message sent
      channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
      channel.getConnection().getControlOutputStream().flush();
      //System.out.println("sent.request:output=" + outputNumber);
      boolean result = false;
      try
      {
        result = session.waitResult();
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (result)
      {
        pipedSocket.setRemoteHost(session.getRemoteHost());
        pipedSocket.setRemotePort(session.getRemotePort());
        return pipedSocket;
      }
    }
    else
    {
      // cannot handle more sessions
    }
    if (handler != null)
    {
      handler.close();
    }
    throw new IOException("Failed to accept remotely using: host " + host + " port " + port + "");
  }
  
  public ServerSocket bindSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    
    if (bind == null)
    {
      bind = "";
    }
    
    String proxyTypeLetter = "B";
    String proxyHost = "";
    int proxyPort = 0;
    String proxyUser = "";
    String proxyPassword = "";
    
    if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
    {
      proxyUser = "*";
      proxyPassword = "*" + SESSION_SEPARATOR + "*";
    }
    
    VTTunnelSession session = null;
    VTTunnelSessionHandler handler = null;
    int channelType = channel.getChannelType();
    
    session = new VTTunnelSession(channel.getConnection(), true);
    handler = new VTTunnelSessionHandler(session, channel);
    
    VTLinkableDynamicMultiplexedInputStream input = channel.getConnection().getInputStream(channelType, handler);
    VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
    
    if (output != null && input != null)
    {
      final int inputNumber = input.number();
      final int outputNumber = output.number();
      
      session.setTunnelInputStream(input);
      session.setTunnelOutputStream(output);
      
      // request message sent
      channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
      channel.getConnection().getControlOutputStream().flush();
      //System.out.println("sent.request:output=" + outputNumber);
      boolean result = false;
      try
      {
        result = session.waitResult();
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (result)
      {
        if (handler != null)
        {
          handler.close();
        }
        host = session.getRemoteHost();
        port = session.getRemotePort();
        VTTunnelServerSocket serverSocket = new VTTunnelServerSocket(channel.getConnection().createRemoteSocketFactory(channel), bind, host, port, connectTimeout, dataTimeout);
        return serverSocket;
      }
    }
    else
    {
      // cannot handle more sessions
    }
    if (handler != null)
    {
      handler.close();
    }
    throw new IOException("Failed to bind remotely using: host " + host + " port " + port + "");
  }
  
  public void unbindSocket(String bind) throws IOException
  {
    if (bind == null)
    {
      bind = "";
    }
    
    String host = "";
    int port = 0;
    int connectTimeout = 0;
    int dataTimeout = 0;
    String proxyTypeLetter = "U";
    String proxyHost = "";
    int proxyPort = 0;
    String proxyUser = "";
    String proxyPassword = "";
    
    if (proxyUser == null || proxyPassword == null || proxyUser.length() == 0 || proxyPassword.length() == 0)
    {
      proxyUser = "*";
      proxyPassword = "*" + SESSION_SEPARATOR + "*";
    }
    
    final int inputNumber = -1;
    final int outputNumber = -1;
    
    //VTTunnelSession session = null;
    //VTTunnelSessionHandler handler = null;
    int channelType = channel.getChannelType();
    
    channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + connectTimeout + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port + SESSION_SEPARATOR + proxyTypeLetter + SESSION_SEPARATOR + proxyHost + SESSION_SEPARATOR + proxyPort + SESSION_SEPARATOR + proxyUser + SESSION_SEPARATOR + proxyPassword).getBytes("UTF-8"));
    channel.getConnection().getControlOutputStream().flush();
  }
  
//  public DatagramSocket create(InetAddress address, int port, int dataTimeout) throws IOException
//  {
//    return create(address.getHostAddress(), port, dataTimeout);
//  }
  
  public DatagramSocket createSocket(String host, int port, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    
    String bind = "";
    
    VTTunnelSession session = null;
    VTTunnelSessionHandler handler = null;
    int channelType = channel.getChannelType();
    
    session = new VTTunnelSession(channel.getConnection(), true);
    VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(session);
    session.setSocket(pipedSocket);
    handler = new VTTunnelSessionHandler(session, channel);
    
    VTLinkableDynamicMultiplexedInputStream input = channel.getConnection().getInputStream(channelType, handler);
    VTLinkableDynamicMultiplexedOutputStream output = channel.getConnection().getOutputStream(channelType, handler);
    
    if (output != null && input != null)
    {
      final int inputNumber = input.number();
      final int outputNumber = output.number();
      
      pipedSocket.setOutputStream(output);
      session.setSocketInputStream(pipedSocket.getInputStream());
      session.setSocketOutputStream(pipedSocket.getOutputStream());
      
      input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
      output.open();
      
      session.setTunnelInputStream(input);
      session.setTunnelOutputStream(output);
      
      // request message sent
      channel.getConnection().getControlOutputStream().writeData(("U" + SESSION_MARK + "U" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + 0 + SESSION_SEPARATOR + dataTimeout + SESSION_SEPARATOR + bind + SESSION_SEPARATOR + host + SESSION_SEPARATOR + port).getBytes("UTF-8"));
      channel.getConnection().getControlOutputStream().flush();
      //System.out.println("sent.request:output=" + outputNumber);
      boolean result = false;
      try
      {
        result = session.waitResult();
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (result)
      {
        VTTunnelDatagramSocket datagramSocket = new VTTunnelDatagramSocket(pipedSocket, session.getRemoteHost(), session.getRemotePort());
        datagramSocket.setTunnelInputStream(pipedSocket.getInputStream());
        datagramSocket.setTunnelOutputStream(pipedSocket.getOutputStream());
        return datagramSocket;
      }
    }
    else
    {
      // cannot handle more sessions
    }
    if (handler != null)
    {
      handler.close();
    }
    throw new IOException("Failed to create datagram tunnel using host " + host + " port " + port + "");
  }
}