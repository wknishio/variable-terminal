package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDISPLAYS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTDISPLAYS()
  {
    this.setFullName("*VTDISPLAYS");
    this.setAbbreviatedName("*VTDP");
    this.setFullSyntax("*VTDISPLAYS");
    this.setAbbreviatedSyntax("*VTDP");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    synchronized (session.getGraphicsDeviceResolver())
    {
      // connection.getResultWriter().write(command);
      // connection.getResultWriter().flush();
      if (session.getGraphicsDeviceResolver().isFinished())
      {
        session.getGraphicsDeviceResolver().joinThread();
      }
      if (!session.getGraphicsDeviceResolver().aliveThread())
      {
        session.getGraphicsDeviceResolver().setFinished(false);
        session.getGraphicsDeviceResolver().startThread();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Another graphical display device search is still running\nVT>");
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