package org.vash.vate.proxy.server;

import java.net.Socket;

import net.sourceforge.jsocksvt.socks.server.UserValidation;

public class VTSocksMultipleUserValidation implements UserValidation
{
  private String[] usernames;
  private String[] passwords;
  
  public VTSocksMultipleUserValidation(String[] usernames, String[] passwords)
  {
    this.usernames = usernames;
    this.passwords = passwords;
  }
  
  public boolean isUserValid(String username, String password, Socket connection)
  {
    for (int i = 0; i < usernames.length; i++)
    {
      if (usernames[i] != null && usernames[i].equals(username) && passwords[i] != null && passwords[i].equals(password))
      {
        return true;
      }
    }
    return false;
  }

  public String[] getUsernames()
  {
    return usernames;
  }

  public String[] getPasswords()
  {
    return passwords;
  }
}