package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTGRAPHICSLINK extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTGRAPHICSLINK()
  {
    this.setFullName("*VTGRAPHICSLINK");
    this.setAbbreviatedName("*VTGL");
    this.setFullSyntax("*VTGRAPHICSLINK [MODE]");
    this.setAbbreviatedSyntax("*VTGL [MD]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      VTConsole.print("\nVT>Remote graphics link start on client failed!\nVT>");
      return;
    }
    if (parsed.length >= 2)
    {
      /*
       * if (splitCommand[1].toUpperCase().startsWith("S")) { synchronized
       * (session.getGraphicsClient()) { if
       * (session.getGraphicsClient().isFinished()) {
       * session.getGraphicsClient().joinThread(); } } if
       * (session.getGraphicsClient().aliveThread()) { } else { VTTerminal.
       * print("\nVT>Remote graphics link is not running!\nVT>" ); } }
       */
      if (parsed[1].toUpperCase().startsWith("V"))
      {
        synchronized (session.getGraphicsClient())
        {
          if (session.getGraphicsClient().isFinished())
          {
            // System.out.println("session.getGraphicsClient().isFinished()");
            session.getGraphicsClient().joinThread();
            // System.out.println("session.getGraphicsClient().joinThread()");
          }
        }
        if (!session.getGraphicsClient().aliveThread())
        {
          session.getGraphicsClient().setFinished(false);
          session.getGraphicsClient().setReadOnly(true);
          connection.getCommandWriter().writeLine(command);
          connection.getCommandWriter().flush();
          session.getGraphicsClient().startThread();
          // System.out.println("session.getGraphicsClient().startThread()");
        }
        else
        {
          session.getGraphicsClient().setReadOnly(true);
          VTConsole.print("\nVT>Remote graphics link set to view mode!\nVT>");
        }
      }
      else if (parsed[1].toUpperCase().startsWith("C"))
      {
        synchronized (session.getGraphicsClient())
        {
          if (session.getGraphicsClient().isFinished())
          {
            // System.out.println("session.getGraphicsClient().isFinished()");
            session.getGraphicsClient().joinThread();
            // System.out.println("session.getGraphicsClient().joinThread()");
          }
        }
        if (!session.getGraphicsClient().aliveThread())
        {
          session.getGraphicsClient().setFinished(false);
          session.getGraphicsClient().setReadOnly(false);
          connection.getCommandWriter().writeLine(command);
          connection.getCommandWriter().flush();
          session.getGraphicsClient().startThread();
          // System.out.println("session.getGraphicsClient().startThread()");
        }
        else
        {
          session.getGraphicsClient().setReadOnly(false);
          VTConsole.print("\nVT>Remote graphics link set to control mode!\nVT>");
        }
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else if (parsed.length == 1)
    {
      synchronized (session.getGraphicsClient())
      {
        if (session.getGraphicsClient().isFinished())
        {
          // System.out.println("session.getGraphicsClient().isFinished()");
          session.getGraphicsClient().joinThread();
          // System.out.println("session.getGraphicsClient().joinThread()");
        }
      }
      if (session.getGraphicsClient().aliveThread())
      {
        // VTTerminal.print("\nVT>Remote graphics link is
        // running!\nVT>");
        connection.getCommandWriter().writeLine(command);
        connection.getCommandWriter().flush();
        session.getGraphicsClient().setStopped(true);
        session.getGraphicsClient().joinThread();
        // System.out.println("session.getGraphicsClient().joinThread()");
      }
      else
      {
        session.getGraphicsClient().setFinished(false);
        session.getGraphicsClient().setReadOnly(false);
        connection.getCommandWriter().writeLine(command);
        connection.getCommandWriter().flush();
        session.getGraphicsClient().startThread();
        // System.out.println("session.getGraphicsClient().startThread()");
      }
    }
    else
    {
      VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
