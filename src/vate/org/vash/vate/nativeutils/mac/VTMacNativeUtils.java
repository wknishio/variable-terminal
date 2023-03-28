package org.vash.vate.nativeutils.mac;

import org.vash.vate.nativeutils.VTNativeUtilsImplementation;

import com.sun.jna.Native;

public class VTMacNativeUtils implements VTNativeUtilsImplementation
{
  // Device opening flags
  private static int O_RDONLY = 0;
  private static int O_WRONLY = 1;
  private static int O_NONBLOCK = 0x0004;
  private static int CLOCK_TICK_RATE = 1193180;
  // private static int IOCPARM_MASK = 0x1fff;
  // IOCTLs
  // private static int KIOCSOUND = ('K' << 8) | 63;
  private static int KDMKTONE = ('K' << 8) | 8;
  private static int CDIOCEJECT = ('c' << 8) | 24;
  private static int CDIOCCLOSE = ('c' << 8) | 28;
  
  private VTMacCLibrary macCLibrary;
  // private VTCLibrary cLibrary;
  
  public VTMacNativeUtils()
  {
    macCLibrary = (VTMacCLibrary) Native.load(("c"), VTMacCLibrary.class);
  }
  
  public int system(String command)
  {
    return macCLibrary.system(command);
  }
  
  public int getchar()
  {
    return macCLibrary.getchar();
  }
  
  public void printf(String format, Object... args)
  {
    macCLibrary.printf(format, args);
  }
  
  /*
   * public boolean beep(int freq, int dur) { return beep(freq, dur, true); }
   */
  
  public boolean beep(int freq, int dur, boolean block)
  {
    boolean returnFlag = false;
    int fd = macCLibrary.open("/dev/console", O_WRONLY);
    if (fd == -1)
    {
      returnFlag = false;
    }
    else
    {
      returnFlag = macCLibrary.ioctl(fd, KDMKTONE, ((int) ((dur << 16) | (CLOCK_TICK_RATE / freq)))) == 0;
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
      macCLibrary.close(fd);
    }
    return returnFlag;
  }
  
  public boolean openDiscDrive()
  {
    int cdrom = macCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (macCLibrary.ioctl(cdrom, CDIOCEJECT) == -1)
    {
      macCLibrary.close(cdrom);
      return false;
    }
    else
    {
      macCLibrary.close(cdrom);
      return true;
    }
  }
  
  public boolean closeDiscDrive()
  {
    int cdrom = macCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (macCLibrary.ioctl(cdrom, CDIOCCLOSE) == -1)
    {
      macCLibrary.close(cdrom);
      return false;
    }
    else
    {
      macCLibrary.close(cdrom);
      return true;
    }
  }
  
  public void exit(int status)
  {
    macCLibrary.exit(status);
  }
  
  public void abort()
  {
    macCLibrary.abort();
  }
  
  public int raise(int signal)
  {
    return macCLibrary.raise(signal);
  }
  
  public int rand()
  {
    return macCLibrary.rand();
  }
  
  public void srand(int seed)
  {
    macCLibrary.srand(seed);
  }
  
  public String getenv(String env)
  {
    return macCLibrary.getenv(env);
  }
  
  public int putenv(String env)
  {
    return macCLibrary.putenv(env);
  }
  
  public int getpid()
  {
    return macCLibrary.getpid();
  }
  
  public int isatty(int fd)
  {
    return macCLibrary.isatty(fd);
  }
  
  public boolean detachConsole()
  {
    try
    {
      // macCLibrary.system("disown -h");
      try
      {
        // macCLibrary.system("exit");
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
      // macCLibrary.system("disown -h");
      try
      {
        // macCLibrary.system("exit");
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