package org.vash.vate.security;

public class VTCredential
{
  private String user;
  private String password;
  
  public VTCredential(String user, String password)
  {
    this.user = user;
    this.password = password;
  }
  
  public String getUser()
  {
    return user;
  }
  
  public String getPassword()
  {
    return password;
  }
}
