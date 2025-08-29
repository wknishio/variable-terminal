package org.vash.vate.console.standard;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import org.vash.vate.console.VTMainConsole;

public class VTStandardConsoleInterruptibleReader implements Runnable
{
  private boolean requested;
  private boolean echo;
  private BufferedReader standardTerminalReader;
  private Object systemConsoleObject;
  private Method readLineMethod;
  private Method readPasswordMethod;
  private BlockingQueue<String> buffer;
  // private static final String ANSIDetectionPattern =
  // "(\\u001B)(\\[)([^R])*([R])";
  private ExecutorService executorService;
  
  public VTStandardConsoleInterruptibleReader()
  {
    // String testString = "\u001B[1;1eeR";
    // System.out.println("matches:" +
    // testString.matches(ANSIDetectionPattern));
    this.buffer = new LinkedBlockingQueue<String>();
    this.executorService = Executors.newFixedThreadPool(1, new ThreadFactory()
    {
      public Thread newThread(Runnable r)
      {
        Thread created = new Thread(null, r, r.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    standardTerminalReader = new BufferedReader(new InputStreamReader(new FileInputStream(FileDescriptor.in)));
    try
    {
      systemConsoleObject = VTMainConsole.getIOConsole();
      // VTStandardConsole.systemconsoleclass = systemConsoleObject != null;
      VTStandardConsole.systemconsolesupport = systemConsoleObject != null;
      if (systemConsoleObject != null)
      {
        readLineMethod = systemConsoleObject.getClass().getMethod("readLine");
        readPasswordMethod = systemConsoleObject.getClass().getMethod("readPassword");
        readLineMethod.setAccessible(true);
        readPasswordMethod.setAccessible(true);
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void setEcho(boolean echo)
  {
    this.echo = echo;
  }
  
  public boolean getEcho()
  {
    return echo;
  }
  
  public synchronized String read() throws InterruptedException
  {
    if (buffer.size() == 0)
    {
      if (requested == false)
      {
        requested = true;
        executorService.execute(this);
      }
    }
    String data = buffer.take();
    requested = false;
    if (data != null)
    {
      return data + "\n";
    }
    return null;
  }
  
  public void input(String data)
  {
    if (data.indexOf('\n') == -1)
    {
      buffer.offer(data);
    }
    else
    {
      int i = 0, j = 0;
      i = data.indexOf('\n');
      // i = indexOfWhitespace(string);
      buffer.offer(data.substring(0, i));
      j = i + 1;
      i = data.indexOf('\n', j);
      // i = indexOfWhitespace(string, j);
      while (i != -1)
      {
        buffer.offer(data.substring(j, i));
        j = i + 1;
        i = data.indexOf('\n', j);
        // i = indexOfWhitespace(string, j);
      }
      if (j < data.length())
      {
        buffer.offer(data.substring(j));
      }
    }
  }
  
  public void run()
  {
    try
    {
      String line = null;
      if (VTStandardConsole.systemconsolesupport)
      {
        if (!echo)
        {
          try
          {
            // line = new String(systemConsole.readPassword());
            line = new String((char[]) readPasswordMethod.invoke(systemConsoleObject));
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
          }
        }
        else
        {
          try
          {
            // line = systemConsole.readLine();
            line = (String) readLineMethod.invoke(systemConsoleObject);
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
          }
        }
      }
      
      if (line != null)
      {
        // int before = line.length();
        // line = line.replaceAll(ANSIDetectionPattern, "");
        // int after = line.length();
        // if (before > after)
        // {
        // VTStandardConsole.ansidetected = true;
        // }
        buffer.offer(line);
      }
      else
      {
        try
        {
          line = standardTerminalReader.readLine();
        }
        catch (IOException e)
        {
          // e.printStackTrace();
          line = "";
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
          line = "";
        }
        if (line == null)
        {
          line = "";
        }
        // int before = line.length();
        // line = line.replaceAll(ANSIDetectionPattern, "");
        // int after = line.length();
        // if (before > after)
        // {
        // VTStandardConsole.ansidetected = true;
        // }
        buffer.offer(line);
      }
    }
    catch (Throwable t)
    {
      
    }
    requested = false;
  }
}