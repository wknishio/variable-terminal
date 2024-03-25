package org.vash.vate.server.console.local.standard.command;

import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.connection.VTServerConnector;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTSETTING extends VTServerStandardLocalConsoleCommandProcessor
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
      String hostAddress = server.getServerConnector().getAddress();
      Integer port = server.getServerConnector().getPort();
      Integer natPort = server.getServerConnector().getNatPort();
      String proxyType = server.getServerConnector().getProxyType();
      String proxyAddress = server.getServerConnector().getProxyAddress();
      Integer proxyPort = server.getServerConnector().getProxyPort();
      String proxyUser = server.getServerConnector().getProxyUser();
      String proxyPassword = server.getServerConnector().getProxyPassword();
      String encryptionType = server.getServerConnector().getEncryptionType();
      String encryptionPassword = "";
      Integer sessionsMaximum = server.getServerConnector().getSessionsMaximum();
      String sessionShell = server.getServerConnector().getSessionShell();
      if (server.getServerConnector().getEncryptionKey() != null)
      {
        encryptionPassword = new String(server.getServerConnector().getEncryptionKey(), "UTF-8");
      }
      message.append("\rVT>List of server connection settings:\nVT>");
      
      if (server.getServerConnector().isPassive())
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
//      if (server.getServerConnector().isUseProxyAuthentication())
//      {
//        message.append("\nVT>Proxy authentication(PA): [Enabled]");
//      }
//      else
//      {
//        message.append("\nVT>Proxy authentication(PA): [Disabled]");
//      }
      message.append("\nVT>Proxy user(PU): [" + proxyUser + "]");
      message.append("\nVT>Proxy password(PK): [" + proxyPassword + "]");
      if (encryptionType.toUpperCase().startsWith("V"))
      {
        message.append("\nVT>Encryption type(ET): [VMPC]");
      }
      else if (encryptionType.toUpperCase().startsWith("Z"))
      {
        message.append("\nVT>Encryption type(ET): [ZUC256]");
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
      VTConsole.print(message.toString());
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
            server.saveServerSettingsFile("vate-server.properties");
            VTConsole.print("\rVT>Saved settings file:[vate-server.properties]\nVT>");
          }
          catch (Throwable t)
          {
            VTConsole.print("\rVT>Failed to save settings file:[vate-server.properties]\nVT>");
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            server.saveServerSettingsFile(parsed[2]);
            VTConsole.print("\rVT>Saved settings file:[" + parsed[2] + "]\nVT>");
          }
          catch (Throwable t)
          {
            VTConsole.print("\rVT>Failed to save settings file:[" + parsed[2] + "]\nVT>");
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("LF"))
      {
        if (parsed.length == 2)
        {
          boolean ok = false;
          try
          {
            server.loadServerSettingsFile("vate-server.properties");
            VTConsole.print("\rVT>Loaded settings file:[vate-server.properties]\nVT>");
            ok = true;
          }
          catch (Throwable t)
          {
            VTConsole.print("\rVT>Failed to load settings file:[vate-server.properties]\nVT>");
          }
          if (ok)
          {
            VTServerConnector connector = server.getServerConnector();
            synchronized (connector)
            {
              connector.interruptConnector();
              connector.notify();
            }
          }
        }
        else if (parsed.length >= 3)
        {
          boolean ok = false;
          try
          {
            server.loadServerSettingsFile(parsed[2]);
            VTConsole.print("\rVT>Loaded settings file:[" + parsed[2] + "]\nVT>");
            ok = true;
          }
          catch (Throwable t)
          {
            VTConsole.print("\rVT>Failed to load settings file:[" + parsed[2] + "]\nVT>");
          }
          if (ok)
          {
            VTServerConnector connector = server.getServerConnector();
            synchronized (connector)
            {
              connector.interruptConnector();
              connector.notify();
            }
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
//      else if (parsed[1].equalsIgnoreCase("RC"))
//      {
//        VTConsole.print("\rVT>Reconfiguring all server settings!\nVT>");
//        server.reconfigure();
//        VTServerConnector connector = server.getServerConnector();
//        synchronized (connector)
//        {
//          connector.interruptConnector();
//          connector.notify();
//        }
//      }
      else if (parsed[1].equalsIgnoreCase("SM"))
      {
        if (parsed.length == 2)
        {
          Integer sessionsMaximum = server.getServerConnector().getSessionsMaximum();
          VTConsole.print("\rVT>Session maximum(SM): [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          Integer sessionsMaximum = Integer.parseInt(parsed[2]);
          if (sessionsMaximum < 0)
          {
            sessionsMaximum = null;
          }
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setSessionsMaximum(sessionsMaximum);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Session maximum(SM) set to: [" + (sessionsMaximum == null ? "" : sessionsMaximum) + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("CM"))
      {
        if (parsed.length == 2)
        {
          if (server.getServerConnector().isPassive())
          {
            VTConsole.print("\rVT>Connection mode(CM): [Passive]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Connection mode(CM): [Active]\nVT>");
          }
        }
        else if (parsed.length >= 3)
        {
          boolean passive = !parsed[2].toUpperCase().startsWith("A");
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setPassive(passive);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Connection mode(CM) set to: [" + (passive ? "Passive" : "Active") + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("CH"))
      {
        if (parsed.length == 2)
        {
          String hostAddress = server.getServerConnector().getAddress();
          VTConsole.print("\rVT>Connection host address(CH): [" + hostAddress + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          String hostAddress = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setAddress(hostAddress);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Connection host address(CH) set to: [" + hostAddress + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("CP"))
      {
        if (parsed.length == 2)
        {
          Integer port = server.getServerConnector().getPort();
          VTConsole.print("\rVT>Connection host port(CP): [" + port + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int port = Integer.parseInt(parsed[2]);
            if (port < 1 || port > 65535)
            {
              VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
            }
            else
            {
              VTServerConnector connector = server.getServerConnector();
              synchronized (connector)
              {
                connector.setPort(port);
                connector.interruptConnector();
                connector.notify();
              }
              VTConsole.print("\rVT>Connection host port(CP) set to: [" + port + "]\nVT>");
            }
          }
          catch (NumberFormatException e)
          {
            VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("PT"))
      {
        if (parsed.length == 2)
        {
          String proxyType = server.getServerConnector().getProxyType();
          if (proxyType == null)
          {
            VTConsole.print("\rVT>Proxy type(PT): [None]\nVT>");
          }
          else if (proxyType.toUpperCase().startsWith("H"))
          {
            VTConsole.print("\rVT>Proxy type(PT): [HTTP]\nVT>");
          }
          else if (proxyType.toUpperCase().startsWith("S"))
          {
            VTConsole.print("\rVT>Proxy type(PT): [SOCKS]\nVT>");
          }
          else if (proxyType.toUpperCase().startsWith("A"))
          {
            VTConsole.print("\rVT>Proxy type(PT): [ANY]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Proxy type(PT): [None]\nVT>");
          }
        }
        else if (parsed.length >= 3)
        {
          String proxyType = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setProxyType(proxyType);
            connector.interruptConnector();
            connector.notify();
          }
          if (proxyType.toUpperCase().startsWith("H"))
          {
            VTConsole.print("\rVT>Proxy type(PT) set to: [HTTP]\nVT>");
          }
          else if (proxyType.toUpperCase().startsWith("S"))
          {
            VTConsole.print("\rVT>Proxy type(PT) set to: [SOCKS]\nVT>");
          }
          else if (proxyType.toUpperCase().startsWith("A"))
          {
            VTConsole.print("\rVT>Proxy type(PT) set to: [ANY]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Proxy type(PT) set to: [None]\nVT>");
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("PH"))
      {
        if (parsed.length == 2)
        {
          String proxyAddress = server.getServerConnector().getProxyAddress();
          VTConsole.print("\rVT>Proxy host address(PH): [" + proxyAddress + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          String proxyAddress = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setProxyAddress(proxyAddress);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Proxy host address set to: [" + proxyAddress + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("PP"))
      {
        if (parsed.length == 2)
        {
          Integer proxyPort = server.getServerConnector().getProxyPort();
          if (proxyPort != null)
          {
            VTConsole.print("\rVT>Proxy host port(PP): [" + proxyPort + "]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Proxy host port(PP): []\nVT>");
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int proxyPort = Integer.parseInt(parsed[2]);
            if (proxyPort < 1 || proxyPort > 65535)
            {
              VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
            }
            else
            {
              VTServerConnector connector = server.getServerConnector();
              synchronized (connector)
              {
                connector.setProxyPort(proxyPort);
                connector.interruptConnector();
                connector.notify();
              }
              VTConsole.print("\rVT>Proxy host port(PP) set to: [" + proxyPort + "]\nVT>");
            }
          }
          catch (NumberFormatException e)
          {
            VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
//      else if (parsed[1].equalsIgnoreCase("PA"))
//      {
//        if (parsed.length == 2)
//        {
//          if (!server.getServerConnector().isUseProxyAuthentication())
//          {
//            VTConsole.print("\rVT>Proxy authentication(PA): [Disabled]\nVT>");
//          }
//          else
//          {
//            VTConsole.print("\rVT>Proxy authentication(PA): [Enabled]\nVT>");
//          }
//        }
//        else if (parsed.length >= 3)
//        {
//          if (parsed[2].toUpperCase().startsWith("E"))
//          {
//            VTServerConnector connector = server.getServerConnector();
//            synchronized (connector)
//            {
//              connector.setUseProxyAuthentication(true);
//              connector.interruptConnector();
//              connector.notify();
//            }
//            VTConsole.print("\rVT>Proxy authentication(PA) set to: [Enabled]\nVT>");
//          }
//          else
//          {
//            VTServerConnector connector = server.getServerConnector();
//            synchronized (connector)
//            {
//              connector.setUseProxyAuthentication(false);
//              connector.interruptConnector();
//              connector.notify();
//            }
//            VTConsole.print("\rVT>Proxy authentication(PA) set to: [Disabled]\nVT>");
//          }
//        }
//        else
//        {
//          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
//        }
//      }
      else if (parsed[1].equalsIgnoreCase("PU"))
      {
        if (parsed.length == 2)
        {
          String proxyUser = server.getServerConnector().getProxyUser();
          VTConsole.print("\rVT>Proxy user(PU): [" + proxyUser + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          String proxyUser = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setProxyUser(proxyUser);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Proxy user(PU) set to: [" + proxyUser + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("PK"))
      {
        if (parsed.length == 2)
        {
          String proxyPassword = server.getServerConnector().getProxyPassword();
          VTConsole.print("\rVT>Proxy password(PK): [" + proxyPassword + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          String proxyPassword = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setProxyPassword(proxyPassword);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Proxy password(PK) set to: [" + proxyPassword + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("ET"))
      {
        if (parsed.length == 2)
        {
          String encryptionType = server.getServerConnector().getEncryptionType();
          if (encryptionType.toUpperCase().startsWith("V"))
          {
            VTConsole.print("\rVT>Encryption type(ET): [VMPC]\nVT>");
          }
          else if (encryptionType.toUpperCase().startsWith("Z"))
          {
            VTConsole.print("\rVT>Encryption type(ET): [ZUC256]\nVT>");
          }
          // else if (encryptionType.toUpperCase().startsWith("B"))
          // {
          // VTConsole.print("\rVT>Encryption type(ET): [BLOWFISH]\nVT>");
          // }
          else if (encryptionType.toUpperCase().startsWith("S"))
          {
            VTConsole.print("\rVT>Encryption type(ET): [SALSA]\nVT>");
          }
          else if (encryptionType.toUpperCase().startsWith("H"))
          {
            VTConsole.print("\rVT>Encryption type(ET): [HC256]\nVT>");
          }
//          else if (encryptionType.toUpperCase().startsWith("G"))
//          {
//            VTConsole.print("\rVT>Encryption type(ET): [GRAIN]\nVT>");
//          }
          else if (encryptionType.toUpperCase().startsWith("I"))
          {
            VTConsole.print("\rVT>Encryption type(ET): [ISAAC]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Encryption type(ET): [None]\nVT>");
          }
        }
        else if (parsed.length >= 3)
        {
          String encryptionType = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setEncryptionType(encryptionType);
            connector.interruptConnector();
            connector.notify();
          }
          if (encryptionType.toUpperCase().startsWith("V"))
          {
            VTConsole.print("\rVT>Encryption type(ET) set to: [VMPC]\nVT>");
          }
          else if (encryptionType.toUpperCase().startsWith("Z"))
          {
            VTConsole.print("\rVT>Encryption type(ET) set to: [ZUC256]\nVT>");
          }
          // else if (encryptionType.toUpperCase().startsWith("B"))
          // {
          // VTConsole.print("\rVT>Encryption type(ET) set to:
          // [BLOWFISH]\nVT>");
          // }
          else if (encryptionType.toUpperCase().startsWith("S"))
          {
            VTConsole.print("\rVT>Encryption type(ET) set to: [SALSA]\nVT>");
          }
          else if (encryptionType.toUpperCase().startsWith("H"))
          {
            VTConsole.print("\rVT>Encryption type(ET) set to: [HC256]\nVT>");
          }
//          else if (encryptionType.toUpperCase().startsWith("G"))
//          {
//            VTConsole.print("\rVT>Encryption type(ET) set to: [GRAIN]\nVT>");
//          }
          else if (encryptionType.toUpperCase().startsWith("I"))
          {
            VTConsole.print("\rVT>Encryption type(ET) set to: [ISAAC]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Encryption type(ET) set to: [None]\nVT>");
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("EK"))
      {
        if (parsed.length == 2)
        {
          String encryptionPassword = "";
          if (server.getServerConnector().getEncryptionKey() != null)
          {
            encryptionPassword = new String(server.getServerConnector().getEncryptionKey(), "UTF-8");
          }
          VTConsole.print("\rVT>Encryption password(EK): [" + encryptionPassword + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          String encryptionPassword = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setEncryptionKey(encryptionPassword.getBytes("UTF-8"));
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Encryption password(EK) set to: [" + encryptionPassword + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("CN"))
      {
        if (parsed.length == 2)
        {
          Integer natPort = server.getServerConnector().getNatPort();
          if (natPort != null)
          {
            VTConsole.print("\rVT>Connection nat port(CN): [" + natPort + "]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Connection nat port(CN): []\nVT>");
          }
        }
        else if (parsed.length >= 3)
        {
          try
          {
            int natPort = Integer.parseInt(parsed[2]);
            if (natPort < 1 || natPort > 65535)
            {
              VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
            }
            else
            {
              VTServerConnector connector = server.getServerConnector();
              if (natPort == 0)
              {
                synchronized (connector)
                {
                  connector.setNatPort(null);
                }
                VTConsole.print("\rVT>Connection nat port(CN) set to: []\nVT>");
              }
              else
              {
                synchronized (connector)
                {
                  connector.setNatPort(natPort);
                }
                VTConsole.print("\rVT>Connection nat port(CN) set to: [" + natPort + "]\nVT>");
              }
            }
          }
          catch (NumberFormatException e)
          {
            VTServerConnector connector = server.getServerConnector();
            synchronized (connector)
            {
              connector.setNatPort(null);
            }
            VTConsole.print("\rVT>Connection nat port(CN) set to: []\nVT>");
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("SS"))
      {
        if (parsed.length == 2)
        {
          String sessionShell = server.getServerConnector().getSessionShell();
          VTConsole.print("\rVT>Session shell(SS): [" + sessionShell + "]\nVT>");
        }
        else if (parsed.length >= 3)
        {
          String sessionShell = parsed[2];
          VTServerConnector connector = server.getServerConnector();
          synchronized (connector)
          {
            connector.setSessionShell(sessionShell);
            connector.interruptConnector();
            connector.notify();
          }
          VTConsole.print("\rVT>Session shell(SS) set to: [" + sessionShell + "]\nVT>");
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else if (parsed[1].equalsIgnoreCase("SA"))
      {
        if (parsed.length >= 3)
        {
          String sessionUsers = parsed[2];
          if (server.setMultipleUserCredentials(sessionUsers))
          {
            VTConsole.print("\rVT>Session accounts(SA) set to: [" + sessionUsers + "]\nVT>");
          }
          else
          {
            VTConsole.print("\rVT>Session accounts(SA) set to: []\nVT>");
          }
        }
        else
        {
          VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
        }
      }
      else
      {
        VTConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForServerCommand(parsed[0]));
      }
    }
  }
  
  public void close()
  {
    
  }
}
