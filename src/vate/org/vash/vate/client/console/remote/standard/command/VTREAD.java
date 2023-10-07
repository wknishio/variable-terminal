package org.vash.vate.client.console.remote.standard.command;

import java.nio.charset.Charset;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

public class VTREAD extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTREAD()
  {
    this.setFullName("*VTREAD");
    this.setAbbreviatedName("*VTRD");
    this.setFullSyntax("*VTREAD <FILE;>");
    this.setAbbreviatedSyntax("*VTRD <FL;>");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length >= 2)
    {
      String parameter = command.substring(parsed[0].length() + 1);
      try
      {
        VTConsole.print("\nVT>Running client text files commands: [" + parameter + "]\nVT>");
        session.getClientWriter().executeFileScripts(parameter, Charset.forName("UTF-8"), true);
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
    return false;
  }
}