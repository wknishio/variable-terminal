package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTECHO extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTECHO()
  {
    this.setFullName("*VTECHO");
    this.setAbbreviatedName("*VTEC");
    this.setFullSyntax("*VTECHO");
    this.setAbbreviatedSyntax("*VTEC");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (server.isEchoCommands())
    {
      server.setEchoCommands(false);
      VTConsole.println("\rVT>Server command echo disabled\nVT>");
    }
    else
    {
      server.setEchoCommands(true);
      VTConsole.println("\rVT>Server command echo enabled\nVT>");
    }
  }
  
  public void close()
  {
    
  }
}
