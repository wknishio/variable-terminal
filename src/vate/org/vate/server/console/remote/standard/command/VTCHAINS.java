package org.vate.server.console.remote.standard.command;

import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTCHAINS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTCHAINS()
  {
    this.setFullName("*VTCHAINS");
    this.setAbbreviatedName("*VTCNS");
    this.setFullSyntax("*VTCHAINS");
    this.setAbbreviatedSyntax("*VTCNS");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      int level = 1;
      connection.getResultWriter().write("\nVT>Instance detected at level [" + level + "]!\nVT>");
      connection.getResultWriter().flush();
      session.getShellCommandExecutor().write(command + " " + (level + 1) + "\n");
      session.getShellCommandExecutor().flush();
    }
    else if (parsed.length == 2)
    {
      int level = Integer.parseInt(parsed[1]);
      connection.getResultWriter().write("\nVT>Instance detected at level [" + level + "]!\nVT>");
      connection.getResultWriter().flush();
      session.getShellCommandExecutor().write(command + " " + (level + 1) + "\n");
      session.getShellCommandExecutor().flush();
    }
  }

  public void close()
  {

  }
}
