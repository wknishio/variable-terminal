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
    String shellEncoding = session.getShellEncoding();
    byte[] commandData;
    int level;
    
    if (parsed.length == 1)
    {
      level = 1;
      if (shellEncoding != null && shellEncoding.length() > 0)
      {
        commandData = (command + " " + (level + 1) + "\n").getBytes(shellEncoding);
      }
      else
      {
        commandData = (command + " " + (level + 1) + "\n").getBytes();
      }
      connection.getResultWriter().write("\rVT>Instance detected at level [" + level + "]!\nVT>");
      connection.getResultWriter().flush();
      try
      {
        session.getOutputWriter().setCommandFilter(command + " " + (level + 1), shellEncoding);
        session.getShellOutputStream().write(commandData);
        session.getShellOutputStream().flush();
      }
      catch (Throwable t)
      {
        
      }
    }
    else if (parsed.length == 2)
    {
      level = Integer.parseInt(parsed[1]);
      if (shellEncoding != null && shellEncoding.length() > 0)
      {
        commandData = (command + " " + (level + 1) + "\n").getBytes(shellEncoding);
      }
      else
      {
        commandData = (command + " " + (level + 1) + "\n").getBytes();
      }
      connection.getResultWriter().write("\rVT>Instance detected at level [" + level + "]!\nVT>");
      connection.getResultWriter().flush();
      try
      {
        session.getOutputWriter().setCommandFilter(command + " " + (level + 1), shellEncoding);
        session.getShellOutputStream().write(commandData);
        session.getShellOutputStream().flush();
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
