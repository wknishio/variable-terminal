package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;

public class VTECHO extends VTClientStandardRemoteConsoleCommandProcessor
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
//		if (VTConsole.isCommandEcho())
//		{
//			VTConsole.setCommandEcho(false);
//		}
//		else
//		{
//			VTConsole.setCommandEcho(true);
//		}
    session.getConnection().getCommandWriter().write(command + "\n");
    session.getConnection().getCommandWriter().flush();
  }

  public void close()
  {

  }
}