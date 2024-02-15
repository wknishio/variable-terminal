package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.commons.httpclient.ProxyClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class VTHttpProxySocket extends VTProxySocket
{
  private ProxyClient proxyClient;
  //private Socket socket;
  
  public VTHttpProxySocket(Socket currentSocket, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
  {
    proxyClient = new ProxyClient(currentSocket);
    proxyClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
    if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      proxyClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyUser, proxyPassword));
    }
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        proxyClient.getHostConfiguration().setHost(host.getHostName(), host.getPort());
        //proxyClient.getParams().setConnectionManagerTimeout(VT.VT_CONNECTION_ATTEMPT_TIMEOUT_MILLISECONDS);
        //proxyClient.getParams().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
        proxySocket = proxyClient.connect().getSocket();
        proxySocket.setTcpNoDelay(true);
        proxySocket.setKeepAlive(true);
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (proxySocket == null)
      {
        throw new IOException("http tunneling failed");
      }
    }
  }
  
  public void connect(SocketAddress endpoint, int timeout) throws IOException
  {
    if (proxySocket == null)
    {
      try
      {
        InetSocketAddress host = (InetSocketAddress) endpoint;
        proxyClient.getHostConfiguration().setHost(host.getHostName(), host.getPort());
        proxyClient.getParams().setConnectionManagerTimeout(timeout);
        //proxyClient.getParams().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
        proxySocket = proxyClient.connect().getSocket();
        proxySocket.setTcpNoDelay(true);
        proxySocket.setKeepAlive(true);
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
      }
      if (proxySocket == null)
      {
        throw new IOException("http tunneling failed");
      }
    }
  }
  
  public void bind(SocketAddress bindpoint) throws IOException
  {
    
  }
}
