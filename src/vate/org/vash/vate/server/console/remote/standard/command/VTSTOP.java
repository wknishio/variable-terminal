package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSTOP extends VTServerStandardRemoteConsoleCommandProcessor
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
    //VTConsole.print("\rVT>Client finalizing server...\nVT>");
    connection.closeSockets();
    VTMainConsole.closeConsole();
    session.getServer().stop();
    // VTExit.exit(0);
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}
