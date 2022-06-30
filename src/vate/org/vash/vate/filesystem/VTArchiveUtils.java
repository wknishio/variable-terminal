package org.vash.vate.filesystem;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
// import java.io.FileInputStream;
// import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.apache.commons.compress.archivers.tar.TarArchiveEntry;
import org.apache.commons.compress.archivers.tar.TarArchiveInputStream;
import org.apache.commons.compress.archivers.tar.TarArchiveOutputStream;
import org.apache.commons.compress.archivers.zip.Zip64Mode;
import org.apache.commons.compress.archivers.zip.ZipArchiveEntry;
import org.apache.commons.compress.archivers.zip.ZipArchiveInputStream;
import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;

public class VTArchiveUtils
{
  // @SuppressWarnings("resource")
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
      if (!zipArchive.delete() || !zipArchive.createNewFile())
      {
        return false;
      }
    }
    // OutputStream fileStream = null;
    ZipArchiveOutputStream zipWriter = null;
    try
    {
      // fileStream = Channels.newOutputStream(new
      // FileOutputStream(zipArchive).getChannel());
      // zipWriter = new ZipArchiveOutputStream(fileStream);
      zipWriter = new ZipArchiveOutputStream(zipArchive);
      zipWriter.setLevel(level);
      zipWriter.setStrategy(strategy);
      zipWriter.setUseZip64(Zip64Mode.AsNeeded);
      for (String contentPath : sourcePaths)
      {
        File nextFile = new File(contentPath).getAbsoluteFile();
        if (nextFile.exists())
        {
          if (nextFile.isFile())
          {
            if (!addFileToZipFile(zipWriter, zipArchive, nextFile, readBuffer, ""))
            {
              return false;
            }
          }
          else if (nextFile.isDirectory())
          {
            if (!addDirectoryToZipFile(zipWriter, zipArchive, nextFile, readBuffer, nextFile.getName()))
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
      if (error)
      {
        return false;
      }
    }
    File trueZipArchive = new File(zipFilePath);
    if (!zipArchive.renameTo(trueZipArchive))
    {
      if (trueZipArchive.delete())
      {
        if (!zipArchive.renameTo(trueZipArchive))
        {
          //zipArchive.delete();
          return false;
        }
        return true;
      }
      else
      {
        return false;
      }
    }
    return true;
  }

  private static boolean addDirectoryToZipFile(ZipArchiveOutputStream zipWriter, File zipArchive, File directory, final byte[] readBuffer, String currentPath) throws IOException
  {
    if (!directory.canRead())
    {
      return false;
    }
    ZipArchiveEntry directoryEntry = new ZipArchiveEntry(currentPath + '/');
    //may treat timestamps here
    zipWriter.putArchiveEntry(directoryEntry);
    zipWriter.closeArchiveEntry();
    zipWriter.flush();
    for (File child : directory.listFiles())
    {
      if (child.isFile())
      {
        if (!addFileToZipFile(zipWriter, zipArchive, child, readBuffer, currentPath + '/'))
        {
          return false;
        }
      }
      else if (child.isDirectory())
      {
        if (!addDirectoryToZipFile(zipWriter, zipArchive, child, readBuffer, currentPath + '/' + child.getName()))
        {
          return false;
        }
      }
    }
    return true;
  }

  // @SuppressWarnings("resource")
  @SuppressWarnings("all")
  private static boolean addFileToZipFile(ZipArchiveOutputStream zipWriter, File zipArchive, File file, final byte[] readBuffer, String currentPath) throws IOException
  {
    if (zipArchive.getAbsoluteFile().equals(file.getAbsoluteFile()))
    {
      return true;
    }
    FileInputStream fileInputStream = null;
    try
    {
      if (!file.canRead())
      {
        return false;
      }
      // fileInputStream = Channels.newInputStream(new
      // FileInputStream(file).getChannel());
      fileInputStream = new FileInputStream(file);
      ZipArchiveEntry zipEntry = new ZipArchiveEntry(currentPath + file.getName());
      //may treat timestamps here
      zipWriter.putArchiveEntry(zipEntry);
      int readBytes;
      while ((readBytes = fileInputStream.read(readBuffer)) > 0)
      {
        zipWriter.write(readBuffer, 0, readBytes);
        zipWriter.flush();
      }
      zipWriter.closeArchiveEntry();
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

  // @SuppressWarnings("resource")
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
    InputStream fileInputStream = null;
    ZipArchiveInputStream zipReader = null;
    try
    {
      // fileStream = Channels.newInputStream(new
      // FileInputStream(file).getChannel());
      fileInputStream = new FileInputStream(file);
      zipReader = new ZipArchiveInputStream(fileInputStream);
      ZipArchiveEntry zipEntry;
      while ((zipEntry = zipReader.getNextZipEntry()) != null)
      {
        if (zipEntry.isDirectory())
        {
          if (!extractDirectoryFromZipFile(zipEntry, destinationPath))
          {
            return false;
          }
        }
        else
        {
          if (!extractFileFromZipFile(zipReader, zipEntry, readBuffer, destinationPath))
          {
            return false;
          }
        }
        // zipReader.closeEntry();
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
  private static boolean extractDirectoryFromZipFile(ZipArchiveEntry zipEntry, String currentPath)
  {
    File directory = new File(currentPath + File.separatorChar + zipEntry.getName());
    if (directory.exists() && directory.isDirectory())
    {
      //may treat timestamps here
    }
    if (directory.mkdirs())
    {
      //may treat timestamps here
      return true;
    }
    else
    {
      return false;
    }
  }

  // @SuppressWarnings("resource")
  @SuppressWarnings("all")
  private static boolean extractFileFromZipFile(ZipArchiveInputStream zipReader, ZipArchiveEntry zipEntry, final byte[] readBuffer, String currentPath) throws IOException
  {
    File directory = new File(currentPath);
    if (!directory.exists())
    {
      if (!directory.mkdirs())
      {
        return false;
      }
    }
    File tempFile = new File(currentPath + File.separatorChar + zipEntry.getName() + ".tmp");
    File finalFile = new File(currentPath + File.separatorChar + zipEntry.getName());
    if (tempFile.exists())
    {
      if (!tempFile.isFile())
      {
        return false;
      }
    }
    else
    {
      if (tempFile.getParentFile() != null)
      {
        tempFile.getParentFile().mkdirs();
      }
      if (!tempFile.createNewFile())
      {
        if (!tempFile.delete() || !tempFile.createNewFile())
        {
          return false;
        }
      }
    }
    
    FileOutputStream fileOutputStream = null;
    try
    {
      // fileStream = Channels.newOutputStream(new
      // FileOutputStream(file).getChannel());
      fileOutputStream = new FileOutputStream(tempFile);
      int readBytes;
      int writtenBytes = 0;
      while ((readBytes = zipReader.read(readBuffer, 0, readBuffer.length)) > 0)
      {
        fileOutputStream.write(readBuffer, 0, readBytes);
        writtenBytes += readBytes;
        if (writtenBytes >= readBuffer.length)
        {
          fileOutputStream.flush();
          writtenBytes = 0;
        }
      }
      fileOutputStream.flush();
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
      if (finalFile.delete())
      {
        if (!tempFile.renameTo(finalFile))
        {
          return false;
        }
        else
        {
          //may treat timestamps here
          return true;
        }
      }
      else
      {
        return false;
      }
    }
    //may treat timestamps here
    return true;
  }

  public static boolean createZipOutputStream(OutputStream output, int level, int strategy, final byte[] readBuffer, String... sourcePaths) throws IOException
  {
    if (sourcePaths == null || sourcePaths.length == 0)
    {
      return false;
    }
    // OutputStream fileStream = null;
    ZipArchiveOutputStream zipWriter = null;
    try
    {
      // fileStream = Channels.newOutputStream(new
      // FileOutputStream(zipArchive).getChannel());
      // zipWriter = new ZipArchiveOutputStream(fileStream);
      zipWriter = new ZipArchiveOutputStream(output);
      zipWriter.setLevel(level);
      zipWriter.setStrategy(strategy);
      zipWriter.setUseZip64(Zip64Mode.AsNeeded);
      // zipWriter.setCreateUnicodeExtraFields(UnicodeExtraFieldPolicy.ALWAYS);
      for (String contentPath : sourcePaths)
      {
        File nextFile = new File(contentPath).getAbsoluteFile();
        if (nextFile.exists())
        {
          if (nextFile.isFile())
          {
            if (!addFileToZipOutputStream(zipWriter, nextFile, readBuffer, ""))
            {
              return false;
            }
          }
          else if (nextFile.isDirectory())
          {
            if (!addDirectoryToZipOutputStream(zipWriter, nextFile, readBuffer, nextFile.getName()))
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
      if (error)
      {
        return false;
      }
    }
    return true;
  }

  private static boolean addDirectoryToZipOutputStream(ZipArchiveOutputStream zipWriter, File directory, final byte[] readBuffer, String currentPath) throws IOException
  {
    if (!directory.canRead())
    {
      return false;
    }
    ZipArchiveEntry directoryEntry = new ZipArchiveEntry(currentPath + '/');
    //may treat timestamps here
    zipWriter.putArchiveEntry(directoryEntry);
    zipWriter.closeArchiveEntry();
    zipWriter.flush();
    for (File child : directory.listFiles())
    {
      if (child.isFile())
      {
        if (!addFileToZipOutputStream(zipWriter, child, readBuffer, currentPath + '/'))
        {
          return false;
        }
      }
      else if (child.isDirectory())
      {
        if (!addDirectoryToZipOutputStream(zipWriter, child, readBuffer, currentPath + '/' + child.getName()))
        {
          return false;
        }
      }
    }
    return true;
  }

  // @SuppressWarnings("resource")
  @SuppressWarnings("all")
  private static boolean addFileToZipOutputStream(ZipArchiveOutputStream zipWriter, File file, final byte[] readBuffer, String currentPath) throws IOException
  {
    FileInputStream fileInputStream = null;
    try
    {
      if (!file.canRead())
      {
        return false;
      }
      // fileInputStream = Channels.newInputStream(new
      // FileInputStream(file).getChannel());
      fileInputStream = new FileInputStream(file);
      ZipArchiveEntry zipEntry = new ZipArchiveEntry(currentPath + file.getName());
      //may treat timestamps here
      // fileEntry.setSize(file.length());
      zipWriter.putArchiveEntry(zipEntry);
      int readBytes;
      while ((readBytes = fileInputStream.read(readBuffer)) > 0)
      {
        zipWriter.write(readBuffer, 0, readBytes);
        zipWriter.flush();
      }
      zipWriter.closeArchiveEntry();
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
  public static boolean extractZipInputStream(InputStream input, final byte[] readBuffer, String destinationPath) throws IOException
  {
    ZipArchiveInputStream zipReader = null;
    try
    {
      // fileStream = Channels.newInputStream(new
      // FileInputStream(file).getChannel());
      zipReader = new ZipArchiveInputStream(input);
      ZipArchiveEntry zipEntry;
      while ((zipEntry = zipReader.getNextZipEntry()) != null)
      {
        if (zipEntry.isDirectory())
        {
          if (!extractDirectoryFromZipStream(zipEntry, destinationPath))
          {
            return false;
          }
        }
        else
        {
          if (!extractFileFromZipStream(zipReader, zipEntry, readBuffer, destinationPath))
          {
            return false;
          }
        }
        // zipReader.closeEntry();
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
  private static boolean extractDirectoryFromZipStream(ZipArchiveEntry zipEntry, String currentPath)
  {
    File directory = new File(currentPath + File.separatorChar + zipEntry.getName());
    if (directory.exists() && directory.isDirectory())
    {
      return true;
    }
    if (directory.mkdirs())
    {
      //may treat timestamps here
      return true;
    }
    else
    {
      return false;
    }
  }

  // @SuppressWarnings("resource")
  @SuppressWarnings("all")
  private static boolean extractFileFromZipStream(ZipArchiveInputStream zipReader, ZipArchiveEntry zipEntry, final byte[] readBuffer, String currentPath) throws IOException
  {
    File directory = new File(currentPath);
    if (!directory.exists())
    {
      if (!directory.mkdirs())
      {
        return false;
      }
    }
    File tempFile = new File(currentPath + File.separatorChar + zipEntry.getName() + ".tmp");
    File finalFile = new File(currentPath + File.separatorChar + zipEntry.getName());
    if (tempFile.exists())
    {
      if (!tempFile.isFile())
      {
        return false;
      }
    }
    else
    {
      if (tempFile.getParentFile() != null)
      {
        tempFile.getParentFile().mkdirs();
      }
      if (!tempFile.createNewFile())
      {
        if (!tempFile.delete() || !tempFile.createNewFile())
        {
          return false;
        }
      }
    }
    
    FileOutputStream fileOutputStream = null;
    try
    {
      // fileStream = Channels.newOutputStream(new
      // FileOutputStream(file).getChannel());
      fileOutputStream = new FileOutputStream(tempFile);
      int readBytes;
      int writtenBytes = 0;
      while ((readBytes = zipReader.read(readBuffer, 0, readBuffer.length)) > 0)
      {
        fileOutputStream.write(readBuffer, 0, readBytes);
        writtenBytes += readBytes;
        if (writtenBytes >= readBuffer.length)
        {
          fileOutputStream.flush();
          writtenBytes = 0;
        }
      }
      fileOutputStream.flush();
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
      if (finalFile.delete())
      {
        if (!tempFile.renameTo(finalFile))
        {
          return false;
        }
        else
        {
          //may treat timestamps here
          return true;
        }
      }
      else
      {
        return false;
      }
    }
    //may treat timestamps here
    return true;
  }

  public static boolean createTarOutputStream(OutputStream output, final byte[] readBuffer, String... sourcePaths) throws IOException
  {
    // System.out.println("createTarOutputStream: " +
    // Arrays.toString(sourcePaths));
    if (sourcePaths == null || sourcePaths.length == 0)
    {
      return false;
    }
    // OutputStream fileStream = null;
    TarArchiveOutputStream tarWriter = null;
    try
    {
      // fileStream = Channels.newOutputStream(new
      // FileOutputStream(zipArchive).getChannel());
      // zipWriter = new ZipArchiveOutputStream(fileStream);
      tarWriter = new TarArchiveOutputStream(output);
      tarWriter.setAddPaxHeadersForNonAsciiNames(true);
      tarWriter.setLongFileMode(TarArchiveOutputStream.LONGFILE_POSIX);
      tarWriter.setBigNumberMode(TarArchiveOutputStream.BIGNUMBER_POSIX);
      for (String contentPath : sourcePaths)
      {
        File nextFile = new File(contentPath).getAbsoluteFile();
        if (nextFile.exists())
        {
          if (nextFile.isFile())
          {
            if (!addFileToTarOutputStream(tarWriter, nextFile, readBuffer, ""))
            {
              return false;
            }
          }
          else if (nextFile.isDirectory())
          {
            if (!addDirectoryToTarOutputStream(tarWriter, nextFile, readBuffer, nextFile.getName()))
            {
              return false;
            }
          }
        }
      }
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    finally
    {
      boolean error = false;
      try
      {
        if (tarWriter != null)
        {
          tarWriter.flush();
          tarWriter.finish();
          tarWriter.close();
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
      if (error)
      {
        return false;
      }
    }
    return true;
  }

  private static boolean addDirectoryToTarOutputStream(TarArchiveOutputStream tarWriter, File directory, final byte[] readBuffer, String currentPath) throws IOException
  {
    // System.out.println("addDirectoryToTarOutputStream: " +
    // directory.toString() +
    // " " + currentPath);
    if (!directory.canRead())
    {
      return false;
    }
    TarArchiveEntry directoryEntry = new TarArchiveEntry(currentPath + '/');
    if (directory.lastModified() != 0)
    {
      //may treat timestamps here
    }
    tarWriter.putArchiveEntry(directoryEntry);
    tarWriter.closeArchiveEntry();
    tarWriter.flush();
    for (File child : directory.listFiles())
    {
      if (child.isFile())
      {
        if (!addFileToTarOutputStream(tarWriter, child, readBuffer, currentPath + '/'))
        {
          return false;
        }
      }
      else if (child.isDirectory())
      {
        if (!addDirectoryToTarOutputStream(tarWriter, child, readBuffer, currentPath + '/' + child.getName()))
        {
          return false;
        }
      }
    }
    return true;
  }

  // @SuppressWarnings("resource")
  @SuppressWarnings("all")
  private static boolean addFileToTarOutputStream(TarArchiveOutputStream tarWriter, File file, final byte[] readBuffer, String currentPath) throws IOException
  {
    // System.out.println("addFileToTarOutputStream: " + file.toString() + "
    // " +
    // currentPath);
    FileInputStream fileInputStream = null;
    try
    {
      if (!file.canRead())
      {
        return false;
      }
      fileInputStream = new FileInputStream(file);
      TarArchiveEntry tarEntry = new TarArchiveEntry(currentPath + file.getName());
      long fileSize = file.length();
      
      tarEntry.setSize(fileSize);
      tarEntry.setModTime(file.lastModified());
      
      tarWriter.putArchiveEntry(tarEntry);
      int readBytes;
      long remaining = fileSize;
      while ((readBytes = fileInputStream.read(readBuffer, 0, (int) Math.min(remaining, readBuffer.length))) > 0)
      {
        tarWriter.write(readBuffer, 0, readBytes);
        tarWriter.flush();
        remaining -= readBytes;
      }
      tarWriter.closeArchiveEntry();
      tarWriter.flush();
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
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
  public static boolean extractTarInputStream(InputStream input, final byte[] readBuffer, String destinationPath) throws IOException
  {
    // System.out.println("extractTarInputStream: " + destinationPath);
    TarArchiveInputStream tarReader = null;
    try
    {
      tarReader = new TarArchiveInputStream(input);
      TarArchiveEntry tarEntry;
      while ((tarEntry = tarReader.getNextTarEntry()) != null)
      {
        if (tarEntry.isDirectory())
        {
          if (!extractDirectoryFromTarInputStream(tarEntry, destinationPath))
          {
            return false;
          }
        }
        else
        {
          if (!extractFileFromTarInputStream(tarReader, tarEntry, readBuffer, destinationPath))
          {
            return false;
          }
        }
        // zipReader.closeEntry();
      }
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
    finally
    {
      if (tarReader != null)
      {
        tarReader.close();
      }
    }
    return true;
  }

  @SuppressWarnings("all")
  private static boolean extractDirectoryFromTarInputStream(TarArchiveEntry tarEntry, String currentPath)
  {
    // System.out.println("extractDirectoryFromTarInputStream: " +
    // tarEntry.getName().toString() + " " + currentPath);
    File directory = new File(currentPath + File.separatorChar + tarEntry.getName());
    if (directory.exists() && directory.isDirectory())
    {
      directory.setLastModified(tarEntry.getModTime().getTime());
      return true;
    }
    if (directory.mkdirs())
    {
      directory.setLastModified(tarEntry.getModTime().getTime());
      return true;
    }
    else
    {
      return false;
    }
  }

  // @SuppressWarnings("resource")
  @SuppressWarnings("all")
  private static boolean extractFileFromTarInputStream(TarArchiveInputStream tarReader, TarArchiveEntry tarEntry, final byte[] readBuffer, String currentPath) throws IOException
  {
    // System.out.println("extractFileFromTarInputStream: " +
    // tarEntry.getName().toString() + " " + currentPath);
    File directory = new File(currentPath);
    if (!directory.exists())
    {
      if (!directory.mkdirs())
      {
        return false;
      }
    }
    File tempFile = new File(currentPath + File.separatorChar + tarEntry.getName() + ".tmp");
    File finalFile = new File(currentPath + File.separatorChar + tarEntry.getName());
    if (tempFile.exists())
    {
      if (!tempFile.isFile())
      {
        return false;
      }
    }
    else
    {
      if (tempFile.getParentFile() != null)
      {
        tempFile.getParentFile().mkdirs();
      }
      if (!tempFile.createNewFile())
      {
        if (!tempFile.delete() || !tempFile.createNewFile())
        {
          return false;
        }
      }
    }

    FileOutputStream fileOutputStream = null;
    try
    {
      fileOutputStream = new FileOutputStream(tempFile);
      int readBytes;
      int writtenBytes = 0;
      while ((readBytes = tarReader.read(readBuffer, 0, readBuffer.length)) > 0)
      {
        fileOutputStream.write(readBuffer, 0, readBytes);
        writtenBytes += readBytes;
        if (writtenBytes >= readBuffer.length)
        {
          fileOutputStream.flush();
          writtenBytes = 0;
        }
      }
      fileOutputStream.flush();
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
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
      if (finalFile.delete())
      {
        if (!tempFile.renameTo(finalFile))
        {
          return false;
        }
        else
        {
          //may treat timestamps here
          return true;
        }
      }
      else
      {
        return false;
      }
    }
    finalFile.setLastModified(tarEntry.getModTime().getTime());
    return true;
  }

  /*
   * public static void main(String[] args) throws IOException {
   * System.out.println(VTZipUtils.createZipFile("zip-test.zip", -1, new byte[64 *
   * 1024],"dist")); System.out.println(VTZipUtils.extractZipFile("zip-test.zip",
   * new byte[64 * 1024],"aua")); }
   */
}