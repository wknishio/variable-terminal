package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTCHAIN extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTCHAIN()
  {
    this.setFullName("*VTCHAIN");
    this.setAbbreviatedName("VTCH");
    this.setFullSyntax("*VTCHAIN");
    this.setAbbreviatedSyntax("*VTCH");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      int level = 1;
      connection.getResultWriter().write("\nVT>Instance detected at level [" + level + "]!\nVT>");
      connection.getResultWriter().flush();
      try
      {
        session.getOutputWriter().setCommandFilter(command + " " + (level + 1));
        session.getShellCommandExecutor().write(command + " " + (level + 1) + "\n");
        session.getShellCommandExecutor().flush();
      }
      catch (Throwable t)
      {
        
      }
    }
    else if (parsed.length == 2)
    {
      int level = Integer.parseInt(parsed[1]);
      connection.getResultWriter().write("\nVT>Instance detected at level [" + level + "]!\nVT>");
      connection.getResultWriter().flush();
      try
      {
        session.getOutputWriter().setCommandFilter(command + " " + (level + 1));
        session.getShellCommandExecutor().write(command + " " + (level + 1) + "\n");
        session.getShellCommandExecutor().flush();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
