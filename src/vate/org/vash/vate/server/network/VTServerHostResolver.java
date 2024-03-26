package org.vash.vate.server.network;

import java.io.IOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

public class VTServerHostResolver extends VTTask
{
  private boolean finished;
  private String host;
  private VTServerSession session;
  private StringBuilder message;
  
  public VTServerHostResolver(VTServerSession session)
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
  
  public void setHost(String host)
  {
    this.host = host;
  }
  
  public void task()
  {
    try
    {
      message.setLength(0);
      InetAddress[] addresses = InetAddress.getAllByName(host);
      message.append("\nVT>List of host addresses for host [" + host + "]:\nVT>");
      for (InetAddress address : addresses)
      {
        message.append("\nVT>Host address: [" + address.getHostAddress() + "]" + "\nVT>Host name: [" + address.getCanonicalHostName() + "]\nVT>");
      }
      message.append("\nVT>End of host addresses list\nVT>");
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
    catch (UnknownHostException e)
    {
      synchronized (this)
      {
        try
        {
          session.getConnection().getResultWriter().write("\nVT>Host [" + host + "] not found!\nVT>");
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