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
  }
  
  public void task()
  {
    synchronized (this)
    {
      try
      {
        if (session.isRestartingShell())
        {
          session.setRestartingShell(false);
          session.getConnection().getResultWriter().write("\nVT>Remote shell started!\nVT>\n");
          session.getConnection().getResultWriter().flush();
        }
        else
        {
          session.getConnection().getResultWriter().write("\nVT>Remote shell started!" + "\nVT>Enter *VTHELP or *VTHL to list available commands in client console\nVT>\n");
          session.getConnection().getResultWriter().flush();
        }
        session.getShellProcessor().waitFor();
      }
      catch (Throwable e)
      {
        
      }
      setStopped(true);
      try
      {
        if (session.isStoppingShell())
        {
          session.setStoppingShell(false);
        }
        session.getConnection().getResultWriter().write("\nVT>Remote shell stopped!\nVT>");
        session.getConnection().getResultWriter().flush();
      }
      catch (IOException e)
      {
        
      }
    }
  }
}