package org.vash.vate.tunnel.connection;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class VTTunnelConnectionProxyAuthenticator extends Authenticator
{
  private String user;
  private String password;
  
  public VTTunnelConnectionProxyAuthenticator(String user, String password)
  {
    this.user = user;
    this.password = password;
  }
  
  public void setUserPassword(String user, String password)
  {
    this.user = user;
    this.password = password;
  }
  
  public PasswordAuthentication getPasswordAuthentication()
  {
    return new PasswordAuthentication(user, password.toCharArray());
  }
}