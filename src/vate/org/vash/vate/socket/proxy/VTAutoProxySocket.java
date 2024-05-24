package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class VTAutoProxySocket extends VTProxySocket
{
  private Socket httpProxySocket;
  private Socket socksProxySocket;
  //private Socket directSocket;
  //private Socket globalSocket;
  
  //private Socket socket;
  
  public VTAutoProxySocket(VTProxy currentProxy, Socket currentSocket)
  {
    super(currentProxy, currentSocket);
    httpProxySocket = new VTHttpProxySocket(currentProxy, currentSocket);
    socksProxySocket = new VTSocksProxySocket(currentProxy, currentSocket);
    //directSocket = new Socket(Proxy.NO_PROXY);
    //globalSocket = new Socket();
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        httpProxySocket.connect(endpoint);
        proxySocket = httpProxySocket;
      }
      catch (Throwable t)
      {
        proxySocket = null;
      }
      if (proxySocket != null && proxySocket.isConnected() && !proxySocket.isClosed())
      {
        return;
      }
      
      try
      {
        socksProxySocket.connect(endpoint);
        proxySocket = socksProxySocket;
      }
      catch (Throwable t)
      {
        proxySocket = null;
      }
      if (proxySocket != null)
      {
        return;
      }
      
      if (proxySocket == null)
      {
        throw new IOException("auto tunneling failed");
      }
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        httpProxySocket.connect(endpoint, timeout);
        proxySocket = httpProxySocket;
      }
      catch (Throwable t)
      {
        proxySocket = null;
      }
      if (proxySocket != null)
      {
        return;
      }
      
      try
      {
        socksProxySocket.connect(endpoint, timeout);
        proxySocket = socksProxySocket;
      }
      catch (Throwable t)
      {
        proxySocket = null;
      }
      if (proxySocket != null)
      {
        return;
      }
      
      if (proxySocket == null)
      {
        throw new IOException("auto tunneling failed");
      }
    }
  }
  
  public void bind(SocketAddress bindpoint) throws IOException
  {
    
  }
}
