package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTDISPLAY extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTDISPLAY()
  {
    this.setFullName("*VTDISPLAY");
    this.setAbbreviatedName("*VTDP");
    this.setFullSyntax("*VTDISPLAY");
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