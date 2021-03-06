package org.vate.server.filesystem;

import java.io.File;
import java.io.IOException;
// import java.net.InetAddress;
// import java.net.NetworkInterface;
// import java.util.Enumeration;

import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTServerFileSystemRootsResolver extends VTTask
{
  private volatile boolean finished;
  private StringBuilder message;
  private VTServerSession session;

  public VTServerFileSystemRootsResolver(VTServerSession session)
  {
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

  public void run()
  {
    try
    {
      message.setLength(0);
      File[] roots = File.listRoots();
      message.append("\nVT>List of file system roots on server:\nVT>");
      for (File root : roots)
      {
        message.append("\nVT>Canonical path: [" + root.getCanonicalPath() + "]");
      }
      message.append("\nVT>\nVT>End of file system roots list\nVT>");
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