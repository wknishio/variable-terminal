package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.server.print.VTServerPrintDataTask;

public class VTPRINTDATA extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTPRINTDATA()
  {
    this.setFullName("*VTPRINTDATA");
    this.setAbbreviatedName("*VTPD");
    this.setFullSyntax("*VTPRINTDATA [MODE] [DATA] [PRINTER]");
    this.setAbbreviatedSyntax("*VTPD [MD] [DT] [PR]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    boolean waitFor = false;
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
          connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Another print data task is still running!\nVT>");
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
          if (session.getPrintDataTask().isFinished())
          {
            session.getPrintDataTask().joinThread();
          }
          if (!session.getPrintDataTask().aliveThread())
          {
            connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            //connection.getResultWriter().write("\rVT>Stopping current print data task...\nVT>");
            //connection.getResultWriter().flush();
            session.getPrintDataTask().setStopped(true);
          }
        }
        else if (waitFor)
        {
          if (!session.getPrintDataTask().aliveThread())
          {
            connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
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
            if (parsed[1].toUpperCase().contains("S"))
            {
              connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              if (parsed[1].toUpperCase().contains("T") || parsed[1].toUpperCase().contains("F") || parsed[1].toUpperCase().contains("U") || parsed[1].toUpperCase().contains("N"))
              {
                session.getPrintDataTask().setFinished(false);
                session.getPrintDataTask().setData(parsed[2]);
                session.getPrintDataTask().setPrintServiceNumber(null);
                if (parsed[1].toUpperCase().contains("T"))
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_TEXT);
                }
                else
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_FILE);
                }
                if (parsed[1].toUpperCase().contains("F"))
                {
                  session.getPrintDataTask().setFileEncoding("F");
                }
                if (parsed[1].toUpperCase().contains("U"))
                {
                  session.getPrintDataTask().setFileEncoding("U");
                }
                if (parsed[1].toUpperCase().contains("N"))
                {
                  session.getPrintDataTask().setFileEncoding("N");
                }
                connection.getResultWriter().write("\rVT>Print mode: [" + parsed[1] + "], data: [" + parsed[2] + "], service: [Default]\nVT>");
                connection.getResultWriter().flush();
                session.getPrintDataTask().startThread();
              }
              else
              {
                connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          else
          {
            if (parsed[1].toUpperCase().contains("S"))
            {
              //connection.getResultWriter().write("\rVT>Stopping current print data task...\nVT>");
              //connection.getResultWriter().flush();
              session.getPrintDataTask().setStopped(true);
            }
            else
            {
              connection.getResultWriter().write("\rVT>Another print data task is still running!\nVT>");
              connection.getResultWriter().flush();
            }
          }
        }
      }
      else if (parsed.length >= 4)
      {
        if (parsed[1].toUpperCase().contains("W"))
        {
          waitFor = true;
        }
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
            if (parsed[1].toUpperCase().contains("S"))
            {
              connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              if (parsed[1].toUpperCase().contains("T") || parsed[1].toUpperCase().contains("F") || parsed[1].toUpperCase().contains("U") || parsed[1].toUpperCase().contains("N"))
              {
                session.getPrintDataTask().setFinished(false);
                session.getPrintDataTask().setData(parsed[2]);
                session.getPrintDataTask().setPrintServiceNumber(Integer.parseInt(parsed[3]));
                if (parsed[1].toUpperCase().contains("T"))
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_TEXT);
                }
                else
                {
                  session.getPrintDataTask().setMode(VTServerPrintDataTask.MODE_FILE);
                }
                if (parsed[1].toUpperCase().contains("F"))
                {
                  session.getPrintDataTask().setFileEncoding("F");
                }
                if (parsed[1].toUpperCase().contains("U"))
                {
                  session.getPrintDataTask().setFileEncoding("U");
                }
                if (parsed[1].toUpperCase().contains("N"))
                {
                  session.getPrintDataTask().setFileEncoding("N");
                }
                connection.getResultWriter().write("\rVT>Print mode: [" + parsed[1] + "], data: [" + parsed[2] + "], service: [" + parsed[3] + "]\nVT>");
                connection.getResultWriter().flush();
                session.getPrintDataTask().startThread();
              }
              else
              {
                connection.getResultWriter().write("\rVT>No print data task is running!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          else
          {
            if (parsed[1].toUpperCase().contains("S"))
            {
              //connection.getResultWriter().write("\rVT>Stopping current print data task...\nVT>");
              //connection.getResultWriter().flush();
              session.getPrintDataTask().setStopped(true);
            }
            else
            {
              connection.getResultWriter().write("\rVT>Another print data task is still running!\nVT>");
              connection.getResultWriter().flush();
            }
          }
        }
      }
      else
      {
        connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
    catch (NumberFormatException e)
    {
      connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      connection.getResultWriter().flush();
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
    session.getPrintDataTask().joinThread();
  }
}