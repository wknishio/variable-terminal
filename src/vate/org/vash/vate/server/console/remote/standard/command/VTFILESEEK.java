package org.vash.vate.server.console.remote.standard.command;

import java.io.File;

import org.vash.vate.filesystem.VTRootList;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.server.filesystem.VTServerFileScanOperation;

public class VTFILESEEK extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTFILESEEK()
  {
    this.setFullName("*VTFILESEEK");
    this.setAbbreviatedName("*VTFS");
    this.setFullSyntax("*VTFILESEEK [MODE] [FILE]");
    this.setAbbreviatedSyntax("*VTFS [MD] [FL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getFileScanOperation())
    {
      if (parsed.length >= 3)
      {
        if (parsed[1].toUpperCase().startsWith("I"))
        {
          if (session.getFileScanOperation().isFinished())
          {
            session.getFileScanOperation().joinThread();
          }
          if (!session.getFileScanOperation().aliveThread())
          {
            session.getFileScanOperation().setFinished(false);
            session.getFileScanOperation().setTarget(new File(parsed[2]));
            session.getFileScanOperation().setOperation(VTServerFileScanOperation.INFO_FILE);
            session.getFileScanOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Another remote file seek is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().startsWith("L"))
        {
          if (session.getFileScanOperation().isFinished())
          {
            session.getFileScanOperation().joinThread();
          }
          if (!session.getFileScanOperation().aliveThread())
          {
            session.getFileScanOperation().setFinished(false);
            session.getFileScanOperation().setTarget(new File(parsed[2]));
            session.getFileScanOperation().setOperation(VTServerFileScanOperation.LIST_FILES);
            session.getFileScanOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Another remote file seek is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 2)
      {
        if (parsed[1].toUpperCase().startsWith("S"))
        {
          if (session.getFileScanOperation().isFinished())
          {
            session.getFileScanOperation().joinThread();
          }
          if (session.getFileScanOperation().aliveThread())
          {
            connection.getResultWriter().write("\rVT>Trying to interrupt remote file seek!\nVT>");
            connection.getResultWriter().flush();
            session.getFileScanOperation().interruptThread();
            session.getFileScanOperation().stopThread();
          }
          else
          {
            connection.getResultWriter().write("\rVT>No remote file seek is running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().startsWith("L"))
        {
          if (session.getFileScanOperation().isFinished())
          {
            session.getFileScanOperation().joinThread();
          }
          if (!session.getFileScanOperation().aliveThread())
          {
            session.getFileScanOperation().setFinished(false);
            session.getFileScanOperation().setTarget(new VTRootList());
            session.getFileScanOperation().setOperation(VTServerFileScanOperation.LIST_FILES);
            session.getFileScanOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Another remote file seek is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 1)
      {
        if (session.getFileScanOperation().isFinished())
        {
          session.getFileScanOperation().joinThread();
        }
        if (!session.getFileScanOperation().aliveThread())
        {
          connection.getResultWriter().write("\rVT>No remote file seek is running!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>A remote file seek is still running!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
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
