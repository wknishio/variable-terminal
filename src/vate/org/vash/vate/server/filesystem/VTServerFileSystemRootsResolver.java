package org.vash.vate.server.filesystem;

import java.io.File;
import java.io.IOException;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerFileSystemRootsResolver extends VTTask
{
  private boolean finished;
  private StringBuilder message;
  private VTServerSession session;
  
  public VTServerFileSystemRootsResolver(VTServerSession session)
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
  
  public void task()
  {
    try
    {
      message.setLength(0);
      File[] roots = File.listRoots();
      message.append("\nVT>List of server file roots:\nVT>");
      for (File root : roots)
      {
        message.append("\nVT>" + (root.isFile() ? "File" : "Directory") + ": [" + root.getName() + "]");
      }
      message.append("\nVT>\nVT>End of server file roots list\nVT>");
      synchronized (this)
      {
        session.getConnection().getResultWriter().write(message.toString());
        session.getConnection().getResultWriter().flush();
        finished = true;
      }
    }
    catch (SecurityException e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\nVT>Security error detected!\nVT>");
          session.getConnection().getResultWriter().flush();
        }
        catch (IOException e1)
        {
          
        }
        finished = true;
      }
    }
    catch (Throwable e)
    {
      
    }
    finished = true;
  }
}