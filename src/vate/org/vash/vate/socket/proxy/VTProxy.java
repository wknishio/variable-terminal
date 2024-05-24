package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

import org.vash.vate.socket.remote.VTRemoteSocketAdapter;

public class VTProxy
{
  public enum VTProxyType
  {
    GLOBAL,
    DIRECT,
    HTTP,
    SOCKS,
    ANY
  };
  
  private VTProxyType proxyType = VTProxyType.GLOBAL;
  private String proxyHost;
  private int proxyPort;
  private String proxyUser;
  private String proxyPassword;
  //private Socket proxyConnection;
  
  public VTProxy(VTProxyType proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    this.proxyType = proxyType;
    this.proxyHost = proxyHost;
    this.proxyPort = proxyPort;
    this.proxyUser = proxyUser;
    this.proxyPassword = proxyPassword;
  }
  
  public void setProxy(VTProxyType proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
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
  
  public VTProxyType getProxyType()
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
  
  public void setProxyType(VTProxyType proxyType)
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
  
  public static Socket next(Socket currentSocket, int timeout, VTProxy... proxies)
  {
    if (proxies == null || proxies.length <= 0)
    {
      if (currentSocket == null)
      {
        currentSocket = new Socket();
      }
      return currentSocket;
    }
    //VTProxy parentProxy = null;
    for (VTProxy currentProxy : proxies)
    {
      currentSocket = nextSocket(currentSocket, timeout, currentProxy);
      //parentProxy = currentProxy;
    }
    return currentSocket;
  }
  
  @SuppressWarnings("all")
  private static Socket nextSocket(Socket currentSocket, int timeout, VTProxy currentProxy)
  {
    Socket nextSocket = null;
    
    if (currentProxy != null)
    {
      if (currentProxy.getProxyType() == VTProxyType.GLOBAL)
      {
        nextSocket = new Socket();
      }
      else if (currentProxy.getProxyType() == VTProxyType.DIRECT)
      {
        nextSocket = new Socket(Proxy.NO_PROXY);
      }
      else if (currentProxy.getProxyType() == VTProxyType.SOCKS)
      {
        nextSocket = new VTSocksProxySocket(currentProxy, currentSocket);
      }
      else if (currentProxy.getProxyType() == VTProxyType.HTTP)
      {
        nextSocket = new VTHttpProxySocket(currentProxy, currentSocket);
      }
      else if (currentProxy.getProxyType() == VTProxyType.ANY)
      {
        nextSocket = new VTAutoProxySocket(currentProxy, currentSocket);
      }
      else
      {
        if (currentSocket == null)
        {
          nextSocket = new Socket();
        }
        else
        {
          nextSocket = currentSocket;
        }
      }
    }
    else
    {
      if (currentSocket == null)
      {
        nextSocket = new Socket();
      }
      else
      {
        nextSocket = currentSocket;
      }
    }
    
    return nextSocket;
  }
  
  public static Socket connect(String host, int port, int timeout, Socket currentSocket, VTProxy... proxies) throws IOException
  {
    if (host == null || host.length() == 0 || host.equals("*"))
    {
      host = "";
    }
    
    if (currentSocket instanceof VTRemoteSocketAdapter)
    {
      VTRemoteSocketAdapter proxySocket = (VTRemoteSocketAdapter)currentSocket;
      return proxySocket.connect(host, port, timeout, 0, proxies);
    }
    
    InetSocketAddress socketAddress = null;
    
    Socket connectionSocket = next(currentSocket, timeout, proxies);
    
    if (connectionSocket instanceof VTProxySocket)
    {
      socketAddress = InetSocketAddress.createUnresolved(host, port);
    }
    else
    {
      socketAddress = new InetSocketAddress(host, port);
    }
    
    if (timeout > 0)
    {
      connectionSocket.connect(socketAddress, timeout);
    }
    else
    {
      connectionSocket.connect(socketAddress);
    }
    connectionSocket.setTcpNoDelay(true);
    connectionSocket.setKeepAlive(true);
    //connectionSocket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return connectionSocket;
  }
  
//  public static Socket connect(String host, int port, Socket proxyConnection, VTProxy proxy) throws IOException
//  {
//    return connect(host, port, proxyConnection, proxy.getProxyType(), proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
//  }
//  
//  public static Socket connect(String host, int port, Socket proxyConnection, Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) throws IOException
//  {
//    if (host == null || host.length() == 0 || host.equals("*"))
//    {
//      host = "";
//    }
//    else
//    {
//      
//    }
//    InetSocketAddress socketAddress = null;
//    
//    if (proxyType == null || proxyType != Proxy.Type.DIRECT)
//    {
//      socketAddress = InetSocketAddress.createUnresolved(host, port);
//    }
//    else
//    {
//      socketAddress = new InetSocketAddress(host, port);
//    }
//    
//    VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
//    
//    Socket socket = next(proxyConnection, proxy);
//    
//    socket.connect(socketAddress);
//    socket.setTcpNoDelay(true);
//    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
//    return socket;
//  }
}
