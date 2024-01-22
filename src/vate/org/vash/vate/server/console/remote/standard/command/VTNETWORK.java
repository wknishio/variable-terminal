package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTNETWORK extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTNETWORK()
  {
    this.setFullName("*VTNETWORK");
    this.setAbbreviatedName("*VTNT");
    this.setFullSyntax("*VTNETWORK [SIDE]");
    this.setAbbreviatedSyntax("*VTNT [SD]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getNetworkInterfaceResolver())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (session.getNetworkInterfaceResolver().isFinished())
      {
        session.getNetworkInterfaceResolver().joinThread();
      }
      if (!session.getNetworkInterfaceResolver().aliveThread())
      {
        session.getNetworkInterfaceResolver().setFinished(false);
        session.getNetworkInterfaceResolver().startThread();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Another network interface search is still running!\nVT>");
        connection.getResultWriter().flush();
      }
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