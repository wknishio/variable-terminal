package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class VTAutoProxySocket extends VTProxySocket
{
  private VTProxySocket httpProxySocket;
  private VTProxySocket socksProxySocket;
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
  
  public void connectSocket(String host, int port, int timeout) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        httpProxySocket.connectSocket(host, port, timeout);
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
        socksProxySocket.connectSocket(host, port, timeout);
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
