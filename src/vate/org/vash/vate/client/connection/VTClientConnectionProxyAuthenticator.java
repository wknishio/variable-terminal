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