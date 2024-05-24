package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import org.vash.vate.nanohttpd.VTNanoHTTPDProxySession;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;

import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;
import net.sourceforge.jsocks.socks.server.UserPasswordAuthenticator;
import net.sourceforge.jsocks.socks.server.UserValidation;

public class VTSocksHttpProxyAuthenticatorUsernamePassword extends UserPasswordAuthenticator
{
  private VTProxy connect_proxy;
  private VTRemoteSocketFactory socket_factory;
  private int connectTimeout;
  
  public VTSocksHttpProxyAuthenticatorUsernamePassword(UserValidation validator, VTProxy proxy, VTRemoteSocketFactory socket_factory, int connectTimeout)
  {
    super(validator);
    this.connect_proxy = proxy;
    this.socket_factory = socket_factory;
    this.connectTimeout = connectTimeout;
  }

  public ServerAuthenticator startSession(Socket s) throws IOException
  {
    PushbackInputStream in = new PushbackInputStream(s.getInputStream());
    OutputStream out = s.getOutputStream();
    int version = in.read();
    //System.out.println("version=" + version);
    if (version != 5)
    {
      if (version != 4)
      {
        if (version != -1)
        {
          in.unread(version);
          //fallback to use http proxy instead
          VTNanoHTTPDProxySession httpProxy = new VTNanoHTTPDProxySession(s, in, true, validator.getUsername(), validator.getPassword(), connect_proxy, socket_factory, connectTimeout);
          try
          {
            httpProxy.run();
          }
          catch (Throwable t)
          {
            //t.printStackTrace();
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
