package org.vash.vate.server.console.shell;

import java.io.IOException;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerShellExitListener extends VTTask
{
  private VTServerSession session;
  
  public VTServerShellExitListener(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.stopped = false;
  }
  
  public boolean isStopped()
  {
    return stopped;
  }
  
  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }
  
  public void task()
  {
    try
    {
      session.getShell().waitFor();
    }
    catch (Throwable e)
    {
      
    }
    stopped = true;
    synchronized (this)
    {
      if (session.getShell() != null)
      {
        try
        {
          if (session.isStoppingShell())
          {
            session.setStoppingShell(false);
            session.getConnection().getResultWriter().write("\nVT>Remote shell stopped!\nVT>");
            session.getConnection().getResultWriter().flush();
          }
          else if (session.isRestartingShell())
          {
            session.getConnection().getResultWriter().write("\nVT>Remote shell stopped!\nVT>");
            session.getConnection().getResultWriter().flush();
          }
          else
          {
            session.getConnection().getResultWriter().write("\nVT>Remote shell stopped!\nVT>");
            session.getConnection().getResultWriter().flush();
          }
        }
        catch (IOException e)
        {
          
        }
      }
    }
//    try
//    {
//      synchronized (session.getShell())
//      {
//        session.getShell().notify();
//      }
//    }
//    catch (Throwable e)
//    {
//      
//    }
  }
}