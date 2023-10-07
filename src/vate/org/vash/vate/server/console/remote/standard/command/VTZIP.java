package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTZIP extends VTServerStandardRemoteConsoleCommandProcessor
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
//    if (parsed.length >= 2 && parsed[1].toUpperCase().startsWith("R"))
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
//              connection.getResultWriter().write("\nVT>Trying to interrupt remote zip file operation!\nVT>");
//              connection.getResultWriter().flush();
//              session.getZipFileOperation().interruptThread();
//              session.getZipFileOperation().stopThread();
//            }
//            else
//            {
//              connection.getResultWriter().write("\nVT>No remote zip file operation is running!\nVT>");
//              connection.getResultWriter().flush();
//            }
//          }
//          else
//          {
//            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//            connection.getResultWriter().flush();
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
//            connection.getResultWriter().write("\nVT>A remote zip file operation is still running!\nVT>");
//            connection.getResultWriter().flush();
//          }
//          else
//          {
//            connection.getResultWriter().write("\nVT>No remote zip file operation is running!\nVT>");
//            connection.getResultWriter().flush();
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
//              connection.getResultWriter().write("\nVT>Trying to interrupt remote zip file operation!\nVT>");
//              connection.getResultWriter().flush();
//              session.getZipFileOperation().interruptThread();
//              session.getZipFileOperation().stopThread();
//            }
//            else
//            {
//              connection.getResultWriter().write("\nVT>No remote zip file operation is running!\nVT>");
//              connection.getResultWriter().flush();
//            }
//          }
//          else
//          {
//            connection.getResultWriter().write("\nVT>Another remote zip file operation is still running!\nVT>");
//            connection.getResultWriter().flush();
//          }
//        }
//        else
//        {
//          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//          connection.getResultWriter().flush();
//        }
//      }
//    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return true;
  }
}