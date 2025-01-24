package org.vash.vate.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.net.URI;

public class VTRootFile extends File
{
  private String rootPath;
  private String rootName;
  private VTRootList parent;
  
  public VTRootFile(String path, VTRootList root)
  {
    super(path);
    this.rootName = path;
    this.rootPath = path;
    this.parent = root;
  }
  
  public boolean canExecute()
  {
    return false;
  }
  
  public boolean canRead()
  {
    return super.canRead();
  }
  
  public boolean canWrite()
  {
    return super.canWrite();
  }
  
  public int compareTo(File pathname)
  {
    return super.compareTo(pathname);
  }
  
  public boolean createNewFile() throws IOException
  {
    return super.createNewFile();
  }
  
  public boolean delete()
  {
    return super.delete();
  }
  
  public void deleteOnExit()
  {
    super.deleteOnExit();
  }
  
  public boolean equals(Object obj)
  {
    return super.equals(obj);
  }
  
  public boolean exists()
  {
    return super.exists();
  }
  
  public File getAbsoluteFile()
  {
    return this;
  }
  
  public String getAbsolutePath()
  {
    return rootPath;
  }
  
  public File getCanonicalFile() throws IOException
  {
    return this;
  }
  
  public String getCanonicalPath() throws IOException
  {
    return rootPath;
  }
  
  public long getFreeSpace()
  {
    try
    {
      // return super.getFreeSpace();
    }
    catch (Throwable t)
    {
      
    }
    return 0;
  }
  
  public String getName()
  {
    return this.rootName;
  }
  
  public String getParent()
  {
    return this.parent.getPath();
  }
  
  public File getParentFile()
  {
    return this.parent;
  }
  
  public String getPath()
  {
    return this.rootPath;
  }
  
  public long getTotalSpace()
  {
    try
    {
      // return super.getTotalSpace();
    }
    catch (Throwable t)
    {
      
    }
    return 0;
  }
  
  public long getUsableSpace()
  {
    try
    {
      // return super.getUsableSpace();
    }
    catch (Throwable t)
    {
      
    }
    return 0;
  }
  
  public int hashCode()
  {
    return super.hashCode();
  }
  
  public boolean isAbsolute()
  {
    return super.isAbsolute();
  }
  
  public boolean isDirectory()
  {
    return super.isDirectory();
  }
  
  public boolean isFile()
  {
    return super.isFile();
  }
  
  public boolean isHidden()
  {
    return super.isHidden();
  }
  
  public long lastModified()
  {
    return super.lastModified();
  }
  
  public long length()
  {
    return super.length();
  }
  
  public String[] list()
  {
    return super.list();
  }
  
  public String[] list(FilenameFilter filter)
  {
    return super.list(filter);
  }
  
  public File[] listFiles()
  {
    return super.listFiles();
  }
  
  public File[] listFiles(FileFilter filter)
  {
    return super.listFiles(filter);
  }
  
  public File[] listFiles(FilenameFilter filter)
  {
    return super.listFiles(filter);
  }
  
  public boolean mkdir()
  {
    return super.mkdir();
  }
  
  public boolean mkdirs()
  {
    return super.mkdirs();
  }
  
  public boolean renameTo(File dest)
  {
    return super.renameTo(dest);
  }
  
  public boolean setExecutable(boolean executable, boolean ownerOnly)
  {
    try
    {
      // return super.setExecutable(executable, ownerOnly);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public boolean setExecutable(boolean executable)
  {
    try
    {
      // return super.setExecutable(executable);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public boolean setLastModified(long time)
  {
    return super.setLastModified(time);
  }
  
  public boolean setReadable(boolean readable, boolean ownerOnly)
  {
    try
    {
      // return super.setReadable(readable, ownerOnly);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public boolean setReadable(boolean readable)
  {
    try
    {
      // return super.setReadable(readable);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public boolean setReadOnly()
  {
    return super.setReadOnly();
  }
  
  public boolean setWritable(boolean writable, boolean ownerOnly)
  {
    try
    {
      // return super.setWritable(writable, ownerOnly);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public boolean setWritable(boolean writable)
  {
    try
    {
      // return super.setWritable(writable);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public String toString()
  {
    return super.toString();
  }
  
  public URI toURI()
  {
    return super.toURI();
  }
  
  protected Object clone() throws CloneNotSupportedException
  {
    return super.clone();
  }
  
  protected void finalize() throws Throwable
  {
    super.finalize();
  }
  
  private static final long serialVersionUID = 1L;
}
