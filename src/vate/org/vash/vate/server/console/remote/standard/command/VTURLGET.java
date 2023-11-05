package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTURLGET extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTURLGET()
  {
    this.setFullName("*VTURLGET");
    this.setAbbreviatedName("*VTUG");
    this.setFullSyntax("*VTURLGET <URL> [RESULT] [OUTPUT]");
    this.setAbbreviatedSyntax("*VTUG <UR> [RT] [OP]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
//    synchronized (session.getURLInvoker())
//    {
//      if (parsed.length >= 3)
//      {
//        if (session.getURLInvoker().isFinished())
//        {
//          session.getURLInvoker().joinThread();
//        }
//        if (!session.getURLInvoker().aliveThread())
//        {
//          session.getURLInvoker().setFinished(false);
//          session.getURLInvoker().setURL(parsed[1]);
//          if (parsed.length >= 3)
//          {
//            session.getURLInvoker().setFileResult(parsed[2]);
//            if (parsed.length >= 4)
//            {
//              session.getURLInvoker().setFileOutput(parsed[3]);
//            }
//            else
//            {
//              session.getURLInvoker().setFileOutput(null);
//            }
//          }
//          else
//          {
//            session.getURLInvoker().setFileResult(null);
//            session.getURLInvoker().setFileOutput(null);
//          }
//          session.getURLInvoker().startThread();
//        }
//        else
//        {
//          connection.getResultWriter().write("\nVT>Another url data transfer is still running!\nVT>");
//          connection.getResultWriter().flush();
//        }
//      }
//      else
//      {
//        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//        connection.getResultWriter().flush();
//      }
//    }
  }
  
  public void close()
  {
//    try
//    {
//      session.getURLInvoker().close();
//    }
//    catch (Throwable t)
//    {
//      
//    }
  }
  
  public boolean remote()
  {
    return false;
  }
}
