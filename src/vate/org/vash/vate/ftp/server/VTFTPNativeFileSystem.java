package org.vash.vate.ftp.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;

import com.guichaguri.minimalftp.impl.NativeFileSystem;

public class VTFTPNativeFileSystem extends NativeFileSystem
{
  protected final File root;
  
  public VTFTPNativeFileSystem(File rootDir)
  {
    super(rootDir);
    this.root = rootDir;
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
//    if (path.length() == 0 || path.equals("/"))
//    {
//      return root;
//    }
    File file = new File(path);
    return file.getAbsoluteFile();
  }
  
  public File findFile(File cwd, String path) throws IOException
  {
//    File file;
//    if (cwd == root)
//    {
//      if (path.length() == 0 || path.equals("/") || path.equals("."))
//      {
//        return root;
//      }
//    }
    File file = new File(cwd, path);
    return file.getAbsoluteFile();
  }
  
  public String getPath(File file)
  {
    return file.getAbsolutePath();
  }
}