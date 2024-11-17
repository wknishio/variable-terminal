package org.vash.vate.server.session;

import java.io.IOException;
import java.net.InetAddress;
import java.util.Collection;

import org.vash.vate.server.connection.VTServerConnectionHandler;
import org.vash.vate.task.VTTask;

public class VTServerSessionListViewer extends VTTask
{
  private boolean finished;
  private VTServerSession session;
  private StringBuilder message;
  
  public VTServerSessionListViewer(VTServerSession session)
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
      int i = 0;
      Collection<VTServerConnectionHandler> connections = session.getServer().getServerConnector().getConnectionHandlers();
      message.append("\nVT>List of client connections with server:\nVT>");
      for (VTServerConnectionHandler handler : connections)
      {
        message.append("\nVT>Number: [" + i++ + "]");
        //message.append("\nVT>Authenticated: [" + (handler.getSessionHandler().isAuthenticated() ? "Yes" : "No") + "]");
        message.append("\nVT>User: [" + (handler.getSessionHandler().getUser() != null ? handler.getSessionHandler().getUser() : "") + "]");
        InetAddress address = handler.getConnection().getConnectionSocket().getInetAddress();
        if (address != null)
        {
          message.append("\nVT>Host address: [" + address.getHostAddress() + "]\nVT>");
          // "\nVT>Host name: [" + address.getCanonicalHostName() + "]\nVT>");
          // "]\nVT>Host name: [" + address.getHostName()
          // +
          // "]\nVT>Canonical host name: [" +
          // address.getCanonicalHostName() +
          // "]\nVT>");
        }
      }
      message.append("\nVT>End of client connections with server list\nVT>");
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