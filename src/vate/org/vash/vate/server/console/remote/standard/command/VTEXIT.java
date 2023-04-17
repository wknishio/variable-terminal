package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTEXIT extends VTServerStandardRemoteConsoleCommandProcessor
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
    //VTConsole.print("\rVT>Client disconnecting...\nVT>");
    connection.closeSockets();
  }
  
  public void close()
  {
    
  }
}
