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
    if (session.isEchoCommands())
    {
      session.setEchoCommands(false);
      connection.getResultWriter().write("VT>Console command echo disabled\nVT>");
      connection.getResultWriter().flush();
    }
    else
    {
      session.setEchoCommands(true);
      connection.getResultWriter().write("VT>Console command echo enabled\nVT>");
      connection.getResultWriter().flush();
    }
  }

  public void close()
  {

  }
}