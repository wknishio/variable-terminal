package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTEXIT extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTEXIT()
  {
    this.setFullName("*VTEXIT");
    this.setAbbreviatedName("*VTEX");
    this.setFullSyntax("*VTEXIT");
    this.setAbbreviatedSyntax("*VTEX");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTConsole.print("\nVT>Disconnecting from server!");
    connection.getCommandWriter().writeLine(command);
    connection.getCommandWriter().flush();
    connection.closeSockets();
    return;
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
