package org.vash.vate.proxy.server;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.Collection;
import java.util.Random;
import java.util.concurrent.ExecutorService;

import org.vash.vate.net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import org.vash.vate.net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;
import org.vash.vate.net.sourceforge.jsocks.socks.server.UserPasswordAuthenticator;
import org.vash.vate.net.sourceforge.jsocks.socks.server.UserValidation;
import org.vash.vate.proxy.client.VTProxy;

public class VTSocksHttpProxyAuthenticatorUsernamePassword extends UserPasswordAuthenticator
{
  private VTProxy connect_proxy;
  private String bind;
  private int connectTimeout;
  private int dataTimeout;
  private ExecutorService executorService;
  private final Collection<String> nonces;
  private final Random random;
  
  public VTSocksHttpProxyAuthenticatorUsernamePassword(UserValidation validator, Collection<String> nonces, Random random, ExecutorService executorService, String bind, int connectTimeout, int dataTimeout, VTProxy proxy)
  {
    super(validator);
    this.nonces = nonces;
    this.random = random;
    this.executorService = executorService;
    this.bind = bind;
    this.connectTimeout = connectTimeout;
    this.dataTimeout = dataTimeout;
    this.connect_proxy = proxy;
  }

  public ServerAuthenticator startSession(Socket socket) throws IOException
  {
    InputStream socketInputStream = socket.getInputStream();
    PushbackInputStream in = null;
    if (socketInputStream instanceof PushbackInputStream)
    {
      in = (PushbackInputStream) socketInputStream;
    }
    else
    {
      in = new PushbackInputStream(socketInputStream);
    }
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
          VTNanoHTTPDProxySession httpProxy = new VTNanoHTTPDProxySession(socket, in, nonces, random, executorService, true, validator.getUsernames(), validator.getPasswords(), bind, connectTimeout, dataTimeout, connect_proxy);
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