package org.vash.vate.socket.remote;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import org.vash.vate.socket.proxy.VTProxy;

public class VTRemoteSocket extends Socket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  private Socket remoteSocket;
  
  public VTRemoteSocket(VTRemoteSocketFactory remoteSocketFactory)
  {
    this.remoteSocketFactory = remoteSocketFactory;
  }
  
  public VTRemoteSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port) throws IOException
  {
    this.remoteSocketFactory = remoteSocketFactory;
    connect(host, port, 0, 0);
  }
  
  public VTRemoteSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port, int connectTimeout) throws IOException
  {
    this.remoteSocketFactory = remoteSocketFactory;
    connect(host, port, connectTimeout);
  }
  
  public VTRemoteSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    this.remoteSocketFactory = remoteSocketFactory;
    connect(host, port, connectTimeout, dataTimeout);
  }
  
  public VTRemoteSocket(VTRemoteSocketFactory remoteSocketFactory, String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException
  {
    this.remoteSocketFactory = remoteSocketFactory;
    connect(host, port, connectTimeout, dataTimeout, proxies);
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (endpoint instanceof InetSocketAddress)
    {
      InetSocketAddress address = (InetSocketAddress) endpoint;
      connect(address.getHostName(), address.getPort());
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (endpoint instanceof InetSocketAddress)
    {
      InetSocketAddress address = (InetSocketAddress) endpoint;
      connect(address.getHostName(), address.getPort());
    }
  }
  
  public void connect(String host, int port) throws IOException
  {
    remoteSocket = remoteSocketFactory.createSocket(host, port, 0, 0, new VTProxy[] {});
  }
  
  public void connect(String host, int port, int connectTimeout) throws IOException
  {
    remoteSocket = remoteSocketFactory.createSocket(host, port, connectTimeout, 0, new VTProxy[] {});
  }
  
  public void connect(String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    remoteSocket = remoteSocketFactory.createSocket(host, port, connectTimeout, dataTimeout, new VTProxy[] {});
  }
  
  public void connect(String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException
  {
    remoteSocket = remoteSocketFactory.createSocket(host, port, connectTimeout, dataTimeout, proxies);
  }
  
  public void bind(SocketAddress bindpoint) throws IOException
  {
    
  }
  
  public InetAddress getInetAddress()
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getLocalAddress();
  }
  
  public int getPort()
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getPort();
  }
  
  public int getLocalPort()
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getLocalSocketAddress();
  }
  
  public SocketChannel getChannel()
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getChannel();
  }
  
  public InputStream getInputStream() throws IOException
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getInputStream();
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.getOutputStream();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.getOOBInline();
  }
  
  public synchronized void setSoTimeout(int timeout) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setSoTimeout(timeout);
  }
  
  public synchronized int getSoTimeout() throws SocketException
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getSoTimeout();
  }
  
  public synchronized void setSendBufferSize(int size) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setSendBufferSize(size);
  }
  
  public synchronized int getSendBufferSize() throws SocketException
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getSendBufferSize();
  }
  
  public synchronized void setReceiveBufferSize(int size) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setReceiveBufferSize(size);
  }
  
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    if (remoteSocket == null)
    {
      return -1;
    }
    return remoteSocket.getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.getReuseAddress();
  }
  
  public synchronized void close() throws IOException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.close();
  }
  
  public void shutdownInput() throws IOException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.shutdownInput();
  }
  
  public void shutdownOutput() throws IOException
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.shutdownOutput();
  }
  
  public String toString()
  {
    if (remoteSocket == null)
    {
      return null;
    }
    return remoteSocket.toString();
  }
  
  public boolean isConnected()
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.isConnected();
  }
  
  public boolean isBound()
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.isBound();
  }
  
  public boolean isClosed()
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.isClosed();
  }
  
  public boolean isInputShutdown()
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.isInputShutdown();
  }
  
  public boolean isOutputShutdown()
  {
    if (remoteSocket == null)
    {
      return false;
    }
    return remoteSocket.isOutputShutdown();
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    if (remoteSocket == null)
    {
      return;
    }
    remoteSocket.setPerformancePreferences(connectionTime, latency, bandwidth);
  }
}