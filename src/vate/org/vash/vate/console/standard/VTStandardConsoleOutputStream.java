package org.vash.vate.console.standard;

import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;

public class VTStandardConsoleOutputStream extends OutputStream
{
  OutputStream output;
  
  public VTStandardConsoleOutputStream(OutputStream out)
  {
    output = out;
  }
  
  public VTStandardConsoleOutputStream(FileDescriptor descriptor)
  {
    output = new FileOutputStream(descriptor);
  }
  
  public void write(int b) throws IOException
  {
    output.write(b);
  }
  
  public final void write(byte[] b, int off, int len) throws IOException
  {
    output.write(b, off, len);
  }
  
  public final void write(byte[] b) throws IOException
  {
    output.write(b);
  }
  
  public void flush() throws IOException
  {
    output.flush();
  }
  
  public void close() throws IOException
  {
    
  }
}