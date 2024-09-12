package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public abstract class VTProxySocket extends Socket
{
  protected VTProxy currentProxy;
  protected Socket currentSocket;
  protected Socket proxySocket;
  
  public VTProxySocket(VTProxy currentProxy, Socket currentSocket)
  {
    this.currentProxy = currentProxy;
    this.currentSocket = currentSocket;
  }
  
  public void connectProxy(int timeout) throws IOException
  {
    if (currentProxy != null && currentSocket != null && currentSocket instanceof VTProxySocket)
    {
      ((VTProxySocket)currentSocket).connectSocket(currentProxy.getProxyHost(), currentProxy.getProxyPort(), timeout);
      currentSocket.setTcpNoDelay(true);
      currentSocket.setKeepAlive(true);
    }
    else if (currentSocket == null || currentSocket.isClosed() || !currentSocket.isConnected())
    {
      InetSocketAddress proxyAddress = new InetSocketAddress(currentProxy.getProxyHost(), currentProxy.getProxyPort());
      currentSocket = new Socket(Proxy.NO_PROXY);
      if (timeout > 0)
      {
        currentSocket.connect(proxyAddress, timeout);
      }
      else
      {
        currentSocket.connect(proxyAddress);
      }
      currentSocket.setTcpNoDelay(true);
      currentSocket.setKeepAlive(true);
    }
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (endpoint instanceof InetSocketAddress)
    {
      InetSocketAddress host = (InetSocketAddress) endpoint;
      connectSocket(host.getHostName(), host.getPort(), 0);
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (endpoint instanceof InetSocketAddress)
    {
      InetSocketAddress host = (InetSocketAddress) endpoint;
      connectSocket(host.getHostName(), host.getPort(), timeout);
    }
  }
  
  public abstract void connectSocket(String host, int port, int timeout) throws IOException;
  
  public InetAddress getInetAddress()
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getLocalAddress();
  }
  
  public int getPort()
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getPort();
  }
  
  public int getLocalPort()
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getLocalSocketAddress();
  }
  
  public SocketChannel getChannel()
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getChannel();
  }
  
  public InputStream getInputStream() throws IOException
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getInputStream();
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.getOutputStream();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.getOOBInline();
  }
  
  public synchronized void setSoTimeout(int timeout) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setSoTimeout(timeout);
  }
  
  public synchronized int getSoTimeout() throws SocketException
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getSoTimeout();
  }
  
  public synchronized void setSendBufferSize(int size) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setSendBufferSize(size);
  }
  
  public synchronized int getSendBufferSize() throws SocketException
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getSendBufferSize();
  }
  
  public synchronized void setReceiveBufferSize(int size) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setReceiveBufferSize(size);
  }
  
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    if (proxySocket == null)
    {
      return -1;
    }
    return proxySocket.getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.getReuseAddress();
  }
  
  public synchronized void close() throws IOException
  {
    if (proxySocket != null)
    {
      try
      {
        proxySocket.close();
      }
      catch (Throwable t)
      {
        
      }
    }
    if (currentSocket != null)
    {
      try
      {
        currentSocket.close();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void shutdownInput() throws IOException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.shutdownInput();
  }
  
  public void shutdownOutput() throws IOException
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.shutdownOutput();
  }
  
  public String toString()
  {
    if (proxySocket == null)
    {
      return null;
    }
    return proxySocket.toString();
  }
  
  public boolean isConnected()
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.isConnected();
  }
  
  public boolean isBound()
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.isBound();
  }
  
  public boolean isClosed()
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.isClosed();
  }
  
  public boolean isInputShutdown()
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.isInputShutdown();
  }
  
  public boolean isOutputShutdown()
  {
    if (proxySocket == null)
    {
      return false;
    }
    return proxySocket.isOutputShutdown();
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    if (proxySocket == null)
    {
      return;
    }
    proxySocket.setPerformancePreferences(connectionTime, latency, bandwidth);
  }
}
