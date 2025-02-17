package org.vash.vate.ftp.server;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Arrays;
import java.util.Comparator;

import org.vash.vate.filesystem.VTFileSeekSorter;

import com.guichaguri.minimalftp.impl.NativeFileSystem;

public class VTFTPNativeFileSystem extends NativeFileSystem
{
  protected final File root;
  protected final Comparator<File> fileSorter = new VTFileSeekSorter();
  
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
    if (path.length() >= 2 && path.length() <= 3 && path.charAt(1) == ':')
    {
      if (!(path.endsWith("/") || path.endsWith("\\")))
      {
        path += '/';
      }
    }
    File file = new File(path);
    return file.getAbsoluteFile();
  }
  
  public File findFile(File cwd, String path) throws IOException
  {
    if (path.startsWith("/"))
    {
      return findFile(path.substring(1));
    }
    if (path.length() >= 2 && path.length() <= 3 && path.charAt(1) == ':')
    {
      if (!(path.endsWith("/") || path.endsWith("\\")))
      {
        path += '/';
      }
      return findFile(path);
    }
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
//    file = findFile(path);
//    if (file.exists())
//    {
//      return file;
//    }
    //return null;
  }
  
  public String getPath(File file)
  {
    return file.getAbsolutePath().replace(File.separatorChar, '/');
  }
  
  public File[] listFiles(File dir) throws IOException
  {
    if (!dir.isDirectory())
    {
      throw new IOException("Not a directory");
    }
    File[] files = dir.listFiles();
    if (files != null)
    {
      Arrays.sort(files, fileSorter);
    }
    return files;
  }
  
//  public boolean canExecute(File file)
//  {
//    return false;
//  }
}