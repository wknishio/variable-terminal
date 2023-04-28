package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.net.SocketException;

public class VTTunnelCloseableSocket extends Socket implements Closeable
{
  private final Socket socket;
  
  public VTTunnelCloseableSocket(Socket socket)
  {
    this.socket = socket;
  }
  
  //public void setCloseable(Closeable closeable)
  //{
    //this.closeable = closeable;
  //}
  
  public InputStream getInputStream() throws IOException
  {
    return socket.getInputStream();
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    return socket.getOutputStream();
  }
  
  public void shutdownOutput() throws IOException
  {
//    if (out != null)
//    {
//      try
//      {
//        out.close();
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
  }
  
  public void shutdownInput() throws IOException
  {
//    if (in != null)
//    {
//      try
//      {
//        in.close();
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
//    if (pipe != null)
//    {
//      try
//      {
//        pipe.close();
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
  }
  
  public boolean isBound()
  {
    return socket.isBound();
  }
  
  public boolean isConnected()
  {
    return socket.isConnected();
  }
  
  public boolean isClosed()
  {
    return socket.isClosed();
  }
  
  public void close() throws IOException
  {
    socket.close();
  }
  
  public void setSoTimeout(int timeout) throws SocketException
  {
    socket.setSoTimeout(timeout);
    //super.setSoTimeout(timeout);
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    socket.setTcpNoDelay(on);
    //super.setTcpNoDelay(true);
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    socket.setSoLinger(on, linger);
    //super.setSoLinger(on, linger);
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    socket.setKeepAlive(on);
    //super.setKeepAlive(false);
  }
}