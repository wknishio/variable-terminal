package org.vash.vate.nativeutils;

import com.sun.jna.Native;

public class VTMinimalNativeUtils implements VTNativeUtilsImplementation
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
  
  public void unbuffered()
  {
    
  }
  
  public void noecho()
  {
    
  }
  
  public void normal()
  {
    
  }
  
  public boolean checkANSI()
  {
    if (isatty(0) != 0 && isatty(1) != 0)
    {
      return true;
    }
    return false;
  }
  
  /* public int true_putenv(String env) { return cLibray.putenv(env); } */
}