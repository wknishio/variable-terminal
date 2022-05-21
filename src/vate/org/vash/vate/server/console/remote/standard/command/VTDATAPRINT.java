package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.server.print.VTServerPrintDataTask;

public class VTDATAPRINT extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTDATAPRINT()
  {
    this.setFullName("*VTDATAPRINT");
    this.setAbbreviatedName("*VTDPR");
    this.setFullSyntax("*VTDATAPRINT [MODE] [DATA] [PRINTER]");
    this.setAbbreviatedSyntax("*VTDPR [MD] [DT] [PR]");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    try
    {
      if (parsed.length == 1)
      {
        if (session.getPrintDataTask().isFinished())
        {
          session.getPrintDataTask().joinThread();
        }
        if (!session.getPrintDataTask().aliveThread())
        {
          connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Another print data task is still running!\nVT>");
          connection.getResultWriter().flush();
        }
      }
      else if (parsed.length == 2)
      {
        if (parsed[1].toUpperCase().startsWith("S"))
        {
          if (session.getPrintDataTask().isFinished())
          {
            session.getPrintDataTask().joinThread();
          }
          if (!session.getPrintDataTask().aliveThread())
          {
            connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Stopping current print data task...\nVT>");
            connection.getResultWriter().flush();
            session.getPrintDataTask().setStopped(true);
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
        synchronized (session.getPrintDataTask())
        {
          // connection.getResultWriter().write(command);
          // connection.getResultWriter().flush();
          if (session.getPrintDataTask().isFinished())
          {
            session.getPrintDataTask().joinThread();
          }
          if (!session.getPrintDataTask().aliveThread())
          {
            if (parsed[1].toUpperCase().startsWith("S"))
            {
              connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              if (parsed[1].toUpperCase().startsWith("T") || parsed[1].toUpperCase().startsWith("F") || parsed[1].toUpperCase().startsWith("U") || parsed[1].toUpperCase().startsWith("N"))
              {
                session.getPrintDataTask().setFinished(false);
                session.getPrintDataTask().setData(parsed[2]);
                session.getPrintDataTask().setPrintServiceNumber(null);
                if (parsed[1].toUpperCase().startsWith("T"))
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_TEXT);
                }
                else
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_FILE);
                }
                if (parsed[1].toUpperCase().startsWith("F"))
                {
                  session.getPrintDataTask().setFileEncoding("F");
                }
                if (parsed[1].toUpperCase().startsWith("U"))
                {
                  session.getPrintDataTask().setFileEncoding("U");
                }
                if (parsed[1].toUpperCase().startsWith("N"))
                {
                  session.getPrintDataTask().setFileEncoding("N");
                }
                connection.getResultWriter().write("\nVT>Print mode: [" + parsed[1] + "], data: [" + parsed[2] + "], service: [Default]\nVT>");
                connection.getResultWriter().flush();
                session.getPrintDataTask().startThread();
              }
              else
              {
                connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          else
          {
            if (parsed[1].toUpperCase().startsWith("S"))
            {
              connection.getResultWriter().write("\nVT>Stopping current print data task...\nVT>");
              connection.getResultWriter().flush();
              session.getPrintDataTask().setStopped(true);
            }
            else
            {
              connection.getResultWriter().write("\nVT>Another print data task is still running!\nVT>");
              connection.getResultWriter().flush();
            }
          }
        }
      }
      else if (parsed.length >= 4)
      {
        synchronized (session.getPrintDataTask())
        {
          // connection.getResultWriter().write(command);
          // connection.getResultWriter().flush();
          if (session.getPrintDataTask().isFinished())
          {
            session.getPrintDataTask().joinThread();
          }
          if (!session.getPrintDataTask().aliveThread())
          {
            if (parsed[1].toUpperCase().startsWith("S"))
            {
              connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              if (parsed[1].toUpperCase().startsWith("T") || parsed[1].toUpperCase().startsWith("F") || parsed[1].toUpperCase().startsWith("U") || parsed[1].toUpperCase().startsWith("N"))
              {
                session.getPrintDataTask().setFinished(false);
                session.getPrintDataTask().setData(parsed[2]);
                session.getPrintDataTask().setPrintServiceNumber(Integer.parseInt(parsed[3]));
                if (parsed[1].toUpperCase().startsWith("T"))
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_TEXT);
                }
                else
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_FILE);
                }
                if (parsed[1].toUpperCase().startsWith("F"))
                {
                  session.getPrintDataTask().setFileEncoding("F");
                }
                if (parsed[1].toUpperCase().startsWith("U"))
                {
                  session.getPrintDataTask().setFileEncoding("U");
                }
                if (parsed[1].toUpperCase().startsWith("N"))
                {
                  session.getPrintDataTask().setFileEncoding("N");
                }
                connection.getResultWriter().write("\nVT>Print mode: [" + parsed[1] + "], data: [" + parsed[2] + "], service: [" + parsed[3] + "]\nVT>");
                connection.getResultWriter().flush();
                session.getPrintDataTask().startThread();
              }
              else
              {
                connection.getResultWriter().write("\nVT>No print data task is running!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          else
          {
            if (parsed[1].toUpperCase().startsWith("S"))
            {
              connection.getResultWriter().write("\nVT>Stopping current print data task...\nVT>");
              connection.getResultWriter().flush();
              session.getPrintDataTask().setStopped(true);
            }
            else
            {
              connection.getResultWriter().write("\nVT>Another print data task is still running!\nVT>");
              connection.getResultWriter().flush();
            }
          }
        }
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
    catch (NumberFormatException e)
    {
      connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
    }
  }

  public void close()
  {

  }
}