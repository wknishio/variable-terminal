package org.vash.vate.server.filesystem;

import java.io.File;
// import java.io.FileInputStream;
import java.io.FileNotFoundException;
// import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedByInterruptException;

import org.vash.vate.filesystem.VTFileUtils;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerFileModifyOperation extends VTTask
{
  public static final int MOVE_FILE = 1;
  public static final int COPY_FILE = 2;
  public static final int REMOVE_FILE = 3;
  public static final int CREATE_FILE = 4;
  public static final int CREATE_DIRECTORY = 5;
  
  private boolean finished;
  private int operation;
  private File sourceFile;
  private File destinationFile;
  private VTServerSession session;
  
  public VTServerFileModifyOperation(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.finished = true;
  }
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  public void setOperation(int operation)
  {
    this.operation = operation;
  }
  
  public void setSourceFile(File sourceFile)
  {
    this.sourceFile = sourceFile;
    if (!sourceFile.isAbsolute())
    {
      this.sourceFile = new File(sourceFile.getPath());
    }
  }
  
  public void setDestinationFile(File destinationFile)
  {
    this.destinationFile = destinationFile;
    if (!destinationFile.isAbsolute())
    {
      this.destinationFile = new File(destinationFile.getPath());
    }
  }
  
  public void task()
  {
    try
    {
      if (operation == MOVE_FILE)
      {
        try
        {
          if (sourceFile.exists())
          {
            if (sourceFile.isFile())
            {
              VTFileUtils.moveFile(sourceFile, destinationFile);
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] moved to [" + destinationFile.getPath() + "] on server!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            else if (sourceFile.isDirectory())
            {
              VTFileUtils.moveDirectory(sourceFile, destinationFile);
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] moved to [" + destinationFile.getPath() + "] on server!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] not found on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] move to [" + destinationFile.getPath() + "] on server interrupted!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] move to [" + destinationFile.getPath() + "] on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      
      if (operation == COPY_FILE)
      {
        try
        {
          if (sourceFile.isFile())
          {
            VTFileUtils.copyFile(sourceFile, destinationFile, false);
          }
          else
          {
            VTFileUtils.copyDirectory(sourceFile, destinationFile, false);
          }
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] copied to [" + destinationFile.getPath() + "] on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (FileNotFoundException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] copy to [" + destinationFile.getPath() + "] on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (ClosedByInterruptException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] copy to [" + destinationFile.getPath() + "] on server interrupted!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (Throwable e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] copy to [" + destinationFile.getPath() + "] on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      
      if (operation == REMOVE_FILE)
      {
        try
        {
          if (sourceFile.exists())
          {
            if (VTFileUtils.truncateDeleteQuietly(sourceFile))
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] removed from server!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            else
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] removal on server failed!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] not found on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (IOException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] removal on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (NullPointerException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] removal on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      
      if (operation == CREATE_FILE)
      {
        try
        {
          if (!sourceFile.exists())
          {
            if (sourceFile.getParentFile() != null)
            {
              sourceFile.getParentFile().mkdirs();
            }
            if (sourceFile.createNewFile())
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] created on server!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            else
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] creation on server failed!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] already exists on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (IOException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] creation on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (NullPointerException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] creation on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      
      if (operation == CREATE_DIRECTORY)
      {
        try
        {
          if (!sourceFile.exists())
          {
            if (sourceFile.mkdirs())
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>Directory [" + sourceFile.getPath() + "] created on server!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            else
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\rVT>Directory [" + sourceFile.getPath() + "] creation on server failed!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\rVT>File [" + sourceFile.getPath() + "] already exists on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (IOException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>Directory [" + sourceFile.getPath() + "] creation on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (NullPointerException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\rVT>Directory [" + sourceFile.getPath() + "] creation on server failed!\nVT>");
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