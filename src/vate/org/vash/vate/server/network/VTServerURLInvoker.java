package org.vash.vate.server.network;

import java.io.FileOutputStream;
import java.io.OutputStream;
import org.vash.vate.network.url.VTURLInvoker;
import org.vash.vate.network.url.VTURLResult;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerURLInvoker extends VTTask
{
  private volatile boolean finished;
  private String url;
  private String file;
  private VTServerSession session;
  //private StringBuilder message;
  private VTURLInvoker invoker;
  
  public VTServerURLInvoker(VTServerSession session)
  {
    this.session = session;
    //this.message = new StringBuilder();
    this.finished = true;
    this.invoker = new VTURLInvoker();
  }
  
  public boolean isFinished()
  {
    return finished;
  }

  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }

  public void setURL(String url)
  {
    this.url = url;
  }
  
  public void setFile(String file)
  {
    this.file = file;
  }
  
  public void run()
  {
    try
    {
      //message.setLength(0);
      OutputStream resultStream = null;
      session.getConnection().getResultWriter().write("VT>Attempting URL Data Transfer URL:[" + url + "]\n");
      session.getConnection().getResultWriter().flush();
      
      if (file != null)
      {
        resultStream = new FileOutputStream(file, true);
      }
      else
      {
        resultStream = session.getConnection().getShellOutputStream();
      }
      
      VTURLResult result = invoker.invokeURL(url, null, 0, 0, resultStream);
      
      if (file != null)
      {
        resultStream.close();
      }
      
      synchronized (this)
      {
        if (result.getError() != null)
        {
          session.getConnection().getResultWriter().write("VT>Failed URL Data Transfer Error:[" + result.getError().getMessage() + "]\n");
          session.getConnection().getResultWriter().flush();
        }
        else
        {
          session.getConnection().getResultWriter().write("VT>Finished URL Data Transfer Status:[" + result.getResponse() + "]\n");
          session.getConnection().getResultWriter().flush();
          finished = true;
        }
        
      }
    }
    catch (Throwable e)
    {
      
    }
    finished = true;
  }
  
  public void close()
  {
    invoker.close();
    try
    {
      super.close();
    }
    catch (Throwable t)
    {
      
    }
  }
}
