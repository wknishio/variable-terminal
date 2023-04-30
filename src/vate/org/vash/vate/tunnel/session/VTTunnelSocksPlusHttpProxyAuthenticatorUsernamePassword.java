package org.vash.vate.tunnel.session;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import org.vash.nanohttpd.VTNanoHTTPDProxySession;

import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;
import net.sourceforge.jsocks.socks.server.UserPasswordAuthenticator;
import net.sourceforge.jsocks.socks.server.UserValidation;

public class VTTunnelSocksPlusHttpProxyAuthenticatorUsernamePassword extends UserPasswordAuthenticator
{  
  public VTTunnelSocksPlusHttpProxyAuthenticatorUsernamePassword(UserValidation validator)
  {
    super(validator);
  }

  public ServerAuthenticator startSession(Socket s) throws IOException {

    PushbackInputStream in = new PushbackInputStream(s.getInputStream());
    OutputStream out = s.getOutputStream();

    int version = in.read();
    if (version != 5)
    {
      if (version != 4)
      {
        in.unread(version);
        VTNanoHTTPDProxySession session = new VTNanoHTTPDProxySession(s, in, validator.getUsername(), validator.getPassword());
        try
        {
          session.run();
        }
        catch (Throwable t)
        {
          
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
