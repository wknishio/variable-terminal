package org.vash.vate.client.console.remote.standard.command;

import java.util.Set;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTSOCKSTUNNEL extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTSOCKSTUNNEL()
  {
    this.setFullName("*VTSOCKSTUNNEL");
    this.setAbbreviatedName("*VTSTN");
    this.setFullSyntax("*VTSOCKSTUNNEL [SIDE] [[BIND] PORT] [USER PASSWORD]");
    this.setAbbreviatedSyntax("*VTSTN [SD] [[BD] PT] [US PW]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
      message.setLength(0);
      message.append("\nVT>List of connection SOCKS tunnels:\nVT>");
      for (VTTunnelChannelSocketListener channel : channels)
      {
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
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
        message.setLength(0);
        message.append("\nVT>List of client connection SOCKS tunnels:\nVT>");
        for (VTTunnelChannelSocketListener channel : channels)
        {
          if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
          {
            message.append("\nVT>Client SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
          }
        }
        message.append("\nVT>End of client connection SOCKS tunnels list\nVT>");
        VTConsole.print(message.toString());
      }
      else if (parsed[1].toUpperCase().startsWith("R"))
      {
        connection.getCommandWriter().write(command + "\n");
        connection.getCommandWriter().flush();
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
            if (channel != null && channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              channel.close();
              session.getTunnelsHandler().getConnection().removeChannel(channel);
              VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
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
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
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
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else if (parsed.length == 4)
    {
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        try
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
            if (channel != null && channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              channel.close();
              session.getTunnelsHandler().getConnection().removeChannel(channel);
              VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort))
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
        try
        {
          // String bindAddress = splitCommand[2];
          int bindPort = Integer.parseInt(parsed[3]);
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
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
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
      }
    }
    else if (parsed.length == 5)
    {
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          String socksUsername = parsed[3];
          String socksPassword = parsed[4];
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
            if (channel != null && channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              channel.close();
              session.getTunnelsHandler().getConnection().removeChannel(channel);
              VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setSOCKSChannel("", bindPort, socksUsername, socksPassword))
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
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
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
    }
    else if (parsed.length >= 6)
    {
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        try
        {
          String bindAddress = parsed[2];
          int bindPort = Integer.parseInt(parsed[3]);
          String socksUsername = parsed[4];
          String socksPassword = parsed[5];
          
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
            if (channel != null && channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
            {
              channel.close();
              session.getTunnelsHandler().getConnection().removeChannel(channel);
              VTConsole.print("\nVT>SOCKS tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort, socksUsername, socksPassword))
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
        try
        {
          // String bindAddress = splitCommand[2];
          int bindPort = Integer.parseInt(parsed[3]);
          if (bindPort < 1 || bindPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
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
    }
    else
    {
      VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
    }
  }
  
  public void close()
  {
    
  }
}
