package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import net.sourceforge.jsocks.socks.Socks4Proxy;
import net.sourceforge.jsocks.socks.Socks5Proxy;
import net.sourceforge.jsocks.socks.SocksSocket;
import net.sourceforge.jsocks.socks.UserPasswordAuthentication;

public class VTSocksProxySocket extends VTProxySocket
{
  private Socks5Proxy socks5Proxy;
  private Socks4Proxy socks4Proxy;
  //private Socket socket;
  
  public VTSocksProxySocket(VTProxy currentProxy, Socket currentSocket)
  {
    super(currentProxy, currentSocket);
  }
  
  public void connectSocket(String host, int port, int timeout) throws IOException
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
        SocksSocket socksSocket = new SocksSocket(socks5Proxy, host, port, timeout);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
        proxySocket = null;
      }
      if (proxySocket != null)
      {
        return;
      }
      try
      {
        connectProxy(timeout);
        socks4Proxy = new Socks4Proxy(null, proxyHost, proxyPort, proxyUser != null ? proxyUser : "", currentSocket);
        SocksSocket socksSocket = new SocksSocket(socks4Proxy, host, port, timeout);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
        proxySocket = null;
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