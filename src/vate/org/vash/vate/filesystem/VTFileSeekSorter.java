package org.vash.vate.filesystem;

import java.io.File;
import java.util.Comparator;

public class VTFileSeekSorter implements Comparator<File>
{
  public int compare(File f1, File f2)
  {
    boolean f1IsDirectory = false;
    boolean f2IsDirectory = false;
    try
    {
      f1IsDirectory = f1.isDirectory();
    }
    catch (Throwable t)
    {
      
    }
    try
    {
      f2IsDirectory = f2.isDirectory();
    }
    catch (Throwable t)
    {
      
    }
    if (f1IsDirectory)
    {
      if (f2IsDirectory)
      {
        String name1 = f1.getName().toUpperCase();
        String name2 = f2.getName().toUpperCase();
        return name1.compareTo(name2);
      }
      else
      {
        return -1;
      }
    }
    else
    {
      if (f2IsDirectory)
      {
        return 1;
      }
      else
      {
        String name1 = f1.getName().toUpperCase();
        String name2 = f2.getName().toUpperCase();
        return name1.compareTo(name2);
      }
    }
  }
}
