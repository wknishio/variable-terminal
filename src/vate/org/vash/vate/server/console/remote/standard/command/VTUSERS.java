package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTUSERS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTUSERS()
  {
    this.setFullName("*VTUSERS");
    this.setAbbreviatedName("*VTUSR");
    this.setFullSyntax("*VTUSERS");
    this.setAbbreviatedSyntax("*VTUSR");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getConnectionListViewer())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (session.getConnectionListViewer().isFinished())
      {
        session.getConnectionListViewer().joinThread();
      }
      if (!session.getConnectionListViewer().aliveThread())
      {
        session.getConnectionListViewer().setFinished(false);
        session.getConnectionListViewer().startThread();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Another server connection list view is still running!\nVT>");
        connection.getResultWriter().flush();
      }
    }
  }
  
  public void close()
  {
    
  }
}
