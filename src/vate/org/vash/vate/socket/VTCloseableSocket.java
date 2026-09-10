package org.vash.vate.socket;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

public class VTCloseableSocket extends Socket implements Closeable
{
  private final Socket proxySocket;
  private final InputStream input;
  private final OutputStream output;
  
  public VTCloseableSocket(Socket socket)
  {
    this.proxySocket = socket;
    this.input = null;
    this.output = null;
  }
  
  public VTCloseableSocket(Socket socket, InputStream input)
  {
    this.proxySocket = socket;
    this.input = input;
    this.output = null;
  }
  
  public VTCloseableSocket(Socket socket, InputStream input, OutputStream output)
  {
    this.proxySocket = socket;
    this.input = input;
    this.output = output;
  }
  
  public InputStream getInputStream() throws IOException
  {
    if (input != null)
    {
      return input;
    }
    return proxySocket.getInputStream();
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    if (output != null)
    {
      return output;
    }
    return proxySocket.getOutputStream();
  }
  
  public InetAddress getInetAddress()
  {
    return proxySocket.getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    return proxySocket.getLocalAddress();
  }
  
  public int getPort()
  {
    return proxySocket.getPort();
  }
  
  public int getLocalPort()
  {
    return proxySocket.getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    return proxySocket.getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return proxySocket.getLocalSocketAddress();
  }
  
  public SocketChannel getChannel()
  {
    return proxySocket.getChannel();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    proxySocket.setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    return proxySocket.getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    proxySocket.setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    return proxySocket.getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    proxySocket.sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    proxySocket.setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    return proxySocket.getOOBInline();
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    proxySocket.setSoTimeout(timeout);
  }
  
  public int getSoTimeout() throws SocketException
  {
    return proxySocket.getSoTimeout();
  }
  
  public void setSendBufferSize(int size) throws SocketException
  {
    proxySocket.setSendBufferSize(size);
  }
  
  public int getSendBufferSize() throws SocketException
  {
    return proxySocket.getSendBufferSize();
  }
  
  public void setReceiveBufferSize(int size) throws SocketException
  {
    proxySocket.setReceiveBufferSize(size);
  }
  
  public int getReceiveBufferSize() throws SocketException
  {
    return proxySocket.getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    proxySocket.setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    return proxySocket.getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    proxySocket.setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    return proxySocket.getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    proxySocket.setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    return proxySocket.getReuseAddress();
  }
  
  public void close() throws IOException
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
  }
  
  public void shutdownInput() throws IOException
  {
    proxySocket.shutdownInput();
  }
  
  public void shutdownOutput() throws IOException
  {
    proxySocket.shutdownOutput();
  }
  
  public String toString()
  {
    return proxySocket.toString();
  }
  
  public boolean isConnected()
  {
    return proxySocket.isConnected();
  }
  
  public boolean isBound()
  {
    return proxySocket.isBound();
  }
  
  public boolean isClosed()
  {
    return proxySocket.isClosed();
  }
  
  public boolean isInputShutdown()
  {
    return proxySocket.isInputShutdown();
  }
  
  public boolean isOutputShutdown()
  {
    return proxySocket.isOutputShutdown();
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    proxySocket.setPerformancePreferences(connectionTime, latency, bandwidth);
  }
}