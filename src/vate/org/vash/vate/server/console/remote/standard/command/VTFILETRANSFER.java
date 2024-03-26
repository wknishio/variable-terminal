package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTFILETRANSFER extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTFILETRANSFER()
  {
    this.setFullName("*VTFILETRANSFER");
    this.setAbbreviatedName("*VTFT");
    this.setFullSyntax("*VTFILETRANSFER [MODE] [SOURCE; TARGET]");
    this.setAbbreviatedSyntax("*VTFT [MD] [SC; TG]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 2)
    {
      if (parsed[1].toUpperCase().startsWith("S"))
      {
        session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(true);
      }
      else
      {
        
      }
    }
    else if (parsed.length >= 4)
    {
      if (parsed[1].toUpperCase().contains("G"))
      {
        if (session.getFileTransferServer().aliveThread())
        {
          session.getFileTransferServer().joinThread();
        }
        session.getFileTransferServer().getHandler().getSession().getTransaction().setFinished(false);
        session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(false);
        session.getFileTransferServer().getHandler().getSession().getTransaction().setCommand(command);
        session.getFileTransferServer().startThread();
      }
      else if (parsed[1].toUpperCase().contains("P"))
      {
        if (session.getFileTransferServer().aliveThread())
        {
          session.getFileTransferServer().joinThread();
        }
        session.getFileTransferServer().getHandler().getSession().getTransaction().setFinished(false);
        session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(false);
        session.getFileTransferServer().getHandler().getSession().getTransaction().setCommand(command);
        session.getFileTransferServer().startThread();
      }
      else
      {
        
      }
    }
    else
    {
      
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
