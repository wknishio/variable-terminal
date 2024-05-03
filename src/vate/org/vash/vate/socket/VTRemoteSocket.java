package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;

public class VTRemoteSocket extends Socket
{
  private final VTRemoteSocketFactory socketFactory;
  
  public VTRemoteSocket(VTRemoteSocketFactory socketFactory)
  {
    this.socketFactory = socketFactory;
  }
  
  public Socket connect(String host, int port, VTProxy... proxies) throws IOException
  {
    return socketFactory.createSocket(host, port, proxies);
  }
  
  public Socket accept(String host, int port, int timeout) throws IOException
  {
    return socketFactory.acceptSocket(host, port, timeout);
  }
}
