package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import org.vash.vate.VT;

public class VTProxy
{
  public enum Type
  {
    DIRECT,
    HTTP,
    SOCKS,
    SOCKS_THEN_HTTP,
    HTTP_THEN_SOCKS
  };
  
  private Proxy.Type proxyType = Proxy.Type.DIRECT;
  private String proxyHost;
  private int proxyPort;
  private String proxyUser;
  private String proxyPassword;
  //private Socket proxyConnection;
  
  public VTProxy(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    this.proxyType = proxyType;
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyUser = proxyUser;
    this.proxyPassword = proxyPassword;
  }
  
  public void setProxy(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    this.proxyType = proxyType;
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyUser = proxyUser;
    this.proxyPassword = proxyPassword;
  }
  
//  public VTProxy(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, Socket proxyConnection)
//  {
//    this.proxyType = proxyType;
//    this.proxyHost = proxyHost;
//    this.proxyPort = proxyPort;
//    this.proxyUser = proxyUser;
//    this.proxyPassword = proxyPassword;
//    this.proxyConnection = proxyConnection;
//  }
  
//  public void setProxy(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, Socket proxyConnection)
//  {
//    this.proxyType = proxyType;
//    this.proxyHost = proxyHost;
//    this.proxyPort = proxyPort;
//    this.proxyUser = proxyUser;
//    this.proxyPassword = proxyPassword;
//    this.proxyConnection = proxyConnection;
//  }
  
  public Proxy.Type getProxyType()
  {
    return proxyType;
  }
  
  public String getProxyHost()
  {
    return proxyHost;
  }
  
  public int getProxyPort()
  {
    return proxyPort;
  }
  
  public String getProxyUser()
  {
    return proxyUser;
  }
  
  public String getProxyPassword()
  {
    return proxyPassword;
  }
  
  //public Socket getProxyConnection()
  //{
    //return proxyConnection;
  //}
  
  public void setProxyType(Proxy.Type proxyType)
  {
    this.proxyType = proxyType;
  }
  
  public void setProxyHost(String proxyHost)
  {
    this.proxyHost = proxyHost;
  }
  
  public void setProxyPort(int proxyPort)
  {
    this.proxyPort = proxyPort;
  }
  
  public void setProxyUser(String proxyUser)
  {
    this.proxyUser = proxyUser;
  }
  
  public void setProxyPassword(String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
  }
  
  //public void setProxyConnection(Socket proxyConnection)
  //{
    //this.proxyConnection = proxyConnection;
  //}
  
  public static Socket next(Socket next, VTProxy... proxies)
  {
    if (proxies == null || proxies.length <= 0)
    {
      if (next == null)
      {
        next = new Socket(Proxy.NO_PROXY);
      }
      return next;
    }
    for (VTProxy proxy : proxies)
    {
      next = next(next, proxy.getProxyType(), proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
    }
    return next;
  }
  
  private static Socket next(Socket proxyConnection, Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    Socket next;
    if (proxyConnection != null && !proxyConnection.isConnected())
    {
      InetSocketAddress socketAddress = null;
      if (proxyConnection instanceof VTSocksProxySocket
      || proxyConnection instanceof VTHttpProxySocket
      || proxyConnection instanceof VTHttpSocksProxySocket)
      {
        socketAddress = InetSocketAddress.createUnresolved(proxyHost, proxyPort);
      }
      else
      {
        socketAddress = new InetSocketAddress(proxyHost, proxyPort);
      }
      try
      {
        proxyConnection.connect(socketAddress);
      }
      catch (Throwable t)
      {
        proxyConnection = null;
      }
    }
    if (proxyType == Proxy.Type.SOCKS)
    {
      next = new VTSocksProxySocket(proxyConnection, proxyHost, proxyPort, proxyUser, proxyPassword);
    }
    else if (proxyType == Proxy.Type.HTTP)
    {
      next = new VTHttpProxySocket(proxyConnection, proxyHost, proxyPort, proxyUser, proxyPassword);
    }
    else if (proxyType == null)
    {
      next = new VTHttpSocksProxySocket(proxyConnection, proxyHost, proxyPort, proxyUser, proxyPassword);
    }
    else
    {
      next = new Socket(Proxy.NO_PROXY);
    }
    return next;
  }
  
  public static Socket connect(String host, int port, Socket proxyConnection, VTProxy proxy) throws IOException
  {
    return connect(host, port, proxyConnection, proxy.getProxyType(), proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
  }
  
  public static Socket connect(String host, int port, Socket proxyConnection, Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) throws IOException
  {
    if (host == null || host.length() == 0 || host.equals("*"))
    {
      host = "";
    }
    else
    {
      
    }
    InetSocketAddress socketAddress = null;
    
    if (proxyType == null || proxyType != Proxy.Type.DIRECT)
    {
      socketAddress = InetSocketAddress.createUnresolved(host, port);
    }
    else
    {
      socketAddress = new InetSocketAddress(host, port);
    }
    
    VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
    
    Socket socket = next(proxyConnection, proxy);
    
    socket.connect(socketAddress);
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
}
