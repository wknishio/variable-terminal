
package org.vash.vate.stream.pipe;

import java.io.OutputStream;
import java.io.IOException;

public final class VTPipedOutputStream extends OutputStream
{
  private VTPipedInputStream sink;
  
  public VTPipedOutputStream(VTPipedInputStream sink) throws IOException
  {
    connect(sink);
  }
  
  public VTPipedOutputStream()
  {
    
  }
  
  public synchronized final void connect(VTPipedInputStream sink) throws IOException
  {
    if (this.sink == sink)
    {
      return;
    }
    if (this.sink != null)
    {
      throw new IOException("OutputStreamPipe already connected");
    }
    this.sink = sink;
  }
  
  public final void write(int b) throws IOException
  {
    sink.put(b);
  }
  
  public final void write(byte b[], int off, int len) throws IOException
  {
    sink.put(b, off, len);
  }
  
  public final void flush() throws IOException
  {
//    sink.flush();
  }
  
  public final void close() throws IOException
  {
    sink.eof();
  }
}