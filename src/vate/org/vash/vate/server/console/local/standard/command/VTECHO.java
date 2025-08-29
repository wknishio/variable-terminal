package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTMainConsole;
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
      VTMainConsole.println("\rVT>Server command echo disabled\nVT>");
    }
    else
    {
      server.setEchoCommands(true);
      VTMainConsole.println("\rVT>Server command echo enabled\nVT>");
    }
  }
  
  public void close()
  {
    
  }
}
