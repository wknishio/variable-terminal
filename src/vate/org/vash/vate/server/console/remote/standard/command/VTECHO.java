package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTECHO extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTECHO()
  {
    this.setFullName("*VTECHO");
    this.setAbbreviatedName("*VTEC");
    this.setFullSyntax("*VTECHO");
    this.setAbbreviatedSyntax("*VTEC");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    int echoState = Integer.parseInt(parsed[1]);
    session.setEchoState(echoState);
    if (echoState == 1 || echoState == 3)
    {
      if (echoState == 1)
      {
        connection.getResultWriter().write("\nVT>Local console echo disabled\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Local console echo enabled\nVT>");
        connection.getResultWriter().flush();
      }
    }
    else
    {
      if (session.isEchoCommands())
      {
        session.setEchoCommands(false);
        connection.getResultWriter().write("\nVT>Remote console echo disabled\nVT>");
        connection.getResultWriter().flush();
      }
      else
      {
        session.setEchoCommands(true);
        connection.getResultWriter().write("\nVT>Remote console echo enabled\nVT>");
        connection.getResultWriter().flush();
      }
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