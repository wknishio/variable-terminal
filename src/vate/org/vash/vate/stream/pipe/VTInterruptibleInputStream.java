package org.vash.vate.stream.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

public final class VTInterruptibleInputStream extends InputStream
{
  private VTInterruptibleStreamRedirector redirector;
  private InputStream source;
  private VTPipedInputStream in;
  private VTPipedOutputStream out;
  
  public VTInterruptibleInputStream(InputStream source, Executor executor)
  {
    this.in = new VTPipedInputStream();
    this.out = new VTPipedOutputStream();
    try
    {
      out.connect(in);
    }
    catch (Throwable e)
    {
      
    }
    this.source = source;
    this.redirector = new VTInterruptibleStreamRedirector(source, out);
    executor.execute(redirector);
  }
  
  public VTInterruptibleInputStream(InputStream source)
  {
    this.in = new VTPipedInputStream();
    this.out = new VTPipedOutputStream();
    try
    {
      out.connect(in);
    }
    catch (Throwable e)
    {
      
    }
    this.source = source;
    this.redirector = new VTInterruptibleStreamRedirector(source, out);
    Thread redirectorThread = new Thread(redirector);
    redirectorThread.setName(redirector.getClass().getSimpleName());
    redirectorThread.setDaemon(true);
    redirectorThread.start();
  }
  
  public void close()
  {
    redirector.stop();
    try
    {
      source.close();
    }
    catch (Throwable e)
    {
      
    }
    try
    {
      out.close();
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public int available() throws IOException
  {
    return in.available();
  }
  
  public int read() throws IOException
  {
    return in.read();
  }
  
  public int read(byte[] b) throws IOException
  {
    return in.read(b);
  }
  
  public int read(byte[] b, int off, int len) throws IOException
  {
    return in.read(b, off, len);
  }
}