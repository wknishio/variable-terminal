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
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int number, int type) throws IOException
  {
    return managedConnection.getInputStream(number, type);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number, int type) throws IOException
  {
    return managedConnection.getOutputStream(number, type);
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
    return managedConnection.getConnectionSocket().getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    return managedConnection.getConnectionSocket().getLocalAddress();
  }
  
  public int getPort()
  {
    return managedConnection.getConnectionSocket().getPort();
  }
  
  public int getLocalPort()
  {
    return managedConnection.getConnectionSocket().getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    return managedConnection.getConnectionSocket().getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return managedConnection.getConnectionSocket().getLocalSocketAddress();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    managedConnection.getConnectionSocket().setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    return managedConnection.getConnectionSocket().getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    managedConnection.getConnectionSocket().setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    return managedConnection.getConnectionSocket().getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    managedConnection.getConnectionSocket().sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    managedConnection.getConnectionSocket().setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    return managedConnection.getConnectionSocket().getOOBInline();
  }
  
  public synchronized void setSoTimeout(int timeout) throws SocketException
  {
    managedConnection.getConnectionSocket().setSoTimeout(timeout);
  }
  
  public synchronized int getSoTimeout() throws SocketException
  {
    return managedConnection.getConnectionSocket().getSoTimeout();
  }
  
  public synchronized void setSendBufferSize(int size) throws SocketException
  {
    managedConnection.getConnectionSocket().setSendBufferSize(size);
  }
  
  public synchronized int getSendBufferSize() throws SocketException
  {
    return managedConnection.getConnectionSocket().getSendBufferSize();
  }
  
  public synchronized void setReceiveBufferSize(int size) throws SocketException
  {
    managedConnection.getConnectionSocket().setReceiveBufferSize(size);
  }
  
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    return managedConnection.getConnectionSocket().getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    managedConnection.getConnectionSocket().setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    return managedConnection.getConnectionSocket().getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    managedConnection.getConnectionSocket().setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    return managedConnection.getConnectionSocket().getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    managedConnection.getConnectionSocket().setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    return managedConnection.getConnectionSocket().getReuseAddress();
  }
  
  public String toString()
  {
    return managedConnection.getConnectionSocket().toString();
  }
  
  public boolean isConnected()
  {
    return managedConnection.getConnectionSocket().isConnected();
  }
  
  public boolean isBound()
  {
    return managedConnection.getConnectionSocket().isBound();
  }
  
  public boolean isClosed()
  {
    return managedConnection.getConnectionSocket().isClosed();
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
    managedConnection.getConnectionSocket().setPerformancePreferences(connectionTime, latency, bandwidth);
  }
}