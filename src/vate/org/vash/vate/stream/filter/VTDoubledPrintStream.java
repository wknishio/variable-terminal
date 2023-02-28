package org.vash.vate.stream.filter;

import java.io.IOException;
import java.io.PrintStream;
import java.util.Locale;

public final class VTDoubledPrintStream extends PrintStream
{
  // private OutputStream first;
  private PrintStream first;
  private PrintStream another;
  
  public VTDoubledPrintStream(PrintStream first, PrintStream another)
  {
    super(first);
    this.first = first;
    this.another = another;
  }
  
  public void setAnother(PrintStream another)
  {
    this.another = another;
  }
  
  public PrintStream getAnother()
  {
    return another;
  }
  
  public final void write(byte[] b, int off, int len)
  {
    first.write(b, off, len);
    if (another != null)
    {
      another.write(b, off, len);
    }
  }
  
  public final void write(byte[] b) throws IOException
  {
    first.write(b);
    if (another != null)
    {
      another.write(b);
    }
  }
  
  public final void write(int b)
  {
    first.write(b);
    if (another != null)
    {
      another.write(b);
    }
  }
  
  public final void flush()
  {
    first.flush();
    if (another != null)
    {
      another.flush();
    }
  }
  
  public final void close()
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
  
  public PrintStream append(char c)
  {
    first.append(c);
    if (another != null)
    {
      another.append(c);
    }
    return this;
  }
  
  public PrintStream append(CharSequence csq, int start, int end)
  {
    first.append(csq, start, end);
    if (another != null)
    {
      another.append(csq, start, end);
    }
    return this;
  }
  
  public PrintStream append(CharSequence csq)
  {
    first.append(csq);
    if (another != null)
    {
      another.append(csq);
    }
    return this;
  }
  
  public boolean checkError()
  {
    return super.checkError();
  }
  
  public PrintStream format(Locale l, String format, Object... args)
  {
    first.format(l, format, args);
    if (another != null)
    {
      another.format(l, format, args);
    }
    return this;
  }
  
  public PrintStream format(String format, Object... args)
  {
    first.format(format, args);
    if (another != null)
    {
      another.format(format, args);
    }
    return this;
  }
  
  public void print(boolean b)
  {
    first.print(b);
    first.flush();
    if (another != null)
    {
      another.print(b);
      another.flush();
    }
  }
  
  public void print(char c)
  {
    first.print(c);
    first.flush();
    if (another != null)
    {
      another.print(c);
      another.flush();
    }
  }
  
  public void print(char[] s)
  {
    first.print(s);
    first.flush();
    if (another != null)
    {
      another.print(s);
      another.flush();
    }
  }
  
  public void print(double d)
  {
    first.print(d);
    first.flush();
    if (another != null)
    {
      another.print(d);
      another.flush();
    }
  }
  
  public void print(float f)
  {
    first.print(f);
    first.flush();
    if (another != null)
    {
      another.print(f);
      another.flush();
    }
  }
  
  public void print(int i)
  {
    first.print(i);
    first.flush();
    if (another != null)
    {
      another.print(i);
      another.flush();
    }
  }
  
  public void print(long l)
  {
    first.print(l);
    first.flush();
    if (another != null)
    {
      another.print(l);
      another.flush();
    }
  }
  
  public void print(Object obj)
  {
    first.print(obj);
    first.flush();
    if (another != null)
    {
      another.print(obj);
      another.flush();
    }
  }
  
  public void print(String s)
  {
    first.print(s);
    first.flush();
    if (another != null)
    {
      another.print(s);
      another.flush();
    }
  }
  
  public PrintStream printf(Locale l, String format, Object... args)
  {
    first.printf(l, format, args);
    first.flush();
    if (another != null)
    {
      another.printf(l, format, args);
      another.flush();
    }
    return this;
  }
  
  public PrintStream printf(String format, Object... args)
  {
    first.printf(format, args);
    first.flush();
    if (another != null)
    {
      another.printf(format, args);
      another.flush();
    }
    return this;
  }
  
  public void println()
  {
    first.println();
    first.flush();
    if (another != null)
    {
      another.println();
      another.flush();
    }
  }
  
  public void println(boolean x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(char x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(char[] x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(double x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(float x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(int x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(long x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(Object x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  public void println(String x)
  {
    first.println(x);
    first.flush();
    if (another != null)
    {
      another.println(x);
      another.flush();
    }
  }
  
  protected void setError()
  {
    super.setError();
  }
}
