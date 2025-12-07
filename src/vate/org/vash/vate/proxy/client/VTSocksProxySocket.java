package org.vash.vate.proxy.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.vash.vate.net.sourceforge.jsocks.socks.Socks4Proxy;
import org.vash.vate.net.sourceforge.jsocks.socks.Socks5Proxy;
import org.vash.vate.net.sourceforge.jsocks.socks.SocksSocket;
import org.vash.vate.net.sourceforge.jsocks.socks.UserPasswordAuthentication;

public class VTSocksProxySocket extends VTProxySocket
{
  private Socks5Proxy socks5Proxy;
  private Socks4Proxy socks4Proxy;
  //private Socket socket;
  
  public VTSocksProxySocket(VTProxy currentProxy, Socket currentSocket)
  {
    super(currentProxy, currentSocket);
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      String proxyHost = currentProxy.getProxyHost();
      int proxyPort = currentProxy.getProxyPort();
      String proxyUser = currentProxy.getProxyUser();
      String proxyPassword = currentProxy.getProxyPassword();
      if (proxyHost == null)
      {
        proxyHost = "";
      }
      
      try
      {
        connectProxy(0);
        socks5Proxy = new Socks5Proxy(null, proxyHost, proxyPort, currentSocket);
        if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
        {
          UserPasswordAuthentication authentication = new UserPasswordAuthentication(proxyUser, proxyPassword);
          socks5Proxy.setAuthenticationMethod(UserPasswordAuthentication.METHOD_ID, authentication);
        }
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(host.getHostName(), host.getPort(), 0, socks5Proxy);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      
      if (proxySocket != null)
      {
        return;
      }
      
      try
      {
        connectProxy(0);
        proxyUser = (proxyUser != null ? proxyUser : "");
        socks4Proxy = new Socks4Proxy(null, proxyHost, proxyPort, proxyUser, currentSocket);
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(host.getHostName(), host.getPort(), 0, socks4Proxy);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      
      if (proxySocket == null)
      {
        throw new IOException("socks tunneling failed");
      }
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (proxySocket == null)
    {
      String proxyHost = currentProxy.getProxyHost();
      int proxyPort = currentProxy.getProxyPort();
      String proxyUser = currentProxy.getProxyUser();
      String proxyPassword = currentProxy.getProxyPassword();
      if (proxyHost == null)
      {
        proxyHost = "";
      }
      
      try
      {
        connectProxy(timeout);
        socks5Proxy = new Socks5Proxy(null, proxyHost, proxyPort, currentSocket);
        if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
        {
          UserPasswordAuthentication authentication = new UserPasswordAuthentication(proxyUser, proxyPassword);
          socks5Proxy.setAuthenticationMethod(UserPasswordAuthentication.METHOD_ID, authentication);
        }
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(host.getHostName(), host.getPort(), timeout, socks5Proxy);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
        proxySocket = null;
      }
      
      try
      {
        connectProxy(timeout);
        proxyUser = (proxyUser != null ? proxyUser : "");
        socks4Proxy = new Socks4Proxy(null, proxyHost, proxyPort, proxyUser, currentSocket);
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(host.getHostName(), host.getPort(), timeout, socks4Proxy);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      
      if (proxySocket == null)
      {
        throw new IOException("socks tunneling failed");
      }
    }
  }
  
  public void bind(SocketAddress bindpoint) throws IOException
  {
    
  }
}