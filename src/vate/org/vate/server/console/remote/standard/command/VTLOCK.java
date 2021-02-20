package org.vate.server.console.remote.standard.command;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTLOCK extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTLOCK()
  {
    this.setFullName("*VTLOCK");
    this.setAbbreviatedName("*VTLK");
    this.setFullSyntax("*VTLOCK <LOGIN> <PASSWORD>");
    this.setAbbreviatedSyntax("*VTLK <LGN PW>");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 3)
    {
      // int separator = splitCommand[1].indexOf('/');
      // if (separator >= 0)
      // {
      // String login = splitCommand[1].substring(0, separator);
      // String password = splitCommand[1].substring(separator + 1);
      // session.getServer().setUniqueUserCredential(login, password);
      // connection.getResultWriter().write("\nVT>Single credential
      // set!\nVT>");
      // connection.getResultWriter().flush();
      // }
      // else
      // {
      // connection.getResultWriter().write("\nVT>Invalid command
      // syntax!" +
      // VTHelpManager.getHelpForClientCommand(splitCommand[0]));
      // connection.getResultWriter().flush();
      // }
      String login = parsed[1];
      String password = parsed[2];
      session.getServer().setUniqueUserCredential(login, password);
      connection.getResultWriter().write("\nVT>Single credential set!\nVT>");
      connection.getResultWriter().flush();
    }
    else
    {
      connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
    }
  }

  public void close()
  {

  }
}
