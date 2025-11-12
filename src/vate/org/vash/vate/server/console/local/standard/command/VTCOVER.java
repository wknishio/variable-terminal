package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTCOVER extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTCOVER()
  {
    this.setFullName("*VTCOVER");
    this.setAbbreviatedName("*VTCV");
    this.setFullSyntax("*VTCOVER");
    this.setAbbreviatedSyntax("*VTCV");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (server.isDaemon())
    {
      
    }
    else
    {
      if (VTMainConsole.isDaemon())
      {
        VTMainConsole.setDaemon(false);
      }
      else
      {
        VTMainConsole.setDaemon(true);
      }
    }
  }
  
  public void close()
  {
    
  }
}
