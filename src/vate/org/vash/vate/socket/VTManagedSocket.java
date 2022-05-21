package org.vash.vate.socket;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class VTManagedSocket extends Socket implements Closeable
{
  private VTManagedCloseableConnection connection;
  private InputStream in;
  private OutputStream out;
  
  public VTManagedSocket(VTManagedCloseableConnection connection, InputStream in, OutputStream out)
  {
    this.connection = connection;
    this.in = in;
    this.out = out;
  }
  
  public Socket getConnectionSocket()
  {
    return connection.getConnectionSocket();
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
    return connection.getInputStream(number);
  }
  
  public OutputStream getOutputStream(int number)
  {
    return connection.getOutputStream(number);
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
  
  public boolean isConnected()
  {
    return connection.isConnected();
  }
}
