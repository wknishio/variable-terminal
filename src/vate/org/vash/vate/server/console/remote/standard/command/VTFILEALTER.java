package org.vash.vate.server.console.remote.standard.command;

import java.io.File;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.server.filesystem.VTServerFileModifyOperation;

public class VTFILEALTER extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTFILEALTER()
  {
    this.setFullName("*VTFILEALTER");
    this.setAbbreviatedName("*VTFA");
    this.setFullSyntax("*VTFILEALTER [MODE] [FILE] [NEXT]");
    this.setAbbreviatedSyntax("*VTFA [MD] [FL] [NX]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    boolean waitFor = false;
    synchronized (session.getFileModifyOperation())
    {
      if (parsed.length >= 4)
      {
        if (parsed[1].toUpperCase().contains("W"))
        {
          waitFor = true;
        }
        if (parsed[1].toUpperCase().contains("M"))
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
            connection.getResultWriter().write("\rVT>Another remote file change is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().contains("C"))
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
            connection.getResultWriter().write("\rVT>Another remote file change is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 3)
      {
        if (parsed[1].toUpperCase().contains("W"))
        {
          waitFor = true;
        }
        if (parsed[1].toUpperCase().contains("F"))
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
            connection.getResultWriter().write("\rVT>Another remote file change is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().contains("D"))
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
            connection.getResultWriter().write("\rVT>Another remote file change is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().contains("R"))
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
            connection.getResultWriter().write("\rVT>Another remote file change is still running!\nVT>");
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
        if (parsed[1].toUpperCase().contains("W"))
        {
          waitFor = true;
        }
        if (parsed[1].toUpperCase().contains("S"))
        {
          if (session.getFileModifyOperation().isFinished())
          {
            session.getFileModifyOperation().joinThread();
          }
          if (session.getFileModifyOperation().aliveThread())
          {
            connection.getResultWriter().write("\rVT>Trying to interrupt remote file change!\nVT>");
            connection.getResultWriter().flush();
            session.getFileModifyOperation().interruptThread();
            session.getFileModifyOperation().stopThread();
          }
          else
          {
            connection.getResultWriter().write("\rVT>No remote file change is running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (waitFor)
        {
          if (!session.getFileModifyOperation().aliveThread())
          {
            connection.getResultWriter().write("\rVT>No remote file change is running!\nVT>");
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
        if (session.getFileModifyOperation().isFinished())
        {
          session.getFileModifyOperation().joinThread();
        }
        if (!session.getFileModifyOperation().aliveThread())
        {
          connection.getResultWriter().write("\rVT>No remote file change is running!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>A remote file change is still running!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
    if (waitFor)
    {
      try
      {
        waitFor();
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
  
  public void waitFor()
  {
    session.getFileModifyOperation().joinThread();
  }
}
