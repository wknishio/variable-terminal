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
  
  public InputStream getInputStream() throws IOException
  {
    return socket.getInputStream();
  }
  
  public OutputStream getOutputStream() throws IOException
  {
    return socket.getOutputStream();
  }
  
  public void shutdownInput() throws IOException
  {
    socket.shutdownInput();
  }
  
  public void shutdownOutput() throws IOException
  {
    socket.shutdownOutput();
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
  }
  
  public void setTcpNoDelay(boolean on) throws SocketException
  {
    socket.setTcpNoDelay(on);
  }
  
  public void setSoLinger(boolean on, int linger) throws SocketException
  {
    socket.setSoLinger(on, linger);
  }
  
  public void setKeepAlive(boolean on) throws SocketException
  {
    socket.setKeepAlive(on);
  }
  
  public boolean isInputShutdown()
  {
    return socket.isInputShutdown();
  }
  
  public boolean isOutputShutdown()
  {
    return socket.isOutputShutdown();
  }
}