package org.vate.client.console.remote.standard.command;

import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;

public class VTQUIT extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTQUIT()
  {
    this.setFullName("*VTQUIT");
    this.setAbbreviatedName("*VTQT");
    this.setFullSyntax("*VTQUIT");
    this.setAbbreviatedSyntax("*VTQT");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getCommandWriter().write(command + "\n");
    connection.getCommandWriter().flush();
    VTConsole.print("\nVT>Finalizing client...");
    // connection.setSkipLine(true);
    connection.closeSockets();
    System.exit(0);
  }

  public void close()
  {

  }
}
