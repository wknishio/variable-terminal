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
    long nanoseconds = ((clientTime + serverTime) / 2);
    long millisseconds = ((clientTime + serverTime) / 2) / 1000000;
    // long estimated = ((clientTime + serverTime) / 2);
    // VTTerminal.printf("\nVT>Current client/server network
    // connection latency: %.2f ms\nVT>", estimated);
    VTConsole.printf("\nVT>Estimated connection latency: [%d] ms or [%d] ns\nVT>", millisseconds, nanoseconds);
  }
  
  public void close()
  {
    
  }
}
