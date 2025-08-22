package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTSTOP extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTSTOP()
  {
    this.setFullName("*VTSTOP");
    this.setAbbreviatedName("*VTST");
    this.setFullSyntax("*VTSTOP");
    this.setAbbreviatedSyntax("*VTST");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    // VTConsole.print("\rVT>Client finalizing server...\nVT>");
    VTSystemConsole.closeConsole();
    server.stop();
    // VTExit.exit(0);
  }
  
  public void close()
  {
    
  }
}
