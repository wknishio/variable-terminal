package org.vash.vate.client.startup;

import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTClientAgentGraphicalStart
{
  public static void main(String[] args)
  {
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
      VTMainNativeUtils.disableTerminalProcessing();
      client.setCommandInputStream(System.in);
      client.setCommandOutputStream(System.out);
      client.start();
    }
    else
    {
      VTClient client = new VTClient();
      client.setDaemon(true);
      VTMainNativeUtils.disableTerminalProcessing();
      client.setCommandInputStream(System.in);
      client.setCommandOutputStream(System.out);
      client.start();
    }
  }
}