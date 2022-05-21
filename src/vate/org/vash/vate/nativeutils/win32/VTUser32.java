package org.vash.vate.nativeutils.win32;

import com.sun.jna.Library;

public interface VTUser32 extends Library
{
  public int BlockInput(boolean block);

  public boolean ExitWindowsEx(int dwOptions, int dwReserved);

  public int FindWindowA(String lpClassName, String lpWindowName);

  public boolean SendMessageA(int hWnd, int Msg, int wParam, int lParam);

  public boolean SetForegroundWindow(int hWnd);

  public boolean ShowWindow(int hWnd, int flags);
}