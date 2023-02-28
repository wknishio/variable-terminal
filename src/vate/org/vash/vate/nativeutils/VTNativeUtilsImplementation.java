package org.vash.vate.nativeutils;

public interface VTNativeUtilsImplementation
{
  public int system(String command);
  
  public int getchar();
  
  public int getch();
  
  public void unbuffered();
  
  public void noecho();
  
  public void normal();
  
  public void printf(String format, Object... args);
  
  /* public boolean beep(int freq, int dur); */
  public boolean beep(int freq, int dur, boolean block);
  
  public boolean openCD();
  
  public boolean closeCD();
  
  public void exit(int status);
  
  public void abort();
  
  public int raise(int signal);
  
  public int rand();
  
  public void srand(int seed);
  
  public String getenv(String env);
  
  public int putenv(String env);
  
  public int getpid();
  
  public int isatty(int fd);
  
  public boolean hideConsole();
  
  public boolean detachConsole();
  
  public boolean attachConsole();
  
  public boolean checkANSI();
}