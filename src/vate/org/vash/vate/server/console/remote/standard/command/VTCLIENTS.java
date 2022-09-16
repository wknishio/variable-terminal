package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTCLIENTS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTCLIENTS()
  {
    this.setFullName("*VTCLIENTS");
    this.setAbbreviatedName("*VTCLS");
    this.setFullSyntax("*VTCLIENTS");
    this.setAbbreviatedSyntax("*VTCLS");
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
