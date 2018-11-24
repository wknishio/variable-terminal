package org.vate.nativeutils.win32;

import org.vate.nativeutils.VTCLibrary;

public interface VTWin32CLibrary extends VTCLibrary
{
	public int _putenv(String env);
	
	public int getch();
	
	public int getche();
	
	public int _isatty(int fd);
}