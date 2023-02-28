package org.vash.vate.console.standard;

import java.io.IOException;
import java.io.Reader;

import org.vash.vate.nativeutils.VTNativeUtils;

public class VTStandardConsoleNativeReader extends Reader
{
  public int read(char[] cbuf, int off, int len) throws IOException
  {
    char data = (char) VTNativeUtils.getchar();
    cbuf[off] = data;
    return 1;
  }
  
  public void close() throws IOException
  {
    
  }
}
