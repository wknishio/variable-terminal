package org.vash.vate.server.console.remote.standard;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.VTServerRemoteConsoleCommandProcessor;

public abstract class VTServerStandardRemoteConsoleCommandProcessor extends VTServerRemoteConsoleCommandProcessor
{
  public VTServerStandardRemoteConsoleCommandProcessor()
  {
    
  }
  
  public String help(String name)
  {
    return VTHelpManager.findHelpForClientCommand(name);
  }
}