package org.vash.vate.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VTHelpManager
{
  private static Properties helpMap = new Properties();
  
  private static String generalModeParameterHelp = 
  ("\n mode parameters:") + 
  ("\n-C: use client module") + 
  (" | -S: use server module | ") + 
  ("\n-A: use agent module") + 
  (" | -D: use daemon module") + 
  ("\n -H: list parameters");
  
  private static String clientModeParametersHelp = 
  ("\n mode parameters:") + 
  ("\n-A: use agent module") + 
  ("\n-H: list parameters");
  
  private static String serverModeParametersHelp = 
  ("\n mode parameters:") + 
  ("\n-D: use daemon module") + 
  ("\n-H: list parameters");
  
  private static String connnectionParametersHelp = 
  ("\n connection parameters:") + 
  ("\n-LF: load connection settings file") + 
  ("\n-CM: connection mode, passive(P), active(A)") + 
  ("\n-CH: connection host, default null") + 
  ("\n-CP: connection port, default 6060") + 
  ("\n-CN: connection NAT port, default null") + 
  ("\n-PT: proxy type, default none, DIRECT(D), AUTO(A), SOCKS(S), HTTP(H)") + 
  ("\n-PH: proxy host, default null") + 
  ("\n-PP: proxy port, default 1080 for SOCKS or 8080 for HTTP") + 
  //("\n-PA: proxy authentication, default disabled(D), enabled(E)") + 
  ("\n-PU: proxy user, default null") + 
  ("\n-PK: proxy password, default null") + 
  ("\n-ET: encryption type, none/RC4(R)/ISAAC(I)/SALSA(S)/HC256(H)/GRAIN(G)/LEA(L)") + 
  ("\n-EK: encryption password, default null") + 
  ("\n-SS: session shell, default null") + 
  ("\n-SU: session user, default null") + 
  ("\n-SK: session password, default null") + 
  ("\n-SM: session maximum, default 0, only in server") + 
  ("\n-SC: session commands, separated by \"*;\", default null, only in client");
  
  public static void initialize()
  {
    InputStream helpStream = null;
    try
    {
      helpStream = VTHelpManager.class.getResourceAsStream("/org/vash/vate/help/resource/vthelp.properties");
      helpMap.load(helpStream);
    }
    catch (IOException e)
    {
      
    }
    finally
    {
      if (helpStream != null)
      {
        try
        {
          helpStream.close();
        }
        catch (IOException e)
        {
          
        }
      }
    }
  }
  
  public static String getMainHelpForClientCommands()
  {
    return helpMap.getProperty("main.client");
  }
  
  public static String getMinHelpForClientCommands()
  {
    return helpMap.getProperty("min.client");
  }
  
  public static String getMainHelpForServerCommands()
  {
    return helpMap.getProperty("main.server");
  }
  
  public static String getMinHelpForServerCommands()
  {
    return helpMap.getProperty("min.server");
  }
  
  public static String printGeneralModeParameterHelp()
  {
    return generalModeParameterHelp;
  }
  
  public static String printClientModeParametersHelp()
  {
    return clientModeParametersHelp;
  }
  
  public static String printServerModeParametersHelp()
  {
    return serverModeParametersHelp;
  }
  
  public static String printConnnectionParametersHelp()
  {
    return connnectionParametersHelp;
  }
  
  public static String getHelpForClientCommand(String command)
  {
    return helpMap.getProperty("client." + command.toLowerCase(), "\nVT>Client console internal command [" + command + "] not found!\nVT>");
  }
  
  public static String getHelpForServerCommand(String command)
  {
    return helpMap.getProperty("server." + command.toLowerCase(), "\nVT>Server console internal command [" + command + "] not found!\nVT>");
  }
  
  public static String findHelpForClientCommand(String command)
  {
    return helpMap.getProperty("client." + command.toLowerCase());
  }
  
  public static String findHelpForServerCommand(String command)
  {
    return helpMap.getProperty("server." + command.toLowerCase());
  }
}