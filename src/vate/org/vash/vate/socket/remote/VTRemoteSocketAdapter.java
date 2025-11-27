package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.vash.vate.proxy.client.VTProxy;

public class VTRemoteSocketAdapter extends Socket
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  
  public VTRemoteSocketAdapter(VTRemoteSocketFactory socketFactory)
  {
    this.remoteSocketFactory = socketFactory;
  }
  
  public Socket connect(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy proxy) throws IOException
  {
    if (bind == null)
    {
      bind = "";
    }
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.connectSocket(bind, host, port, connectTimeout, dataTimeout, proxy);
  }
  
  public Socket connect(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (bind == null)
    {
      bind = "";
    }
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.connectSocket(bind, host, port, connectTimeout, dataTimeout);
  }
  
  public Socket accept(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.acceptSocket(bind, host, port, connectTimeout, dataTimeout);
  }
  
  public ServerSocket bind(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.bindSocket(bind, host, port, connectTimeout, dataTimeout);
  }
  
  public void unbind(String bind) throws IOException
  {
    if (bind == null)
    {
      bind = "";
    }
    remoteSocketFactory.unbindSocket(bind);
  }
  
  public DatagramSocket create(String host, int port, int dataTimeout) throws IOException
  {
    if (host == null)
    {
      host = "";
    }
    return remoteSocketFactory.createSocket(host, port, dataTimeout);
  }
}