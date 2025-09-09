package org.vash.vate.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

public class VTZipUtils
{
  private static final class VTZipOutputStream extends ZipOutputStream
  {
    private VTZipOutputStream(OutputStream out)
    {
      super(out);
    }
    
    public void setStrategy(int strategy)
    {
      def.setStrategy(strategy);
    }
  }
  
  @SuppressWarnings("all")
  public static boolean createZipFile(String zipFilePath, int level, int strategy, final byte[] readBuffer, String... sourcePaths) throws IOException
  {
    if (sourcePaths == null || sourcePaths.length == 0)
    {
      return false;
    }
    File zipArchive = new File(zipFilePath + ".tmp");
    if (zipArchive.exists())
    {
      if (!zipArchive.isFile())
      {
        return false;
      }
    }
    else if (!zipArchive.createNewFile())
    {
      return false;
    }
    OutputStream fileStream = null;
    VTZipOutputStream zipWriter = null;
    try
    {
      fileStream = new FileOutputStream(zipArchive);
      zipWriter = new VTZipOutputStream(fileStream);
      zipWriter.setLevel(level);
      zipWriter.setStrategy(strategy);
      for (String contentPath : sourcePaths)
      {
        File nextFile = new File(contentPath).getAbsoluteFile();
        if (nextFile.exists())
        {
          if (nextFile.isFile())
          {
            if (!addFileToZip(zipWriter, zipArchive, nextFile, readBuffer, ""))
            {
              return false;
            }
          }
          else if (nextFile.isDirectory())
          {
            if (!addDirectoryToZip(zipWriter, zipArchive, nextFile, readBuffer, nextFile.getName()))
            {
              return false;
            }
          }
        }
      }
    }
    finally
    {
      boolean error = false;
      try
      {
        if (zipWriter != null)
        {
          zipWriter.flush();
          zipWriter.finish();
          zipWriter.close();
        }
        else
        {
          error = true;
        }
      }
      catch (IOException e)
      {
        error = true;
      }
      if (fileStream != null)
      {
        fileStream.close();
      }
      if (error)
      {
        return false;
      }
    }
    File trueZipArchive = new File(zipFilePath);
    if (!zipArchive.renameTo(trueZipArchive))
    {
      VTFileUtils.truncateDeleteQuietly(trueZipArchive);
      return zipArchive.renameTo(trueZipArchive);
    }
    // may treat timestamps here
    return true;
  }
  
  @SuppressWarnings("all")
  private static boolean addDirectoryToZip(ZipOutputStream zipWriter, File zipArchive, File directory, final byte[] readBuffer, String currentPath) throws IOException
  {
    if (!directory.canRead())
    {
      return false;
    }
    ZipEntry directoryEntry = new ZipEntry(currentPath + '/');
    // may treat timestamps here
    zipWriter.putNextEntry(directoryEntry);
    zipWriter.closeEntry();
    zipWriter.flush();
    for (File child : directory.listFiles())
    {
      if (child.isFile())
      {
        if (!addFileToZip(zipWriter, zipArchive, child, readBuffer, currentPath + '/'))
        {
          return false;
        }
      }
      else if (child.isDirectory())
      {
        if (!addDirectoryToZip(zipWriter, zipArchive, child, readBuffer, currentPath + '/' + child.getName()))
        {
          return false;
        }
      }
    }
    return true;
  }
  
  @SuppressWarnings("all")
  private static boolean addFileToZip(ZipOutputStream zipWriter, File zipArchive, File file, final byte[] readBuffer, String currentPath) throws IOException
  {
    if (zipArchive.getAbsoluteFile().equals(file.getAbsoluteFile()))
    {
      return true;
    }
    InputStream fileInputStream = null;
    try
    {
      if (!file.canRead())
      {
        return false;
      }
      fileInputStream = new FileInputStream(file);
      ZipEntry fileEntry = new ZipEntry(currentPath + file.getName());
      // may treat timestamps here
      zipWriter.putNextEntry(fileEntry);
      int readBytes;
      while ((readBytes = fileInputStream.read(readBuffer)) > 0)
      {
        zipWriter.write(readBuffer, 0, readBytes);
        zipWriter.flush();
      }
      zipWriter.closeEntry();
      zipWriter.flush();
    }
    finally
    {
      if (fileInputStream != null)
      {
        fileInputStream.close();
      }
    }
    return true;
  }
  
