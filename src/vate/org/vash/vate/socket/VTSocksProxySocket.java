package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import net.sourceforge.jsocks.socks.Socks4Proxy;
import net.sourceforge.jsocks.socks.Socks5Proxy;
import net.sourceforge.jsocks.socks.SocksSocket;
import net.sourceforge.jsocks.socks.UserPasswordAuthentication;

public class VTSocksProxySocket extends VTProxySocket
{
  private Socks5Proxy proxyClient5;
  private Socks4Proxy proxyClient4;
  //private Socket socket;
  
  public VTSocksProxySocket(Socket proxyConnection, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    if (proxyHost == null)
    {
      proxyHost = "";
    }
    proxyClient5 = new Socks5Proxy(null, proxyHost, proxyPort, proxyConnection);
    if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      UserPasswordAuthentication authentication = new UserPasswordAuthentication(proxyUser, proxyPassword);
      proxyClient5.setAuthenticationMethod(UserPasswordAuthentication.METHOD_ID, authentication);
    }
    proxyClient4 = new Socks4Proxy(null, proxyHost, proxyPort, proxyUser != null ? proxyUser : "", proxyConnection);
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient5, host.getHostName(), host.getPort());
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
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient4, host.getHostName(), host.getPort());
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
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient5, host.getHostName(), host.getPort());
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
        InetSocketAddress host = (InetSocketAddress) endpoint;
        SocksSocket socksSocket = new SocksSocket(proxyClient4, host.getHostName(), host.getPort());
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
