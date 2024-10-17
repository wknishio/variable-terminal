package org.vash.vate.server.console.local;

import org.vash.vate.console.command.VTConsoleCommandProcessor;
import org.vash.vate.server.VTServer;

public abstract class VTServerLocalConsoleCommandProcessor extends VTConsoleCommandProcessor
{
  protected VTServer server;
  protected StringBuilder message = new StringBuilder();
  
  public VTServerLocalConsoleCommandProcessor()
  {
    
  }
  
  public void setServer(VTServer server)
  {
    this.server = server;
  }
  
  public void register()
  {
    VTServerLocalConsoleCommandSelector.addCustomCommandProcessorClass(this.getClass().getName());
  }
  
  public void waitFor()
  {
    
  }
}
