package org.vash.vate.nativeutils.win32;

import com.sun.jna.Library;

public interface VTWinmm extends Library
{
  public int mciSendStringA(String lpstrCommand, String lpstrReturnString, int uReturnLength, int hwndCallback);

  public boolean mciExecute(String pszCommand);
}