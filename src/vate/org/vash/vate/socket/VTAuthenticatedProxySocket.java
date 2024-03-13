package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;

public class VTAuthenticatedProxySocket extends Socket
{
  private final VTAuthenticatedProxySocketFactory socketFactory;
  
  public VTAuthenticatedProxySocket(VTAuthenticatedProxySocketFactory socketFactory)
  {
    this.socketFactory = socketFactory;
  }
  
  public Socket connect(String host, int port, VTProxy... proxies) throws IOException
  {
    return socketFactory.createSocket(host, port, null, proxies);
  }
}
