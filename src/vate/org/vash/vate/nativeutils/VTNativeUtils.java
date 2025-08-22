package org.vash.vate.nativeutils;

public interface VTNativeUtils
{
  public int system(String command);
  
  public int getchar();
  
  public int getch();
  
  public void printf(String format, Object... args);
  
  /* public boolean beep(int freq, int dur); */
  public boolean beep(int freq, int dur, boolean block);
  
  public boolean openDiscDrive();
  
  public boolean closeDiscDrive();
  
  public int getpid();
  
  public int isatty(int fd);
  
  public boolean hideConsole();
  
  public boolean detachConsole();
  
  public boolean attachConsole();
  
  public boolean checkANSI();
}