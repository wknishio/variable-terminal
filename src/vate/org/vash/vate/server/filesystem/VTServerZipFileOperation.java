package org.vash.vate.server.filesystem;

import java.io.File;
import java.nio.channels.ClosedByInterruptException;
import java.util.zip.Deflater;

import org.vash.vate.VT;
import org.vash.vate.filesystem.VTZipUtils;
//import org.vash.vate.filesystem.VTArchiveUtils;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerZipFileOperation extends VTTask
{
  private static final int fileZipBufferSize = VT.VT_FILE_BUFFER_SIZE_BYTES;
  private boolean finished;
  private int operation;
  private String zipFilePath;
  private String[] sourcePaths;
  private final byte[] readBuffer;
  private VTServerSession session;
  
  public VTServerZipFileOperation(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
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
  
  public void task()
  {
    try
    {
      if (operation == VT.VT_ZIP_FILE_COMPRESS)
      {
        try
        {
          if (VTZipUtils.createZipFile(zipFilePath, Deflater.BEST_SPEED, Deflater.DEFAULT_STRATEGY, readBuffer, sourcePaths))
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] compressed on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] compression on server failed!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] compression on server interrupted!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] compression on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      else if (operation == VT.VT_ZIP_FILE_UNCOMPRESS)
      {
        try
        {
          if (VTZipUtils.createZipFile(zipFilePath, Deflater.NO_COMPRESSION, Deflater.DEFAULT_STRATEGY, readBuffer, sourcePaths))
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] uncompressed on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] uncompression on server failed!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] uncompression on server interrupted!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] uncompression on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      else
      {
        try
        {
          if (VTZipUtils.extractZipFile(zipFilePath, readBuffer, sourcePaths[0]))
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] decompressed to [" + sourcePaths[0] + "] on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] decompression on server failed!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] decompression on server interrupted!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Zip file [" + zipFilePath + "] decompression on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
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