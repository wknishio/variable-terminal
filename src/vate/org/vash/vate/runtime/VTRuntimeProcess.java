package org.vash.vate.runtime;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Field;
import java.util.concurrent.ExecutorService;
import org.vash.vate.reflection.VTReflectionUtils;

import com.sun.jna.Pointer;
import com.sun.jna.platform.win32.Kernel32;
import com.sun.jna.platform.win32.WinNT;

public class VTRuntimeProcess
{
  private String command;
  
  private ProcessBuilder builder;
  private Process process;
  private InputStream in;
  private InputStream err;
  private OutputStream out;
  
  // private VTRuntimeProcessKill killer = new VTRuntimeProcessKill();
  private VTRuntimeProcessOutputConsumer outputConsumer;
  // private VTRuntimeProcessOutputConsumer errorConsumer;
  private VTRuntimeProcessExitListener exitListener;
  private VTRuntimeProcessTimeoutKill timeoutKill;
  
  private ExecutorService threads;
  private BufferedWriter writer;
  private volatile boolean verbose;
  private volatile boolean restart;
  // private volatile boolean killed;
  private volatile long timeout;
  // private volatile long pid;
  
  public VTRuntimeProcess(String command, ProcessBuilder builder, ExecutorService threads, BufferedWriter writer, boolean verbose, boolean restart, long timeout)
  {
    this.command = command;
    this.builder = builder;
    this.threads = threads;
    this.writer = writer;
    this.verbose = verbose;
    this.restart = restart;
    this.timeout = timeout;
  }
  
  public ProcessBuilder getBuilder()
  {
    return builder;
  }
  
//  private class VTRuntimeProcessKill implements Runnable
//  {
//    public void run()
//    {
//      kill();
//    }
//    
//  }
  
  public long getPID()
  {
    return getProcessID(process);
  }
  
  public void start() throws Throwable
  {
    this.process = builder.start();
    this.in = process.getInputStream();
    this.err = process.getErrorStream();
    this.out = process.getOutputStream();
    
    this.exitListener = new VTRuntimeProcessExitListener(this);
    threads.execute(exitListener);
    
    if (writer != null)
    {
      this.outputConsumer = new VTRuntimeProcessOutputConsumer(in, writer, verbose);
      // this.errorConsumer = new VTRuntimeProcessOutputConsumer(err, writer,
      // verbose);
      threads.execute(outputConsumer);
      // threads.execute(errorConsumer);
    }
    
    // threads.execute(errorConsumer);
    
    if (timeout > 0)
    {
      this.timeoutKill = new VTRuntimeProcessTimeoutKill(this, timeout);
      threads.execute(timeoutKill);
    }
  }
  
  /*
   * public boolean isRunning() { try { process.exitValue(); return false; }
   * catch (IllegalThreadStateException e) { return true; } }
   */
  
  public Integer getExitValue()
  {
    try
    {
      return process.exitValue();
    }
    catch (Throwable e)
    {
      return null;
    }
  }
  
  public String getCommand()
  {
    return command;
  }
  
  public int waitFor() throws InterruptedException
  {
    return process.waitFor();
  }
  
  public InputStream getIn()
  {
    return in;
  }
  
  public InputStream getErr()
  {
    return err;
  }
  
  public OutputStream getOut()
  {
    return out;
  }
  
  public long getTimeout()
  {
    return timeout;
  }
  
  public void setRestart(boolean restart)
  {
    this.restart = restart;
  }
  
  public boolean isRestart()
  {
    return this.restart;
  }
  
  public void setVerbose(boolean verbose)
  {
    this.verbose = verbose;
  }
  
  public boolean isVerbose()
  {
    return this.verbose;
  }
  
  public boolean isAlive()
  {
    return isAlive(process);
  }
  
  public boolean restart()
  {
    try
    {
      stop();
      start();
      return true;
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public void stop()
  {
    // threads.execute(killer);
    kill();
  }
  
  private void kill()
  {
    if (process != null && isAlive(process))
    {
      killProcess(process, 0);
    }
    
    if (outputConsumer != null)
    {
      try
      {
        outputConsumer.stop();
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (timeoutKill != null)
    {
      try
      {
        timeoutKill.stop();
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (in != null)
    {
      try
      {
        in.close();
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (out != null)
    {
      try
      {
        out.close();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public void destroy()
  {
    this.restart = false;
    stop();
  }
  
  public void finalize()
  {
    destroy();
  }
  
  private static void forceKillProcessID(long pid) throws Throwable
  {
    if (pid < 0)
    {
      return;
    }
    Runtime rt = Runtime.getRuntime();
    if (VTReflectionUtils.detectWindows())
    {
      rt.exec("taskkill /f /PID " + pid);
    }
    else
    {
      rt.exec("kill -9 " + pid);
    }
  }
  
  private static long getProcessID(Process p)
  {
    long result = -1;
    try
    {
      // for windows
      if (p.getClass().getName().equals("java.lang.Win32Process") || p.getClass().getName().equals("java.lang.ProcessImpl"))
      {
        Field f = p.getClass().getDeclaredField("handle");
        f.setAccessible(true);
        long handl = f.getLong(p);
        Kernel32 kernel = Kernel32.INSTANCE;
        WinNT.HANDLE hand = new WinNT.HANDLE();
        hand.setPointer(Pointer.createConstant(handl));
        result = kernel.GetProcessId(hand);
        // f.setAccessible(false);
      }
      // for unix based operating systems
      else if (p.getClass().getName().equals("java.lang.UNIXProcess"))
      {
        Field f = p.getClass().getDeclaredField("pid");
        f.setAccessible(true);
        result = f.getLong(p);
        // f.setAccessible(false);
      }
    }
    catch (Throwable ex)
    {
      result = -1;
    }
    return result;
  }
  
  private static boolean isAlive(Process process)
  {
    if (process == null)
    {
      return false;
    }
    boolean alive = true;
    try
    {
      process.exitValue();
      alive = false;
    }
    catch (Throwable t)
    {
      
    }
    return alive;
  }
  
  private static void killProcess(Process process, long delay)
  {
    long pid = getProcessID(process);
    // int seconds = 0;
    // int limit = 30;
    boolean killed = false;
    // int returncode = 0;
    if (process != null)
    {
      if (isAlive(process))
      {
        try
        {
          process.destroy();
        }
        catch (Throwable t)
        {
          
        }
        if (delay > 0)
        {
          try
          {
            Thread.sleep(delay);
          }
          catch (Throwable e)
          {
            
          }
        }
        killed = isAlive(process);
      }
      if (!killed)
      {
        try
        {
          forceKillProcessID(pid);
        }
        catch (Throwable e)
        {
          
        }
      }
    }
  }
}