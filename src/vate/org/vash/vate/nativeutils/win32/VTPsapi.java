package org.vash.vate.nativeutils.win32;

import com.sun.jna.Library;

public interface VTPsapi extends Library
{
  public boolean EnumProcesses(int[] pProcessIds, int cb, int[] pBytesReturned);
}
