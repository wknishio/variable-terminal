package org.vash.vate.client.startup;

import org.vash.vate.client.VTClient;
import org.vash.vate.console.VTConsole;

public class VTClientIncognitoStandardStart
{
  public static void main(String[] args)
  {
    //System.setProperty("java.awt.headless", "true");
    VTConsole.setLanterna(true);
    VTConsole.setGraphical(false);
    VTConsole.setRemoteIcon(true);
    VTConsole.setDaemon(true);
    
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
        System.exit(-1);
      }
      // client.initialize();
      client.start();
    }
    else
    {
      VTClient client = new VTClient();
      // client.initialize();
      // client.configure();
      client.setDaemon(true);
      client.start();
    }
  }
}