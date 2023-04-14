package org.vash.vate.client.console.remote.standard.command;

import java.util.Set;

import org.vash.vate.VT;
import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTTUNNEL extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTTUNNEL()
  {
    this.setFullName("*VTTUNNEL");
    this.setAbbreviatedName("*VTTN");
    this.setFullSyntax("*VTTUNNEL [SIDE] [[BIND] PORT] [[HOST] PORT] [OPTION=VALUE]");
    this.setAbbreviatedSyntax("*VTTN [SD] [[BD] PT] [[HT] PT] [OP=VL]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    int channelType = VT.VT_MULTIPLEXED_CHANNEL_TYPE_DIRECT;
    if (parsed.length == 1)
    {
      message.setLength(0);
      message.append("\nVT>List of connection tunnels:\nVT>");
      Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
      for (VTTunnelChannelSocketListener channel : channels)
      {
        if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
        {
          message.append("\nVT>Client TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Server TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
        }
        if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
        {
          message.append("\nVT>Client SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
        }
      }
      VTConsole.print(message.toString());
      connection.getCommandWriter().write(command + "\n");
      connection.getCommandWriter().flush();
    }
    else if (parsed.length == 2)
    {
      //VTConsole.println("parsed[1]:" + parsed[1]);
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
        message.setLength(0);
        message.append("\nVT>List of client connection tunnels:\nVT>");
        for (VTTunnelChannelSocketListener channel : channels)
        {
          if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
          {
            message.append("\nVT>Client TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Server TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
          }
          if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
          {
            message.append("\nVT>Client SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
          }
        }
        message.append("\nVT>End of client connection tunnels list\nVT>");
        VTConsole.print(message.toString());
      }
      else if (parsed[1].toUpperCase().startsWith("R"))
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
      if (parsed[1].toUpperCase().startsWith("L"))
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
            VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
            if (channel != null)
            {
              channel.close();
              session.getTunnelsHandler().getConnection().removeChannel(channel);
              VTConsole.print("\nVT>Tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setSOCKSChannel(channelType, "", bindPort))
              {
                VTConsole.print("\nVT>SOCKS tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>SOCKS tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
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
      else if (parsed[1].toUpperCase().startsWith("R"))
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
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        String socksUser = null;
        String socksPassword = null;
        
        int remainingLength = parsed.length;
        while (parsed[remainingLength - 1].contains("="))
        {
          String option = parsed[remainingLength - 1].split("=")[0];
          String value = parsed[remainingLength - 1].split("=")[1];
          if (option.toUpperCase().startsWith("A") && value.contains("/"))
          {
            socksUser = value.split("/")[0];
            socksPassword = value.split("/")[1];
          }
//          if (option.toUpperCase().startsWith("R"))
//          {
//            if (value.toUpperCase().startsWith("U"))
//            {
//              channelType |= VT.VT_MULTIPLEXED_CHANNEL_TYPE_PERFORMANCE_UNLIMITED;
//            }
//          }
          if (option.toUpperCase().startsWith("C"))
          {
            if (value.toUpperCase().startsWith("L"))
            {
              channelType |= VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_LZ4;
            }
            else if (value.toUpperCase().startsWith("Z"))
            {
              channelType |= VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_ZSTD;
            }
          }
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
              VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
              if (channel != null)
              {
                channel.close();
                session.getTunnelsHandler().getConnection().removeChannel(channel);
                VTConsole.print("\nVT>Tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().setSOCKSChannel(channelType, "", bindPort, socksUser, socksPassword))
                {
                  VTConsole.print("\nVT>SOCKS tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
                }
                else
                {
                  VTConsole.print("\nVT>SOCKS tunnel bound in client address [*" + " " + bindPort + "] cannot be set!\nVT>");
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, "", bindPort, "", redirectPort))
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
              VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
              if (channel != null)
              {
                channel.close();
                session.getTunnelsHandler().getConnection().removeChannel(channel);
                VTConsole.print("\nVT>Tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().setSOCKSChannel(channelType, bindAddress, bindPort, socksUser, socksPassword))
                {
                  VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                }
                else
                {
                  VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, "", bindPort, redirectAddress, redirectPort))
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, bindAddress, bindPort, "", redirectPort))
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, bindAddress, bindPort, redirectAddress, redirectPort))
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
      else if (parsed[1].toUpperCase().startsWith("R"))
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
