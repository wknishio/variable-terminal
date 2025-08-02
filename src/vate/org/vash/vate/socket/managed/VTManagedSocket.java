package org.vash.vate.socket.managed;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;

import org.vash.vate.VT;
import org.vash.vate.stream.filter.VTBufferedOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedSocket extends Socket
{
  private final VTManagedConnection connection;
  private final VTLinkableDynamicMultiplexedInputStream in;
  private final VTLinkableDynamicMultiplexedOutputStream out;
  private final InputStream input;
  private final OutputStream output;
  
  public VTManagedSocket(VTManagedConnection connection)
  {
    this.connection = connection;
    this.in = connection.getInputStream(connection.getInputStreamIndexStart());
    this.out = connection.getOutputStream(connection.getOutputStreamIndexStart());
    this.input = new BufferedInputStream(in, VT.VT_STANDARD_BUFFER_SIZE_BYTES);
    this.output = new VTBufferedOutputStream(out, VT.VT_STANDARD_BUFFER_SIZE_BYTES, true);
  }
  
//  public Socket getConnectionSocket()
//  {
//    return managedConnection.getConnectionSocket();
//  }
  
  public VTManagedConnection getConnection()
  {
    return connection;
  }
  
  public void requestPing()
  {
    connection.requestPing();
  }
  
  public long checkPing()
  {
    return connection.checkPing();
  }
  
  public long checkPing(long timeoutNanoSeconds)
  {
    return connection.checkPing(timeoutNanoSeconds);
  }
  
  public InputStream getInputStream()
  {
    return input;
  }
  
  public OutputStream getOutputStream()
  {
    return output;
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(Object link)
  {
    return connection.getInputStream(link);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(Object link)
  {
    return connection.getOutputStream(link);
  }
  
  public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, Object link)
  {
    return connection.getInputStream(type, link);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, Object link)
  {
    return connection.getOutputStream(type, link);
  }
  
  public int setInputStreamOutputStream(Object link, OutputStream outputStream, Closeable closeable)
  {
    return connection.setInputStreamOutputStream(link, outputStream, closeable);
  }
  
  public int setInputStreamOutputStream(int type, Object link, OutputStream outputStream, Closeable closeable)
  {
    return connection.setInputStreamOutputStream(type, link, outputStream, closeable);
  }
  
  public InputStream createBufferedInputStream(int number)
  {
    return connection.createBufferedInputStream(number);
  }
  
  public OutputStream createBufferedOutputStream(int number)
  {
    return connection.createBufferedOutputStream(number);
  }
  
  public InputStream createBufferedInputStream(int type, int number)
  {
    return connection.createBufferedInputStream(type, number);
  }
  
  public OutputStream createBufferedOutputStream(int type, int number)
  {
    return connection.createBufferedOutputStream(type, number);
  }
  
  public InputStream createBufferedInputStream(Object link)
  {
    return connection.createBufferedInputStream(link);
  }
  
  public OutputStream createBufferedOutputStream(Object link)
  {
    return connection.createBufferedOutputStream(link);
  }
  
  public InputStream createBufferedInputStream(int type, Object link)
  {
    return connection.createBufferedInputStream(type, link);
  }
  
  public OutputStream createBufferedOutputStream(int type, Object link)
  {
    return connection.createBufferedOutputStream(type, link);
  }
  
  public void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
  {
    connection.releaseInputStream(stream);
  }
  
  public void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
  {
    connection.releaseOutputStream(stream);
  }
  
  public int getInputStreamIndexStart()
  {
    return connection.getInputStreamIndexStart();
  }
  
  public int getOutputStreamIndexStart()
  {
    return connection.getOutputStreamIndexStart();
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
    connection.close();
  }
  
  public InetAddress getInetAddress()
  {
    return connection.getSocket().getInetAddress();
  }
  
  public InetAddress getLocalAddress()
  {
    return connection.getSocket().getLocalAddress();
  }
  
  public int getPort()
  {
    return connection.getSocket().getPort();
  }
  
  public int getLocalPort()
  {
    return connection.getSocket().getLocalPort();
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    return connection.getSocket().getRemoteSocketAddress();
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return connection.getSocket().getLocalSocketAddress();
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    connection.getSocket().setTcpNoDelay(on);
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    return connection.getSocket().getTcpNoDelay();
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    connection.getSocket().setSoLinger(on, linger);
  }
  
  public int getSoLinger() throws SocketException
  {
    return connection.getSocket().getSoLinger();
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    connection.getSocket().sendUrgentData(data);
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    connection.getSocket().setOOBInline(on);
  }
  
  public boolean getOOBInline() throws SocketException
  {
    return connection.getSocket().getOOBInline();
  }
  
  public synchronized void setSoTimeout(int timeout) throws SocketException
  {
    connection.getSocket().setSoTimeout(timeout);
  }
  
  public synchronized int getSoTimeout() throws SocketException
  {
    return connection.getSocket().getSoTimeout();
  }
  
  public synchronized void setSendBufferSize(int size) throws SocketException
  {
    connection.getSocket().setSendBufferSize(size);
  }
  
  public synchronized int getSendBufferSize() throws SocketException
  {
    return connection.getSocket().getSendBufferSize();
  }
  
  public synchronized void setReceiveBufferSize(int size) throws SocketException
  {
    connection.getSocket().setReceiveBufferSize(size);
  }
  
  public synchronized int getReceiveBufferSize() throws SocketException
  {
    return connection.getSocket().getReceiveBufferSize();
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    connection.getSocket().setKeepAlive(on);
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    return connection.getSocket().getKeepAlive();
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    connection.getSocket().setTrafficClass(tc);
  }
  
  public int getTrafficClass() throws SocketException
  {
    return connection.getSocket().getTrafficClass();
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    connection.getSocket().setReuseAddress(on);
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    return connection.getSocket().getReuseAddress();
  }
  
  public String toString()
  {
    return connection.getSocket().toString();
  }
  
  public boolean isConnected()
  {
    return connection.getSocket().isConnected();
  }
  
  public boolean isBound()
  {
    return connection.getSocket().isBound();
  }
  
  public boolean isClosed()
  {
    return connection.getSocket().isClosed();
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
    connection.getSocket().setPerformancePreferences(connectionTime, latency, bandwidth);
  }
  
  public long getOutputRateBytesPerSecond()
  {
    return connection.getOutputRateBytesPerSecond();
  }
  
  public void setOutputRateBytesPerSecond(long bytesPerSecond)
  {
    connection.setOutputRateBytesPerSecond(bytesPerSecond);
  }
}