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

import org.vash.vate.VT;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;

public class VTTunnelPipedSocket extends Socket implements Closeable
{
  private InputStream in;
  private VTLinkableDynamicMultiplexedOutputStream out;
  private OutputStream pipe;
  private Closeable closeable;
  private String host;
  private int port;
  private volatile boolean closed = false;
  
  public VTTunnelPipedSocket(Closeable closeable)
  {
    this.closeable = closeable;
    VTPipedInputStream pipeSink = new VTPipedInputStream(VT.VT_STANDARD_BUFFER_SIZE_BYTES);
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
  
  public void setOutputStream(VTLinkableDynamicMultiplexedOutputStream output) throws IOException
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
    return closed && out.closed();
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
    //if (closeable != null)
    //{
      //try
      //{
        //closeable.close();
      //}
      //catch (Throwable e)
      //{
        
      //}
    //}
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    //super.setSoTimeout(timeout);
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    //super.setTcpNoDelay(true);
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    //super.setSoLinger(on, linger);
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    //super.setKeepAlive(false);
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
}