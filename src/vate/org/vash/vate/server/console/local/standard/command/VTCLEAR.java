package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTCLEAR extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTCLEAR()
  {
    this.setFullName("*VTCLEAR");
    this.setAbbreviatedName("*VTCL");
    this.setFullSyntax("*VTCLEAR");
    this.setAbbreviatedSyntax("*VTCL");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTSystemConsole.clear();
    VTSystemConsole.print("VT>");
  }
  
  public void close()
  {
    
  }
}
