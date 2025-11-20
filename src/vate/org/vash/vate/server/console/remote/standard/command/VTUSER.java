package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTUSER extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTUSER()
  {
    this.setFullName("*VTUSER");
    this.setAbbreviatedName("*VTUS");
    this.setFullSyntax("*VTUSER");
    this.setAbbreviatedSyntax("*VTUS");
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
        connection.getResultWriter().write("\rVT>Another server connection list view is still running!\nVT>");
        connection.getResultWriter().flush();
      }
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
