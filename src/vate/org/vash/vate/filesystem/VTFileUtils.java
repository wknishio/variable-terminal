package org.vash.vate.filesystem;

import java.io.File;
import java.io.RandomAccessFile;

import vate.org.apache.commons.io.FileUtils;

public class VTFileUtils extends FileUtils
{
  public static boolean truncateQuietly(File file)
  {
    if (file == null)
    {
      return false;
    }
    try
    {
      RandomAccessFile raf = new RandomAccessFile(file, "rw");
      raf.setLength(0);
      raf.close();
      return true;
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public static boolean truncateDeleteQuietly(File file)
  {
    if (file == null)
    {
      return false;
    }
    if (file.isFile())
    {
      truncateQuietly(file);
    }
    if (file.isDirectory())
    {
      for (File child : file.listFiles())
      {
        truncateDeleteQuietly(child);
      }
    }
    return deleteQuietly(file);
  }
}