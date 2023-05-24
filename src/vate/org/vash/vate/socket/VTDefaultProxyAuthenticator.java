package org.vash.vate.socket;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;
import java.util.LinkedHashMap;
import java.util.Map;

public class VTDefaultProxyAuthenticator extends Authenticator
{
  //private String user;
  //private String password;
  private static final PasswordAuthentication INVALID = new PasswordAuthentication(" ", " ".toCharArray());
  private static final Map<String, VTDefaultProxy> PROXIES = new LinkedHashMap<String, VTDefaultProxy>();
  private static final VTDefaultProxyAuthenticator INSTANCE = new VTDefaultProxyAuthenticator();
  
  public static VTDefaultProxyAuthenticator getInstance()
  {
    return INSTANCE;
  }
  
  private VTDefaultProxyAuthenticator()
  {
    
  }
  
  public static void putProxy(String proxyHost, int proxyPort, VTDefaultProxy proxy)
  {
    try
    {
      InetAddress site = InetAddress.getByName(proxyHost);
      if (site != null && (site.isLoopbackAddress() || site.isAnyLocalAddress()))
      {
        proxyHost = site.getHostName();
      }
    }
    catch (Throwable t)
    {
      
    }
    //System.out.println("putProxy().proxyHost=[" + proxyHost + "]");
    //System.out.println("putProxy().proxyPort=[" + proxyPort + "]");
    //System.out.println("putProxy().proxyUser=[" + proxy.getProxyUser() + "]");
    //System.out.println("putProxy().proxyPassword=[" + proxy.getProxyPassword() + "]");
    PROXIES.put(proxyHost + "/" + proxyPort, proxy);
  }
  
  public static void removeProxy(String proxyHost, int proxyPort)
  {
    try
    {
      InetAddress site = InetAddress.getByName(proxyHost);
      if (site != null && (site.isLoopbackAddress() || site.isAnyLocalAddress()))
      {
        proxyHost = site.getHostName();
      }
    }
    catch (Throwable t)
    {
      
    }
    //System.out.println("removeProxy().proxyHost=" + proxyHost);
    //System.out.println("removeProxy().proxyPort=" + proxyPort);
    PROXIES.remove(proxyHost + "/" + proxyPort);
  }
  
  public PasswordAuthentication getPasswordAuthentication()
  {
    //System.out.println("getPasswordAuthentication()");
    String proxyHost = getRequestingHost();
    int proxyPort = getRequestingPort();
    InetAddress site = getRequestingSite();
    
    if (site != null && (site.isAnyLocalAddress() || site.isLoopbackAddress()))
    {
      proxyHost = site.getHostName();
    }
    //System.out.println("getPasswordAuthentication().proxyHost=[" + proxyHost + "]");
    //System.out.println("getPasswordAuthentication().proxyPort=[" + proxyPort + "]");
    
    VTDefaultProxy proxy = PROXIES.get(proxyHost + "/" + proxyPort);
    if (proxy != null)
    {
      //System.out.println("getPasswordAuthentication().proxyUser=[" + proxy.getProxyUser() + "]");
      //System.out.println("getPasswordAuthentication().proxyPassword=[" + proxy.getProxyPassword() + "]");
      return new PasswordAuthentication(proxy.getProxyUser(), proxy.getProxyPassword().toCharArray());
    }
    return INVALID;
  }
}