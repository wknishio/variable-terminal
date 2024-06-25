package org.vash.vate.server.network;

import java.io.ByteArrayInputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;
import org.vash.vate.url.VTURLInvoker;
import org.vash.vate.url.VTURLResult;

public class VTServerURLInvoker extends VTTask
{
  private boolean finished;
  private String url;
  private String fileResult;
  private String fileOutput;
  private VTServerSession session;
  // private StringBuilder message;
  //private VTURLInvoker invoker;
  
  public VTServerURLInvoker(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    // this.message = new StringBuilder();
    this.finished = true;
    //this.invoker = new VTURLInvoker();
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
  
  public void setFileResult(String fileResult)
  {
    this.fileResult = fileResult;
  }
  
  public void setFileOutput(String fileOutput)
  {
    this.fileOutput = fileOutput;
  }
  
  public void task()
  {
    try
    {
      // message.setLength(0);
      OutputStream resultOutputStream = null;
      InputStream outputInputStream = null;
      session.getConnection().getResultWriter().write("VT>Attempting URL Data Transfer URL:[" + url + "]\n");
      session.getConnection().getResultWriter().flush();
      
      if (fileResult != null)
      {
        resultOutputStream = new FileOutputStream(fileResult);
      }
      else
      {
        resultOutputStream = session.getConnection().getShellOutputStream();
      }
      
      if (fileOutput != null)
      {
        try
        {
          outputInputStream = new FileInputStream(fileOutput);
        }
        catch (Throwable t)
        {
          try
          {
            outputInputStream = new ByteArrayInputStream(fileOutput.getBytes("UTF-8"));
          }
          catch (Throwable t1)
          {
            outputInputStream = null;
          }
        }
      }
      else
      {
        outputInputStream = null;
      }
      
      VTURLResult result = VTURLInvoker.invokeURL(url, null, 15000, 15000, null, null, outputInputStream, resultOutputStream);
      
      if (fileResult != null)
      {
        resultOutputStream.close();
      }
      
      if (fileOutput != null)
      {
        outputInputStream.close();
      }
      
      synchronized (this)
      {
        if (result.isFailed())
        {
          session.getConnection().getResultWriter().write("VT>Failed URL Data Transfer Error:[" + result.getResponse() + "]\n");
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
    try
    {
      super.close();
    }
    catch (Throwable t)
    {
      
    }
  }
}
