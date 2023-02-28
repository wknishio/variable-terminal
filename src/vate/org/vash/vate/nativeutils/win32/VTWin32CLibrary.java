package org.vash.vate.nativeutils.win32;

import org.vash.vate.nativeutils.VTCLibrary;

import com.sun.jna.Pointer;

public interface VTWin32CLibrary extends VTCLibrary
{
  public int _putenv(String env);
  
  public int getch();
  
  public int getche();
  
  public int _isatty(int fd);
  
  public Pointer freopen(String path, String mode, Pointer stream);
}