package org.vash.vate.ftp.server;

import java.io.File;
import java.net.InetAddress;

import org.vash.vate.com.guichaguri.minimalftp.FTPConnection;
import org.vash.vate.com.guichaguri.minimalftp.api.IFileSystem;
import org.vash.vate.com.guichaguri.minimalftp.api.IUserAuthenticator;

public class VTFTPAuthenticator implements IUserAuthenticator<File>
{
  private final IFileSystem<File> fs;
  private final String authUsername;
  private final String authPassword;
  
  public VTFTPAuthenticator(IFileSystem<File> fs, String username, String password)
  {
    this.fs = fs;
    this.authUsername = username;
    this.authPassword = password;
  }
  
  public boolean acceptsHost(FTPConnection con, InetAddress host)
  {
    return true;
  }
  
  public boolean needsUsername(FTPConnection con)
  {
    return authUsername != null && authUsername.length() > 0;
  }
  
  public boolean needsPassword(FTPConnection con, String username, InetAddress host)
  {
    return authPassword != null && authPassword.length() > 0;
  }
  
  public IFileSystem<File> authenticate(FTPConnection con, InetAddress host, String username, String password) throws AuthException
  {
    if (authUsername != null && authUsername.length() > 0)
    {
      if (!authUsername.equals(username))
      {
        throw new AuthException();
      }
    }
    if (authPassword != null && authPassword.length() > 0)
    {
      if (!authPassword.equals(password))
      {
        throw new AuthException();
      }
    }
    return fs;
  }
}