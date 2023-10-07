package org.vash.vate.client.console.remote.standard;

import org.vash.vate.client.console.remote.VTClientRemoteConsoleCommandProcessor;
import org.vash.vate.help.VTHelpManager;

public abstract class VTClientStandardRemoteConsoleCommandProcessor extends VTClientRemoteConsoleCommandProcessor
{
  public VTClientStandardRemoteConsoleCommandProcessor()
  {
    
  }
  
  public String help(String name)
  {
    return VTHelpManager.findHelpForClientCommand(name);
  }
  
  public boolean remote()
  {
    return false;
  }
}
