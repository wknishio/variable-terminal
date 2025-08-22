package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
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
      if (VTSystemConsole.isDaemon())
      {
        VTSystemConsole.setDaemon(false);
        server.enableTrayIcon();
        VTSystemConsole.print("\rVT>Server console interface enabled\nVT>");
      }
      else
      {
        VTSystemConsole.setDaemon(true);
        server.disableTrayIcon();
        VTSystemConsole.print("\rVT>Server console interface disabled\nVT>");
      }
    }
  }
  
  public void close()
  {
    
  }
}
