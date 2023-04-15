package org.vash.vate.client.connection;

import java.net.Authenticator;
import java.net.InetAddress;
import java.net.PasswordAuthentication;

public class VTClientConnectionProxyAuthenticator extends Authenticator
{
  private VTClientConnector connector;
  
  public VTClientConnectionProxyAuthenticator(VTClientConnector connector)
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