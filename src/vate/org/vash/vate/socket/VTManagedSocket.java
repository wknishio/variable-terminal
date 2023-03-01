package org.vash.vate.socket;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

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
  
  public InputStream getInputStream(int number)
  {
    return managedConnection.getInputStream(number);
  }
  
  public OutputStream getOutputStream(int number)
  {
    return managedConnection.getOutputStream(number);
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
}
