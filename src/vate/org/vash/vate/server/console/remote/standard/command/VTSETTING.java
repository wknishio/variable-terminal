package org.vash.vate.server.console.remote.standard.command;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.connection.VTServerConnector;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTSETTING extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTSETTING()
  {
    this.setFullName("*VTSETTING");
    this.setAbbreviatedName("*VTSE");
    this.setFullSyntax("*VTSETTING [NAME] [VALUE]");
    this.setAbbreviatedSyntax("*VTSE [NM] [VL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      message.setLength(0);
      String hostAddress = session.getServer().getServerConnector().getAddress();
      Integer port = session.getServer().getServerConnector().getPort();
      Integer natPort = session.getServer().getServerConnector().getNatPort();
      String proxyType = session.getServer().getServerConnector().getProxyType();
      String proxyAddress = session.getServer().getServerConnector().getProxyAddress();
      Integer proxyPort = session.getServer().getServerConnector().getProxyPort();
      String proxyUser = session.getServer().getServerConnector().getProxyUser();
      String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
      String encryptionType = session.getServer().getServerConnector().getEncryptionType();
      String encryptionPassword = "";
      Integer sessionsMaximum = session.getServer().getServerConnector().getSessionsMaximum();
      String sessionShell = session.getServer().getServerConnector().getSessionShell();
      if (session.getServer().getServerConnector().getEncryptionKey() != null)
      {
        encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
      }
      message.append("\nVT>List of server connection settings:\nVT>");
      if (session.getServer().getServerConnector().isPassive())
      {
        message.append("\nVT>Connection mode(CM): [Passive]");
      }
      else
      {
        message.append("\nVT>Connection mode(CM): [Active]");
      }
      
      message.append("\nVT>Connection host address(CH): [" + hostAddress + "]");
      message.append("\nVT>Connection host port(CP): [" + port + "]");
      if (natPort != null)
      {
        message.append("\nVT>Connection nat port(CN): [" + natPort + "]");
      }
      else
      {
        message.append("\nVT>Connection nat port(CN): []");
      }
      if (proxyType == null)
      {
        message.append("\nVT>Proxy type(PT): [None]");
      }
      else if (proxyType.toUpperCase().startsWith("H"))
      {
        message.append("\nVT>Proxy type(PT): [HTTP]");
      }
      else if (proxyType.toUpperCase().startsWith("S"))
      {
        message.append("\nVT>Proxy type(PT): [SOCKS]");
      }
      else if (proxyType.toUpperCase().startsWith("A"))
      {
        message.append("\nVT>Proxy type(PT): [ANY]");
      }
      else
      {
        message.append("\nVT>Proxy type(PT): [None]");
      }
      message.append("\nVT>Proxy host address(PH): [" + proxyAddress + "]");
      if (proxyPort != null)
      {
        message.append("\nVT>Proxy host port(PP): [" + proxyPort + "]");
      }
      else
      {
        message.append("\nVT>Proxy host port(PP): []");
      }
//      if (session.getServer().getServerConnector().isUseProxyAuthentication())
//      {
//        message.append("\nVT>Proxy authentication(PA): [Enabled]");
//      }
//      else
//      {
//        message.append("\nVT>Proxy authentication(PA): [Disabled]");
//      }
      message.append("\nVT>Proxy user(PU): [" + proxyUser + "]");
      message.append("\nVT>Proxy password(PK): [" + proxyPassword + "]");
      if (encryptionType.toUpperCase().startsWith("R"))
      {
        message.append("\nVT>Encryption type(ET): [RC4]");
      }
      else if (encryptionType.toUpperCase().startsWith("L"))
      {
        message.append("\nVT>Encryption type(ET): [LEA]");
      }
      // else if (encryptionType.toUpperCase().startsWith("B"))
      // {
      // message.append("\nVT>Encryption type(ET): [BLOWFISH]");
      // }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        message.append("\nVT>Encryption type(ET): [SALSA]");
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        message.append("\nVT>Encryption type(ET): [HC256]");
      }
//      else if (encryptionType.toUpperCase().startsWith("G"))
//      {
//        message.append("\nVT>Encryption type(ET): [GRAIN]");
//      }
      else if (encryptionType.toUpperCase().startsWith("I"))
      {
        message.append("\nVT>Encryption type(ET): [ISAAC]");
      }
      else
      {
        message.append("\nVT>Encryption type(ET): [None]");
      }
      message.append("\nVT>Encryption password(EK): [" + encryptionPassword + "]");
      message.append("\nVT>Session shell(SS): [" + sessionShell + "]");
      message.append("\nVT>Session maximum(SM): [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]");
      message.append("\nVT>\nVT>End of server connection settings list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
      message.setLength(0);
    }
    else if (parsed.length >= 2)
    {
      if (parsed[1].equalsIgnoreCase("SF"))
      {
        if (parsed.length == 2)
        {
          try
          {
            session.getServer().saveServerSettingsFile("vate-server.properties");
            connection.getResultWriter().write("\nVT>Saved settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Failed to save settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            session.getServer().saveServerSettingsFile(parsed[2]);
            connection.getResultWriter().write("\nVT>Saved settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Failed to save settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("LF"))
      {
        if (parsed.length == 2)
        {
          try
          {
            session.getServer().loadServerSettingsFile("vate-server.properties");
            VTServerConnector connector = session.getServer().getServerConnector();
            synchronized (connector)
            {
              connector.interruptConnector();
              connector.notify();
            }
            connection.getResultWriter().write("\nVT>Loaded settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Failed to load settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            session.getServer().loadServerSettingsFile(parsed[2]);
            VTServerConnector connector = session.getServer().getServerConnector();
            synchronized (connector)
            {
              connector.interruptConnector();
              connector.notify();
            }
            connection.getResultWriter().write("\nVT>Loaded settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Failed to load settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("SM"))
      {
        if (parsed.length == 2)
        {
          Integer sessionsMaximum = session.getServer().getServerConnector().getSessionsMaximum();
          connection.getResultWriter().write("\nVT>Session maximum(SM): [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          Integer sessionsMaximum = Integer.parseInt(parsed[2]);
          if (sessionsMaximum < 0)
          {
            sessionsMaximum = null;
          }
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setSessionsMaximum(sessionsMaximum);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Session maximum(SM) set to: [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CM"))
      {
        if (parsed.length == 2)
        {
          if (session.getServer().getServerConnector().isPassive())
          {
            connection.getResultWriter().write("\nVT>Connection mode(CM): [Passive]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Connection mode(CM): [Active]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          boolean passive = !parsed[2].toUpperCase().startsWith("A");
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setPassive(passive);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Connection mode(CM) set to: [" + (passive ? "Passive" : "Active") + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CH"))
      {
        if (parsed.length == 2)
        {
          String hostAddress = session.getServer().getServerConnector().getAddress();
          connection.getResultWriter().write("\nVT>Connection host address(CH): [" + hostAddress + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          String hostAddress = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setAddress(hostAddress);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Connection host address(CH) set to: [" + hostAddress + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CP"))
      {
        if (parsed.length == 2)
        {
          Integer port = session.getServer().getServerConnector().getPort();
          connection.getResultWriter().write("\nVT>Connection host port(CP): [" + port + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int port = Integer.parseInt(parsed[2]);
            if (port < 1 || port > 65535)
            {
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              VTServerConnector connector = session.getServer().getServerConnector();
              synchronized (connector)
              {
                connector.setPort(port);
                connector.interruptConnector();
                connector.notify();
              }
              connection.getResultWriter().write("\nVT>Connection host port(CP) set to: [" + port + "]\nVT>");
              connection.getResultWriter().flush();
            }
          }
          catch (NumberFormatException e)
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PT"))
      {
        if (parsed.length == 2)
        {
          String proxyType = session.getServer().getServerConnector().getProxyType();
          if (proxyType == null)
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT): [None]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT): [HTTP]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT): [SOCKS]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("A"))
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT): [ANY]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT): [None]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          String proxyType = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setProxyType(proxyType);
            connector.interruptConnector();
            connector.notify();
          }
          if (proxyType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [HTTP]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [SOCKS]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("A"))
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [ANY]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Proxy type(PT) set to: [None]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PH"))
      {
        if (parsed.length == 2)
        {
          String proxyAddress = session.getServer().getServerConnector().getProxyAddress();
          connection.getResultWriter().write("\nVT>Proxy host address(PH): [" + proxyAddress + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          String proxyAddress = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setProxyAddress(proxyAddress);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Proxy host address set to: [" + proxyAddress + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PP"))
      {
        if (parsed.length == 2)
        {
          Integer proxyPort = session.getServer().getServerConnector().getProxyPort();
          if (proxyPort != null)
          {
            connection.getResultWriter().write("\nVT>Proxy host port(PP): [" + proxyPort + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Proxy host port(PP): []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int proxyPort = Integer.parseInt(parsed[2]);
            if (proxyPort < 1 || proxyPort > 65535)
            {
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              VTServerConnector connector = session.getServer().getServerConnector();
              synchronized (connector)
              {
                connector.setProxyPort(proxyPort);
                connector.interruptConnector();
                connector.notify();
              }
              connection.getResultWriter().write("\nVT>Proxy host port(PP) set to: [" + proxyPort + "]\nVT>");
              connection.getResultWriter().flush();
            }
          }
          catch (NumberFormatException e)
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
//      else if (parsed[1].equalsIgnoreCase("PA"))
//      {
//        if (parsed.length == 2)
//        {
//          if (!session.getServer().getServerConnector().isUseProxyAuthentication())
//          {
//            connection.getResultWriter().write("\nVT>Proxy authentication(PA): [Disabled]\nVT>");
//            connection.getResultWriter().flush();
//          }
//          else
//          {
//            connection.getResultWriter().write("\nVT>Proxy authentication(PA): [Enabled]\nVT>");
//            connection.getResultWriter().flush();
//          }
//        }
//        else if (parsed.length >= 3)
//        {
//          if (parsed[2].toUpperCase().startsWith("E"))
//          {
//            VTServerConnector connector = session.getServer().getServerConnector();
//            synchronized (connector)
//            {
//              connector.setUseProxyAuthentication(true);
//              connector.interruptConnector();
//              connector.notify();
//            }
//            connection.getResultWriter().write("\nVT>Proxy authentication(PA) set to: [Enabled]\nVT>");
//            connection.getResultWriter().flush();
//          }
//          else
//          {
//            VTServerConnector connector = session.getServer().getServerConnector();
//            synchronized (connector)
//            {
//              connector.setUseProxyAuthentication(false);
//              connector.interruptConnector();
//              connector.notify();
//            }
//            connection.getResultWriter().write("\nVT>Proxy authentication(PA) set to: [Disabled]\nVT>");
//            connection.getResultWriter().flush();
//          }
//        }
//        else
//        {
//          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//          connection.getResultWriter().flush();
//        }
//      }
      else if (parsed[1].equalsIgnoreCase("PU"))
      {
        if (parsed.length == 2)
        {
          String proxyUser = session.getServer().getServerConnector().getProxyUser();
          connection.getResultWriter().write("\nVT>Proxy user(PU): [" + proxyUser + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          String proxyUser = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setProxyUser(proxyUser);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Proxy user(PU) set to: [" + proxyUser + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PK"))
      {
        if (parsed.length == 2)
        {
          String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
          connection.getResultWriter().write("\nVT>Proxy password(PK): [" + proxyPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          String proxyPassword = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setProxyPassword(proxyPassword);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Proxy password(PK) set to: [" + proxyPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("ET"))
      {
        if (parsed.length == 2)
        {
          String encryptionType = session.getServer().getServerConnector().getEncryptionType();
          if (encryptionType.toUpperCase().startsWith("R"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [RC4]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("L"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [LEA]\nVT>");
            connection.getResultWriter().flush();
          }
          // else if (encryptionType.toUpperCase().startsWith("B"))
          // {
          // connection.getResultWriter().write("\nVT>Encryption type(ET):
          // [BLOWFISH]\nVT>");
          // connection.getResultWriter().flush();
          // }
          else if (encryptionType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [SALSA]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [HC256]\nVT>");
            connection.getResultWriter().flush();
          }
//          else if (encryptionType.toUpperCase().startsWith("G"))
//          {
//            connection.getResultWriter().write("\nVT>Encryption type(ET): [GRAIN]\nVT>");
//            connection.getResultWriter().flush();
//          }
          else if (encryptionType.toUpperCase().startsWith("I"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [ISAAC]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [None]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          String encryptionType = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setEncryptionType(encryptionType);
            connector.interruptConnector();
            connector.notify();
          }
          if (encryptionType.toUpperCase().startsWith("R"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [RC4]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("L"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [LEA]\nVT>");
            connection.getResultWriter().flush();
          }
          // else if (encryptionType.toUpperCase().startsWith("B"))
          // {
          // connection.getResultWriter().write("\nVT>Encryption type(ET) set
          // to: [BLOWFISH]\nVT>");
          // connection.getResultWriter().flush();
          // }
          else if (encryptionType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [SALSA]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [HC256]\nVT>");
            connection.getResultWriter().flush();
          }
//          else if (encryptionType.toUpperCase().startsWith("G"))
//          {
//            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [GRAIN]\nVT>");
//            connection.getResultWriter().flush();
//          }
          else if (encryptionType.toUpperCase().startsWith("I"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [ISAAC]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [None]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("EK"))
      {
        if (parsed.length == 2)
        {
          String encryptionPassword = "";
          if (session.getServer().getServerConnector().getEncryptionKey() != null)
          {
            encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
          }
          connection.getResultWriter().write("\nVT>Encryption password(EK): [" + encryptionPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          String encryptionPassword = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setEncryptionKey(encryptionPassword.getBytes("UTF-8"));
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Encryption password(EK) set to: [" + encryptionPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CN"))
      {
        if (parsed.length == 2)
        {
          Integer natPort = session.getServer().getServerConnector().getNatPort();
          if (natPort != null)
          {
            connection.getResultWriter().write("\nVT>Connection nat port(CN): [" + natPort + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Connection nat port(CN): []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int natPort = Integer.parseInt(parsed[2]);
            if (natPort < 1 || natPort > 65535)
            {
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              VTServerConnector connector = session.getServer().getServerConnector();
              if (natPort == 0)
              {
                synchronized (connector)
                {
                  connector.setNatPort(null);
                }
                connection.getResultWriter().write("\nVT>Connection nat port(CN) set to: []\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                synchronized (connector)
                {
                  connector.setNatPort(natPort);
                }
                connection.getResultWriter().write("\nVT>Connection nat port(CN) set to: [" + natPort + "]\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          catch (NumberFormatException e)
          {
            VTServerConnector connector = session.getServer().getServerConnector();
            synchronized (connector)
            {
              connector.setNatPort(null);
            }
            connection.getResultWriter().write("\nVT>Connection nat port(CN) set to: []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("SS"))
      {
        if (parsed.length == 2)
        {
          String sessionShell = session.getServer().getServerConnector().getSessionShell();
          
          connection.getResultWriter().write("\nVT>Session shell(SS): [" + sessionShell + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          String sessionShell = parsed[2];
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setSessionShell(sessionShell);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Session shell(SS) set to: [" + sessionShell + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("SA"))
      {
        if (parsed.length >= 3)
        {
          String sessionUsers = parsed[2];
          if (session.getServer().setMultipleUserCredentials(sessionUsers))
          {
            connection.getResultWriter().write("\nVT>Session accounts(SA) set to: [" + sessionUsers + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Session accounts(SA) set to: []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
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
