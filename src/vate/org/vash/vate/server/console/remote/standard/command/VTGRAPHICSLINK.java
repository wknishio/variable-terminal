package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTGRAPHICSLINK extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTGRAPHICSLINK()
  {
    this.setFullName("*VTGRAPHICSLINK");
    this.setAbbreviatedName("*VTGL");
    this.setFullSyntax("*VTGRAPHICSLINK [MODE]");
    this.setAbbreviatedSyntax("*VTGL [MD]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      if (parsed[1].toUpperCase().startsWith("V"))
      {
        session.getGraphicsServer().joinThread();
        session.getGraphicsServer().setReadOnly(true);
        session.getGraphicsServer().startThread();
      }
      else if (parsed[1].toUpperCase().startsWith("C"))
      {
        session.getGraphicsServer().joinThread();
        session.getGraphicsServer().setReadOnly(false);
        session.getGraphicsServer().startThread();
      }
      else
      {
        
      }
    }
    else if (parsed.length == 1)
    {
      if (session.getGraphicsServer().aliveThread())
      {
        session.getGraphicsServer().setStopped(true);
        session.getGraphicsServer().joinThread();
      }
      else
      {
        session.getGraphicsServer().joinThread();
        session.getGraphicsServer().setReadOnly(false);
        session.getGraphicsServer().startThread();
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
