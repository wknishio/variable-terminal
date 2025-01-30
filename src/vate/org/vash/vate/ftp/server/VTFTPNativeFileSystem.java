package org.vash.vate.ftp.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

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
  
  public File getParent(File file) throws IOException
  {
    if(file.getParentFile() == null)
    {
      throw new FileNotFoundException("No parent found for this file");
    }
    return file.getParentFile();
  }
  
  public File findFile(String path) throws IOException
  {
    File file = new File(path);
    return file;
  }
}