package org.vash.vate.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import org.vash.vate.nanohttpd.VTNanoHTTPDProxySession;

import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;
import net.sourceforge.jsocks.socks.server.UserPasswordAuthenticator;
import net.sourceforge.jsocks.socks.server.UserValidation;

public class VTSocksHttpAdaptiveProxyAuthenticatorUsernamePassword extends UserPasswordAuthenticator
{
  private VTProxy connect_proxy;
  public VTSocksHttpAdaptiveProxyAuthenticatorUsernamePassword(UserValidation validator, VTProxy proxy)
  {
    super(validator);
    this.connect_proxy = proxy;
  }

  public ServerAuthenticator startSession(Socket s) throws IOException
  {
    PushbackInputStream in = new PushbackInputStream(s.getInputStream());
    OutputStream out = s.getOutputStream();
    int version = in.read();
    if (version != 5)
    {
      if (version != 4)
      {
        //System.out.println("version=" + version);
        if (version != -1)
        {
          in.unread(version);
          //fallback to use http proxy instead
          VTNanoHTTPDProxySession httpProxy = new VTNanoHTTPDProxySession(s, in, validator.getUsername(), validator.getPassword(), connect_proxy, true);
          try
          {
            httpProxy.run();
          }
          catch (Throwable t)
          {
            
          }
        }
      }
      return null;
    }
    if (!selectSocks5Authentication(in, out, METHOD_ID))
      return null;
    if (!doUserPasswordAuthentication(s, in, out))
      return null;
    return new ServerAuthenticatorNone(in, out);
  }
}
