package org.vash.vate.proxy.client;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketAddress;

import vate.org.apache.commons.httpclient.ProxyClient;
import vate.org.apache.commons.httpclient.UsernamePasswordCredentials;
import vate.org.apache.commons.httpclient.auth.AuthScope;

public class VTHttpProxySocket extends VTProxySocket
{
  private ProxyClient httpProxyClient;
  //private Socket socket;
  
  public VTHttpProxySocket(VTProxy currentProxy, Socket currentSocket)
  {
    super(currentProxy, currentSocket);
  }
  
  public void connect(SocketAddress endpoint) throws IOException
  {
    if (proxySocket == null)
    {
      String proxyHost = currentProxy.getProxyHost();
      int proxyPort = currentProxy.getProxyPort();
      String proxyUser = currentProxy.getProxyUser();
      String proxyPassword = currentProxy.getProxyPassword();
      
      try
      {
        connectProxy(0);
        httpProxyClient = new ProxyClient(currentSocket);
        httpProxyClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
        if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
        {
          httpProxyClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyUser, proxyPassword));
          httpProxyClient.getParams().setAuthenticationPreemptive(true);
        }
        InetSocketAddress host = (InetSocketAddress) endpoint;
        httpProxyClient.getHostConfiguration().setHost(host.getHostName(), host.getPort());
        //proxyClient.getParams().setConnectionManagerTimeout(VT.VT_CONNECTION_ATTEMPT_TIMEOUT_MILLISECONDS);
        //proxyClient.getParams().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
        proxySocket = httpProxyClient.connect().getSocket();
        proxySocket.setTcpNoDelay(true);
        proxySocket.setKeepAlive(true);
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
        proxySocket = null;
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
      String proxyHost = currentProxy.getProxyHost();
      int proxyPort = currentProxy.getProxyPort();
      String proxyUser = currentProxy.getProxyUser();
      String proxyPassword = currentProxy.getProxyPassword();
      
      try
      {
        connectProxy(timeout);
        httpProxyClient = new ProxyClient(currentSocket);
        httpProxyClient.getHostConfiguration().setProxy(proxyHost, proxyPort);
        if (proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
        {
          httpProxyClient.getState().setProxyCredentials(AuthScope.ANY, new UsernamePasswordCredentials(proxyUser, proxyPassword));
          httpProxyClient.getParams().setAuthenticationPreemptive(true);
        }
        InetSocketAddress host = (InetSocketAddress) endpoint;
        httpProxyClient.getHostConfiguration().setHost(host.getHostName(), host.getPort());
        if (timeout > 0)
        {
          httpProxyClient.getParams().setConnectionManagerTimeout(timeout);
        }
        //proxyClient.getParams().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
        proxySocket = httpProxyClient.connect().getSocket();
        proxySocket.setTcpNoDelay(true);
        proxySocket.setKeepAlive(true);
      }
      catch (Throwable t)
      {
        //t.printStackTrace();
        proxySocket = null;
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
