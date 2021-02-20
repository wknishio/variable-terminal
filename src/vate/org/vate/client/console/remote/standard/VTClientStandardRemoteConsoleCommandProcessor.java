package org.vate.client.console.remote.standard;

import org.vate.client.console.remote.VTClientRemoteConsoleCommandProcessor;
import org.vate.help.VTHelpManager;

public abstract class VTClientStandardRemoteConsoleCommandProcessor extends VTClientRemoteConsoleCommandProcessor
{
  public VTClientStandardRemoteConsoleCommandProcessor()
  {

  }

  public String help(String name)
  {
    return VTHelpManager.getHelpForClientCommand(name);
  }
}
