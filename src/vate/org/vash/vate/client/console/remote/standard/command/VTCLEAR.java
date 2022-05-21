package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTCLEAR extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTCLEAR()
  {
    this.setFullName("*VTCLEAR");
    this.setAbbreviatedName("*VTCLR");
    this.setFullSyntax("*VTCLEAR");
    this.setAbbreviatedSyntax("*VTCLR");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    VTConsole.clear();
    VTConsole.print("VT>");
  }

  public void close()
  {

  }
}