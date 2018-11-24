package org.vate.nativeutils.win32;

import com.sun.jna.Library;

public interface VTKernel32 extends Library
{
	public boolean Beep(int freq, int duration);
	
	public void Sleep(int delay);
	
	public int GetCurrentProcessId();
}