package org.vash.vate.server.console.remote.standard.command;

import java.util.Set;

import org.vash.vate.VT;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTTUNNEL extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTTUNNEL()
  {
    this.setFullName("*VTTUNNEL");
    this.setAbbreviatedName("*VTTN");
    this.setFullSyntax("*VTTUNNEL [MODE] [[BIND] PORT] [[HOST] PORT] [USERNAME/PASSWORD]");
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
      for (VTTunnelChannelSocketListener channel : session.getTunnelsHandler().getConnection().getChannels())
      {
        if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
        {
          message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Client TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
        }
        if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
        {
          message.append("\nVT>Server SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
        }
      }
      message.append("\nVT>End of connection tunnels list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
    }
    else if (parsed.length == 2)
    {
      if (parsed[1].toUpperCase().contains("R") && !parsed[1].toUpperCase().contains("L"))
      {
        Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
        message.setLength(0);
        message.append("\nVT>List of server connection tunnels:\nVT>");
        for (VTTunnelChannelSocketListener channel : channels)
        {
          if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
          {
            message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Client TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
          }
          if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
          {
            message.append("\nVT>Server SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
          }
        }
        message.append("\nVT>End of server connection tunnels list\nVT>");
        connection.getResultWriter().write(message.toString());
        connection.getResultWriter().flush();
      }
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
      }
    }
    else if (parsed.length == 3)
    {
      if (parsed[1].toUpperCase().contains("R") && !parsed[1].toUpperCase().contains("L"))
      {
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          if (bindPort < 1 || bindPort > 65535)
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
          }
          else
          {
            VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
            if (channel != null)
            {
              channel.close();
              session.getTunnelsHandler().getConnection().removeChannel(channel);
              connection.getResultWriter().write("\nVT>Tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setSOCKSChannel(channelType, "", bindPort))
              {
                connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
        }
        catch (IndexOutOfBoundsException e)
        {
          connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          connection.getResultWriter().flush();
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
    else if (parsed.length >= 4)
    {
      if (parsed[1].toUpperCase().contains("R") && !parsed[1].toUpperCase().contains("L"))
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
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
              if (channel != null)
              {
                channel.close();
                session.getTunnelsHandler().getConnection().removeChannel(channel);
                connection.getResultWriter().write("\nVT>Tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().setSOCKSChannel(channelType, "", bindPort, socksUser, socksPassword))
                {
                  connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                  connection.getResultWriter().flush();
                }
                else
                {
                  connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
                  connection.getResultWriter().flush();
                }
              }
            }
          }
          else
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
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
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, "", bindPort, "", redirectPort))
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          else if (isPort(parsed[3]))
          {
            String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]);
            if (bindPort < 1 || bindPort > 65535)
            {
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
              if (channel != null)
              {
                channel.close();
                session.getTunnelsHandler().getConnection().removeChannel(channel);
                connection.getResultWriter().write("\nVT>Tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().setSOCKSChannel(channelType, bindAddress, bindPort, socksUser, socksPassword))
                {
                  connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                  connection.getResultWriter().flush();
                }
                else
                {
                  connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
                  connection.getResultWriter().flush();
                }
              }
            }
          }
          else
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
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
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, "", bindPort, redirectAddress, redirectPort))
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
                connection.getResultWriter().flush();
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
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, bindAddress, bindPort, "", redirectPort))
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          else
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            connection.getResultWriter().flush();
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
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
              connection.getResultWriter().flush();
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel(channelType, bindAddress, bindPort, redirectAddress, redirectPort))
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
          catch (Throwable e)
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
      else
      {
        connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        connection.getResultWriter().flush();
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
