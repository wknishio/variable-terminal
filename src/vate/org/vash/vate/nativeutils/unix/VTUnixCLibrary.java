package org.vash.vate.nativeutils.unix;

import com.sun.jna.Library;

public interface VTUnixCLibrary extends Library
{
  public void printf(String format, Object... args);
  
  // public int scanf(String format, Object... args);
  public int getchar();
  
  public int system(String command);
  
  public int raise(int signal);
  
  // public int atoi(String number);
  // public long atol(String number);
  public void exit(int status);
  
  public void abort();
  
  public int rand();
  
  public void srand(int seed);
  
  public String getenv(String env);
  
  public int isatty(int fd);
  
  public int getpid();
  
  public int getppid();
}