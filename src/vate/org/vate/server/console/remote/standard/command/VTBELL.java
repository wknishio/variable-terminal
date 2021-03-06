package org.vate.server.console.remote.standard.command;

import org.vate.console.VTConsole;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTBELL extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTBELL()
  {
    this.setFullName("*VTBELL");
    this.setAbbreviatedName("*VTBL");
    this.setFullSyntax("*VTBELL");
    this.setAbbreviatedSyntax("*VTBL");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getResultWriter().write("\nVT>Invoking server terminal bell...\nVT>");
    connection.getResultWriter().flush();
    VTConsole.bell();
  }

  public void close()
  {

  }
}