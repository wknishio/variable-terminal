package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedSocket extends Socket implements Closeable
{
  private VTManagedConnection managedConnection;
  private VTLinkableDynamicMultiplexedInputStream in;
  private VTLinkableDynamicMultiplexedOutputStream out;
  
  public VTManagedSocket(VTManagedConnection managedConnection, VTLinkableDynamicMultiplexedInputStream in, VTLinkableDynamicMultiplexedOutputStream out)
  {
    this.managedConnection = managedConnection;
    this.in = in;
    this.out = out;
  }
  
//  public Socket getConnectionSocket()
//  {
//    return managedConnection.getConnectionSocket();
//  }
  
  public VTManagedConnection getConnection()
  {
    return managedConnection;
  }
  
  public void requestPing()
  {
    managedConnection.requestPing();
  }
  
  public long checkPing()
  {
    return managedConnection.checkPing();
  }
  
  public long checkPing(long timeoutNanoSeconds)
  {
    return managedConnection.checkPing(timeoutNanoSeconds);
  }
  
  public InputStream getInputStream()
  {
    return in;
  }
  
  public OutputStream getOutputStream()
  {
    return out;
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int number) throws IOException
  {
    return managedConnection.getInputStream(number);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number) throws IOException
  {
    return managedConnection.getOutputStream(number);
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number) throws IOException
  {
    return managedConnection.getInputStream(type, number);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number) throws IOException
  {
    return managedConnection.getOutputStream(type, number);
  }
  
  public void shutdownOutput() throws IOException
  {
    if (out != null)
    {
      try
      {
        out.close();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public void shutdownInput() throws IOException
  {
    if (in != null)
    {
      try
      {
        in.close();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public void close() throws IOException
  {
    managedConnection.close();
  }
  
  public InetAddress getInetAddress()
  {
    return managedConnection.getSocket().getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    return managedConnection.getSocket().getLocalAddress();
  }
  
  public int getPort()
  {
    return managedConnection.getSocket().getPort();
  }
  
  public int getLocalPort()
  {
    return managedConnection.getSocket().getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    return managedConnection.getSocket().getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return managedConnection.getSocket().getLocalSocketAddress();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    managedConnection.getSocket().setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    return managedConnection.getSocket().getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    managedConnection.getSocket().setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    return managedConnection.getSocket().getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    managedConnection.getSocket().sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    managedConnection.getSocket().setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    return managedConnection.getSocket().getOOBInline();
  }
  
  public synchronized void setSoTimeout(int timeout) throws SocketException
  {
    managedConnection.getSocket().setSoTimeout(timeout);
  }
  
  public synchronized int getSoTimeout() throws SocketException
  {
    return managedConnection.getSocket().getSoTimeout();
  }
  
  public synchronized void setSendBufferSize(int size) throws SocketException
  {
    managedConnection.getSocket().setSendBufferSize(size);
  }
  
  public synchronized int getSendBufferSize() throws SocketException
  {
    return managedConnection.getSocket().getSendBufferSize();
  }
  
  public synchronized void setReceiveBufferSize(int size) throws SocketException
  {
    managedConnection.getSocket().setReceiveBufferSize(size);
  }
  
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    return managedConnection.getSocket().getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    managedConnection.getSocket().setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    return managedConnection.getSocket().getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    managedConnection.getSocket().setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    return managedConnection.getSocket().getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    managedConnection.getSocket().setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    return managedConnection.getSocket().getReuseAddress();
  }
  
  public String toString()
  {
    return managedConnection.getSocket().toString();
  }
  
  public boolean isConnected()
  {
    return managedConnection.getSocket().isConnected();
  }
  
  public boolean isBound()
  {
    return managedConnection.getSocket().isBound();
  }
  
  public boolean isClosed()
  {
    return managedConnection.getSocket().isClosed();
  }
  
  public boolean isInputShutdown()
  {
    return in.closed();
  }
  
  public boolean isOutputShutdown()
  {
    return out.closed();
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    managedConnection.getSocket().setPerformancePreferences(connectionTime, latency, bandwidth);
  }
  
  public long getOutputRateBytesPerSecond()
  {
    return managedConnection.getOutputRateBytesPerSecond();
  }
  
  public void setOutputRateBytesPerSecond(long bytesPerSecond)
  {
    managedConnection.setOutputRateBytesPerSecond(bytesPerSecond);
  }
}