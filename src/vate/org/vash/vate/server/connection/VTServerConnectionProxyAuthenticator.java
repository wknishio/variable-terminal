package org.vash.vate.server.connection;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;

public class VTServerConnectionProxyAuthenticator extends Authenticator
{
  private VTServerConnector connector;
  
  public VTServerConnectionProxyAuthenticator(VTServerConnector connector)
  {
    this.connector = connector;
  }
  
  public PasswordAuthentication getPasswordAuthentication()
  {
    try
    {
      int proxyPort = connector.getProxyPort();
      if (proxyPort != getRequestingPort())
      {
        return null;
      }
      
      String proxyHost = connector.getProxyAddress();
      if (proxyHost != null && proxyHost.length() > 0)
      {
        if (proxyHost.equals(getRequestingHost()))
        {
          // ok
        }
        else
        {
          InetAddress site = getRequestingSite();
          // if (proxyHost.equals(site.getHostAddress()) ||
          // proxyHost.equals(site.getHostName()) ||
          // proxyHost.equals(site.getCanonicalHostName()))
          if (proxyHost.equals(site.getHostAddress()))
          {
            // ok
          }
          else
          {
            // failed
            return null;
          }
        }
      }
      return new PasswordAuthentication(connector.getProxyUser(), connector.getProxyPassword().toCharArray());
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
}
