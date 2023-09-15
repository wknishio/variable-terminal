package org.vash.vate.socket.managed;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedSocket extends Socket implements Closeable
{
  private VTManagedConnection managedConnection;
  private InputStream in;
  private OutputStream out;
  
  public VTManagedSocket(VTManagedConnection managedConnection, InputStream in, OutputStream out)
  {
    this.managedConnection = managedConnection;
    this.in = in;
    this.out = out;
  }
  
//  public Socket getConnectionSocket()
//  {
//    return managedConnection.getConnectionSocket();
//  }
  
  public VTManagedConnection getManagedConnection()
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
  
  public boolean isConnected()
  {
    return managedConnection.isConnected();
  }
  
  public boolean isClosed()
  {
    return managedConnection.isClosed();
  }
  
  public boolean isBound()
  {
    return managedConnection.isBound();
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
}
