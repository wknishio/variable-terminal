package org.vash.vate.filesystem;

import java.io.File;
import java.util.Comparator;

public class VTFileTransferSorter implements Comparator<File>
{
  public int compare(File o1, File o2)
  {
    if (o1.isDirectory())
    {
      if (o2.isDirectory())
      {
        String name1 = o1.getName().toUpperCase();
        String name2 = o2.getName().toUpperCase();
        return name1.compareTo(name2);
      }
      else
      {
        return -1;
      }
    }
    else
    {
      if (o2.isDirectory())
      {
        return 1;
      }
      else
      {
        Long length1 = Long.valueOf(o1.length());
        Long length2 = Long.valueOf(o2.length());
        return length1.compareTo(length2);
      }
    }
  }
}
