package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSTOP extends VTServerStandardRemoteConsoleCommandProcessor
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
    //VTConsole.print("\rVT>Client finalizing server...\nVT>");
    connection.closeSockets();
    VTConsole.closeConsole();
    session.getServer().stop();
    // VTExit.exit(0);
  }
  
  public void close()
  {
    
  }
}
