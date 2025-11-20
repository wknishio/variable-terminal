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
    boolean waitFor = false;
    if (parsed.length == 2)
    {
      if (parsed[1].toUpperCase().contains("W"))
      {
        waitFor = true;
      }
      if (parsed[1].toUpperCase().contains("S"))
      {
        //session.getFileTransferServer().getHandler().getSession().getTransaction().setStopped(true);
      }
      else if (waitFor)
      {
//        if (!session.getFileTransferServer().aliveThread())
//        {
//          connection.getResultWriter().write("\rVT>No file transfer is running!\nVT>");
//          connection.getResultWriter().flush();
//        }
      }
    }
    else if (parsed.length >= 4)
    {
      if (parsed[1].toUpperCase().contains("W"))
      {
        waitFor = true;
      }
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
    return true;
  }
  
  public void waitFor()
  {
    session.getFileTransferServer().joinThread();
  }
}
