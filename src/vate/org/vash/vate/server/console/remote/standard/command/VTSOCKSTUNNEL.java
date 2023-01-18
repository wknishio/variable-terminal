package org.vash.vate.server.console.remote.standard.command;

import java.util.Set;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTSOCKSTUNNEL extends VTServerStandardRemoteConsoleCommandProcessor
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
      message.setLength(0);
      for (VTTunnelChannelSocketListener channel : session.getSOCKSTunnelsHandler().getConnection().getChannels())
      {
        message.append("\nVT>Server SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
      }
      message.append("\nVT>End of connection SOCKS tunnels list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
    }
    else if (parsed.length == 2)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        Set<VTTunnelChannelSocketListener> channels = session.getSOCKSTunnelsHandler().getConnection().getChannels();
        message.setLength(0);
        message.append("\nVT>List of server connection SOCKS tunnels:\nVT>");
        for (VTTunnelChannelSocketListener channel : channels)
        {
          message.append("\nVT>Server SOCKS bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]\nVT>");
        }
        message.append("\nVT>End of server connection SOCKS tunnels list\nVT>");
        connection.getResultWriter().write(message.toString());
        connection.getResultWriter().flush();
      }
    }
    else if (parsed.length == 3)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
          if (channel != null)
          {
            channel.close();
            session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel("", bindPort);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        catch (IndexOutOfBoundsException e)
        {
          
        }
        catch (NumberFormatException e)
        {
          
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        
      }
    }
    else if (parsed.length == 4)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        try
        {
          String bindAddress = parsed[2];
          int bindPort = Integer.parseInt(parsed[3]);
          VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
          if (channel != null)
          {
            channel.close();
            session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        catch (IndexOutOfBoundsException e)
        {
          
        }
        catch (NumberFormatException e)
        {
          
        }
        catch (Throwable e)
        {
          
        }
      }
    }
    else if (parsed.length == 5)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        try
        {
          int bindPort = Integer.parseInt(parsed[2]);
          String socksUsername = parsed[3];
          String socksPassword = parsed[4];
          VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
          if (channel != null)
          {
            channel.close();
            session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel("", bindPort, socksUsername, socksPassword);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        catch (IndexOutOfBoundsException e)
        {
          
        }
        catch (NumberFormatException e)
        {
          
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        
      }
    }
    else if (parsed.length >= 6)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        try
        {
          String bindAddress = parsed[2];
          int bindPort = Integer.parseInt(parsed[3]);
          String socksUsername = parsed[4];
          String socksPassword = parsed[5];
          
          VTTunnelChannelSocketListener channel = session.getSOCKSTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
          if (channel != null)
          {
            channel.close();
            session.getSOCKSTunnelsHandler().getConnection().removeChannel(channel);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            session.getSOCKSTunnelsHandler().getConnection().setSOCKSChannel(bindAddress, bindPort, socksUsername, socksPassword);
            connection.getResultWriter().write("\nVT>SOCKS tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
            connection.getResultWriter().flush();
          }
        }
        catch (IndexOutOfBoundsException e)
        {
          
        }
        catch (NumberFormatException e)
        {
          
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        
      }
    }
  }

  public void close()
  {
    
  }
}
