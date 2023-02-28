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
        server.enableTrayIcon();
        VTConsole.print("\rVT>Server console interface enabled\nVT>");
        VTConsole.setDaemon(false);
      }
      else
      {
        server.disableTrayIcon();
        VTConsole.print("\rVT>Server console interface disabled\nVT>");
        VTConsole.setDaemon(true);
        Object waiter = VTConsole.getSynchronizationObject();
        synchronized (waiter)
        {
          while (VTConsole.isDaemon())
          {
            waiter.wait();
          }
        }
      }
    }
  }
  
  public void close()
  {
    
  }
}
