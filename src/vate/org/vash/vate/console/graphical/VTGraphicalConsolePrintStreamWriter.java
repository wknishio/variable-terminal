package org.vash.vate.console.graphical;

import java.io.IOException;
import java.io.Writer;

public class VTGraphicalConsolePrintStreamWriter extends Writer
{
  public void close() throws IOException
  {
    
  }
  
  public void flush() throws IOException
  {
    VTGraphicalConsoleWriter.flush();
  }
  
  public void write(char[] cbuf, int off, int len) throws IOException
  {
    VTGraphicalConsoleWriter.write(cbuf, off, len);
  }
  
  public Writer append(char c) throws IOException
  {
    append(c);
    return this;
  }
  
  public Writer append(CharSequence csq, int start, int end) throws IOException
  {
    append(csq, start, end);
    return this;
  }
  
  public Writer append(CharSequence csq) throws IOException
  {
    append(csq);
    return this;
  }
  
  public void write(char[] cbuf) throws IOException
  {
    VTGraphicalConsoleWriter.write(cbuf, 0, cbuf.length);
  }
  
  public void write(int c) throws IOException
  {
    VTGraphicalConsole.write((char) c);
  }
  
  public void write(String str, int off, int len) throws IOException
  {
    VTGraphicalConsoleWriter.write(str.toCharArray(), off, len);
  }
  
  public void write(String str) throws IOException
  {
    VTGraphicalConsoleWriter.write(str);
  }
}