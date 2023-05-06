package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTSTOP extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTSTOP()
  {
    this.setFullName("*VTSTOP");
    this.setAbbreviatedName("*VTSTP");
    this.setFullSyntax("*VTSTOP");
    this.setAbbreviatedSyntax("*VTSTP");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTConsole.print("\nVT>Stopping server!");
    connection.getCommandWriter().write(command + "\n");
    connection.getCommandWriter().flush();
  }
  
  public void close()
  {
    
  }
}
