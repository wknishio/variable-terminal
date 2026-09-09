package org.vash.vate.stream.base;

import java.io.DataOutputStream;
import java.io.FilterOutputStream;
import java.io.IOException;
import java.io.OutputStream;

import org.vash.vate.sheepy.util.text.Base85.Z85Encoder;

public class VTZ85OutputStream extends FilterOutputStream
{
  private DataOutputStream output;
  private Z85Encoder encoder = new Z85Encoder();
  
  public VTZ85OutputStream(OutputStream out)
  {
    super(out);
    this.output = new DataOutputStream(out);
  }
  
  public void write(byte[] buf, int off, int len) throws IOException
  {
    output.write(encoder.encode(buf, off, len));
  }
  
  public void write(byte[] buf) throws IOException
  {
    output.write(encoder.encode(buf));
  }
}