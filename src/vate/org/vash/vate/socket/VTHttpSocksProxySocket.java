package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class VTHttpSocksProxySocket extends VTProxySocket
{
  private Socket httpSocket;
  private Socket socksSocket;
  //private Socket socket;
  
  public VTHttpSocksProxySocket(Socket proxyConnection, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    httpSocket = new VTHttpProxySocket(proxyConnection, proxyHost, proxyPort, proxyUser, proxyPassword);
    socksSocket = new VTSocksProxySocket(proxyConnection, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        httpSocket.connect(endpoint);
        proxySocket = httpSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (proxySocket != null)
      {
        return;
      }
      try
      {
        socksSocket.connect(endpoint);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (proxySocket == null)
      {
        throw new IOException("http/socks tunneling failed");
      }
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        httpSocket.connect(endpoint, timeout);
        proxySocket = httpSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (proxySocket != null)
      {
        return;
      }
      try
      {
        socksSocket.connect(endpoint, timeout);
        proxySocket = socksSocket;
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (proxySocket == null)
      {
        throw new IOException("http/socks tunneling failed");
      }
    }
  }
  
  public void bind(SocketAddress bindpoint) throws IOException
  {
    
  }
}
