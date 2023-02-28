package org.vash.vate.console.standard;

import java.io.BufferedReader;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Method;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;
import org.vash.vate.console.VTConsole;

public class VTStandardConsoleInterruptibleReader implements Runnable
{
  // private volatile boolean stopped;
  private volatile boolean requested;
  private volatile boolean echo;
  // private volatile boolean discard;
  private BufferedReader standardTerminalReader;
  private volatile Object systemConsoleObject;
  private volatile Method readLineMethod;
  private volatile Method readPasswordMethod;
  private volatile BlockingQueue<String> buffer;
  // private static final String ANSIDetectionPattern =
  // "(\\u001B)(\\[)([^R])*([R])";
  private Executor executor;
  
  public VTStandardConsoleInterruptibleReader()
  {
    // String testString = "\u001B[1;1eeR";
    // System.out.println("matches:" +
    // testString.matches(ANSIDetectionPattern));
    this.buffer = new LinkedBlockingQueue<String>();
    this.executor = Executors.newFixedThreadPool(1, new ThreadFactory()
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
      systemConsoleObject = VTConsole.getIOConsole();
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
  
  public synchronized String read() throws InterruptedException
  {
    if (buffer.size() == 0)
    {
      if (requested == false)
      {
        requested = true;
        executor.execute(this);
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