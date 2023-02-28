package org.vash.vate.client.console.remote.standard.command;

import java.util.Set;

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
    this.setFullSyntax("*VTTUNNEL [SIDE] [[BIND] PORT] [[HOST] PORT] [USER/PASSWORD]");
    this.setAbbreviatedSyntax("*VTTN [SD] [[BD] PT] [[HT] PT] [US/PW]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
      message.setLength(0);
      message.append("\nVT>List of connection tunnels:\nVT>");
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
      VTConsole.println("parsed[1]:" + parsed[1]);
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
              if (session.getTunnelsHandler().getConnection().setSOCKSChannel("", bindPort))
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
        if (parsed[parsed.length - 1].contains("/"))
        {
          socksUser = parsed[parsed.length - 1].split("/")[0];
          socksPassword = parsed[parsed.length - 1].split("/")[1];
          remainingLength--;
        }
        
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
                if (session.getTunnelsHandler().getConnection().setSOCKSChannel("", bindPort, socksUser, socksPassword))
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel("", bindPort, "", redirectPort))
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
                if (session.getTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort, socksUser, socksPassword))
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel("", bindPort, redirectAddress, redirectPort))
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, "", redirectPort))
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
              if (session.getTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, redirectAddress, redirectPort))
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
