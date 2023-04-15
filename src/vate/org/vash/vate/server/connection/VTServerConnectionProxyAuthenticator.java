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
    PasswordAuthentication invalid = new PasswordAuthentication(" ", " ".toCharArray());
    if (!connector.isUseProxyAuthentication())
    {
      return invalid;
    }
    try
    {
      int proxyPort = connector.getProxyPort();
      if (proxyPort != getRequestingPort())
      {
        return invalid;
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
            return invalid;
          }
        }
      }
      return new PasswordAuthentication(connector.getProxyUser(), connector.getProxyPassword().toCharArray());
    }
    catch (Throwable t)
    {
      
    }
    return invalid;
  }
}
