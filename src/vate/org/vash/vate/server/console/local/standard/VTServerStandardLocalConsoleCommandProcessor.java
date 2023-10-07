package org.vash.vate.server.console.local.standard;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.local.VTServerLocalConsoleCommandProcessor;

public abstract class VTServerStandardLocalConsoleCommandProcessor extends VTServerLocalConsoleCommandProcessor
{
  public VTServerStandardLocalConsoleCommandProcessor()
  {
  }
  
  public String help(String name)
  {
    return VTHelpManager.findHelpForServerCommand(name);
  }
  
  public boolean remote()
  {
    return false;
  }
}