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
      String pingLimit = session.getServer().getPingLimit() > 0 ? "" + session.getServer().getPingLimit() : "";
      String pingInterval = session.getServer().getPingInterval() > 0 ? "" + session.getServer().getPingInterval() : "";
      if (session.getServer().getServerConnector().getEncryptionKey() != null)
      {
        encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
      }
      message.append("\rVT>List of server connection settings:\nVT>");
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
        message.append("\nVT>Proxy type(PT): []");
      }
      else if (proxyType.toUpperCase().startsWith("H"))
      {
        message.append("\nVT>Proxy type(PT): [HTTP]");
      }
      else if (proxyType.toUpperCase().startsWith("S"))
      {
        message.append("\nVT>Proxy type(PT): [SOCKS]");
      }
      else if (proxyType.toUpperCase().startsWith("P"))
      {
        message.append("\nVT>Proxy type(PT): [PLUS]");
      }
      else
      {
        message.append("\nVT>Proxy type(PT): []");
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
      if (encryptionType.toUpperCase().startsWith("S"))
      {
        message.append("\nVT>Encryption type(ET): [SALSA]");
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        message.append("\nVT>Encryption type(ET): [HC]");
      }
      else if (encryptionType.toUpperCase().startsWith("Z"))
      {
        message.append("\nVT>Encryption type(ET): [ZUC]");
      }
      else if (encryptionType.toUpperCase().startsWith("T"))
      {
        message.append("\nVT>Encryption type(ET): [THREEFISH]");
      }
      else if (encryptionType.toUpperCase().startsWith("L"))
      {
        message.append("\nVT>Encryption type(ET): [LEA]");
      }
      else
      {
        message.append("\nVT>Encryption type(ET): []");
      }
      message.append("\nVT>Encryption password(EK): [" + encryptionPassword + "]");
      message.append("\nVT>Ping limit(PL): [" + pingLimit + "]");
      message.append("\nVT>Ping interval(PI): [" + pingInterval + "]");
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
            connection.getResultWriter().write("\rVT>Saved settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\rVT>Failed to save settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            session.getServer().saveServerSettingsFile(parsed[2]);
            connection.getResultWriter().write("\rVT>Saved settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\rVT>Failed to save settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
            connection.getResultWriter().write("\rVT>Loaded settings file:[vate-server.properties]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\rVT>Failed to load settings file:[vate-server.properties]");
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
            connection.getResultWriter().write("\rVT>Loaded settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\rVT>Failed to load settings file:[" + parsed[2] + "]");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("SM"))
      {
        if (parsed.length == 2)
        {
          Integer sessionsMaximum = session.getServer().getServerConnector().getSessionsMaximum();
          connection.getResultWriter().write("\rVT>Session maximum(SM): [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Session maximum(SM) set to: [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CM"))
      {
        if (parsed.length == 2)
        {
          if (session.getServer().getServerConnector().isPassive())
          {
            connection.getResultWriter().write("\rVT>Connection mode(CM): [Passive]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Connection mode(CM): [Active]\nVT>");
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
          connection.getResultWriter().write("\rVT>Connection mode(CM) set to: [" + (passive ? "Passive" : "Active") + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CH"))
      {
        if (parsed.length == 2)
        {
          String hostAddress = session.getServer().getServerConnector().getAddress();
          connection.getResultWriter().write("\rVT>Connection host address(CH): [" + hostAddress + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Connection host address(CH) set to: [" + hostAddress + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("CP"))
      {
        if (parsed.length == 2)
        {
          Integer port = session.getServer().getServerConnector().getPort();
          connection.getResultWriter().write("\rVT>Connection host port(CP): [" + port + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int port = Integer.parseInt(parsed[2]);
            if (port < 1 || port > 65535)
            {
              connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
              connection.getResultWriter().write("\rVT>Connection host port(CP) set to: [" + port + "]\nVT>");
              connection.getResultWriter().flush();
            }
          }
          catch (NumberFormatException e)
          {
            connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
            connection.getResultWriter().write("\rVT>Proxy type(PT): []\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT): [HTTP]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT): [SOCKS]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("P"))
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT): [PLUS]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT): []\nVT>");
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
            connection.getResultWriter().write("\rVT>Proxy type(PT) set to: [HTTP]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT) set to: [SOCKS]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (proxyType.toUpperCase().startsWith("P"))
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT) set to: [PLUS]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Proxy type(PT) set to: []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PH"))
      {
        if (parsed.length == 2)
        {
          String proxyAddress = session.getServer().getServerConnector().getProxyAddress();
          connection.getResultWriter().write("\rVT>Proxy host address(PH): [" + proxyAddress + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Proxy host address set to: [" + proxyAddress + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
            connection.getResultWriter().write("\rVT>Proxy host port(PP): [" + proxyPort + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Proxy host port(PP): []\nVT>");
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
              connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
              connection.getResultWriter().write("\rVT>Proxy host port(PP) set to: [" + proxyPort + "]\nVT>");
              connection.getResultWriter().flush();
            }
          }
          catch (NumberFormatException e)
          {
            connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
//      else if (parsed[1].equalsIgnoreCase("PA"))
//      {
//        if (parsed.length == 2)
//        {
//          if (!session.getServer().getServerConnector().isUseProxyAuthentication())
//          {
//            connection.getResultWriter().write("\rVT>Proxy authentication(PA): [Disabled]\nVT>");
//            connection.getResultWriter().flush();
//          }
//          else
//          {
//            connection.getResultWriter().write("\rVT>Proxy authentication(PA): [Enabled]\nVT>");
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
//            connection.getResultWriter().write("\rVT>Proxy authentication(PA) set to: [Enabled]\nVT>");
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
//            connection.getResultWriter().write("\rVT>Proxy authentication(PA) set to: [Disabled]\nVT>");
//            connection.getResultWriter().flush();
//          }
//        }
//        else
//        {
//          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
//          connection.getResultWriter().flush();
//        }
//      }
      else if (parsed[1].equalsIgnoreCase("PU"))
      {
        if (parsed.length == 2)
        {
          String proxyUser = session.getServer().getServerConnector().getProxyUser();
          connection.getResultWriter().write("\rVT>Proxy user(PU): [" + proxyUser + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Proxy user(PU) set to: [" + proxyUser + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PK"))
      {
        if (parsed.length == 2)
        {
          String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
          connection.getResultWriter().write("\rVT>Proxy password(PK): [" + proxyPassword + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Proxy password(PK) set to: [" + proxyPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("ET"))
      {
        if (parsed.length == 2)
        {
          String encryptionType = session.getServer().getServerConnector().getEncryptionType();
          if (encryptionType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET): [SALSA]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET): [HC]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("Z"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET): [ZUC]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("T"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET): [THREEFISH]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("L"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET): [LEA]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET): []\nVT>");
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
          if (encryptionType.toUpperCase().startsWith("S"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET) set to: [SALSA]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("H"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET) set to: [HC]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("Z"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET) set to: [ZUC]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("T"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET) set to: [THREEFISH]\nVT>");
            connection.getResultWriter().flush();
          }
          else if (encryptionType.toUpperCase().startsWith("L"))
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET) set to: [LEA]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Encryption type(ET) set to: []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
          connection.getResultWriter().write("\rVT>Encryption password(EK): [" + encryptionPassword + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Encryption password(EK) set to: [" + encryptionPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
            connection.getResultWriter().write("\rVT>Connection nat port(CN): [" + natPort + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Connection nat port(CN): []\nVT>");
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
              connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
                connection.getResultWriter().write("\rVT>Connection nat port(CN) set to: []\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                synchronized (connector)
                {
                  connector.setNatPort(natPort);
                }
                connection.getResultWriter().write("\rVT>Connection nat port(CN) set to: [" + natPort + "]\nVT>");
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
            connection.getResultWriter().write("\rVT>Connection nat port(CN) set to: []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("SS"))
      {
        if (parsed.length == 2)
        {
          String sessionShell = session.getServer().getServerConnector().getSessionShell();
          
          connection.getResultWriter().write("\rVT>Session shell(SS): [" + sessionShell + "]\nVT>");
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
          connection.getResultWriter().write("\rVT>Session shell(SS) set to: [" + sessionShell + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
            connection.getResultWriter().write("\rVT>Session accounts(SA) set to: [" + sessionUsers + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\rVT>Session accounts(SA) set to: []\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PL"))
      {
        if (parsed.length == 2)
        {
          String pingLimit = (session.getServer().getPingLimit() > 0 ? "" + session.getServer().getPingLimit() : "");
          connection.getResultWriter().write("\rVT>Ping limit(PL): [" + pingLimit + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int pingLimit = Integer.parseInt(parsed[2]);
            if (pingLimit > 0)
            {
              session.getServer().setPingLimit(pingLimit);
              connection.getResultWriter().write("\rVT>Ping limit(PL) set to: [" + (session.getServer().getPingLimit() > 0 ? session.getServer().getPingLimit() : "") + "]\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              session.getServer().setPingLimit(0);
              connection.getResultWriter().write("\rVT>Ping limit(PL) set to: [" + (session.getServer().getPingLimit() > 0 ? session.getServer().getPingLimit() : "") + "]\nVT>");
              connection.getResultWriter().flush();
            }
          }
          catch (NumberFormatException e)
          {
            session.getServer().setPingLimit(0);
            connection.getResultWriter().write("\rVT>Ping limit(PL) set to: [" + (session.getServer().getPingLimit() > 0 ? session.getServer().getPingLimit() : "") + "]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("PI"))
      {
        if (parsed.length == 2)
        {
          String pingInterval = (session.getServer().getPingInterval() > 0 ? "" + session.getServer().getPingInterval() : "");
          connection.getResultWriter().write("\rVT>Ping interval(PI): [" + pingInterval + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int pingInterval = Integer.parseInt(parsed[2]);
            if (pingInterval > 0)
            {
              session.getServer().setPingInterval(pingInterval);
              connection.getResultWriter().write("\rVT>Ping interval(PI) set to: [" + (session.getServer().getPingInterval() > 0 ? session.getServer().getPingInterval() : "") + "]\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              session.getServer().setPingInterval(0);
              connection.getResultWriter().write("\rVT>Ping interval(PI) set to: [" + (session.getServer().getPingInterval() > 0 ? session.getServer().getPingInterval() : "") + "]\nVT>");
              connection.getResultWriter().flush();
            }
          }
          catch (NumberFormatException e)
          {
            session.getServer().setPingInterval(0);
            connection.getResultWriter().write("\rVT>Ping interval(PI) set to: [" + (session.getServer().getPingInterval() > 0 ? session.getServer().getPingInterval() : "") + "]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else
      {
        connection.getResultWriter().write("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
