package org.vash.vate.socket.ftpserver;

import java.io.File;

import com.guichaguri.minimalftp.impl.NativeFileSystem;

public class VTFTPNativeFileSystem extends NativeFileSystem
{
  public VTFTPNativeFileSystem(File rootDir)
  {
    super(rootDir);
  }

  protected boolean isInside(File dir, File file)
  {
    return true;
  }
}