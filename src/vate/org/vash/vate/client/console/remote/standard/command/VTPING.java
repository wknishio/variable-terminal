package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;

public class VTPING extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTPING()
  {
    this.setFullName("*VTPING");
    this.setAbbreviatedName("*VTPG");
    this.setFullSyntax("*VTPING");
    this.setAbbreviatedSyntax("*VTPG");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    session.ping();
    connection.getCommandWriter().writeLine(command);
    connection.getCommandWriter().flush();
    long clientTime = session.getLocalNanoDelay();
    long serverTime = session.getRemoteNanoDelay();
    long clientNanoseconds = clientTime;
    long clientMilliseconds = clientTime / 1000000;
    long serverNanoseconds = serverTime;
    long serverMilliseconds = serverTime / 1000000;
    VTMainConsole.printf("\rVT>Client connection latency: [%d] ns or [%d] ms" +
    "\nVT>Server connection latency: [%d] ns or [%d] ms" +
    "\nVT>", clientNanoseconds, clientMilliseconds, serverNanoseconds, serverMilliseconds);
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
