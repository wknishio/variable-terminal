package org.vash.vate.stream.filter;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class VTDoubledInputStream extends FilterInputStream
{
  private InputStream another;
  
  protected VTDoubledInputStream(InputStream first, InputStream second)
  {
    super(first);
    this.another = second;
  }
  
  public final int available() throws IOException
  {
    int available = in.available();
    if (available <= 0)
    {
      available = another.available();
    }
    return available;
  }
  
  public final void close() throws IOException
  {
//    try
//    {
//      flush();
//    }
//    catch (Throwable t)
//    {
//      
//    }
    // nothing
  }
}
