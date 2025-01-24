package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.socket.remote.VTRemoteSocketFactory;

import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;

public class VTSocksHttpProxyAuthenticatorNone extends ServerAuthenticatorNone
{
  private VTProxy connect_proxy;
  private VTRemoteSocketFactory socket_factory;
  private int connectTimeout;
  private ExecutorService executorService;
  private String bind;
  
  public VTSocksHttpProxyAuthenticatorNone(ExecutorService executorService, String bind, int connectTimeout, VTRemoteSocketFactory socket_factory, VTProxy proxy)
  {
    this.executorService = executorService;
    this.connect_proxy = proxy;
    this.socket_factory = socket_factory;
    this.connectTimeout = connectTimeout;
    this.bind = bind;
  }
  
  public ServerAuthenticator startSession(Socket socket) throws IOException
  {
    PushbackInputStream in = new PushbackInputStream(socket.getInputStream());
    OutputStream out = socket.getOutputStream();
    int version = in.read();
    //System.out.println("version=" + version);
    if (version == 5)
    {
      if (!selectSocks5Authentication(in, out, 0))
        return null;
    }
    else if (version == 4)
    {
      // Else it is the request message allready, version 4
      in.unread(version);
    }
    else
    {
      //System.out.println("version=" + version);
      if (version != -1)
      {
        in.unread(version);
        //fallback to use http proxy instead
        VTNanoHTTPDProxySession httpProxy = new VTNanoHTTPDProxySession(socket, in, null, null, executorService, true, null, null, connect_proxy, socket_factory, connectTimeout, bind);
        try
        {
          httpProxy.run();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
      }
      return null;
    }
    return new ServerAuthenticatorNone(in, out);
  }
}