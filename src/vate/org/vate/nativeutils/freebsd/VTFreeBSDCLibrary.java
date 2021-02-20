package org.vate.nativeutils.freebsd;

import org.vate.nativeutils.unix.VTUnixCLibrary;

public interface VTFreeBSDCLibrary extends VTUnixCLibrary
{
  public int open(String path, int flags);

  public int ioctl(int fd, int type, Object... args);

  public int close(int fd);

  public int putenv(String env);

  public int getpid();
}
