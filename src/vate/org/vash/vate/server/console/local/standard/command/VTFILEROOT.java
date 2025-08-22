package org.vash.vate.server.console.local.standard.command;

import java.io.File;

import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.filesystem.VTRootList;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTFILEROOT extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTFILEROOT()
  {
    this.setFullName("*VTFILEROOT");
    this.setAbbreviatedName("*VTFR");
    this.setFullSyntax("*VTFILEROOT");
    this.setAbbreviatedSyntax("*VTFR");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    File[] roots = new VTRootList().listFiles();
    message.append("\rVT>List of server file roots:\nVT>");
    for (File root : roots)
    {
      //message.append("\nVT>Canonical path: [" + root.getCanonicalPath() + "]");
      message.append("\nVT>" + (root.isFile() ? "File" : "Directory") + ": [" + root.getName() + "]");
    }
    message.append("\nVT>\nVT>End of server file roots list\nVT>");
    VTSystemConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}