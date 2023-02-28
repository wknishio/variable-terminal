package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;

public class VTZIP extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTZIP()
  {
    this.setFullName("*VTZIP");
    this.setAbbreviatedName("*VTZP");
    this.setFullSyntax("*VTZIP <SIDE> [MODE] [ZIP FILE;]");
    this.setAbbreviatedSyntax("*VTZP <SD> [MD] [ZP FL;]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
//    if (parsed.length >= 2 && parsed[1].toUpperCase().startsWith("L"))
//    {
//      synchronized (session.getZipFileOperation())
//      {
//        if (parsed.length == 3)
//        {
//          if (parsed[2].toUpperCase().startsWith("S"))
//          {
//            if (session.getZipFileOperation().isFinished())
//            {
//              session.getZipFileOperation().joinThread();
//            }
//            if (session.getZipFileOperation().aliveThread())
//            {
//              VTConsole.print("\nVT>Trying to interrupt local zip file operation!\nVT>");
//              session.getZipFileOperation().interruptThread();
//              session.getZipFileOperation().stopThread();
//            }
//            else
//            {
//              VTConsole.print("\nVT>No local zip file operation is running!\nVT>");
//            }
//          }
//          else
//          {
//            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//          }
//        }
//        else if (parsed.length == 2)
//        {
//          if (session.getZipFileOperation().isFinished())
//          {
//            session.getZipFileOperation().joinThread();
//          }
//          if (session.getZipFileOperation().aliveThread())
//          {
//            VTConsole.print("\nVT>A local zip file operation is still running!\nVT>");
//          }
//          else
//          {
//            VTConsole.print("\nVT>No local zip file operation is running!\nVT>");
//          }
//        }
//        else if (parsed.length >= 5)
//        {
//          if (session.getZipFileOperation().isFinished())
//          {
//            session.getZipFileOperation().joinThread();
//          }
//          if (!session.getZipFileOperation().aliveThread())
//          {
//            if (parsed[2].toUpperCase().startsWith("C"))
//            {
//              session.getZipFileOperation().setOperation(VT.VT_ZIP_FILE_COMPRESS);
//            }
//            else if (parsed[2].toUpperCase().startsWith("U"))
//            {
//              session.getZipFileOperation().setOperation(VT.VT_ZIP_FILE_UNCOMPRESS);
//            }
//            else
//            {
//              session.getZipFileOperation().setOperation(VT.VT_ZIP_FILE_DECOMPRESS);
//            }
//            session.getZipFileOperation().setFinished(false);
//            session.getZipFileOperation().setZipFilePath(parsed[3]);
//            session.getZipFileOperation().setSourcePaths(parsed[4].split(";"));
//            session.getZipFileOperation().startThread();
//          }
//          else if (parsed[2].toUpperCase().startsWith("S"))
//          {
//            if (session.getZipFileOperation().aliveThread())
//            {
//              VTConsole.print("\nVT>Trying to interrupt local zip file operation!\nVT>");
//              session.getZipFileOperation().interruptThread();
//              session.getZipFileOperation().stopThread();
//            }
//            else
//            {
//              VTConsole.print("\nVT>No local zip file operation is running!\nVT>");
//            }
//          }
//          else
//          {
//            VTConsole.print("\nVT>Another local zip file operation is still running!\nVT>");
//          }
//        }
//        else
//        {
//          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//        }
//      }
//    }
//    else if (parsed.length >= 2 && parsed[1].toUpperCase().startsWith("R"))
//    {
//      connection.getCommandWriter().write(command + "\n");
//      connection.getCommandWriter().flush();
//    }
//    else
//    {
//      VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//    }
  }
  
  public void close()
  {
    
  }
}
