package org.vash.vate.socket;

import java.net.Socket;

import net.sourceforge.jsocks.socks.server.UserValidation;

public class VTSocksSingleUserValidation implements UserValidation
{
  private String username;
  private String password;
  
  public VTSocksSingleUserValidation(String username, String password)
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