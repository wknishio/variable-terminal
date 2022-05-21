package org.vash.vate.nativeutils;

import com.sun.jna.Native;

public class VTMinimalNativeUtils implements VTNativeUtilsImplementation
{
  private VTMinimalCLibrary cLibray = (VTMinimalCLibrary) Native.load("c", VTMinimalCLibrary.class);

  public int system(String command)
  {
    return cLibray.system(command);
  }

  public int getchar()
  {
    return cLibray.getchar();
  }

  public void printf(String format, Object... args)
  {
    cLibray.printf(format, args);
  }

  /* public boolean beep(int freq, int dur) { return false; } */

  public boolean beep(int freq, int dur, boolean block)
  {
    return false;
  }

  public boolean openCD()
  {
    return false;
  }

  public boolean closeCD()
  {
    return false;
  }

  public void exit(int status)
  {
    cLibray.exit(status);
  }

  public void abort()
  {
    cLibray.abort();
  }

  public int raise(int signal)
  {
    return cLibray.raise(signal);
  }

  public int rand()
  {
    return cLibray.rand();
  }

  public void srand(int seed)
  {
    cLibray.srand(seed);
  }

  public String getenv(String env)
  {
    return cLibray.getenv(env);
  }

  public int putenv(String env)
  {
    return cLibray.putenv(env);
  }

  public int getpid()
  {
    return 0;
  }

  public int isatty(int fd)
  {
    return cLibray.isatty(fd);
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