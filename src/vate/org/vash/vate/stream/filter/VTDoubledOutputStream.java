package org.vash.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public final class VTDoubledOutputStream extends FilterOutputStream
{
  // private OutputStream first;
  private OutputStream another;
  private boolean closeOnError;
  
  public VTDoubledOutputStream(OutputStream first, OutputStream second, boolean closeOnError)
  {
    super(first);
    this.another = second;
    this.closeOnError = closeOnError;
  }
  
  public void setAnother(OutputStream another)
  {
    if (this.another != null && closeOnError)
    {
      try
      {
        this.another.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    this.another = another;
  }
  
  public OutputStream getAnother()
  {
    return another;
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    out.write(b, off, len);
    if (another != null)
    {
      try
      {
        another.write(b, off, len);
      }
      catch (Throwable t)
      {
        if (closeOnError)
        {
          try
          {
            another.close();
          }
          catch (Throwable e)
          {
            
          }
        }
        another = null;
      }
    }
  }
  
  public final void write(byte[] b) throws IOException
  {
    out.write(b);
    if (another != null)
    {
      try
      {
        another.write(b);
      }
      catch (Throwable t)
      {
        // another = null;
        if (closeOnError)
        {
          try
          {
            another.close();
          }
          catch (Throwable e)
          {
            
          }
        }
        another = null;
      }
    }
  }
  
  public final void write(int b) throws IOException
  {
    out.write(b);
    if (another != null)
    {
      try
      {
        another.write(b);
      }
      catch (Throwable t)
      {
        // another = null;
        if (closeOnError)
        {
          try
          {
            another.close();
          }
          catch (Throwable e)
          {
            
          }
        }
        another = null;
      }
    }
  }
  
  public final void flush() throws IOException
  {
    out.flush();
    if (another != null)
    {
      try
      {
        another.flush();
      }
      catch (Throwable t)
      {
        // another = null;
        if (closeOnError)
        {
          try
          {
            another.close();
          }
          catch (Throwable e)
          {
            
          }
        }
        another = null;
      }
    }
  }
  
  public final void close() throws IOException
  {
//		try
//		{
//			flush();
//		}
//		catch (Throwable t)
//		{
//			
//		}
    // nothing
  }
}
