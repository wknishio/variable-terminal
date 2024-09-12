package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.Socket;

import org.vash.vate.socket.proxy.VTProxy;

public class VTRemoteSocketAdapter extends Socket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  
  public VTRemoteSocketAdapter(VTRemoteSocketFactory socketFactory)
  {
    this.remoteSocketFactory = socketFactory;
  }
  
  public Socket connectSocket(String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.connectSocket(host, port, connectTimeout, dataTimeout, proxies);
  }
  
  public Socket acceptSocket(String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.acceptSocket(host, port, connectTimeout, dataTimeout);
  }
}