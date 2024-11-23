package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

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
  private ExecutorService executorService;
  
  public VTSocksHttpProxyAuthenticatorUsernamePassword(UserValidation validator, VTProxy proxy, VTRemoteSocketFactory socket_factory, int connectTimeout, ExecutorService executorService)
  {
    super(validator);
    this.connect_proxy = proxy;
    this.socket_factory = socket_factory;
    this.connectTimeout = connectTimeout;
    this.executorService = executorService;
  }

  public ServerAuthenticator startSession(Socket socket) throws IOException
  {
    PushbackInputStream in = new PushbackInputStream(socket.getInputStream());
    OutputStream out = socket.getOutputStream();
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
          VTNanoHTTPDProxySession httpProxy = new VTNanoHTTPDProxySession(socket, in, executorService, true, validator.getUsernames(), validator.getPasswords(), connect_proxy, socket_factory, connectTimeout);
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
    if (!doUserPasswordAuthentication(socket, in, out))
      return null;
    return new ServerAuthenticatorNone(in, out);
  }
}
