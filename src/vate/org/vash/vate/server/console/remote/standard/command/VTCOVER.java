package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.console.VTMainConsole;
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
      connection.getResultWriter().write("\rVT>Server console interface is unavailable\nVT>");
      connection.getResultWriter().flush();
    }
    else
    {
      if (VTMainConsole.isDaemon())
      {
        VTMainConsole.setDaemon(false);
        session.getServer().enableTrayIcon();
        connection.getResultWriter().write("\rVT>Server console interface enabled\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        VTMainConsole.setDaemon(true);
        session.getServer().disableTrayIcon();
        connection.getResultWriter().write("\rVT>Server console interface disabled\nVT>");
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
