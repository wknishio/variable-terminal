package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.SocketException;
import java.nio.channels.SocketChannel;

import org.vash.vate.VTSystem;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public class VTTunnelPipedSocket extends Socket implements Closeable
{
  private InputStream in;
  private OutputStream out;
  private OutputStream pipe;
  private Closeable closeable;
  private String host = "";
  private int port = 0;
  private volatile boolean closed = false;
  
  public VTTunnelPipedSocket(Closeable closeable)
  {
    this.closeable = closeable;
    VTPipedInputStream pipeSink = new VTPipedInputStream(VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES);
    VTPipedOutputStream pipeSource = new VTPipedOutputStream();
    try
    {
      pipeSink.connect(pipeSource);
    }
    catch (Throwable t)
    {
      
    }
    this.in = pipeSink;
    this.pipe = pipeSource;
  }
  
  public VTTunnelPipedSocket(Closeable closeable, InputStream in, OutputStream out)
  {
    this.closeable = closeable;
    this.in = in;
    this.out = out;
  }
  
  public void setOutputStream(OutputStream output) throws IOException
  {
    this.out = output;
  }
  
  //public void setCloseable(Closeable closeable)
  //{
    //this.closeable = closeable;
  //}
  
  public InputStream getInputStream()
  {
    return in;
  }
  
  public OutputStream getOutputStream()
  {
    return out;
  }
  
  public OutputStream getInputStreamSource()
  {
    return pipe;
  }
  
  public void shutdownOutput() throws IOException
  {
    
  }
  
  public void shutdownInput() throws IOException
  {
    
  }
  
  public boolean isBound()
  {
    return !isClosed();
  }
  
  public boolean isConnected()
  {
    return !isClosed();
  }
  
  public boolean isClosed()
  {
    return closed;
  }
  
  public void close() throws IOException
  {
    if (closed)
    {
      return;
    }
    closed = true;
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
    if (pipe != null)
    {
      try
      {
        pipe.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    if (closeable != null)
    {
      try
      {
        closeable.close();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public void setRemotePort(int port)
  {
    this.port = port;
  }
  
  public void setRemoteHost(String host)
  {
    this.host = host;
  }
  
  public InetAddress getInetAddress()
  {
    try
    {
      return InetAddress.getByName(host);
    }
    catch (Throwable e)
    {
      
    }
    return null;
  }
  
  public int getPort()
  {
    return port;
  }
  
  public SocketAddress getRemoteSocketAddress()
  {
    return InetSocketAddress.createUnresolved(host, port);
  }
  
  public InetAddress getLocalAddress()
  {
    return null;
  }
  
  public int getLocalPort()
  {
    return -1;
  }
  
  public SocketAddress getLocalSocketAddress()
  {
    return null;
  }
  
  public SocketChannel getChannel()
  {
    return null;
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    
  }
  
  public boolean getTcpNoDelay() throws SocketException
  {
    return false;
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    
  }
  
  public int getSoLinger() throws SocketException
  {
    return -1;
  }
  
  public void sendUrgentData (int data) throws IOException
  {
    
  }
  
  public void setOOBInline(boolean on) throws SocketException
  {
    
  }
  
  public boolean getOOBInline() throws SocketException
  {
    return false;
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    
  }
  
  public int getSoTimeout() throws SocketException
  {
    return -1;
  }
  
  public void setSendBufferSize(int size) throws SocketException
  {
    
  }
  
  public int getSendBufferSize() throws SocketException
  {
    return -1;
  }
  
  public void setReceiveBufferSize(int size) throws SocketException
  {
    
  }
  
  public int getReceiveBufferSize() throws SocketException
  {
    return -1;
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    
  }
  
  public boolean getKeepAlive() throws SocketException
  {
    return false;
  }
  
  public void setTrafficClass(int tc) throws SocketException
  {
    
  }
  
  public int getTrafficClass() throws SocketException
  {
    return -1;
  }
  
  public void setReuseAddress(boolean on) throws SocketException
  {
    
  }
  
  public boolean getReuseAddress() throws SocketException
  {
    return false;
  }
  
  public boolean isInputShutdown()
  {
    return false;
  }
  
  public boolean isOutputShutdown()
  {
    return false;
  }
  
  public void setPerformancePreferences(int connectionTime, int latency, int bandwidth)
  {
    
  }
}