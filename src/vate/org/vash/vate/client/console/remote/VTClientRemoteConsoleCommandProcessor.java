package org.vash.vate.client.console.remote;

import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.command.VTConsoleCommandProcessor;
import org.vash.vate.stream.endian.VTLittleEndianOutputStream;

public abstract class VTClientRemoteConsoleCommandProcessor extends VTConsoleCommandProcessor
{
  protected VTClientSession session;
  protected VTClientConnection connection;
  protected VTLittleEndianOutputStream commandWriter;
  protected StringBuilder message = new StringBuilder();
  
  public VTClientRemoteConsoleCommandProcessor()
  {
    // VTClientRemoteConsoleCommandSelector.addCustomCommandProcessorClass(this.getClass().getName());
  }
  
  public void setSession(VTClientSession session)
  {
    this.session = session;
    this.connection = session.getConnection();
    this.commandWriter = session.getConnection().getCommandWriter();
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
