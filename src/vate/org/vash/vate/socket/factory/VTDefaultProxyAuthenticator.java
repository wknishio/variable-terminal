package org.vash.vate.socket.factory;

import java.net.Authenticator;
import java.net.PasswordAuthentication;

public class VTDefaultProxyAuthenticator extends Authenticator
{
  private String user;
  private String password;
  
  public VTDefaultProxyAuthenticator(String user, String password)
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