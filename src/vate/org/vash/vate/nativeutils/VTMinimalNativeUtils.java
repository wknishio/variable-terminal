package org.vash.vate.nativeutils;

import com.sun.jna.Native;

public class VTMinimalNativeUtils implements VTNativeUtils
{
  private VTCLibrary cLibrary = (VTCLibrary) Native.load("c", VTCLibrary.class);
  
  public int system(String command)
  {
    return cLibrary.system(command);
  }
  
  public int getchar()
  {
    return cLibrary.getchar();
  }
  
  public void printf(String format, Object... args)
  {
    cLibrary.printf(format, args);
  }
  
  /* public boolean beep(int freq, int dur) { return false; } */
  
  public boolean beep(int freq, int dur, boolean block)
  {
    return false;
  }
  
  public boolean openDiscDrive()
  {
    return false;
  }
  
  public boolean closeDiscDrive()
  {
    return false;
  }
  
  public void exit(int status)
  {
    cLibrary.exit(status);
  }
  
  public void abort()
  {
    cLibrary.abort();
  }
  
  public int raise(int signal)
  {
    return cLibrary.raise(signal);
  }
  
  public int rand()
  {
    return cLibrary.rand();
  }
  
  public void srand(int seed)
  {
    cLibrary.srand(seed);
  }
  
  public int getpid()
  {
    return 0;
  }
  
  public int isatty(int fd)
  {
    return cLibrary.isatty(fd);
  }
  
  public boolean detachConsole()
  {
    return false;
  }
  
  public boolean attachConsole()
  {
    return false;
  }
  
  public boolean hideConsole()
  {
    return false;
  }
  
  public int getch()
  {
    return getchar();
  }
  
  public boolean checkANSI()
  {
    if (isatty(0) != 0 && isatty(1) != 0)
    {
      return true;
    }
    return false;
  }
  
  public void echo(boolean enabled)
  {
    try
    {
      if (!enabled)
      {
        Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "stty -echo < /dev/tty"}).waitFor();
      }
      else
      {
        Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "stty echo < /dev/tty"}).waitFor();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void icanon(boolean enabled)
  {
    try
    {
      if (!enabled)
      {
        Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "stty -icanon < /dev/tty"}).waitFor();
      }
      else
      {
        Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "stty icanon < /dev/tty"}).waitFor();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void raw()
  {
    try
    {
      Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "stty raw < /dev/tty"}).waitFor();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void sane()
  {
    try
    {
      Runtime.getRuntime().exec(new String[] {"/bin/sh", "-c", "stty sane < /dev/tty"}).waitFor();
    }
    catch (Throwable t)
    {
      
    }
  }
}