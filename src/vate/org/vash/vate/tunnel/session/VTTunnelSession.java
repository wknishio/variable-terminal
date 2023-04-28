package org.vash.vate.tunnel.session;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.connection.VTTunnelConnection;

public class VTTunnelSession implements Closeable
{
  private VTTunnelConnection connection;
  private VTTunnelCloseableSocket socket;
  private VTLinkableDynamicMultiplexedInputStream inputStream;
  private VTLinkableDynamicMultiplexedOutputStream outputStream;
  private final boolean originator;
  private int outputNumber;
  private int inputNumber;
  private volatile boolean closed;
  
  public VTTunnelSession(VTTunnelConnection connection, Socket socket, boolean originator) throws IOException
  {
    this.connection = connection;
    this.socket = new VTTunnelCloseableSocket(socket);
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
  
  public void setOutputNumber(int outputNumber)
  {
    this.outputNumber = outputNumber;
  }
  
  public void setInputNumber(int inputNumber)
  {
    this.inputNumber = inputNumber;
  }
  
  public int getOutputNumber()
  {
    return outputNumber;
  }
  
  public int getInputNumber()
  {
    return inputNumber;
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
      if (outputStream != null)
      {
        outputStream.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    try
    {
      if (inputStream != null)
      {
        //inputStream.removePropagated(this);
        inputStream.close();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
    }
    connection.releaseInputStream(inputStream);
    connection.releaseOutputStream(outputStream);
    outputStream = null;
    inputStream = null;
    socket = null;
  }
  
  public VTTunnelCloseableSocket getSocket()
  {
    return this.socket;
  }
  
  public VTLinkableDynamicMultiplexedInputStream getTunnelInputStream()
  {
    return inputStream;
  }
  
  public void setTunnelInputStream(VTLinkableDynamicMultiplexedInputStream inputStream)
  {
    this.inputStream = inputStream;
    //this.inputStream.addPropagated(this);
  }
  
  public VTLinkableDynamicMultiplexedOutputStream getTunnelOutputStream()
  {
    return outputStream;
  }
  
  public void setTunnelOutputStream(VTLinkableDynamicMultiplexedOutputStream outputStream)
  {
    this.outputStream = outputStream;
  }
}