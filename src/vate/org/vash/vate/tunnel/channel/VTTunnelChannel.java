package org.vash.vate.tunnel.channel;

import java.net.InetSocketAddress;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.vash.vate.VTSystem;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.security.VTSplitMix64Random;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannel
{
  public static final char TUNNEL_TYPE_TCP = 'T';
  public static final char TUNNEL_TYPE_SOCKS = 'S';
  public static final char TUNNEL_TYPE_UDP = 'U';
  public static final char TUNNEL_TYPE_FTP = 'F';
  public static final char TUNNEL_TYPE_ANY = 'A';
  
  private final char tunnelType;
  private int channelType = VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
  private final VTTunnelConnection connection;
  private final Collection<VTTunnelSessionHandler> sessions;
  private final Random random = new VTSplitMix64Random(new VTBlake3SecureRandom().nextLong());
  
  private int connectTimeout;
  private int dataTimeout;
  private InetSocketAddress bindAddress;
  private String network = "";
  private String bindHost;
  private int bindPort;
  private String redirectHost;
  private int redirectPort;
  private String tunnelUsername;
  private String tunnelPassword;
  private VTProxy proxy;
  
  public char getTunnelType()
  {
    return tunnelType;
  }
  
  public int getChannelType()
  {
    return channelType;
  }
  
  public void setChannelType(int channelType)
  {
    this.channelType = channelType;
  }
  
  public Random getRandom()
  {
    return random;
  }
  
  // SOCKS/HTTP/FTP bind tunnel without authentication
  public VTTunnelChannel(int channelType, VTTunnelConnection connection, int connectTimeout, int dataTimeout, String bindHost, int bindPort, boolean ftp, VTProxy proxy)
  {
    String network = "";
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      String[] split = bindHost.split(";");
      bindHost = split[0];
      network = split[1];
    }
    if (ftp)
    {
      this.tunnelType = TUNNEL_TYPE_FTP;
    }
    else
    {
      this.tunnelType = TUNNEL_TYPE_SOCKS;
    }
    this.channelType = channelType;
    this.connection = connection;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.network = network;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.proxy = proxy;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.sessions = new ConcurrentLinkedQueue<VTTunnelSessionHandler>();
  }
  
  // SOCKS/HTTP/FTP bind tunnel with authentication
  public VTTunnelChannel(int channelType, VTTunnelConnection connection, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String username, String password, boolean ftp, VTProxy proxy)
  {
    String network = "";
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      String[] split = bindHost.split(";");
      bindHost = split[0];
      network = split[1];
    }
    if (ftp)
    {
      this.tunnelType = TUNNEL_TYPE_FTP;
    }
    else
    {
      this.tunnelType = TUNNEL_TYPE_SOCKS;
    }
    this.channelType = channelType;
    this.connection = connection;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.network = network;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.proxy = proxy;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.tunnelUsername = username;
    this.tunnelPassword = password;
    this.sessions = new ConcurrentLinkedQueue<VTTunnelSessionHandler>();
  }
  
  // TCP bind redirect tunnel
  public VTTunnelChannel(int channelType, VTTunnelConnection connection, int connectTimeout, int dataTimeout, String bindHost, int bindPort, String redirectHost, int redirectPort, VTProxy proxy)
  {
    String network = "";
    int idx = bindHost.indexOf(';');
    if (idx >= 0)
    {
      String[] split = bindHost.split(";");
      bindHost = split[0];
      network = split[1];
    }
    this.tunnelType = TUNNEL_TYPE_TCP;
    this.channelType = channelType;
    this.connection = connection;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.network = network;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.redirectHost = redirectHost;
    this.redirectPort = redirectPort;
    this.proxy = proxy;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.sessions = new ConcurrentLinkedQueue<VTTunnelSessionHandler>();
  }
  
  //Generic response channel
  public VTTunnelChannel(int channelType, VTTunnelConnection connection)
  {
    this.tunnelType = TUNNEL_TYPE_ANY;
    this.channelType = channelType;
    this.connection = connection;
    this.sessions = new ConcurrentLinkedQueue<VTTunnelSessionHandler>();
  }
  
  public String toString()
  {
    return String.valueOf(this.getBindHost() + " " + bindPort);
  }
  
  public boolean equals(Object other)
  {
    return this.toString().equals(other.toString());
  }
  
  public void close()
  {
    // closed = true;
    //synchronized (sessions)
    //{
      for (VTTunnelSessionHandler handler : sessions)
      {
        try
        {
          handler.getSession().close();
        }
        catch (Throwable e)
        {
          // e.printStackTrace();
        }
      }
    //}
    sessions.clear();
  }
  
  public VTTunnelConnection getConnection()
  {
    return connection;
  }
  
  public InetSocketAddress getBindAddress()
  {
    return bindAddress;
  }
  
  public String getTunnelUsername()
  {
    return tunnelUsername;
  }
  
  public String getTunnelPassword()
  {
    return tunnelPassword;
  }
  
  public void setTunnelUsername(String username)
  {
    this.tunnelUsername = username;
  }
  
  public void setTunnelPassword(String password)
  {
    this.tunnelPassword = password;
  }
  
  public void addSession(VTTunnelSessionHandler handler)
  {
    sessions.add(handler);
  }
  
  public boolean removeSession(VTTunnelSessionHandler handler)
  {
    return sessions.remove(handler);
  }
  
  public int getConnectTimeout()
  {
    return connectTimeout;
  }
  
  public int getDataTimeout()
  {
    return dataTimeout;
  }
  
  public String getNetwork()
  {
    return network;
  }
  
  public String getBindHost()
  {
    if (bindHost == null || bindHost.length() == 0)
    {
      return "*";
    }
    return bindHost;
  }
  
  public int getBindPort()
  {
    return bindPort;
  }
  
  public String getRedirectHost()
  {
    if (redirectHost == null || redirectHost.length() == 0)
    {
      return "*";
    }
    return redirectHost;
  }
  
  public int getRedirectPort()
  {
    return redirectPort;
  }
  
  public void setNetwork(String network)
  {
    this.network = network;
  }
  
  public void setRedirectAddress(String redirectHost, int redirectPort)
  {
    this.redirectHost = redirectHost;
    this.redirectPort = redirectPort;
  }
  
  public void setConnectTimeout(int connectTimeout)
  {
    this.connectTimeout = connectTimeout;
  }
  
  public void setDataTimeout(int dataTimeout)
  {
    this.dataTimeout = dataTimeout;
  }
  
  public void setProxy(VTProxy proxy)
  {
    this.proxy = proxy;
  }
  
  public VTProxy getProxy()
  {
    return proxy;
  }
}