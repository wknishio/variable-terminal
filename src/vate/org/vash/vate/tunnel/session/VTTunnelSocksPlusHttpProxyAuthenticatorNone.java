package org.vash.vate.tunnel.session;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import org.vash.nanohttpd.HTTPProxySession;

import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;

public class VTTunnelSocksPlusHttpProxyAuthenticatorNone extends ServerAuthenticatorNone
{
  public ServerAuthenticator startSession(Socket s) throws IOException {

    PushbackInputStream in = new PushbackInputStream(s.getInputStream());
    OutputStream out = s.getOutputStream();

    int version = in.read();
    if (version == 5) {
      if (!selectSocks5Authentication(in, out, 0))
        return null;
    } else if (version == 4) {
      // Else it is the request message allready, version 4
      in.unread(version);
    } else {
      in.unread(version);
      HTTPProxySession session = new HTTPProxySession(s, in, null, null);
      try
      {
        session.run();
      }
      catch (Throwable t)
      {
        
      }
      
      return null;
    }
      

    return new ServerAuthenticatorNone(in, out);
  }
}
