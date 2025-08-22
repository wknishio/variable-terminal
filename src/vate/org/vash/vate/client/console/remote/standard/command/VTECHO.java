package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTSystemConsole;

public class VTECHO extends VTClientStandardRemoteConsoleCommandProcessor
{
  private int state = 0;
  
  public VTECHO()
  {
    this.setFullName("*VTECHO");
    this.setAbbreviatedName("*VTEC");
    this.setFullSyntax("*VTECHO");
    this.setAbbreviatedSyntax("*VTEC");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (state == 0)
    {
      state = 1;
      
      session.getConnection().getCommandWriter().writeLine(command + " " + state);
      session.getConnection().getCommandWriter().flush();
      
      if (VTSystemConsole.isCommandEcho())
      {
        VTSystemConsole.setCommandEcho(false);
      }
      else
      {
        VTSystemConsole.setCommandEcho(true);
      }
    }
    else if (state == 1)
    {
      state = 2;
      
      session.getConnection().getCommandWriter().writeLine(command + " " + state);
      session.getConnection().getCommandWriter().flush();
    }
    else if (state == 2)
    {
      state = 3;
      
      session.getConnection().getCommandWriter().writeLine(command + " " + state);
      session.getConnection().getCommandWriter().flush();
      
      if (VTSystemConsole.isCommandEcho())
      {
        VTSystemConsole.setCommandEcho(false);
      }
      else
      {
        VTSystemConsole.setCommandEcho(true);
      }
    }
    else if (state == 3)
    {
      state = 0;
      
      session.getConnection().getCommandWriter().writeLine(command + " " + state);
      session.getConnection().getCommandWriter().flush();
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}