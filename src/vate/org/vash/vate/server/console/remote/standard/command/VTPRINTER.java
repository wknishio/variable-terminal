package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTPRINTER extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTPRINTER()
  {
    this.setFullName("*VTPRINTER");
    this.setAbbreviatedName("*VTPR");
    this.setFullSyntax("*VTPRINTER [PRINTER]");
    this.setAbbreviatedSyntax("*VTPR [PR]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getPrintServiceResolver())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (session.getPrintServiceResolver().isFinished())
      {
        session.getPrintServiceResolver().joinThread();
      }
      if (!session.getPrintServiceResolver().aliveThread())
      {
        if (parsed.length >= 2)
        {
          try
          {
            int order = Integer.parseInt(parsed[1]);
            session.getPrintServiceResolver().setNumber(order);
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Print service number [" + parsed[1] + "] is invalid!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          session.getPrintServiceResolver().setNumber(-1);
        }
        session.getPrintServiceResolver().setFinished(false);
        session.getPrintServiceResolver().startThread();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Another print service search is still running!\nVT>");
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