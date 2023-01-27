package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTHOSTCHECK extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTHOSTCHECK()
  {
    this.setFullName("*VTHOSTCHECK");
    this.setAbbreviatedName("*VTHTC");
    this.setFullSyntax("*VTHOSTCHECK <HOST>");
    this.setAbbreviatedSyntax("*VTHTC <HT>");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getHostResolver())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (parsed.length >= 2)
      {
        if (session.getHostResolver().isFinished())
        {
          session.getHostResolver().joinThread();
        }
        if (!session.getHostResolver().aliveThread())
        {
          session.getHostResolver().setFinished(false);
          session.getHostResolver().setHost(parsed[1]);
          session.getHostResolver().startThread();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Another network host resolution is still running!\nVT>");
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
