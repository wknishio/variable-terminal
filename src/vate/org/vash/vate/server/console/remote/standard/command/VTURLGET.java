package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTURLGET extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTURLGET()
  {
    this.setFullName("*VTURLGET");
    this.setAbbreviatedName("*VTUG");
    this.setFullSyntax("*VTURLGET <URL FILE>");
    this.setAbbreviatedSyntax("*VTUG <UR FL>");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getURLInvoker())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (parsed.length >= 3)
      {
        if (session.getURLInvoker().isFinished())
        {
          session.getURLInvoker().joinThread();
        }
        if (!session.getURLInvoker().aliveThread())
        {
          session.getURLInvoker().setFinished(false);
          session.getURLInvoker().setURL(parsed[1]);
          if (parsed.length >= 3)
          {
            session.getURLInvoker().setFile(parsed[2]);
          }
          else
          {
            session.getURLInvoker().setFile(null);
          }
          session.getURLInvoker().startThread();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Another url data transfer is still running!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
  }

  public void close()
  {
    try
    {
      session.getURLInvoker().close();
    }
    catch (Throwable t)
    {
      
    }
  }
}
