package org.vate.nativeutils.win32;

import com.sun.jna.Library;
import com.sun.jna.platform.win32.WinNT.HANDLE;

public interface VTKernel32 extends Library
{
	public boolean Beep(int freq, int duration);
	
	public void Sleep(int delay);
	
	public int GetCurrentProcessId();
	
	public boolean FreeConsole();
	
	public boolean AllocConsole();
	
	public boolean AttachConsole(int pid);
	
	public int GetConsoleWindow();
	
	public HANDLE GetStdHandle(int nStdHandle);
	
	public boolean SetStdHandle(int nStdHandle, HANDLE handle);
	
	public HANDLE CreateFileA(String name, int arg2, int arg3, com.sun.jna.platform.win32.WinBase.SECURITY_ATTRIBUTES security, int arg5, int arg6, HANDLE handle);
}