package org.vash.vate.socket;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import net.sourceforge.jsocks.socks.Socks4Proxy;
import net.sourceforge.jsocks.socks.Socks5Proxy;
import net.sourceforge.jsocks.socks.SocksSocket;
import net.sourceforge.jsocks.socks.UserPasswordAuthentication;

public class VTSocksProxySocket extends Socket
{
  private Socks5Proxy proxyClient5;
  private Socks4Proxy proxyClient4;
  private Socket socket;
  
  public VTSocksProxySocket(Socket proxyConnection, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    if (proxyHost == null)
    {
      proxyHost = "";
    }
    proxyClient5 = new Socks5Proxy(null, proxyHost, proxyPort, proxyConnection);
    if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      UserPasswordAuthentication authentication = new UserPasswordAuthentication(proxyUser, proxyPassword);
      proxyClient5.setAuthenticationMethod(UserPasswordAuthentication.METHOD_ID, authentication);
    }
    proxyClient4 = new Socks4Proxy(null, proxyHost, proxyPort, proxyUser != null ? proxyUser : "", proxyConnection);
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (socket == null)
    {
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient5, host.getHostName(), host.getPort());
        socket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (socket != null)
      {
        return;
      }
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient4, host.getHostName(), host.getPort());
        socket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (socket == null)
      {
        throw new IOException("socks tunneling failed");
      }
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (socket == null)
    {
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient5, host.getHostName(), host.getPort());
        socket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (socket != null)
      {
        return;
      }
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient4, host.getHostName(), host.getPort());
        socket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (socket == null)
      {
        throw new IOException("socks tunneling failed");
      }
    }
  }
  
  public void bind(SocketAddress bindpoint) throws IOException
  {
    
  }
  
  public InetAddress getInetAddress()
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getLocalAddress();
  }
  
  public int getPort()
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getPort();
  }
  
  public int getLocalPort()
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getLocalSocketAddress();
  }
  
  public SocketChannel getChannel()
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getChannel();
  }
  
  public InputStream getInputStream() throws IOException
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getInputStream();
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    if (socket == null)
    {
      return null;
    }
    return socket.getOutputStream();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    if (socket == null)
    {
      return false;
    }
    return socket.getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    if (socket == null)
    {
      return;
    }
    socket.sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    if (socket == null)
    {
      return false;
    }
    return socket.getOOBInline();
  }
  
  public synchronized void setSoTimeout(int timeout) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setSoTimeout(timeout);
  }
  
  public synchronized int getSoTimeout() throws SocketException
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getSoTimeout();
  }
  
  public synchronized void setSendBufferSize(int size) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setSendBufferSize(size);
  }
  
  public synchronized int getSendBufferSize() throws SocketException
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getSendBufferSize();
  }
  
  public synchronized void setReceiveBufferSize(int size) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setReceiveBufferSize(size);
  }
  
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    if (socket == null)
    {
      return false;
    }
    return socket.getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    if (socket == null)
    {
      return -1;
    }
    return socket.getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    if (socket == null)
    {
      return;
    }
    socket.setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    if (socket == null)
    {
      return false;
    }
    return socket.getReuseAddress();
  }
  
  public synchronized void close() throws IOException
  {
    if (socket == null)
    {
      return;
    }
    socket.close();
  }
  
  public void shutdownInput() throws IOException
  {
    if (socket == null)
    {
      return;
    }
    socket.shutdownInput();
  }
  
  public void shutdownOutput() throws IOException
  {
    if (socket == null)
    {
      return;
    }
    socket.shutdownOutput();
  }
  
  public String toString()
  {
    if (socket == null)
    {
      return null;
    }
    return socket.toString();
  }
  
  public boolean isConnected()
  {
    if (socket == null)
    {
      return false;
    }
    return socket.isConnected();
  }
  
  public boolean isBound()
  {
    if (socket == null)
    {
      return false;
    }
    return socket.isBound();
  }
  
  public boolean isClosed()
  {
    if (socket == null)
    {
      return false;
    }
    return socket.isClosed();
  }
  
  public boolean isInputShutdown()
  {
    if (socket == null)
    {
      return false;
    }
    return socket.isInputShutdown();
  }
  
  public boolean isOutputShutdown()
  {
    if (socket == null)
    {
      return false;
    }
    return socket.isOutputShutdown();
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    if (socket == null)
    {
      return;
    }
    socket.setPerformancePreferences(connectionTime, latency, bandwidth);
  }
}
