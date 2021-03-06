package org.vate.server.console.remote.standard.command;

import java.io.File;

import org.vate.help.VTHelpManager;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vate.server.filesystem.VTServerFileModifyOperation;

public class VTFILEMODIFY extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTFILEMODIFY()
  {
    this.setFullName("*VTFILEMODIFY");
    this.setAbbreviatedName("*VTFM");
    this.setFullSyntax("*VTFILEMODIFY [MODE] [FILE] [NEXT]");
    this.setAbbreviatedSyntax("*VTFM [MD] [FL] [NX]");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getFileModifyOperation())
    {
      if (parsed.length >= 4)
      {
        if (parsed[1].toUpperCase().startsWith("M"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (!session.getFileModifyOperation().aliveThread())
          {
            session.getFileModifyOperation().setFinished(false);
            session.getFileModifyOperation().setSourceFile(new File(parsed[2]));
            session.getFileModifyOperation().setDestinationFile(new File(parsed[3]));
            session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.MOVE_FILE);
            session.getFileModifyOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().startsWith("C"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (!session.getFileModifyOperation().aliveThread())
          {
            session.getFileModifyOperation().setFinished(false);
            session.getFileModifyOperation().setSourceFile(new File(parsed[2]));
            session.getFileModifyOperation().setDestinationFile(new File(parsed[3]));
            session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.COPY_FILE);
            session.getFileModifyOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 3)
      {
        if (parsed[1].toUpperCase().startsWith("F"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (!session.getFileModifyOperation().aliveThread())
          {
            session.getFileModifyOperation().setFinished(false);
            session.getFileModifyOperation().setSourceFile(new File(parsed[2]));
            session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.CREATE_FILE);
            session.getFileModifyOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().startsWith("D"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (!session.getFileModifyOperation().aliveThread())
          {
            session.getFileModifyOperation().setFinished(false);
            session.getFileModifyOperation().setSourceFile(new File(parsed[2]));
            session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.CREATE_DIRECTORY);
            session.getFileModifyOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().startsWith("R"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (!session.getFileModifyOperation().aliveThread())
          {
            session.getFileModifyOperation().setFinished(false);
            session.getFileModifyOperation().setSourceFile(new File(parsed[2]));
            session.getFileModifyOperation().setOperation(VTServerFileModifyOperation.REMOVE_FILE);
            session.getFileModifyOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another remote file modification is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 2)
      {
        if (parsed[1].toUpperCase().startsWith("S"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (!session.getFileModifyOperation().aliveThread())
          {
            connection.getResultWriter().write("\nVT>Trying to interrupt remote file modification!\nVT>");
            connection.getResultWriter().flush();
            session.getFileModifyOperation().interruptThread();
            session.getFileModifyOperation().stopThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>No remote file modification is running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 1)
      {
        if (session.getFileModifyOperation().isFinished())
        {
          session.getFileModifyOperation().joinThread();
        }
        if (!session.getFileModifyOperation().aliveThread())
        {
          connection.getResultWriter().write("\nVT>No remote file modification is running!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>A remote file modification is still running!\nVT>");
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

  }
}
