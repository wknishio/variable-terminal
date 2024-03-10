package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
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
      if (VTConsole.isDaemon())
      {
        VTConsole.setDaemon(false);
        server.enableTrayIcon();
        VTConsole.print("\rVT>Server console interface enabled\nVT>");
      }
      else
      {
        VTConsole.setDaemon(true);
        server.disableTrayIcon();
        VTConsole.print("\rVT>Server console interface disabled\nVT>");
      }
    }
  }
  
  public void close()
  {
    
  }
}
