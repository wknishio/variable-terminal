package org.vash.vate.console.graphical;

public class VTGraphicalConsoleWriter
{
  public VTGraphicalConsoleWriter()
  {
    
  }
  
  public synchronized static void close()
  {
    VTGraphicalConsole.close();
  }
  
  public synchronized static void flush()
  {
    VTGraphicalConsole.flushBuffered(true);
  }
  
  public synchronized static void write(String str)
  {
    // VTGraphicalConsole.write(str);
    for (int i = 0; i < str.length(); i++)
    {
      VTGraphicalConsole.write(str.charAt(i));
    }
  }
  
  public synchronized static void write(char[] cbuf, int off, int len)
  {
    // VTGraphicalConsole.write(String.valueOf(cbuf, off, len));
    for (int i = 0; i < len; i++)
    {
      VTGraphicalConsole.write(cbuf[off + i]);
    }
  }
  
  /*
   * public synchronized static void clear() { VTGraphicalConsole.staticClear();
   * }
   */
}