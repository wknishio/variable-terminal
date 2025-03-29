package org.vash.vate.proxy.client;

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
    //directSocket = new Socket(Proxy.NO_PROXY);
    //globalSocket = new Socket();
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        connectProxy(0);
        httpProxySocket = new VTHttpProxySocket(currentProxy, currentSocket);
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
        connectProxy(0);
        socksProxySocket = new VTSocksProxySocket(currentProxy, currentSocket);
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
        connectProxy(timeout);
        httpProxySocket = new VTHttpProxySocket(currentProxy, currentSocket);
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
        connectProxy(timeout);
        socksProxySocket = new VTSocksProxySocket(currentProxy, currentSocket);
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
