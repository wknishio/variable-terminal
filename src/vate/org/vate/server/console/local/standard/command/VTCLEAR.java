package org.vate.server.console.local.standard.command;

import org.vate.console.VTConsole;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTCLEAR extends VTServerStandardLocalConsoleCommandProcessor
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
