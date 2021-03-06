package org.vate.server.console.remote.standard.command;

import org.vate.help.VTHelpManager;
import org.vate.server.connection.VTServerConnector;
import org.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;

public class VTACCESS extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTACCESS()
  {
    this.setFullName("*VTACCESS");
    this.setAbbreviatedName("*VTAC");
    this.setFullSyntax("*VTACCESS [NAME] [VALUE]");
    this.setAbbreviatedSyntax("*VTAC [NM] [VL]");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      message.setLength(0);
      int sessionsLimit = session.getServer().getServerConnector().getSessionsLimit();
      String hostAddress = session.getServer().getServerConnector().getAddress();
      Integer port = session.getServer().getServerConnector().getPort();
      String proxyType = session.getServer().getServerConnector().getProxyType();
      String proxyAddress = session.getServer().getServerConnector().getProxyAddress();
      Integer proxyPort = session.getServer().getServerConnector().getProxyPort();
      String proxyUser = session.getServer().getServerConnector().getProxyUser();
      String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
      String encryptionType = session.getServer().getServerConnector().getEncryptionType();
      String encryptionPassword = "";
      Integer natPort = session.getServer().getServerConnector().getNatPort();
      if (session.getServer().getServerConnector().getEncryptionKey() != null)
      {
        encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
      }
      message.append("\nVT>List of connection settings on server:\nVT>");
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
        message.append("\nVT>Connection nat port(NP): [" + natPort + "]");
      }
      else
      {
        message.append("\nVT>Connection nat port(NP): []");
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
      if (session.getServer().getServerConnector().isUseProxyAuthentication())
      {
        message.append("\nVT>Proxy authentication(PA): [Enabled]");
      }
      else
      {
        message.append("\nVT>Proxy authentication(PA): [Disabled]");
      }
      message.append("\nVT>Proxy user(PU): [" + proxyUser + "]");
      message.append("\nVT>Proxy password(PS): [" + proxyPassword + "]");
      if (encryptionType.toUpperCase().startsWith("R"))
      {
        message.append("\nVT>Encryption type(ET): [RC4]");
      }
      else if (encryptionType.toUpperCase().startsWith("A"))
      {
        message.append("\nVT>Encryption type(ET): [AES]");
      }
      else
      {
        message.append("\nVT>Encryption type(ET): [None]");
      }
      message.append("\nVT>Encryption password(ES): [" + encryptionPassword + "]");
      message.append("\nVT>Sessions limit(SL): [" + sessionsLimit + "]");
      message.append("\nVT>\nVT>End of connection settings list on server\nVT>");
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
            session.getServer().saveServerSettingsFile("variable-terminal-server.properties");
            connection.getResultWriter().write("\nVT>Saved settings file:[variable-terminal-server.properties]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Failed to save settings file:[variable-terminal-server.properties]");
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
            session.getServer().loadServerSettingsFile("variable-terminal-server.properties");
            VTServerConnector connector = session.getServer().getServerConnector();
            synchronized (connector)
            {
              connector.interruptConnector();
              connector.notify();
            }
            connection.getResultWriter().write("\nVT>Loaded settings file:[variable-terminal-server.properties]");
            connection.getResultWriter().flush();
          }
          catch (Throwable t)
          {
            connection.getResultWriter().write("\nVT>Failed to load settings file:[variable-terminal-server.properties]");
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
      else if (parsed[1].equalsIgnoreCase("SL"))
      {
        if (parsed.length == 2)
        {
          int sessionsLimit = session.getServer().getServerConnector().getSessionsLimit();
          connection.getResultWriter().write("\nVT>Sessions limit(SL): [" + sessionsLimit + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else if (parsed.length >= 3)
        {
          int sessionsLimit = Integer.parseInt(parsed[2]);
          if (sessionsLimit < 0)
          {
            sessionsLimit = 0;
          }
          VTServerConnector connector = session.getServer().getServerConnector();
          synchronized (connector)
          {
            connector.setSessionsLimit(sessionsLimit);
            connector.interruptConnector();
            connector.notify();
          }
          connection.getResultWriter().write("\nVT>Sessions limit(SL) set to: [" + sessionsLimit + "]\nVT>");
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
      else if (parsed[1].equalsIgnoreCase("PA"))
      {
        if (parsed.length == 2)
        {
          if (!session.getServer().getServerConnector().isUseProxyAuthentication())
          {
            connection.getResultWriter().write("\nVT>Proxy authentication(PA): [Disabled]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Proxy authentication(PA): [Enabled]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else if (parsed.length >= 3)
        {
          if (parsed[2].toUpperCase().startsWith("E"))
          {
            VTServerConnector connector = session.getServer().getServerConnector();
            synchronized (connector)
            {
              connector.setUseProxyAuthentication(true);
              connector.interruptConnector();
              connector.notify();
            }
            connection.getResultWriter().write("\nVT>Proxy authentication(PA) set to: [Enabled]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            VTServerConnector connector = session.getServer().getServerConnector();
            synchronized (connector)
            {
              connector.setUseProxyAuthentication(false);
              connector.interruptConnector();
              connector.notify();
            }
            connection.getResultWriter().write("\nVT>Proxy authentication(PA) set to: [Disabled]\nVT>");
            connection.getResultWriter().flush();
          }
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
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
      else if (parsed[1].equalsIgnoreCase("PS"))
      {
        if (parsed.length == 2)
        {
          String proxyPassword = session.getServer().getServerConnector().getProxyPassword();
          connection.getResultWriter().write("\nVT>Proxy password(PS): [" + proxyPassword + "]\nVT>");
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
          connection.getResultWriter().write("\nVT>Proxy password(PS) set to: [" + proxyPassword + "]\nVT>");
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
          else if (encryptionType.toUpperCase().startsWith("A"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET): [AES]\nVT>");
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
          else if (encryptionType.toUpperCase().startsWith("A"))
          {
            connection.getResultWriter().write("\nVT>Encryption type(ET) set to: [AES]\nVT>");
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
      else if (parsed[1].equalsIgnoreCase("ES"))
      {
        if (parsed.length == 2)
        {
          String encryptionPassword = "";
          if (session.getServer().getServerConnector().getEncryptionKey() != null)
          {
            encryptionPassword = new String(session.getServer().getServerConnector().getEncryptionKey(), "UTF-8");
          }
          connection.getResultWriter().write("\nVT>Encryption password(ES): [" + encryptionPassword + "]\nVT>");
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
          connection.getResultWriter().write("\nVT>Encryption password(ES) set to: [" + encryptionPassword + "]\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
        }
      }
      else if (parsed[1].equalsIgnoreCase("NP"))
      {
        if (parsed.length == 2)
        {
          Integer natPort = session.getServer().getServerConnector().getNatPort();
          if (natPort != null)
          {
            connection.getResultWriter().write("\nVT>Connection nat port(NP): [" + natPort + "]\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>Connection nat port(NP): []\nVT>");
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
                connection.getResultWriter().write("\nVT>Connection nat port(NP) set to: []\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                synchronized (connector)
                {
                  connector.setNatPort(natPort);
                }
                connection.getResultWriter().write("\nVT>Connection nat port(NP) set to: [" + natPort + "]\nVT>");
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
            connection.getResultWriter().write("\nVT>Connection nat port(NP) set to: []\nVT>");
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
}
