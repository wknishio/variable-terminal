package org.vash.vate.server.filesystem;

import java.io.File;
import java.io.IOException;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerFileScanOperation extends VTTask
{
  public static final int INFO_FILE = 1;
  public static final int LIST_FILES = 2;
  
  private boolean finished;
  private int operation;
  private File target;
  private StringBuilder message;
  private VTServerSession session;
  //private final Comparator<File> fileSorter = new VTFileSeekSorter();
  
  public VTServerFileScanOperation(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.message = new StringBuilder();
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
  
  public void setTarget(File target)
  {
    this.target = target;
    if (!target.isAbsolute())
    {
      this.target = new File(target.getPath());
    }
  }
  
  public void task()
  {
    try
    {
      if (operation == INFO_FILE)
      {
        try
        {
          if (target.exists())
          {
            if (target.isFile())
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\nVT>Information about file: [" + target.getPath() + "]" + "\nVT>" + "\nVT>Type: [File]" + "\nVT>Name: [" + target.getName() + "]" + "\nVT>Path: [" + target.getPath() + "]" + "\nVT>Absolute path: [" + target.getAbsolutePath() + "]" + "\nVT>Canonical path: [" + target.getCanonicalPath() + "]" + "\nVT>File size: [" + target.length() + "] bytes" + "\nVT>" + "\nVT>End of information about file: [" + target.getPath() + "]" + "\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            else if (target.isDirectory())
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\nVT>Information about file: [" + target.getPath() + "]" + "\nVT>" + "\nVT>Type: [Directory]" + "\nVT>Name: [" + target.getName() + "]" + "\nVT>Path: [" + target.getPath() + "]" + "\nVT>Absolute path: [" + target.getAbsolutePath() + "]" + "\nVT>Canonical path: [" + target.getCanonicalPath() + "]" + "\nVT>Total of files in directory: [" + target.list().length + "] files" + "\nVT>" + "\nVT>End of information about file: [" + target.getPath() + "]" + "\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>File [" + target.getPath() + "] not found on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (SecurityException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Security error detected!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (IOException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>File [" + target.getPath() + "] inspection on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (NullPointerException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>File [" + target.getPath() + "] inspection on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      
      if (operation == LIST_FILES)
      {
        try
        {
          if (target.exists())
          {
            if (target.isDirectory())
            {
              message.setLength(0);
              message.append("\nVT>List of files in directory [" + target.getPath() + "]:\nVT>");
              File[] files = target.listFiles();
             // Arrays.sort(files, fileSorter);
              for (File file : files)
              {
                message.append("\nVT>" + (file.isFile() ? "File" : "Directory") + ": [" + file.getName() + "]");
              }
              message.append("\nVT>\nVT>End of list of files in directory [" + target.getPath() + "]");
              message.append("\nVT>Total of files in directory: [" + files.length + "]\nVT>");
              synchronized (this)
              {
                session.getConnection().getResultWriter().write(message.toString());
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            else if (target.isFile())
            {
              synchronized (this)
              {
                session.getConnection().getResultWriter().write("\nVT>File [" + target.getPath() + "] is not a directory on server!\nVT>");
                session.getConnection().getResultWriter().flush();
                finished = true;
              }
            }
            /*
             * else { connection.getResultWriter().
             * write("\nVT>File of unknown type detected!\nVT>");
             * connection.getResultWriter().flush(); }
             */
          }
          else
          {
            synchronized (this)
            {
              session.getConnection().getResultWriter().write("\nVT>Directory [" + target.getPath() + "] not found on server!\nVT>");
              session.getConnection().getResultWriter().flush();
              finished = true;
            }
          }
        }
        catch (SecurityException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Security error detected!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (IOException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>File [" + target.getPath() + "] inspection on server failed!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        catch (NullPointerException e)
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>File [" + target.getPath() + "] inspection on server failed!\nVT>");
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