package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.server.connection.VTServerConnector;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTCONFIGURE extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTCONFIGURE()
  {
    this.setFullName("*VTCONFIGURE");
    this.setAbbreviatedName("*VTCF");
    this.setFullSyntax("*VTCONFIGURE");
    this.setAbbreviatedSyntax("*VTCF");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    VTSystemConsole.print("\rVT>Reconfiguring all server settings!\nVT>");
    server.reconfigure();
    VTServerConnector connector = server.getServerConnector();
    synchronized (connector)
    {
      connector.interruptConnector();
      connector.notify();
    }
    // VTConsole.print("\nVT>Finished reconfiguring all server settings!\nVT>");
  }
  
  public void close()
  {
    
  }
}
