package org.vash.vate.ftp.server;

import java.io.File;
import java.io.IOException;

import com.guichaguri.minimalftpvt.impl.NativeFileSystem;

public class VTFTPNativeFileSystem extends NativeFileSystem
{
  protected final File root;
  //protected final Comparator<File> fileSorter = new VTFileSeekSorter();
  
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
    if (file.getParentFile() == null)
    {
      return root;
    }
    return file.getParentFile();
  }
  
  public File findFile(String path) throws IOException
  {
    //path = path.replace(":\\/", ":/");
    //System.out.println("findFile(" + path + ")");
    try
    {
      if (path.length() == 0 || path.equals("/"))
      {
        return root;
      }
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
    catch (Throwable t)
    {
      throw new IOException(t.getMessage());
    }
  }
  
  public File findFile(File cwd, String path) throws IOException
  {
    //path = path.replace(":\\/", ":/");
    //System.out.println("findFile(" + cwd + "," + path + ")");
    try
    {
      if (cwd.getAbsolutePath().equals(root.getAbsolutePath()))
      {
        return findFile(path);
      }
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
      File file = new File(cwd, path);
      return file.getAbsoluteFile();
    }
    catch (Throwable t)
    {
      throw new IOException(t.getMessage());
    }
  }
  
  public String getName(File file)
  {
    String name = file.getName();
    if (name.endsWith("\\") || name.endsWith("/"))
    {
      return name.substring(0, name.length() - 1);
    }
    return name;
  }
  
  public String getPath(File file)
  {
    try
    {
      String path = file.getAbsolutePath().replace('\\', '/');
      if (path.length() > 1 && path.endsWith("/"))
      {
        return path.substring(0, path.length() - 1);
      }
      return path;
    }
    catch (Throwable t)
    {
      
    }
    return "";
  }
  
  public File[] listFiles(File dir) throws IOException
  {
    File[] files = new File[] {};
    try
    {
      files = dir.listFiles();
      if (files != null)
      {
        //Arrays.sort(files, fileSorter);
      }
      else
      {
        throw new IOException("Inaccessible or not a directory");
      }
    }
    catch (Throwable t)
    {
      throw new IOException(t.getMessage());
    }
    return files;
  }
  
//  public boolean canExecute(File file)
//  {
//    return false;
//  }
}