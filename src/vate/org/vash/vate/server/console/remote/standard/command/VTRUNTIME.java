package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTRUNTIME extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTRUNTIME()
  {
    this.setFullName("*VTRUNTIME");
    this.setAbbreviatedName("*VTRT");
    this.setFullSyntax("*VTRUNTIME <MODE> [.]");
    this.setAbbreviatedSyntax("*VTRT <MD> [.]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getRuntimeExecutor())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (parsed.length > 1)
      {
        if (session.getRuntimeExecutor().isFinished())
        {
          session.getRuntimeExecutor().joinThread();
        }
        if (!session.getRuntimeExecutor().aliveThread())
        {
          session.getRuntimeExecutor().setFinished(false);
          session.getRuntimeExecutor().setCommand(command);
          session.getRuntimeExecutor().startThread();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Another runtime execution is still running!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
  }
  
  public void close()
  {
    
  }
}
