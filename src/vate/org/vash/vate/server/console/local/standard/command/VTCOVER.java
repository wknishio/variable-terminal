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
        server.enableTrayIcon();
        VTMainConsole.print("\rVT>Server console interface enabled\nVT>");
      }
      else
      {
        VTMainConsole.setDaemon(true);
        server.disableTrayIcon();
        VTMainConsole.print("\rVT>Server console interface disabled\nVT>");
      }
    }
  }
  
  public void close()
  {
    
  }
}
