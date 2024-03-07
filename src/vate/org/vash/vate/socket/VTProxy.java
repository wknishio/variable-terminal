package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Socket;

public class VTProxy
{
  public enum VTProxyType
  {
    GLOBAL,
    DIRECT,
    HTTP,
    SOCKS,
    AUTO
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
  
  public static Socket next(Socket currentSocket, VTProxy... proxies)
  {
    if (proxies == null || proxies.length <= 0)
    {
      if (currentSocket == null)
      {
        currentSocket = new Socket();
      }
      return currentSocket;
    }
    for (VTProxy proxy : proxies)
    {
      currentSocket = nextSocket(currentSocket, proxy);
    }
    return currentSocket;
  }
  
  @SuppressWarnings("all")
  private static Socket nextSocket(Socket currentSocket, VTProxy proxy)
  {
    Socket nextSocket = null;
    
    if (proxy != null)
    {
      if (proxy.getProxyType() == VTProxyType.GLOBAL)
      {
        nextSocket = new Socket();
        
//        if (currentSocket != null)
//        {
//          try
//          {
//            currentSocket.close();
//          }
//          catch (Throwable t)
//          {
//            
//          }
//        }
      }
      else if (proxy.getProxyType() == VTProxyType.DIRECT)
      {
        nextSocket = new Socket(Proxy.NO_PROXY);
        
//        if (currentSocket != null)
//        {
//          try
//          {
//            currentSocket.close();
//          }
//          catch (Throwable t)
//          {
//            
//          }
//        }
      }
      else if (proxy.getProxyType() == VTProxyType.SOCKS)
      {
        if (currentSocket != null && !currentSocket.isConnected())
        {
          try
          {
            currentSocket = connect(proxy.getProxyHost(), proxy.getProxyPort(), currentSocket);
          }
          catch (Throwable t)
          {
            currentSocket = null;
          }
        }
        nextSocket = new VTSocksProxySocket(currentSocket, proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
      }
      else if (proxy.getProxyType() == VTProxyType.HTTP)
      {
        if (currentSocket != null && !currentSocket.isConnected())
        {
          try
          {
            currentSocket = connect(proxy.getProxyHost(), proxy.getProxyPort(), currentSocket);
          }
          catch (Throwable t)
          {
            currentSocket = null;
          }
        }
        nextSocket = new VTHttpProxySocket(currentSocket, proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
      }
      else if (proxy.getProxyType() == VTProxyType.AUTO)
      {
        if (currentSocket != null && !currentSocket.isConnected())
        {
          try
          {
            currentSocket = connect(proxy.getProxyHost(), proxy.getProxyPort(), currentSocket);
          }
          catch (Throwable t)
          {
            currentSocket = null;
          }
        }
        nextSocket = new VTAutoProxySocket(currentSocket, proxy.getProxyHost(), proxy.getProxyPort(), proxy.getProxyUser(), proxy.getProxyPassword());
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
  
  public static Socket connect(String host, int port, Socket currentSocket, VTProxy... proxies) throws IOException
  {
    if (host == null || host.length() == 0 || host.equals("*"))
    {
      host = "";
    }
    
    InetSocketAddress socketAddress = null;
    
    Socket connectionSocket = next(currentSocket, proxies);
    
    if (connectionSocket instanceof VTProxySocket)
    {
      socketAddress = InetSocketAddress.createUnresolved(host, port);
    }
    else
    {
      socketAddress = new InetSocketAddress(host, port);
    }
    
    connectionSocket.connect(socketAddress);
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
