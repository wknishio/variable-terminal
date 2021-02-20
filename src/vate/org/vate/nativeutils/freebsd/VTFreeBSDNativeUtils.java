package org.vate.nativeutils.freebsd;

import org.vate.nativeutils.VTNativeUtilsImplementation;

import com.sun.jna.Native;

public class VTFreeBSDNativeUtils implements VTNativeUtilsImplementation
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

  private VTFreeBSDCLibrary freebsdCLibrary;

  public VTFreeBSDNativeUtils()
  {
    freebsdCLibrary = (VTFreeBSDCLibrary) Native.loadLibrary("c", VTFreeBSDCLibrary.class);
  }

  public int system(String command)
  {
    return freebsdCLibrary.system(command);
  }

  public int getchar()
  {
    return freebsdCLibrary.getchar();
  }

  public void printf(String format, Object... args)
  {
    freebsdCLibrary.printf(format, args);
  }

  /*
   * public boolean beep(int freq, int dur) { return beep(freq, dur, true); }
   */

  public boolean beep(int freq, int dur, boolean block)
  {
    boolean returnFlag = false;
    int fd = freebsdCLibrary.open("/dev/console", O_WRONLY);
    if (fd == -1)
    {
      returnFlag = false;
    }
    else
    {
      returnFlag = freebsdCLibrary.ioctl(fd, KDMKTONE, ((int) ((dur << 16) | (CLOCK_TICK_RATE / freq)))) == 0;
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
      freebsdCLibrary.close(fd);
    }
    return returnFlag;
  }

  public boolean openCD()
  {
    int cdrom = freebsdCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (freebsdCLibrary.ioctl(cdrom, CDIOCEJECT) == -1)
    {
      freebsdCLibrary.close(cdrom);
      return false;
    }
    else
    {
      freebsdCLibrary.close(cdrom);
      return true;
    }
  }

  public boolean closeCD()
  {
    int cdrom = freebsdCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
    if (cdrom == -1)
    {
      return false;
    }
    if (freebsdCLibrary.ioctl(cdrom, CDIOCCLOSE) == -1)
    {
      freebsdCLibrary.close(cdrom);
      return false;
    }
    else
    {
      freebsdCLibrary.close(cdrom);
      return true;
    }
  }

  public void exit(int status)
  {
    freebsdCLibrary.exit(status);
  }

  public void abort()
  {
    freebsdCLibrary.abort();
  }

  public int raise(int signal)
  {
    return freebsdCLibrary.raise(signal);
  }

  public int rand()
  {
    return freebsdCLibrary.rand();
  }

  public void srand(int seed)
  {
    freebsdCLibrary.srand(seed);
  }

  public String getenv(String env)
  {
    return freebsdCLibrary.getenv(env);
  }

  public int putenv(String env)
  {
    return freebsdCLibrary.putenv(env);
  }

  public int getpid()
  {
    return freebsdCLibrary.getpid();
  }

  public int isatty(int fd)
  {
    return freebsdCLibrary.isatty(fd);
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
      Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty -icanon min 1 < /dev/tty" });
    }
    catch (Throwable t)
    {

    }
  }

  public void noecho()
  {
    try
    {
      Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty -echo < /dev/tty" });
    }
    catch (Throwable t)
    {

    }
  }

  public void normal()
  {
    try
    {
      Runtime.getRuntime().exec(new String[] { "/bin/sh", "-c", "stty icanon echo < /dev/tty" });
    }
    catch (Throwable t)
    {

    }
  }
}