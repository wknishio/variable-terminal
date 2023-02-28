package org.vash.vate.server.console.remote.standard.command;

import java.util.Set;

import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelSocketListener;

public class VTTCPTUNNEL extends VTServerStandardRemoteConsoleCommandProcessor
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
      message.setLength(0);
      for (VTTunnelChannelSocketListener channel : session.getTunnelsHandler().getConnection().getChannels())
      {
        if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
        {
          message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Client TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
        }
      }
      message.append("\nVT>End of connection TCP tunnels list\nVT>");
      connection.getResultWriter().write(message.toString());
      connection.getResultWriter().flush();
    }
    else if (parsed.length == 2)
    {
      if (parsed[1].toUpperCase().startsWith("R"))
      {
        Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
        message.setLength(0);
        message.append("\nVT>List of server connection TCP tunnels:\nVT>");
        for (VTTunnelChannelSocketListener channel : channels)
        {
          if (channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
          {
            message.append("\nVT>Server TCP bind address: [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "]" + "\nVT>Client TCP redirect address: [" + channel.getChannel().getRedirectHost() + " " + channel.getChannel().getRedirectPort() + "]" + "\nVT>");
          }
        }
        message.append("\nVT>End of server connection TCP tunnels list\nVT>");
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
          VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener("", bindPort);
          if (channel != null && channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
          {
            channel.close();
            session.getTunnelsHandler().getConnection().removeChannel(channel);
            connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
            connection.getResultWriter().flush();
          }
          else
          {
            connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [*" + " " + bindPort + "] not found!\nVT>");
            connection.getResultWriter().flush();
          }
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
          if (isPort(parsed[2]) && isPort(parsed[3]))
          {
            int bindPort = Integer.parseInt(parsed[2]);
            int redirectPort = Integer.parseInt(parsed[3]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel("", bindPort, "", redirectPort))
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
          else
          {
            String bindAddress = parsed[2];
            int bindPort = Integer.parseInt(parsed[3]);
            if (bindPort < 1 || bindPort > 65535)
            {
              
            }
            else
            {
              VTTunnelChannelSocketListener channel = session.getTunnelsHandler().getConnection().getChannelSocketListener(bindAddress, bindPort);
              if (channel != null && channel.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                channel.close();
                session.getTunnelsHandler().getConnection().removeChannel(channel);
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + channel.getChannel().getBindHost() + " " + channel.getChannel().getBindPort() + "] removed!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>TCP tunnel bound in server address [" + bindAddress + " " + bindPort + "] not found!\nVT>");
                connection.getResultWriter().flush();
              }
            }
          }
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
          if (isPort(parsed[2]) && isPort(parsed[4]))
          {
            int bindPort = Integer.parseInt(parsed[2]);
            String redirectAddress = parsed[3];
            int redirectPort = Integer.parseInt(parsed[4]);
            if (bindPort < 1 || bindPort > 65535 || redirectPort < 1 || redirectPort > 65535)
            {
              
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel("", bindPort, redirectAddress, redirectPort))
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
              
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, "", redirectPort))
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
            
          }
          
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
          String redirectAddress = parsed[4];
          int redirectPort = Integer.parseInt(parsed[5]);
          
          if (session.getTunnelsHandler().getConnection().setTCPChannel(bindAddress, bindPort, redirectAddress, redirectPort))
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
        catch (Throwable e)
        {
          
        }
      }
    }
    else
    {
      
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
