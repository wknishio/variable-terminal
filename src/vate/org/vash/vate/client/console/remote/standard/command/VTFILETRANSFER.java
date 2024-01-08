package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;

public class VTFILETRANSFER extends VTClientStandardRemoteConsoleCommandProcessor
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
    synchronized (session.getFileTransferClient().getHandler().getSession().getTransaction())
    {
      if (parsed.length == 2)
      {
        if (parsed[1].toUpperCase().startsWith("S"))
        {
          if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
          {
            session.getFileTransferClient().joinThread();
          }
          if (session.getFileTransferClient().aliveThread())
          {
            // VTTerminal.print(command);
            session.getFileTransferClient().getHandler().getSession().getTransaction().setInterrupted();
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
            session.getFileTransferClient().getHandler().getSession().getTransaction().setStopped(true);
          }
          else
          {
            // VTTerminal.print(command + "\nVT>No file
            // transfer is running!\nVT>");
            VTConsole.print("\nVT>No file transfer is running!\nVT>");
          }
        }
        /*
         * else if (splitCommand[1].toUpperCase().startsWith("A")) { if
         * (session.getFileTransferClient().getHandler().
         * getSession().getTransfer().isFinished()) {
         * session.getFileTransferClient().joinThread(); } if
         * (session.getFileTransferClient().aliveThread()) { VTTerminal.
         * print("\nVT>A file transfer is running!\nVT>"); } else { VTTerminal.
         * print("\nVT>No file transfer is running!\nVT>"); } }
         */
        else
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else if (parsed.length >= 4)
      {
        if (parsed[1].toUpperCase().contains("G") && !parsed[1].toUpperCase().contains("P"))
        {
          if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
          {
            session.getFileTransferClient().joinThread();
          }
          if (!session.getFileTransferClient().aliveThread())
          {
            session.getFileTransferClient().getHandler().getSession().getTransaction().setFinished(false);
            session.getFileTransferClient().getHandler().getSession().getTransaction().setStopped(false);
            session.getFileTransferClient().getHandler().getSession().getTransaction().setCommand(command);
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
            session.getFileTransferClient().startThread();
          }
          else
          {
            VTConsole.print("\nVT>Another file transfer is still running!\nVT>");
          }
        }
        else if (parsed[1].toUpperCase().contains("P") && !parsed[1].toUpperCase().contains("G"))
        {
          if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
          {
            session.getFileTransferClient().joinThread();
          }
          if (!session.getFileTransferClient().aliveThread())
          {
            session.getFileTransferClient().getHandler().getSession().getTransaction().setFinished(false);
            session.getFileTransferClient().getHandler().getSession().getTransaction().setStopped(false);
            session.getFileTransferClient().getHandler().getSession().getTransaction().setCommand(command);
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
            session.getFileTransferClient().startThread();
          }
          else
          {
            VTConsole.print("\nVT>Another file transfer is still running!\nVT>");
          }
        }
        else
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else if (parsed.length == 1)
      {
        if (session.getFileTransferClient().getHandler().getSession().getTransaction().isFinished())
        {
          session.getFileTransferClient().joinThread();
        }
        if (session.getFileTransferClient().aliveThread())
        {
          // long transferDataSize =
          // session.getFileTransferClient().getHandler().getSession().getTransaction().getTransferDataSize();
          // long transferDataCount =
          // session.getFileTransferClient().getHandler().getSession().getTransaction().getTransferDataCount();
          // if (transferDataSize != 0 && transferDataCount != 0)
          // {
          // double completeness =
          // ((double)transferDataCount) * 100 /
          // ((double)transferDataSize);
          // VTConsole.printf("\nVT>A file transfer is
          // running!" +
          // "\nVT>Transferred [" + transferDataCount +
          // "] of [" + transferDataSize + "] bytes
          // (%.2f%%)\nVT>", completeness);
          // VTConsole.print("\nVT>A file transfer is running!" +
          // "\nVT>Transferred [" + transferDataCount + "] bytes!\nVT>");
          // }
          // else
          // {
          // VTConsole.print("\nVT>A file transfer is running!\nVT>");
          // }
          VTConsole.print("\nVT>A file transfer is running!\nVT>");
        }
        else
        {
          VTConsole.print("\nVT>No file transfer is running!\nVT>");
        }
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
