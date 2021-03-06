package org.vate.client.console.remote.standard.command;

import org.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vate.console.VTConsole;

public class VTSTOP extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTSTOP()
  {
    this.setFullName("*VTSTOP");
    this.setAbbreviatedName("*VTSTP");
    this.setFullSyntax("*VTSTOP");
    this.setAbbreviatedSyntax("*VTSTP");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getCommandWriter().write(command + "\n");
    connection.getCommandWriter().flush();
    VTConsole.print("\nVT>Finalizing server...");
  }

  public void close()
  {

  }
}