  @SuppressWarnings("all")
  public static boolean extractZipFile(String zipFilePath, final byte[] readBuffer, String destinationPath) throws IOException
  {
    File file = new File(zipFilePath);
    if (file.exists() && !file.isFile())
    {
      return false;
    }
    if (destinationPath.equalsIgnoreCase(""))
    {
      destinationPath = ".";
    }
    File directory = new File(destinationPath);
    if (!directory.exists())
    {
      if (!directory.mkdirs())
      {
        return false;
      }
    }
    else if (directory.exists())
    {
      if (!directory.isDirectory())
      {
        return false;
      }
    }
    InputStream fileStream = null;
    ZipInputStream zipReader = null;
    try
    {
      fileStream = new FileInputStream(file);
      zipReader = new ZipInputStream(fileStream);
      ZipEntry zipEntry;
      while ((zipEntry = zipReader.getNextEntry()) != null)
      {
        if (zipEntry.isDirectory())
        {
          if (!extractDirectoryFromZip(zipEntry, destinationPath))
          {
            return false;
          }
        }
        else
        {
          if (!extractFileFromZip(zipReader, zipEntry, readBuffer, destinationPath))
          {
            return false;
          }
        }
        zipReader.closeEntry();
      }
    }
    finally
    {
      if (zipReader != null)
      {
        zipReader.close();
      }
    }
    return true;
  }
  
  @SuppressWarnings("all")
  private static boolean extractDirectoryFromZip(ZipEntry zipEntry, String destinationPath)
  {
    //System.out.println("regex:[" + "....\\//\\".replaceAll("[..]+([/]|[\\\\])+", "") + "]");
    String zipEntryName = zipEntry.getName().replaceAll("[..]+([/]|[\\\\])+", "");
    
    File directory = new File(destinationPath + File.separatorChar + zipEntryName);
    if (directory.exists() && directory.isDirectory())
    {
      // may treat timestamps here
      return true;
    }
    if (directory.mkdirs())
    {
      // may treat timestamps here
      return true;
    }
    return false;
  }
  
  @SuppressWarnings("all")
  private static boolean extractFileFromZip(ZipInputStream zipReader, ZipEntry zipEntry, final byte[] readBuffer, String destinationPath) throws IOException
  {
    //System.out.println("regex:[" + "....\\//\\".replaceAll("[..]+([/]|[\\\\])+", "") + "]");
    String zipEntryName = zipEntry.getName().replaceAll("[..]+([/]|[\\\\])+", "");
    
    File directory = new File(destinationPath);
    if (!directory.exists())
    {
      if (!directory.mkdirs())
      {
        return false;
      }
    }
    File tempFile = new File(destinationPath + File.separatorChar + zipEntryName + ".tmp");
    File finalFile = new File(destinationPath + File.separatorChar + zipEntryName);
    if (finalFile.exists())
    {
      if (!finalFile.isFile())
      {
        return false;
      }
    }
    else if (!finalFile.createNewFile())
    {
      return false;
    }
    OutputStream fileOutputStream = null;
    try
    {
      fileOutputStream = new FileOutputStream(tempFile);
      int readBytes;
      while ((readBytes = zipReader.read(readBuffer)) > 0)
      {
        fileOutputStream.write(readBuffer, 0, readBytes);
        fileOutputStream.flush();
      }
    }
    finally
    {
      if (fileOutputStream != null)
      {
        fileOutputStream.close();
      }
    }
    if (!tempFile.renameTo(finalFile))
    {
      VTFileUtils.truncateDeleteQuietly(finalFile);
      return tempFile.renameTo(finalFile);
    }
    // may treat timestamps here
    return true;
  }
  
  /*
   * public static void main(String[] args) throws IOException {
   * System.out.println(SAWZipUtils.createZipFile("zip-test.zip", -1, new
   * byte[64 * 1024],"dist"));
   * System.out.println(SAWZipUtils.extractZipFile("zip-test.zip", new byte[64 *
   * 1024],"aua")); }
   */
}