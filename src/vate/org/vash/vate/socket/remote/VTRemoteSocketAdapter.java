package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.Socket;

import org.vash.vate.socket.proxy.VTProxy;

public class VTRemoteSocketAdapter extends Socket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  
  public VTRemoteSocketAdapter(VTRemoteSocketFactory socketFactory)
  {
    this.remoteSocketFactory = socketFactory;
  }
  
  public Socket connect(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException
  {
    if (bind == null)
    {
      bind = "";
    }
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.connectSocket(bind, host, port, connectTimeout, dataTimeout, proxies);
  }
  
  public Socket accept(String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.acceptSocket(host, port, connectTimeout, dataTimeout);
  }
  
  public DatagramSocket create(String host, int port, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.createSocket(host, port, dataTimeout);
  }
  
  public DatagramSocket create(InetAddress address, int port, int dataTimeout) throws IOException
  {
    return remoteSocketFactory.createSocket(address, port, dataTimeout);
  }
}