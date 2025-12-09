package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;

public class VTSAVE extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTSAVE()
  {
    this.setFullName("*VTSAVE");
    this.setAbbreviatedName("*VTSV");
    this.setFullSyntax("*VTSAVE [FILE]");
    this.setAbbreviatedSyntax("*VTSV [FL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      try
      {
        session.getClient().saveClientSettingsFile("vate-client.properties");
        VTMainConsole.print("\rVT>Client settings file save to [vate-client.properties] completed!\nVT>");
      }
      catch (Throwable t)
      {
        VTMainConsole.print("\rVT>Client settings file save to [vate-client.properties] failed!\nVT>");
      }
    }
    else if (parsed.length >= 2)
    {
      try
      {
        session.getClient().saveClientSettingsFile(parsed[1]);
        VTMainConsole.print("\rVT>Client settings file save to [" + parsed[1] + "] completed!\nVT>");
      }
      catch (Throwable t)
      {
        VTMainConsole.print("\rVT>Client settings file save to [" + parsed[1] + "] failed!\nVT>");
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