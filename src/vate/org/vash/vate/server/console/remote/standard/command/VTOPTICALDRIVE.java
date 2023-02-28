package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTOPTICALDRIVE extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTOPTICALDRIVE()
  {
    this.setFullName("*VTOPTICALDRIVE");
    this.setAbbreviatedName("*VTOPD");
    this.setFullSyntax("*VTOPTICALDRIVE <MODE>");
    this.setAbbreviatedSyntax("*VTOPD <MD>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getOpticalDriveOperation())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (parsed.length >= 2)
      {
        if (parsed[1].toUpperCase().startsWith("O"))
        {
          if (session.getOpticalDriveOperation().isFinished())
          {
            session.getOpticalDriveOperation().joinThread();
          }
          if (!session.getOpticalDriveOperation().aliveThread())
          {
            session.getOpticalDriveOperation().setFinished(false);
            session.getOpticalDriveOperation().setOpen(true);
            session.getOpticalDriveOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another optical disc drive operation is still running!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed[1].toUpperCase().startsWith("C"))
        {
          if (session.getOpticalDriveOperation().isFinished())
          {
            session.getOpticalDriveOperation().joinThread();
          }
          if (!session.getOpticalDriveOperation().aliveThread())
          {
            session.getOpticalDriveOperation().setFinished(false);
            session.getOpticalDriveOperation().setOpen(false);
            session.getOpticalDriveOperation().startThread();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Another optical disc drive operation is still running!\nVT>");
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
  }
  
  public void close()
  {
    
  }
}
