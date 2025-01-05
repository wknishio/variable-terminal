package org.vash.vate.nativeutils.bsd;

import org.vash.vate.nativeutils.VTNativeUtilsInstance;

import com.sun.jna.Native;

public class VTBSDNativeUtils implements VTNativeUtilsInstance
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
  
  private VTBSDCLibrary bsdCLibrary;
  
  public VTBSDNativeUtils()
  {
    bsdCLibrary = (VTBSDCLibrary) Native.load("c", VTBSDCLibrary.class);
  }
  
  public int system(String command)
  {
    return bsdCLibrary.system(command);
  }
  
  public int getchar()
  {
    return bsdCLibrary.getchar();
  }
  
  public void printf(String format, Object... args)
  {
    bsdCLibrary.printf(format, args);
  }
  
  /*
   * public boolean beep(int freq, int dur) { return beep(freq, dur, true); }
   */
  
  public boolean beep(int freq, int dur, boolean block)
  {
    boolean returnFlag = false;
    int fd = bsdCLibrary.open("/dev/console", O_WRONLY);
    if (fd == -1)
    {
      returnFlag = false;
    }
    else
    {
      returnFlag = bsdCLibrary.ioctl(fd, KDMKTONE, ((int) ((dur << 16) | (CLOCK_TICK_RATE / freq)))) == 0;
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
      bsdCLibrary.close(fd);
    }
    return returnFlag;
  }
  
  public boolean openDiscDrive()
  {
    int cdrom = bsdCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (bsdCLibrary.ioctl(cdrom, CDIOCEJECT) == -1)
    {
      bsdCLibrary.close(cdrom);
      return false;
    }
    else
    {
      bsdCLibrary.close(cdrom);
      return true;
    }
  }
  
  public boolean closeDiscDrive()
  {
    int cdrom = bsdCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (bsdCLibrary.ioctl(cdrom, CDIOCCLOSE) == -1)
    {
      bsdCLibrary.close(cdrom);
      return false;
    }
    else
    {
      bsdCLibrary.close(cdrom);
      return true;
    }
  }
  
  public void exit(int status)
  {
    bsdCLibrary.exit(status);
  }
  
  public void abort()
  {
    bsdCLibrary.abort();
  }
  
  public int raise(int signal)
  {
    return bsdCLibrary.raise(signal);
  }
  
  public int rand()
  {
    return bsdCLibrary.rand();
  }
  
  public void srand(int seed)
  {
    bsdCLibrary.srand(seed);
  }
  
  public int getpid()
  {
    return bsdCLibrary.getpid();
  }
  
  public int isatty(int fd)
  {
    return bsdCLibrary.isatty(fd);
  }
  
  public boolean detachConsole()
  {
    try
    {
      // freebsdCLibrary.system("disown -h");
      try
      {
        // freebsdCLibrary.system("exit");
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
      // freebsdCLibrary.system("disown -h");
      try
      {
        // freebsdCLibrary.system("exit");
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