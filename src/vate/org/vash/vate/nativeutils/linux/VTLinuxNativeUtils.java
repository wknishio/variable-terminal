package org.vash.vate.nativeutils.linux;

import org.vash.vate.nativeutils.VTNativeUtilsImplementation;

import com.sun.jna.Native;

public class VTLinuxNativeUtils implements VTNativeUtilsImplementation
{
  // Device opening flags
  private static int O_RDONLY = 0;
  private static int O_WRONLY = 1;
  private static int O_NONBLOCK = 0x0800;
  private static int CLOCK_TICK_RATE = 1193180;
  // IOCTLs
  // private static int KIOCSOUND = 0x4B2F;
  private static int KDMKTONE = 0x4B30;
  private static int CDROMEJECT = 0x5309;
  private static int CDROMCLOSETRAY = 0x5319;
  
  private VTLinuxCLibrary linuxCLibrary;
  
  public VTLinuxNativeUtils()
  {
    linuxCLibrary = (VTLinuxCLibrary) Native.load("c", VTLinuxCLibrary.class);
  }
  
  public int system(String command)
  {
    return linuxCLibrary.system(command);
  }
  
  public int getchar()
  {
    return linuxCLibrary.getchar();
  }
  
  public void printf(String format, Object... args)
  {
    linuxCLibrary.printf(format, args);
  }
  
  /*
   * public boolean beep(int freq, int dur) { return beep(freq, dur, true); }
   */
  
  public boolean beep(int freq, int dur, boolean block)
  {
    boolean returnFlag = false;
    int fd = linuxCLibrary.open("/dev/console", O_WRONLY);
    if (fd == -1)
    {
      returnFlag = false;
    }
    else
    {
      returnFlag = linuxCLibrary.ioctl(fd, KDMKTONE, ((int) ((dur << 16) | (CLOCK_TICK_RATE / freq)))) == 0;
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
      linuxCLibrary.close(fd);
    }
    return returnFlag;
  }
  
  public boolean openCD()
  {
    int cdrom = linuxCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (linuxCLibrary.ioctl(cdrom, CDROMEJECT) == -1)
    {
      linuxCLibrary.close(cdrom);
      return false;
    }
    else
    {
      linuxCLibrary.close(cdrom);
      return true;
    }
  }
  
  public boolean closeCD()
  {
    int cdrom = linuxCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (linuxCLibrary.ioctl(cdrom, CDROMCLOSETRAY) == -1)
    {
      linuxCLibrary.close(cdrom);
      return false;
    }
    else
    {
      linuxCLibrary.close(cdrom);
      return true;
    }
  }
  
  public void exit(int status)
  {
    linuxCLibrary.exit(status);
  }
  
  public void abort()
  {
    linuxCLibrary.abort();
  }
  
  public int raise(int signal)
  {
    return linuxCLibrary.raise(signal);
  }
  
  public int rand()
  {
    return linuxCLibrary.rand();
  }
  
  public void srand(int seed)
  {
    linuxCLibrary.srand(seed);
  }
  
  public String getenv(String env)
  {
    return linuxCLibrary.getenv(env);
  }
  
  public int putenv(String env)
  {
    return linuxCLibrary.putenv(env);
  }
  
  public int getpid()
  {
    return linuxCLibrary.getpid();
  }
  
  public int isatty(int fd)
  {
    return linuxCLibrary.isatty(fd);
  }
  
  public boolean detachConsole()
  {
    try
    {
      // linuxCLibrary.system("disown -h");
      try
      {
        // linuxCLibrary.system("exit");
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
      // linuxCLibrary.system("disown -h");
      try
      {
        // linuxCLibrary.system("exit");
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