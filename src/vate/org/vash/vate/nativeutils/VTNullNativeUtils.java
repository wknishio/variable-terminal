package org.vash.vate.nativeutils;

import java.io.FileDescriptor;
import java.io.PrintStream;

import org.vash.vate.console.standard.VTStandardConsoleOutputStream;

public class VTNullNativeUtils implements VTNativeUtilsImplementation
{
  /* public boolean beep(int freq, int dur) { return false; } */
  private PrintStream out;
  
  public VTNullNativeUtils()
  {
    out = new PrintStream(new VTStandardConsoleOutputStream(FileDescriptor.out));
  }
  
  public boolean beep(int freq, int dur, boolean block)
  {
    return false;
  }
  
  public boolean closeCD()
  {
    return false;
  }
  
  public int getchar()
  {
    return 0;
  }
  
  public boolean openCD()
  {
    return false;
  }
  
  public void printf(String format, Object... args)
  {
    out.printf(format, args);
  }
  
  public int system(String command)
  {
    return 0;
  }
  
  public void exit(int status)
  {
    
  }
  
  public void abort()
  {
    
  }
  
  public int raise(int signal)
  {
    return 0;
  }
  
  public int rand()
  {
    return 0;
  }
  
  public void srand(int seed)
  {
    
  }
  
  public String getenv(String env)
  {
    return null;
  }
  
  public int putenv(String env)
  {
    return 1;
  }
  
  public int getpid()
  {
    return 0;
  }
  
  public int isatty(int fd)
  {
    return 0;
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
    return 0;
  }
  
  public void unbuffered()
  {
    
  }
  
  public void normal()
  {
    
  }
  
  public void noecho()
  {
    
  }
  
  public boolean checkANSI()
  {
    return false;
  }
}
