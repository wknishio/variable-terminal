package org.vate.tunnel.channel;

import java.net.InetSocketAddress;
import java.util.LinkedList;
import java.util.List;

import org.vate.tunnel.connection.VTTunnelConnection;
import org.vate.tunnel.session.VTTunnelSessionHandler;

public class VTTunnelChannel
{
  private VTTunnelConnection connection;
  private InetSocketAddress bindAddress;
  // private InetSocketAddress redirectAddress;
  private List<VTTunnelSessionHandler> sessions;
  private String socksUsername;
  private String socksPassword;
  private String bindHost;
  private int bindPort;
  private String redirectHost;
  private int redirectPort;

  // SOCKS tunnel without authentication
  public VTTunnelChannel(VTTunnelConnection connection, String bindHost, int bindPort)
  {
    this.connection = connection;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.sessions = new LinkedList<VTTunnelSessionHandler>();
  }

  // SOCKS tunnel with authentication
  public VTTunnelChannel(VTTunnelConnection connection, String bindHost, int bindPort, String socksUsername, String socksPassword)
  {
    this.connection = connection;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.socksUsername = socksUsername;
    this.socksPassword = socksPassword;
    this.sessions = new LinkedList<VTTunnelSessionHandler>();
  }

  // TCP tunnel
  public VTTunnelChannel(VTTunnelConnection connection, String bindHost, int bindPort, String redirectHost, int redirectPort)
  {
    this.connection = connection;
    this.bindHost = bindHost;
    this.bindPort = bindPort;
    this.redirectHost = redirectHost;
    this.redirectPort = redirectPort;
    if (bindHost != null && bindHost.length() > 0)
    {
      this.bindAddress = new InetSocketAddress(bindHost, bindPort);
    }
    else
    {
      this.bindAddress = new InetSocketAddress(bindPort);
    }
    this.sessions = new LinkedList<VTTunnelSessionHandler>();
  }

  public String toString()
  {
    return String.valueOf(this.getBindHost() + " " + bindPort);
  }

  public boolean equals(Object other)
  {
    return this.toString().equals(other.toString());
  }

  public synchronized void close()
  {
    // closed = true;
    synchronized (sessions)
    {
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
    }
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

  public String getSocksUsername()
  {
    return socksUsername;
  }

  public String getSocksPassword()
  {
    return socksPassword;
  }

  public void setSocksUsername(String socksUsername)
  {
    this.socksUsername = socksUsername;
  }

  public void setSocksPassword(String socksPassword)
  {
    this.socksPassword = socksPassword;
  }

  public synchronized void addSession(VTTunnelSessionHandler handler)
  {
    sessions.add(handler);
  }

  public synchronized boolean removeSession(VTTunnelSessionHandler handler)
  {
    return sessions.remove(handler);
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

  public void setRedirectAddress(String redirectHost, int redirectPort)
  {
    this.redirectHost = redirectHost;
    this.redirectPort = redirectPort;
  }
}