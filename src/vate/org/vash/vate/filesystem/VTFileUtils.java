package org.vash.vate.filesystem;

import java.io.File;
import java.io.RandomAccessFile;

import org.apache.commons.io.FileUtils;

public class VTFileUtils extends FileUtils
{
  public static boolean truncateFile(File file)
  {
    if (file == null)
    {
      return false;
    }
    try
    {
      if (file.isFile())
      {
        RandomAccessFile raf = new RandomAccessFile(file, "rw");
        raf.setLength(0);
        raf.close();
        return true;
      }
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public static boolean truncateThenDeleteQuietly(File file)
  {
    if (file == null)
    {
      return false;
    }
    if (file.isFile())
    {
      truncateFile(file);
    }
    if (file.isDirectory())
    {
      for (File child : file.listFiles())
      {
        truncateThenDeleteQuietly(child);
      }
    }
    return deleteQuietly(file);
  }
}