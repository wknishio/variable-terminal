package org.vash.vate.client.startup;

// import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTClientAgentGraphicalStart
{
  public static void main(String[] args)
  {
    // System.setProperty("java.awt.headless", "true");
    VTSystemConsole.setGraphical(false);
    VTSystemConsole.setSeparated(false);
    VTSystemConsole.setRemoteIcon(true);
    VTSystemConsole.setDaemon(true);
    
    if (args.length >= 1)
    {
      VTClient client = new VTClient();
      client.setDaemon(true);
      try
      {
        client.parseParameters(args);
      }
      catch (Throwable e)
      {
        VTRuntimeExit.exit(-1);
      }
      // client.initialize();
      client.start();
    }
    else
    {
      VTClient client = new VTClient();
      client.setDaemon(true);
      // client.initialize();
      // client.configure();
      client.start();
    }
  }
}