package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.VT;
import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelBindSocketListener;

public class VTTUNNEL extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTTUNNEL()
  {
    this.setFullName("*VTTUNNEL");
    this.setAbbreviatedName("*VTTN");
    this.setFullSyntax("*VTTUNNEL [MODE] [[BIND] PORT] [[HOST] PORT] [USER/PASSWORD]");
    this.setAbbreviatedSyntax("*VTTN [MD] [[BD] PT] [[HT] PT] [US/PW]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    int channelType = VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT;
    if (parsed.length > 1)
    {
      if (parsed[1].toUpperCase().contains("F"))
      {
        channelType |= VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_LZ4;
      }
      if (parsed[1].toUpperCase().contains("H"))
      {
        channelType |= VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD;
      }
    }
    if (parsed.length == 1)
    {
      message.setLength(0);
      message.append("\nVT>List of connection tunnels:\nVT>");
      //Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
      for (VTTunnelChannelBindSocketListener listener : session.getTunnelsHandler().getConnection().getBindListeners().toArray(new VTTunnelChannelBindSocketListener[] { }))
      {
        if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
        {
          message.append("\nVT>Client TCP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]" + "\nVT>Server TCP redirect address: [" + listener.getChannel().getRedirectHost() + " " + listener.getChannel().getRedirectPort() + "]" + "\nVT>");
        }
        if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
        {
          message.append("\nVT>Client SOCKS/HTTP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]\nVT>");
        }
      }
      VTConsole.print(message.toString());
      connection.getCommandWriter().write(command + "\n");
      connection.getCommandWriter().flush();
    }
    else if (parsed.length == 2)
    {
      //VTConsole.println("parsed[1]:" + parsed[1]);
      if (parsed[1].toUpperCase().contains("L") && !parsed[1].toUpperCase().contains("R"))
      {
        message.setLength(0);
        message.append("\nVT>List of client connection tunnels:\nVT>");
        //Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
        for (VTTunnelChannelBindSocketListener listener : session.getTunnelsHandler().getConnection().getBindListeners().toArray(new VTTunnelChannelBindSocketListener[] { }))
        {
          if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
          {
            message.append("\nVT>Client TCP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]" + "\nVT>Server TCP redirect address: [" + listener.getChannel().getRedirectHost() + " " + listener.getChannel().getRedirectPort() + "]" + "\nVT>");
          }
          if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
          {
            message.append("\nVT>Client SOCKS/HTTP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]\nVT>");
          }
        }
        message.append("\nVT>End of client connection tunnels list\nVT>");
        VTConsole.print(message.toString());
      }
      else if (parsed[1].toUpperCase().contains("R"))
      {
        connection.getCommandWriter().write(command + "\n");
        connection.getCommandWriter().flush();
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else if (parsed.length == 3)
    {
      if (parsed[1].toUpperCase().contains("L") && !parsed[1].toUpperCase().contains("R"))
      {
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            VTTunnelChannelBindSocketListener listener = session.getTunnelsHandler().getConnection().getBindListener("", bindPort);
            if (listener != null)
            {
              listener.close();
              session.getTunnelsHandler().getConnection().removeBindListener(listener);
              VTConsole.print("\nVT>Tunnel bound in client address [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "] removed!\nVT>");
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, "", bindPort))
              {
                VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
              }
            }
          }
        }
        catch (IndexOutOfBoundsException e)
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
        catch (NumberFormatException e)
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else if (parsed[1].toUpperCase().contains("R"))
      {
        connection.getCommandWriter().write(command + "\n");
        connection.getCommandWriter().flush();
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else if (parsed.length >= 4)
    {
      if (parsed[1].toUpperCase().contains("L") && !parsed[1].toUpperCase().contains("R"))
      {
        String socksUser = null;
        String socksPassword = null;
        
        int remainingLength = parsed.length;
        while (parsed[remainingLength - 1].contains("/"))
        {
          String value = parsed[remainingLength - 1];
          socksUser = value.split("/")[0];
          socksPassword = value.split("/")[1];
          remainingLength--;
        }
        //System.out.println("socksUser:" + socksUser);
        //System.out.println("socksPassword:" + socksPassword);
        
        if (remainingLength == 3)
        {
          if (isPort(parsed[2]))
          {
            int bindPort = Integer.parseInt(parsed[2]);
            if (bindPort < 1 || bindPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              VTTunnelChannelBindSocketListener listener = session.getTunnelsHandler().getConnection().getBindListener("", bindPort);
              if (listener != null)
              {
                if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS && socksUser != null && socksPassword != null)
                {
                  if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, "", bindPort, socksUser, socksPassword))
                  {
                    VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
                  }
                  else
                  {
                    VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
                  }
                }
                else
                {
                  listener.close();
                  session.getTunnelsHandler().getConnection().removeBindListener(listener);
                  VTConsole.print("\nVT>Tunnel bound in client address [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "] removed!\nVT>");
                }
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, "", bindPort, socksUser, socksPassword))
                {
                  VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
                }
                else
                {
                  VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
                }
              }
            }
          }
          else
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
        }
        else if (remainingLength == 4)
        {
          if (isPort(parsed[2]) && isPort(parsed[3]))
          {
            int bindPort = Integer.parseInt(parsed[2]);
            int redirectPort = Integer.parseInt(parsed[3]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, "", bindPort, "", redirectPort))
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
              }
            }
          }
          else if (isPort(parsed[3]))
          {
            String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]);
            if (bindPort < 1 || bindPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              VTTunnelChannelBindSocketListener listener = session.getTunnelsHandler().getConnection().getBindListener(bindAddress, bindPort);
              if (listener != null)
              {
                if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS && socksUser != null && socksPassword != null)
                {
                  if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, bindAddress, bindPort, socksUser, socksPassword))
                  {
                    VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                  }
                  else
                  {
                    VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
                  }
                }
                else
                {
                  listener.close();
                  session.getTunnelsHandler().getConnection().removeBindListener(listener);
                  VTConsole.print("\nVT>Tunnel bound in client address [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "] removed!\nVT>");
                }
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, bindAddress, bindPort, socksUser, socksPassword))
                {
                  VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                }
                else
                {
                  VTConsole.print("\nVT>SOCKS/HTTP tunnel bound in client address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
                }
              }
            }
          }
          else
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
        }
        else if (remainingLength == 5)
        {
          if (isPort(parsed[2]) && isPort(parsed[4]))
          {
            int bindPort = Integer.parseInt(parsed[2]);
            String redirectAddress = parsed[3];
            int redirectPort = Integer.parseInt(parsed[4]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, "", bindPort, redirectAddress, redirectPort))
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
              }
            }
          }
          else if (isPort(parsed[3]) && isPort(parsed[4]))
          {
            String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]);
            int redirectPort = Integer.parseInt(parsed[4]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, bindAddress, bindPort, "", redirectPort))
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
              }
            }
          }
          else
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
        }
        else if (remainingLength >= 6)
        {
          try
          {
            String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]);
            String redirectAddress = parsed[4];
            int redirectPort = Integer.parseInt(parsed[5]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, bindAddress, bindPort, redirectAddress, redirectPort))
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
              }
            }
          }
          catch (Throwable e)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
        }
        else
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else if (parsed[1].toUpperCase().contains("R"))
      {
        connection.getCommandWriter().write(command + "\n");
        connection.getCommandWriter().flush();
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
  }
  
  private static boolean isPort(String value)
  {
    try
    {
      Integer.parseInt(value);
      return true;
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public void close()
  {
    
  }
}
