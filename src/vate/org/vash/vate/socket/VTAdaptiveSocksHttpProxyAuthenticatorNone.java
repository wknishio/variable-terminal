package org.vash.vate.socket;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PushbackInputStream;
import java.net.Socket;

import org.vash.vate.nanohttpd.VTNanoHTTPDProxySession;

import net.sourceforge.jsocks.socks.server.ServerAuthenticator;
import net.sourceforge.jsocks.socks.server.ServerAuthenticatorNone;

public class VTAdaptiveSocksHttpProxyAuthenticatorNone extends ServerAuthenticatorNone
{
  private VTProxy connect_proxy; 
  public VTAdaptiveSocksHttpProxyAuthenticatorNone(VTProxy proxy)
  {
    this.connect_proxy = proxy;
  }
  
  public ServerAuthenticator startSession(Socket s) throws IOException
  {
    PushbackInputStream in = new PushbackInputStream(s.getInputStream());
    OutputStream out = s.getOutputStream();
    int version = in.read();
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
        VTNanoHTTPDProxySession httpProxy = new VTNanoHTTPDProxySession(s, in, null, null, connect_proxy, true);
        try
        {
          httpProxy.run();
        }
        catch (Throwable t)
        {
          
        }
      }
      return null;
    }
    return new ServerAuthenticatorNone(in, out);
  }
}
