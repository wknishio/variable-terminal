package org.vash.vate.server.opticaldrive;

import org.vash.vate.nativeutils.VTNativeUtils;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerOpticalDriveOperation extends VTTask
{
  private volatile boolean finished;
  private boolean open;
  private VTServerSession session;

  public VTServerOpticalDriveOperation(VTServerSession session)
  {
    this.session = session;
    this.finished = true;
  }

  public void setOpen(boolean open)
  {
    this.open = open;
  }

  public boolean isFinished()
  {
    return finished;
  }

  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }

  public void run()
  {
    try
    {
      if (open)
      {
        if (VTNativeUtils.openCD())
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Optical disc drive opened on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        else
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Optical disc drive has not opened on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
      else
      {
        if (VTNativeUtils.closeCD())
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Optical disc drive closed on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
        else
        {
          synchronized (this)
          {
            session.getConnection().getResultWriter().write("\nVT>Optical disc drive has not closed on server!\nVT>");
            session.getConnection().getResultWriter().flush();
            finished = true;
          }
        }
      }
    }
    catch (Throwable e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\nVT>Optical disc drive operation failed on server!\nVT>");
          session.getConnection().getResultWriter().flush();
        }
        catch (Throwable t)
        {
          
        }
      }
      finished = true;
    }
    finished = true;
  }
}