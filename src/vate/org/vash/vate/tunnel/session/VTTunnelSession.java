package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.connection.VTTunnelConnection;

public class VTTunnelSession implements Closeable
{
  private VTTunnelConnection connection;
  private Socket socket;
  private InputStream socketInputStream;
  private OutputStream socketOutputStream;
  private VTLinkableDynamicMultiplexedInputStream tunnelInputStream;
  private VTLinkableDynamicMultiplexedOutputStream tunnelOutputStream;
  private final boolean originator;
  private Object waiter = new Object();
  private Boolean result = null;
  private String remoteHost;
  private int remotePort;
  private volatile boolean closed;
  
//  public VTTunnelSession(VTTunnelConnection connection, Socket socket, InputStream socketInputStream, OutputStream socketOutputStream, boolean originator)
//  {
//    this.connection = connection;
//    this.socket = new VTTunnelCloseableSocket(socket);
//    this.socketInputStream = socketInputStream;
//    this.socketOutputStream = socketOutputStream;
//    this.originator = originator;
//  }
  
  public VTTunnelSession(VTTunnelConnection connection, boolean originator)
  {
    this.connection = connection;
    this.originator = originator;
  }
  
//  public VTTunnelSession(VTTunnelConnection connection, VTLinkableDynamicMultiplexedInputStream inputStream)
//  {
//    this.connection = connection;
//    this.inputStream = inputStream;
//    this.originator = false;
//  }
  
  public boolean isOriginator()
  {
    return originator;
  }
  
  
  /* public boolean isReady() { return ready; } */
  
  /* public void setReady(boolean ready) { this.ready = ready; } */
  
  /*
   * public void linger() { if (socket != null) { try { socket.setSoLinger(true,
   * 0); } catch (Throwable e) { } } }
   */
  
  public void close() throws IOException
  {
    if (closed)
    {
      return;
    }
    if (result == null)
    {
      setResult(false);
    }
    closed = true;
    try
    {
      if (socket != null)
      {
        socket.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      if (tunnelOutputStream != null)
      {
        tunnelOutputStream.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      if (tunnelInputStream != null)
      {
        //inputStream.removePropagated(this);
        tunnelInputStream.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    connection.releaseInputStream(tunnelInputStream);
    connection.releaseOutputStream(tunnelOutputStream);
    tunnelInputStream = null;
    tunnelOutputStream = null;
    socket = null;
  }
  
  public Socket getSocket()
  {
    return this.socket;
  }
  
  public void setSocket(Socket socket)
  {
    this.socket = socket; 
  }
  
  public VTLinkableDynamicMultiplexedInputStream getTunnelInputStream()
  {
    return tunnelInputStream;
  }
  
  public void setTunnelInputStream(VTLinkableDynamicMultiplexedInputStream tunnelInputStream)
  {
    this.tunnelInputStream = tunnelInputStream;
    //this.inputStream.addPropagated(this);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getTunnelOutputStream()
  {
    return tunnelOutputStream;
  }
  
  public void setTunnelOutputStream(VTLinkableDynamicMultiplexedOutputStream outputStream)
  {
    this.tunnelOutputStream = outputStream;
  }
  
  public InputStream getSocketInputStream()
  {
    return socketInputStream;
  }
  
  public OutputStream getSocketOutputStream()
  {
    return socketOutputStream;
  }
  
  public void setSocketInputStream(InputStream socketInputStream)
  {
    this.socketInputStream = socketInputStream;
  }
  
  public void setSocketOutputStream(OutputStream socketOutputStream)
  {
    this.socketOutputStream = socketOutputStream;
  }
  
  public boolean waitResult() throws InterruptedException
  {
    while (result == null)
    {
      synchronized (waiter)
      {
        waiter.wait();
      }
    }
    return result;
  }
  
  public void setResult(boolean result)
  {
    this.result = result;
    synchronized (waiter)
    {
      waiter.notifyAll();
    }
  }
  
  public String getRemoteHost()
  {
    return remoteHost;
  }
  
  public void setRemoteHost(String host)
  {
    remoteHost = host;
  }
  
  public int getRemotePort()
  {
    return remotePort;
  }
  
  public void setRemotePort(int port)
  {
    remotePort = port;
  }
}