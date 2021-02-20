package org.vate.server.console.local.standard;

import org.vate.help.VTHelpManager;
import org.vate.server.console.local.VTServerLocalConsoleCommandProcessor;

public abstract class VTServerStandardLocalConsoleCommandProcessor extends VTServerLocalConsoleCommandProcessor
{
  public VTServerStandardLocalConsoleCommandProcessor()
  {

  }

  public String help(String name)
  {
    return VTHelpManager.getHelpForServerCommand(name);
  }
}