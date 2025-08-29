package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTMainConsole;
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
    VTMainConsole.clear();
    VTMainConsole.print("VT>");
  }
  
  public void close()
  {
    
  }
}
