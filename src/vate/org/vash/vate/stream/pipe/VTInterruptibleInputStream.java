package org.vash.vate.stream.pipe;

import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.Executor;

import org.vash.vate.VT;

public final class VTInterruptibleInputStream extends InputStream
{
  private final VTInterruptibleStreamRedirector redirector;
  private final InputStream source;
  private final VTPipedInputStream in;
  private final VTPipedOutputStream out;
  
  public VTInterruptibleInputStream(final InputStream source, final Executor executor)
  {
    this.in = new VTPipedInputStream(VT.VT_REDUCED_BUFFER_SIZE_BYTES);
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
  
  public VTInterruptibleInputStream(final InputStream source)
  {
    this.in = new VTPipedInputStream(VT.VT_REDUCED_BUFFER_SIZE_BYTES);
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
  
  public int read(final byte[] b) throws IOException
  {
    return in.read(b);
  }
  
  public int read(final byte[] b, final int off, final int len) throws IOException
  {
    return in.read(b, off, len);
  }
}