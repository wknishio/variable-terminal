package org.vash.vate.server.console.remote.standard.command;

import java.net.Proxy;

import org.vash.vate.VT;
import org.vash.vate.help.VTHelpManager;
import org.vash.vate.server.console.remote.standard.VTServerStandardRemoteConsoleCommandProcessor;
import org.vash.vate.socket.VTProxy;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.channel.VTTunnelChannelBindSocketListener;

public class VTTUNNEL extends VTServerStandardRemoteConsoleCommandProcessor
{
  public VTTUNNEL()
  {
    this.setFullName("*VTTUNNEL");
    this.setAbbreviatedName("*VTTN");
    this.setFullSyntax("*VTTUNNEL [MODE] [[BIND] PORT] [[HOST] PORT] [TYPE/PROXY/PORT][/][USER/PASS]");
    this.setAbbreviatedSyntax("*VTTN [MD] [[BD] PT] [[HT] PT] [TP/PX/PT][/][US/PW]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    //Proxy.Type proxyType = Proxy.Type.DIRECT;
    //String proxyHost = "";
    //int proxyPort = 0;
    VTProxy proxy = new VTProxy(Proxy.Type.DIRECT, "", 0, null, null);
    int channelType = VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
    
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
      for (VTTunnelChannelBindSocketListener listener : session.getTunnelsHandler().getConnection().getBindListeners().toArray(new VTTunnelChannelBindSocketListener[] { }))
      {
        if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
        {
          message.append("\nVT>Server TCP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]" + "\nVT>Client TCP redirect address: [" + listener.getChannel().getRedirectHost() + " " + listener.getChannel().getRedirectPort() + "]" + "\nVT>");
        }
        if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
        {
          message.append("\nVT>Server SOCKS/HTTP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]\nVT>");
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
        message.setLength(0);
        message.append("\nVT>List of server connection tunnels:\nVT>");
        //Set<VTTunnelChannelSocketListener> channels = session.getTunnelsHandler().getConnection().getChannels();
        for (VTTunnelChannelBindSocketListener listener : session.getTunnelsHandler().getConnection().getBindListeners().toArray(new VTTunnelChannelBindSocketListener[] { }))
        {
          if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_TCP)
          {
            message.append("\nVT>Server TCP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]" + "\nVT>Client TCP redirect address: [" + listener.getChannel().getRedirectHost() + " " + listener.getChannel().getRedirectPort() + "]" + "\nVT>");
          }
          if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
          {
            message.append("\nVT>Server SOCKS/HTTP bind address: [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "]\nVT>");
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
            VTTunnelChannelBindSocketListener listener = session.getTunnelsHandler().getConnection().getBindListener("", bindPort);
            if (listener != null)
            {
              listener.close();
              session.getTunnelsHandler().getConnection().removeBindListener(listener);
              connection.getResultWriter().write("\nVT>Tunnel bound in server address [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "] removed!\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, "", bindPort, proxy))
              {
                connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
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
        //String proxyUser = null;
        //String proxyPassword = null;
        
        String tunnelUser = null;
        String tunnelPassword = null;
        
        int remainingLength = parsed.length;
        while (parsed[remainingLength - 1].contains("/"))
        {
          int first = parsed[remainingLength - 1].indexOf('/');
          int last = parsed[remainingLength - 1].lastIndexOf('/');
          
          if (first != last)
          {
            String value = parsed[remainingLength - 1];
            String[] values = value.split("/");
            if (values.length >= 3)
            {
              if (values[0].toUpperCase().startsWith("H"))
              {
                proxy.setProxyType(Proxy.Type.HTTP);
                proxy.setProxyPort(8080);
              }
              else if (values[0].toUpperCase().startsWith("S"))
              {
                proxy.setProxyType(Proxy.Type.SOCKS);
                proxy.setProxyPort(1080);
              }
              proxy.setProxyHost(values[1]);
              try
              {
                proxy.setProxyPort(Integer.parseInt(values[2]));
              }
              catch (Throwable t)
              {
                
              }
              if (values.length >= 5)
              {
                proxy.setProxyUser(values[3]);
                proxy.setProxyPassword(values[4]);
              }
            }
            remainingLength--;
          }
          else
          {
            String value = parsed[remainingLength - 1];
            if (tunnelUser == null && tunnelPassword == null)
            {
              if (value.length() > 1)
              {
                tunnelUser = value.split("/")[0];
                tunnelPassword = value.split("/")[1];
              }
              else
              {
                tunnelUser = "";
                tunnelPassword = "";
              }
            }
            else
            {
//              if (value.length() > 1)
//              {
//                proxy.setProxyUser(value.split("/")[0]);
//                proxy.setProxyPassword(value.split("/")[1]);
//              }
//              else
//              {
//                proxy.setProxyUser("");
//                proxy.setProxyPassword("");
//              }
            }
            remainingLength--;
          }
        }
        
        //System.out.println("socksUser:[" + socksUser + "]");
        //System.out.println("socksPassword:[" + socksPassword + "]");
        //System.out.println("proxyUser:[" + proxy.getProxyUser() + "]");
        //System.out.println("proxyPassword:[" + proxy.getProxyPassword() + "]");
        
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
              VTTunnelChannelBindSocketListener listener = session.getTunnelsHandler().getConnection().getBindListener("", bindPort);
              if (listener != null)
              {
                if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS && tunnelUser != null && tunnelPassword != null)
                {
                  if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, "", bindPort, tunnelUser, tunnelPassword, proxy))
                  {
                    connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                    connection.getResultWriter().flush();
                  }
                  else
                  {
                    connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
                    connection.getResultWriter().flush();
                  }
                }
                else
                {
                  listener.close();
                  session.getTunnelsHandler().getConnection().removeBindListener(listener);
                  connection.getResultWriter().write("\nVT>Tunnel bound in server address [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "] removed!\nVT>");
                  connection.getResultWriter().flush();
                }
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, "", bindPort, tunnelUser, tunnelPassword, proxy))
                {
                  connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [*" + " " + bindPort + "] set!\nVT>");
                  connection.getResultWriter().flush();
                }
                else
                {
                  connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [*" + " " + bindPort + "] cannot be set!\nVT>");
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
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, "", bindPort, "", redirectPort, proxy))
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
              VTTunnelChannelBindSocketListener listener = session.getTunnelsHandler().getConnection().getBindListener(bindAddress, bindPort);
              if (listener != null)
              {
                if (listener.getChannel().getTunnelType() == VTTunnelChannel.TUNNEL_TYPE_SOCKS && tunnelUser != null && tunnelPassword != null)
                {
                  if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, bindAddress, bindPort, tunnelUser, tunnelPassword, proxy))
                  {
                    connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                    connection.getResultWriter().flush();
                  }
                  else
                  {
                    connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
                    connection.getResultWriter().flush();
                  }
                }
                else
                {
                  listener.close();
                  session.getTunnelsHandler().getConnection().removeBindListener(listener);
                  connection.getResultWriter().write("\nVT>Tunnel bound in server address [" + listener.getChannel().getBindHost() + " " + listener.getChannel().getBindPort() + "] removed!\nVT>");
                  connection.getResultWriter().flush();
                }
              }
              else
              {
                if (session.getTunnelsHandler().getConnection().bindSOCKSListener(channelType, bindAddress, bindPort, tunnelUser, tunnelPassword, proxy))
                {
                  connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [" + bindAddress + " " + bindPort + "] set!\nVT>");
                  connection.getResultWriter().flush();
                }
                else
                {
                  connection.getResultWriter().write("\nVT>SOCKS/HTTP tunnel bound in server address [" + bindAddress + " " + bindPort + "] cannot be set!\nVT>");
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
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, "", bindPort, redirectAddress, redirectPort, proxy))
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
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, bindAddress, bindPort, "", redirectPort, proxy))
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
              if (session.getTunnelsHandler().getConnection().bindTCPRedirectListener(channelType, bindAddress, bindPort, redirectAddress, redirectPort, proxy))
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
  
  public boolean remote()
  {
    return true;
  }
}
