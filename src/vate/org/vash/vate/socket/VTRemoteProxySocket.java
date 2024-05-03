package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;

public class VTRemoteProxySocket extends Socket
{
  private final VTRemoteProxySocketFactory socketFactory;
  
  public VTRemoteProxySocket(VTRemoteProxySocketFactory socketFactory)
  {
    this.socketFactory = socketFactory;
  }
  
  public Socket connect(String host, int port, VTProxy... proxies) throws IOException
  {
    return socketFactory.createSocket(host, port, null, proxies);
  }
  
  public Socket accept(String host, int port, int timeout) throws IOException
  {
    return socketFactory.acceptSocket(host, port, timeout);
  }
}
