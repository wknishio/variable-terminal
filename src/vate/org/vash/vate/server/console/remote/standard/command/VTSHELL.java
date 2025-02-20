package org.vash.vate.server.console.remote.standard.command;

import java.io.File;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.shell.adapter.VTShellProcessor;

public class VTSHELL extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSHELL()
  {
    this.setFullName("*VTSHELL");
    this.setAbbreviatedName("*VTSH");
    this.setFullSyntax("*VTSHELL <OPTIONS> [.]");
    this.setAbbreviatedSyntax("*VTSH <OP> [.]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      if (parsed[1].toUpperCase().contains("R"))
      {
        connection.getResultWriter().write("\nVT>Starting remote shell...\nVT>");
        connection.getResultWriter().flush();
        // session.setRestartingShell(true);
        session.restartShell();
      }
      else if (parsed[1].toUpperCase().contains("N"))
      {
        connection.getResultWriter().write("\nVT>Setting null remote shell...\nVT>");
        connection.getResultWriter().flush();
        session.stopShell();
        session.setShellBuilder(new String[] {}, null, null);
        session.setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
        
        // session.restartShell();
      }
      else if (parsed[1].toUpperCase().contains("D"))
      {
        connection.getResultWriter().write("\nVT>Setting remote shell command to: [Default]\nVT>");
        connection.getResultWriter().flush();
        session.stopShell();
        session.setShellBuilder(null, null, null);
        session.setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
        session.restartShell();
      }
      else if (parsed[1].toUpperCase().contains("B"))
      {
        connection.getResultWriter().write("\nVT>Setting beanshell remote shell...\nVT>");
        connection.getResultWriter().flush();
        session.stopShell();
        session.setShellBuilder(null, null, null);
        session.setShellType(VTShellProcessor.SHELL_TYPE_BEANSHELL);
        session.restartShell();
      }
//      else if (parsed[1].toUpperCase().contains("G"))
//      {
//        session.stopShell();
//        session.setShellType(VTShellProcessor.SHELL_TYPE_GROOVYSH);
//        session.restartShell();
//      }
      else if (parsed[1].toUpperCase().contains("S"))
      {
        connection.getResultWriter().write("\nVT>Stopping remote shell...\nVT>");
        connection.getResultWriter().flush();
        session.setStoppingShell(true);
        session.stopShell();
        session.waitShell();
        session.waitShellThreads();
      }
      else if (parsed[1].toUpperCase().contains("C"))
      {
        if (parsed.length >= 3 && (parsed[2].length() > 0))
        {
          String[] nextShell = new String[parsed.length - 2];
          String nextShellCommand = "";
          for (int i = 0; i < nextShell.length; i++)
          {
            nextShell[i] = parsed[i + 2];
            nextShellCommand += (" " + nextShell[i]);
          }
          connection.getResultWriter().write("\nVT>Setting remote shell command to: [" + nextShellCommand.substring(1) + "]\nVT>");
          connection.getResultWriter().flush();
          session.stopShell();
          session.setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
          session.setShellBuilder(nextShell, null, null);
          session.restartShell();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Setting remote shell command to: [Default]\nVT>");
          connection.getResultWriter().flush();
          session.stopShell();
          session.setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
          session.setShellBuilder(null, null, null);
          session.restartShell();
        }
      }
      else if (parsed[1].toUpperCase().contains("E"))
      {
        if (parsed.length >= 3 && (parsed[2].length() > 0))
        {
          String encoding = parsed[2];
          if (session.setShellEncoding(encoding))
          {
            connection.getResultWriter().write("\nVT>Set remote shell encoding to: [" + encoding + "]\nVT>");
            connection.getResultWriter().flush();
            // session.restartShell();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Invalid remote shell encoding: [" + encoding + "]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          session.setShellEncoding(null);
          connection.getResultWriter().write("\nVT>Set remote shell encoding to: [Default]\nVT>");
          connection.getResultWriter().flush();
          // session.restartShell();
        }
      }
      else if (parsed[1].toUpperCase().contains("P"))
      {
        if (parsed.length >= 3 && (parsed[2].length() > 0))
        {
          int index = parsed[0].length() + parsed[1].length() + 2;
          String directory = command.substring(index);
          File directoryFile = new File(directory);
          if (session.setShellDirectory(directoryFile))
          {
            connection.getResultWriter().write("\nVT>Set remote shell directory to: [" + directory + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Invalid remote shell directory: [" + directory + "]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          session.setShellDirectory(null);
          connection.getResultWriter().write("\nVT>Set remote shell directory to: [Default]\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
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
  
  public boolean remote()
  {
    return false;
  }
}
