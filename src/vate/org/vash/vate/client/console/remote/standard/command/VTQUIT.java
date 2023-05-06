package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTQUIT extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTQUIT()
  {
    this.setFullName("*VTQUIT");
    this.setAbbreviatedName("*VTQT");
    this.setFullSyntax("*VTQUIT");
    this.setAbbreviatedSyntax("*VTQT");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    connection.getCommandWriter().write(command + "\n");
    connection.getCommandWriter().flush();
    VTConsole.closeConsole();
    session.getClient().stop();
  }
  
  public void close()
  {
    
  }
}
