package org.vash.vate.client.console.remote.standard.command;

import java.util.Set;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTTCPTUNNEL extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTTCPTUNNEL()
  {
    this.setFullName("*VTTCPTUNNEL");
    this.setAbbreviatedName("*VTTTN");
    this.setFullSyntax("*VTTCPTUNNEL [SIDE] [[BIND] PORT] [[HOST] PORT]");
    this.setAbbreviatedSyntax("*VTTTN [SD] [[BD] PT] [[HT] PT]");
  }

  public void execute(String command, String[] parsed) throws Exception
  {
    if (parsed.length == 1)
    {
      Set<VTTunnelChannelSocketListener> channels = session.getTCPTunnelsHandler().getConnection().getChannels();
      message.setLength(0);
      message.append("\nVT>List of connection TCP tunnels:\nVT>");
      for (VTTunnelChannelSocketListener channel : channels)
      {
        message.append("\nVT>Client TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Server TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
      }
      VTConsole.print(message.toString());
      connection.getCommandWriter().write(command + "\n");
      connection.getCommandWriter().flush();
    }
    else if (parsed.length == 2)
    {
      if (parsed[1].toUpperCase().startsWith("L"))
      {
        Set<VTTunnelChannelSocketListener> channels = session.getTCPTunnelsHandler().getConnection().getChannels();
        message.setLength(0);
        message.append("\nVT>List of client connection TCP tunnels:\nVT>");
        for (VTTunnelChannelSocketListener channel : channels)
        {
          message.append("\nVT>Client TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
        }
        message.append("\nVT>End of client connection TCP tunnels list\nVT>");
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
            VTTunnelChannelSocketListener channel = session.getTCPTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
            if (channel != null)
            {
              channel.close();
              session.getTCPTunnelsHandler().getConnection().removeChannel(channel);
              VTConsole.print("\nVT>TCP tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            }
            else
            {
              VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] not found!\nVT>");
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
              session.getTCPTunnelsHandler().getConnection().setTCPChannel("", bindPort, "", redirectPort);
              VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
            }
          }
          else
          {
            String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]);
            if (bindPort < 1 || bindPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              VTTunnelChannelSocketListener channel = session.getTCPTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
              if (channel != null)
              {
                channel.close();
                session.getTCPTunnelsHandler().getConnection().removeChannel(channel);
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] not found!\nVT>");
              }
            }
          }
        }
        catch (Throwable e)
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else if (parsed[1].toUpperCase().startsWith("R"))
      {
        try
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
              connection.getCommandWriter().write(command + "\n");
              connection.getCommandWriter().flush();
            }
          }
          else
          {
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
        }
        catch (Throwable e1)
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
              if (session.getTCPTunnelsHandler().getConnection().setTCPChannel("", bindPort, redirectAddress, redirectPort))
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [*" + " " + bindPort + "] set!\nVT>");
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
              if (session.getTCPTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, "", redirectPort))
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
              }
              else
              {
                VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
              }
            }
          }
          else
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
        }
        catch (Throwable e)
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else if (parsed[1].toUpperCase().startsWith("R"))
      {
        try
        {
          if (isPort(parsed[2]) && isPort(parsed[4]))
          {
            int bindPort = Integer.parseInt(parsed[2]);
            //String redirectAddress = parsed[3]; 
            int redirectPort = Integer.parseInt(parsed[4]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              connection.getCommandWriter().write(command + "\n");
              connection.getCommandWriter().flush();
            }
          }
          else if (isPort(parsed[3]) && isPort(parsed[4]))
          {
            //String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]); 
            int redirectPort = Integer.parseInt(parsed[4]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
            }
            else
            {
              connection.getCommandWriter().write(command + "\n");
              connection.getCommandWriter().flush();
            }
          }
          else
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
        }
        catch (Throwable e1)
        {
          VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        }
      }
      else
      {
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
          String redirectAddress = parsed[4];
          int redirectPort = Integer.parseInt(parsed[5]);
          if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            if (session.getTCPTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, redirectAddress, redirectPort))
            {
              VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
            }
            else
            {
              VTConsole.print("\nVT>TCP tunnel bound in client address [" + bindAddress + " " + bindPort + "] set!\nVT>");
            }
          }
        }
        catch (Throwable e)
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
          // String redirectAddress = splitCommand[4];
          int redirectPort = Integer.parseInt(parsed[5]);
          if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
          {
            VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
          }
          else
          {
            connection.getCommandWriter().write(command + "\n");
            connection.getCommandWriter().flush();
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
    else
    {
      VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
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
