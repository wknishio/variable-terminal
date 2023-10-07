package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTCOVER extends VTServerStandardRemoteConsoleCommandProcessor
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
    if (session.getServer().isDaemon())
    {
      connection.getResultWriter().write("\nVT>Server console interface is unavailable\nVT>");
      connection.getResultWriter().flush();
    }
    else
    {
      if (VTConsole.isDaemon())
      {
        session.getServer().enableTrayIcon();
        VTConsole.setDaemon(false);
        connection.getResultWriter().write("\nVT>Server console interface enabled\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        session.getServer().disableTrayIcon();
        VTConsole.setDaemon(true);
        connection.getResultWriter().write("\nVT>Server console interface disabled\nVT>");
        connection.getResultWriter().flush();
      }
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
