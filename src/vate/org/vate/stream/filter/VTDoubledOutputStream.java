package org.vate.stream.filter;

import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import org.vate.console.graphical.VTGraphicalConsoleNullOutputStream;

public final class VTDoubledOutputStream extends FilterOutputStream
{
  private OutputStream first;
  private OutputStream second;

  public VTDoubledOutputStream(OutputStream first, OutputStream second)
  {
    super(new VTGraphicalConsoleNullOutputStream());
    this.first = first;
    this.second = second;
  }

  public final void write(byte[] b, int off, int len) throws IOException
  {
    first.write(b, off, len);
    second.write(b, off, len);
  }

  public final void write(byte[] b) throws IOException
  {
    first.write(b);
    second.write(b);
  }

  public final void write(int b) throws IOException
  {
    first.write(b);
    second.write(b);
  }

  public final void flush() throws IOException
  {
    first.flush();
    second.flush();
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
