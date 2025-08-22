package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTSystemConsole;

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
    connection.getCommandWriter().writeLine(command);
    connection.getCommandWriter().flush();
    VTSystemConsole.closeConsole();
    session.getClient().stop();
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
