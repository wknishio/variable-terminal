package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

public class VTAutoProxySocket extends VTProxySocket
{
  private Socket httpSocket;
  private Socket socksSocket;
  //private Socket directSocket;
  //private Socket globalSocket;
  
  //private Socket socket;
  
  public VTAutoProxySocket(Socket currentSocket, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    httpSocket = new VTHttpProxySocket(currentSocket, proxyHost, proxyPort, proxyUser, proxyPassword);
    socksSocket = new VTSocksProxySocket(currentSocket, proxyHost, proxyPort, proxyUser, proxyPassword);
    //directSocket = new Socket(Proxy.NO_PROXY);
    //globalSocket = new Socket();
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
//    InetSocketAddress unresolved = null;
//    InetSocketAddress resolved = null;
//    
//    if (endpoint instanceof InetSocketAddress)
//    {
//      unresolved = (InetSocketAddress) endpoint;
//    }
    
    if (proxySocket == null)
    {
      try
      {
        httpSocket.connect(endpoint);
        proxySocket = httpSocket;
      }
      catch (Throwable t)
      {
        
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
        
      }
      if (proxySocket != null)
      {
        return;
      }
      
//      if (unresolved != null)
//      {
//        try
//        {
//          if (unresolved.isUnresolved())
//          {
//            resolved = new InetSocketAddress(unresolved.getHostName(), unresolved.getPort());
//          }
//          else
//          {
//            resolved = unresolved;
//          }
//        }
//        catch (Throwable t)
//        {
//          
//        }
//      }
//      
//      try
//      {
//        directSocket.connect(resolved != null ? resolved : endpoint);
//        proxySocket = socksSocket;
//      }
//      catch (Throwable t)
//      {
//        
//      }
//      if (proxySocket != null)
//      {
//        return;
//      }
//      
//      try
//      {
//        globalSocket.connect(resolved != null ? resolved : endpoint);
//        proxySocket = socksSocket;
//      }
//      catch (Throwable t)
//      {
//        
//      }
//      if (proxySocket != null)
//      {
//        return;
//      }
      
      if (proxySocket == null)
      {
        throw new IOException("auto tunneling failed");
      }
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
//    InetSocketAddress unresolved = null;
//    InetSocketAddress resolved = null;
//    
//    if (endpoint instanceof InetSocketAddress)
//    {
//      unresolved = (InetSocketAddress) endpoint;
//    }
    
    if (proxySocket == null)
    {
      try
      {
        httpSocket.connect(endpoint, timeout);
        proxySocket = httpSocket;
      }
      catch (Throwable t)
      {
        
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
        
      }
      if (proxySocket != null)
      {
        return;
      }
      
//      if (unresolved != null)
//      {
//        try
//        {
//          if (unresolved.isUnresolved())
//          {
//            resolved = new InetSocketAddress(unresolved.getHostName(), unresolved.getPort());
//          }
//          else
//          {
//            resolved = unresolved;
//          }
//        }
//        catch (Throwable t)
//        {
//          
//        }
//      }
//      
//      try
//      {
//        directSocket.connect(resolved != null ? resolved : endpoint, timeout);
//        proxySocket = socksSocket;
//      }
//      catch (Throwable t)
//      {
//        
//      }
//      if (proxySocket != null)
//      {
//        return;
//      }
//      
//      try
//      {
//        globalSocket.connect(resolved != null ? resolved : endpoint, timeout);
//        proxySocket = socksSocket;
//      }
//      catch (Throwable t)
//      {
//        
//      }
//      if (proxySocket != null)
//      {
//        return;
//      }
      
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
