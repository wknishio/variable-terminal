package org.vate.nativeutils.openbsd;

import org.vate.nativeutils.VTCLibrary;

public interface VTOpenBSDCLibrary extends VTCLibrary
{
	public int open(String path, int flags);
	
	public int ioctl(int fd, int type, Object... args);
	
	public int close(int fd);
	
	public int putenv(String env);
	
	public int getpid();
}