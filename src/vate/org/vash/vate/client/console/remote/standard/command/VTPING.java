package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;

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
    session.getNanoPingService().ping();
    // connection.getCommandWriter().write(command + "\n");
    // connection.getCommandWriter().flush();
    long clientTime = session.getLocalNanoDelay();
    long serverTime = session.getRemoteNanoDelay();
    long clientNanoseconds = clientTime;
    long clientMillisseconds = clientTime / 1000000;
    long serverNanoseconds = serverTime;
    long serverMillisseconds = serverTime / 1000000;
    // long estimated = ((clientTime + serverTime) / 2);
    // VTTerminal.printf("\nVT>Current client/server network
    // connection latency: %.2f ms\nVT>", estimated);
    VTConsole.printf("\nVT>Client connection latency: [%d] ns or [%d] ms\nVT>Server connection latency: [%d] ns or [%d] ms\nVT>", clientNanoseconds, clientMillisseconds, serverNanoseconds, serverMillisseconds);
  }
  
  public void close()
  {
    
  }
}
