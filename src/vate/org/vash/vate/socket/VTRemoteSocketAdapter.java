package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;

public class VTRemoteSocketAdapter extends Socket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  
  public VTRemoteSocketAdapter(VTRemoteSocketFactory socketFactory)
  {
    this.remoteSocketFactory = socketFactory;
  }
  
  public Socket connect(String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.createSocket(host, port, connectTimeout, dataTimeout, proxies);
  }
  
  public Socket accept(String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.acceptSocket(host, port, connectTimeout, dataTimeout);
  }
}