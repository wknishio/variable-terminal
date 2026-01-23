package org.vash.vate.client.startup;

import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTClientAgentStandardStart
{
  public static void main(String[] args)
  {
    // System.setProperty("java.awt.headless", "true");
    VTMainConsole.setGraphical(false);
    VTMainConsole.setSeparated(false);
    VTMainConsole.setRemoteIcon(true);
    VTMainConsole.setDaemon(true);
    
    if (args.length >= 1)
    {
      VTClient client = new VTClient();
      try
      {
        client.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      client.setDaemon(true);
      client.start();
    }
    else
    {
      VTClient client = new VTClient();
      client.setDaemon(true);
      client.start();
    }
  }
}