package org.vash.vate.filesystem;

import java.io.File;
import java.io.FileFilter;
import java.io.FilenameFilter;
import java.io.IOException;
import java.lang.reflect.Method;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

public class VTRootList extends File
{
  private static final String path = "/";
  
  public VTRootList()
  {
    super(path);
  }
  
  public File[] getRootFiles()
  {
    File[] list = File.listRoots();
    List<File> rootList = new ArrayList<File>();
    for (File rootFile : list)
    {
      rootList.add(new VTRootFile(rootFile.getPath(), this));
    }
    File[] rootArray = new VTRootFile[rootList.size()];
    int i = 0;
    for (File rootFile : rootList)
    {
      rootArray[i++] = rootFile;
    }
    return rootArray;
  }
  
  public String[] getRootPaths()
  {
    File[] list = getRootFiles();
    String[] rootPaths = new String[list.length];
    int i = 0;
    for (File root : list)
    {
      rootPaths[i] = new String(root.getPath());
    }
    return rootPaths;
  }
  
  public boolean canExecute()
  {
    return false;
  }
  
  public boolean canRead()
  {
    return true;
  }
  
  public boolean canWrite()
  {
    return false;
  }
  
  public int compareTo(File pathname)
  {
    return super.compareTo(pathname);
  }
  
  public boolean createNewFile() throws IOException
  {
    return false;
  }
  
  public boolean delete()
  {
    return false;
  }
  
  public void deleteOnExit()
  {
    return;
  }
  
  public boolean equals(Object obj)
  {
    return super.equals(obj);
  }
  
  public boolean exists()
  {
    return true;
  }
  
  public File getAbsoluteFile()
  {
    return this;
  }
  
  public String getAbsolutePath()
  {
    return path;
  }
  
  public File getCanonicalFile() throws IOException
  {
    return this;
  }
  
  public String getCanonicalPath() throws IOException
  {
    return path;
  }
  
  public long getFreeSpace()
  {
    long freeSpace = 0;
    try
    {
      Method freeSpaceMethod = File.class.getMethod("getFreeSpace");
      // freeSpaceMethod.setAccessible(true);
      for (File root : getRootFiles())
      {
        freeSpace += (Long) freeSpaceMethod.invoke(root);
        // freeSpace += root.getFreeSpace();
      }
    }
    catch (Throwable t)
    {
      
    }
    return freeSpace;
  }
  
  public String getName()
  {
    return path;
  }
  
  public String getParent()
  {
    return null;
  }
  
  public File getParentFile()
  {
    return null;
  }
  
  public String getPath()
  {
    return path;
  }
  
  public long getTotalSpace()
  {
    long totalSpace = 0;
    try
    {
      Method totalSpaceMethod = File.class.getMethod("getTotalSpace");
      // totalSpaceMethod.setAccessible(true);
      for (File root : getRootFiles())
      {
        totalSpace += (Long) totalSpaceMethod.invoke(root);
        // totalSpace += root.getTotalSpace();
      }
    }
    catch (Throwable t)
    {
      
    }
    return totalSpace;
  }
  
  public long getUsableSpace()
  {
    long usableSpace = 0;
    try
    {
      Method usableSpaceMethod = File.class.getMethod("getUsableSpace");
      // usableSpaceMethod.setAccessible(true);
      for (File root : getRootFiles())
      {
        usableSpace += (Long) usableSpaceMethod.invoke(root);
        // totalSpace += root.getUsableSpace();
      }
    }
    catch (Throwable t)
    {
      
    }
    return usableSpace;
  }
  
  public int hashCode()
  {
    return super.hashCode();
  }
  
  public boolean isAbsolute()
  {
    return true;
  }
  
  public boolean isDirectory()
  {
    return true;
  }
  
  public boolean isFile()
  {
    return false;
  }
  
  public boolean isHidden()
  {
    return false;
  }
  
  public long lastModified()
  {
    return 1;
  }
  
  public long length()
  {
    return 0;
  }
  
  public String[] list()
  {
    return getRootPaths();
  }
  
  public String[] list(FilenameFilter filter)
  {
    String names[] = list();
    if ((names == null) || (filter == null))
    {
      return names;
    }
    List<String> v = new ArrayList<String>();
    for (int i = 0 ; i < names.length ; i++)
    {
      if (filter.accept(this, names[i]))
      {
        v.add(names[i]);
      }
    }
    return v.toArray(new String[v.size()]);
  }
    
  public File[] listFiles()
  {
    return getRootFiles();
  }
  
  public File[] listFiles(FileFilter filter)
  {
    String ss[] = list();
    if (ss == null)
    {
      return null;
    }
    ArrayList<File> files = new ArrayList<File>();
    for (String s : ss)
    {
      File f = new File(this, s);
      if ((filter == null) || filter.accept(f))
      {
        files.add(f);
      }
    }
    return files.toArray(new File[files.size()]);
  }
  
  public File[] listFiles(FilenameFilter filter)
  {
    String ss[] = list();
    if (ss == null)
    {
      return null;
    }
    ArrayList<File> files = new ArrayList<File>();
    for (String s : ss)
    {
      if ((filter == null) || filter.accept(this, s))
      {
        files.add(new File(this, s));
      }
    }
    return files.toArray(new File[files.size()]);
  }
  
  public boolean mkdir()
  {
    return false;
  }
  
  public boolean mkdirs()
  {
    return false;
  }
  
  public boolean renameTo(File dest)
  {
    return false;
  }
  
  public boolean setExecutable(boolean executable, boolean ownerOnly)
  {
    return false;
  }
  
  public boolean setExecutable(boolean executable)
  {
    return false;
  }
  
  public boolean setLastModified(long time)
  {
    return false;
  }
  
  public boolean setReadable(boolean readable, boolean ownerOnly)
  {
    return false;
  }
  
  public boolean setReadable(boolean readable)
  {
    return false;
  }
  
  public boolean setReadOnly()
  {
    return false;
  }
  
  public boolean setWritable(boolean writable, boolean ownerOnly)
  {
    return false;
  }
  
  public boolean setWritable(boolean writable)
  {
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
  
  /*
   * public static void main(String[] args) throws IOException,
   * URISyntaxException { VTRootList dir = new VTRootList();
   * System.out.println(dir.toString()); }
   */
}