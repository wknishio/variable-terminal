package org.vash.vate.server.console.local.standard.command;

import java.io.File;

import org.vash.vate.console.VTConsole;
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
