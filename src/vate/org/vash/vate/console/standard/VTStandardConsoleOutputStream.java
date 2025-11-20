package org.vash.vate.console.standard;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VTStandardConsoleOutputStream extends OutputStream
{
  private final VTStandardConsole console;
  private OutputStream output;
  
  public VTStandardConsoleOutputStream(final VTStandardConsole console, OutputStream out)
  {
    this.console = console;
    this.output = out;
  }
  
  public VTStandardConsoleOutputStream(final VTStandardConsole console, FileDescriptor descriptor)
  {
    this.console = console;
    this.output = new FileOutputStream(descriptor);
  }
  
  public void write(int b) throws IOException
  {
    output.write(b);
    if (console != null)
    {
      console.setlastOutputLineEmpty(b == '\n');
    }
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    output.write(b, off, len);
    if (console != null && len > 0)
    {
      console.setlastOutputLineEmpty(b[off + len - 1] == '\n');
    }
  }
  
  public final void write(byte[] b) throws IOException
  {
    write(b, 0, b.length);
  }
  
  public void flush() throws IOException
  {
    output.flush();
  }
  
  public void close() throws IOException
  {
    
  }
}