package org.vash.vate.nativeutils.sunos;

import org.vash.vate.nativeutils.unix.VTUnixCLibrary;

public interface VTSunOSCLibrary extends VTUnixCLibrary
{
  public int open(String path, int flags);

  public int ioctl(int fd, int type, Object... args);

  public int close(int fd);

  public int putenv(String env);
}
