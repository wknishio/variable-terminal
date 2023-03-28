package org.vash.vate.nativeutils.sunos;

import org.vash.vate.nativeutils.VTNativeUtilsImplementation;

import com.sun.jna.Native;

public class VTSunOSNativeUtils implements VTNativeUtilsImplementation
{
  // Device opening flags
  private static int O_RDONLY = 0;
  private static int O_WRONLY = 1;
  private static int O_NONBLOCK = 0x0080;
  // IOCTLs
  private static int PIT_HZ = 1193182;
  private static int KIOCMKTONE = ('k' << 8) | 27;
  // private static int KDMKTONE = KIOCMKTONE ;
  private static int CDROMEJECT = (0x04 << 8) | 159;
  private static int CDROMCLOSETRAY = (0x04 << 8) | 172;
  
  private VTSunOSCLibrary sunosCLibrary;
  
  public VTSunOSNativeUtils()
  {
    sunosCLibrary = (VTSunOSCLibrary) Native.load("c", VTSunOSCLibrary.class);
  }
  
  public int system(String command)
  {
    return sunosCLibrary.system(command);
  }
  
  public int getchar()
  {
    return sunosCLibrary.getchar();
  }
  
  public void printf(String format, Object... args)
  {
    sunosCLibrary.printf(format, args);
  }
  
  /*
   * public boolean beep(int freq, int dur) { return beep(freq, dur, true); }
   */
  
  public boolean beep(int freq, int dur, boolean block)
  {
    boolean returnFlag = false;
    int fd = sunosCLibrary.open("/dev/console", O_WRONLY);
    if (fd == -1)
    {
      returnFlag = false;
    }
    else
    {
      returnFlag = sunosCLibrary.ioctl(fd, KIOCMKTONE, ((int) ((dur << 16) | PIT_HZ / freq))) == 0;
      if (returnFlag && block)
      {
        try
        {
          Thread.sleep(dur);
        }
        catch (InterruptedException e)
        {
          returnFlag = false;
        }
      }
      sunosCLibrary.close(fd);
    }
    return returnFlag;
  }
  
  public boolean openDiscDrive()
  {
    // int cdrom = sunosCLibrary.open("/dev/rdsk/c0t1d0s0", O_RDONLY |
    // O_NONBLOCK);
    int cdrom = sunosCLibrary.open("/dev/sr0", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (sunosCLibrary.ioctl(cdrom, CDROMEJECT) == -1)
    {
      sunosCLibrary.close(cdrom);
      return false;
    }
    else
    {
      sunosCLibrary.close(cdrom);
      return true;
    }
  }
  
  public boolean closeDiscDrive()
  {
    // int cdrom = sunosCLibrary.open("/dev/rdsk/c0t1d0s0", O_RDONLY |
    // O_NONBLOCK);
    int cdrom = sunosCLibrary.open("/dev/sr0", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (sunosCLibrary.ioctl(cdrom, CDROMCLOSETRAY) == -1)
    {
      sunosCLibrary.close(cdrom);
      return false;
    }
    else
    {
      sunosCLibrary.close(cdrom);
      return true;
    }
  }
  
  public void exit(int status)
  {
    sunosCLibrary.exit(status);
  }
  
  public void abort()
  {
    sunosCLibrary.abort();
  }
  
  public int raise(int signal)
  {
    return sunosCLibrary.raise(signal);
  }
  
  public int rand()
  {
    return sunosCLibrary.rand();
  }
  
  public void srand(int seed)
  {
    sunosCLibrary.srand(seed);
  }
  
  public String getenv(String env)
  {
    return sunosCLibrary.getenv(env);
  }
  
  public int putenv(String env)
  {
    return sunosCLibrary.putenv(env);
  }
  
  public int getpid()
  {
    return sunosCLibrary.getpid();
  }
  
  public int isatty(int fd)
  {
    return sunosCLibrary.isatty(fd);
  }
  
  public boolean detachConsole()
  {
    try
    {
      // sunosCLibrary.system("disown -h");
      try
      {
        // sunosCLibrary.system("exit");
      }
      catch (Throwable t)
      {
        
      }
    }
    catch (Throwable t)
    {
      
    }
    return true;
  }
  
  public boolean attachConsole()
  {
    return false;
  }
  
  public boolean hideConsole()
  {
    try
    {
      // sunosCLibrary.system("disown -h");
      try
      {
        // sunosCLibrary.system("exit");
      }
      catch (Throwable t)
      {
        
      }
    }
    catch (Throwable t)
    {
      
    }
    return true;
  }
  
  public int getch()
  {
    return getchar();
  }
  
  public void unbuffered()
  {
    try
    {
      Runtime.getRuntime().exec(new String[]
      { "/bin/sh", "-c", "stty -icanon min 1 < /dev/tty" });
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void noecho()
  {
    try
    {
      Runtime.getRuntime().exec(new String[]
      { "/bin/sh", "-c", "stty -echo < /dev/tty" });
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void normal()
  {
    try
    {
      Runtime.getRuntime().exec(new String[]
      { "/bin/sh", "-c", "stty icanon echo < /dev/tty" });
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public boolean checkANSI()
  {
    if (isatty(0) != 0 && isatty(1) != 0)
    {
      return true;
    }
    return false;
  }
}