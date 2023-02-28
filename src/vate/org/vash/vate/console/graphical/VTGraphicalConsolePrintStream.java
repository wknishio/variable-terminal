package org.vash.vate.console.graphical;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.Locale;

public class VTGraphicalConsolePrintStream extends PrintStream
{
  private PrintWriter outWriter;
  
  public VTGraphicalConsolePrintStream()
  {
    super(new OutputStream()
    {
      @Override
      public void write(int b) throws IOException
      {
        
      }
    }, false);
    this.outWriter = new PrintWriter(new VTGraphicalConsolePrintStreamWriter(), false);
  }
  
  public PrintStream append(char c)
  {
    outWriter.append(c);
    return this;
  }
  
  public PrintStream append(CharSequence csq, int start, int end)
  {
    outWriter.append(csq, start, end);
    return this;
  }
  
  public PrintStream append(CharSequence csq)
  {
    outWriter.append(csq);
    return this;
  }
  
  public boolean checkError()
  {
    return super.checkError();
  }
  
  protected void clearError()
  {
    try
    {
      // super.clearError();
      super.getClass().getMethod("clearError").invoke(this);
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close()
  {
    // outWriter.close();
  }
  
  public void flush()
  {
    outWriter.flush();
  }
  
  public PrintStream format(Locale l, String format, Object... args)
  {
    outWriter.format(l, format, args);
    return this;
  }
  
  public PrintStream format(String format, Object... args)
  {
    outWriter.format(format, args);
    return this;
  }
  
  public void print(boolean b)
  {
    outWriter.print(b);
    outWriter.flush();
  }
  
  public void print(char c)
  {
    outWriter.print(c);
    outWriter.flush();
  }
  
  public void print(char[] s)
  {
    outWriter.print(s);
    outWriter.flush();
  }
  
  public void print(double d)
  {
    outWriter.print(d);
    outWriter.flush();
  }
  
  public void print(float f)
  {
    outWriter.print(f);
    outWriter.flush();
  }
  
  public void print(int i)
  {
    outWriter.print(i);
    outWriter.flush();
  }
  
  public void print(long l)
  {
    outWriter.print(l);
    outWriter.flush();
  }
  
  public void print(Object obj)
  {
    outWriter.print(obj);
    outWriter.flush();
  }
  
  public void print(String s)
  {
    outWriter.print(s);
    outWriter.flush();
  }
  
  public PrintStream printf(Locale l, String format, Object... args)
  {
    outWriter.printf(l, format, args);
    outWriter.flush();
    return this;
  }
  
  public PrintStream printf(String format, Object... args)
  {
    outWriter.printf(format, args);
    outWriter.flush();
    return this;
  }
  
  public void println()
  {
    outWriter.println();
    outWriter.flush();
  }
  
  public void println(boolean x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(char x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(char[] x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(double x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(float x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(int x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(long x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(Object x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  public void println(String x)
  {
    outWriter.println(x);
    outWriter.flush();
  }
  
  protected void setError()
  {
    super.setError();
  }
  
  public void write(byte[] buf, int off, int len)
  {
    outWriter.write(new String(buf, off, len));
  }
  
  public void write(int b)
  {
    outWriter.write(b);
  }
  
  public void write(byte[] b) throws IOException
  {
    outWriter.write(new String(b, 0, b.length));
  }
}