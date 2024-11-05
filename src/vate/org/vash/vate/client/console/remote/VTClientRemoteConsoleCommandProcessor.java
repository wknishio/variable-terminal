package org.vash.vate.client.console.remote;

import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.command.VTConsoleCommandProcessor;

public abstract class VTClientRemoteConsoleCommandProcessor extends VTConsoleCommandProcessor
{
  protected VTClientSession session;
  protected VTClientConnection connection;
  protected StringBuilder message = new StringBuilder();
  
  public VTClientRemoteConsoleCommandProcessor()
  {
    // VTClientRemoteConsoleCommandSelector.addCustomCommandProcessorClass(this.getClass().getName());
  }
  
  public void setSession(VTClientSession session)
  {
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void register()
  {
    VTClientRemoteConsoleCommandSelector.addCustomCommandProcessorClass(this.getClass().getName());
  }
  
  public void waitFor()
  {
    
  }
  
  // public String help(String name)
  // {
  // return VTHelpManager.getHelpForClientCommand(name);
  // }
}
