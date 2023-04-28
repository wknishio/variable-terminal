package org.vash.vate.tunnel.session;

import java.net.Socket;

import net.sourceforge.jsocks.socks.server.UserValidation;

public class VTTunnelSocksSingleUserValidation implements UserValidation
{
  private String username;
  private String password;
  
  public VTTunnelSocksSingleUserValidation(String username, String password)
  {
    this.username = username;
    this.password = password;
  }
  
  public boolean isUserValid(String username, String password, Socket connection)
  {
    return this.username.equals(username) && this.password.equals(password);
  }

  public String getUsername()
  {
    return username;
  }

  public String getPassword()
  {
    return password;
  }
}