package org.vash.vate.server.console.local.standard.command;

import java.io.File;

import org.vash.vate.console.VTConsole;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTFILEROOTS extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTFILEROOTS()
  {
    this.setFullName("*VTFILEROOTS");
    this.setAbbreviatedName("*VTFRTS");
    this.setFullSyntax("*VTFILEROOTS");
    this.setAbbreviatedSyntax("*VTFRTS");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    File[] roots = File.listRoots();
    message.append("\rVT>List of server file system roots:\nVT>");
    for (File root : roots)
    {
      message.append("\nVT>Canonical path: [" + root.getCanonicalPath() + "]");
    }
    message.append("\nVT>\nVT>End of server file system roots list\nVT>");
    VTConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
