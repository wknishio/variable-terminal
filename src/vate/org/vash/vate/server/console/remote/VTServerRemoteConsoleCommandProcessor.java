package org.vash.vate.server.console.remote;

import org.vash.vate.console.command.VTConsoleCommandProcessor;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;

public abstract class VTServerRemoteConsoleCommandProcessor extends VTConsoleCommandProcessor
{
  protected VTServerSession session;
  protected VTServerConnection connection;
  protected StringBuilder message = new StringBuilder();
  
  public VTServerRemoteConsoleCommandProcessor()
  {
    
  }
  
  public void setSession(VTServerSession session)
  {
    this.session = session;
    this.connection = session.getConnection();
  }
  
  public void register()
  {
    VTServerRemoteConsoleCommandSelector.addCustomCommandProcessorClass(this.getClass().getName());
  }
  
  public void waitFor()
  {
    
  }
}