package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTSTOP extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTSTOP()
  {
    this.setFullName("*VTSTOP");
    this.setAbbreviatedName("*VTST");
    this.setFullSyntax("*VTSTOP");
    this.setAbbreviatedSyntax("*VTST");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTConsole.print("\nVT>Stopping server!");
    connection.getCommandWriter().writeLine(command);
    connection.getCommandWriter().flush();
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
