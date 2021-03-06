package org.vate.client.filesystem;

import java.io.File;
import java.nio.channels.ClosedByInterruptException;
import java.util.zip.Deflater;

import org.vate.VT;
import org.vate.client.session.VTClientSession;
import org.vate.console.VTConsole;
import org.vate.filesystem.VTArchiveUtils;
import org.vate.task.VTTask;

public class VTClientZipFileOperation extends VTTask
{
  private static final int fileZipBufferSize = VT.VT_FILE_DATA_BUFFER_SIZE;
  private volatile boolean finished;
  private volatile int operation;
  private String zipFilePath;
  private String[] sourcePaths;
  private final byte[] readBuffer;
  // private VTClientSession session;

  public VTClientZipFileOperation(VTClientSession session)
  {
    // this.session = session;
    this.finished = true;
    this.readBuffer = new byte[fileZipBufferSize];
  }

  public void setOperation(int operation)
  {
    this.operation = operation;
  }

  public boolean isFinished()
  {
    return finished;
  }

  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }

  public void setZipFilePath(String zipFilePath)
  {
    this.zipFilePath = zipFilePath;
    File zipFile = new File(zipFilePath);
    if (!zipFile.isAbsolute())
    {
      this.zipFilePath = new File(zipFilePath).getAbsolutePath();
    }
  }

  public void setSourcePaths(String[] sourcePaths)
  {
    String[] checkedPaths = new String[sourcePaths.length];
    int i = 0;
    for (String sourcePath : sourcePaths)
    {
      File sourceFile = new File(sourcePath);
      checkedPaths[i] = sourceFile.getAbsolutePath();
      if (!sourceFile.isAbsolute())
      {
        checkedPaths[i] = new File(sourcePath).getAbsolutePath();
      }
      i++;
    }
    this.sourcePaths = checkedPaths;
  }

  public void run()
  {
    try
    {
      if (operation == VT.VT_ZIP_FILE_COMPRESS)
      {
        try
        {
          if (VTArchiveUtils.createZipFile(zipFilePath, Deflater.BEST_SPEED, Deflater.FILTERED, readBuffer, sourcePaths))
          {
            synchronized (this)
            {
              VTConsole.print("\nVT>Zip file [" + zipFilePath + "] compressed on client!\nVT>");
              finished = true;
            }
          }
          else
          {
            synchronized (this)
            {
              VTConsole.print("\nVT>Zip file [" + zipFilePath + "] compression on client failed!\nVT>");
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            VTConsole.print("\nVT>Zip file [" + zipFilePath + "] compression on client interrupted!\nVT>");
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            VTConsole.print("\nVT>Zip file [" + zipFilePath + "] compression on client failed!\nVT>");
            finished = true;
          }
        }
      }
      else if (operation == VT.VT_ZIP_FILE_UNCOMPRESS)
      {
        try
        {
          if (VTArchiveUtils.createZipFile(zipFilePath, Deflater.NO_COMPRESSION, Deflater.FILTERED, readBuffer, sourcePaths))
          {
            synchronized (this)
            {
              VTConsole.print("\nVT>Zip file [" + zipFilePath + "] uncompressed on client!\nVT>");
              finished = true;
            }
          }
          else
          {
            synchronized (this)
            {
              VTConsole.print("\nVT>Zip file [" + zipFilePath + "] uncompression on client failed!\nVT>");
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            VTConsole.print("\nVT>Zip file [" + zipFilePath + "] uncompression on client interrupted!\nVT>");
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            VTConsole.print("\nVT>Zip file [" + zipFilePath + "] uncompression on client failed!\nVT>");
            finished = true;
          }
        }
      }
      else
      {
        try
        {
          if (VTArchiveUtils.extractZipFile(zipFilePath, readBuffer, sourcePaths[0]))
          {
            synchronized (this)
            {
              VTConsole.print("\nVT>Zip file [" + zipFilePath + "] decompressed to [" + sourcePaths[0] + "] on client!\nVT>");
              finished = true;
            }
          }
          else
          {
            synchronized (this)
            {
              VTConsole.print("\nVT>Zip file [" + zipFilePath + "] decompression on client failed!\nVT>");
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            VTConsole.print("\nVT>Zip file [" + zipFilePath + "] decompression on client interrupted!\nVT>");
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            VTConsole.print("\nVT>Zip file [" + zipFilePath + "] decompression on client failed!\nVT>");
            finished = true;
          }
        }
      }
    }
    catch (Throwable e)
    {

    }
    finished = true;
  }
}