package org.vash.vate.socket.proxy;

import java.io.IOException;
import java.net.Socket;
import java.net.SocketAddress;

import org.apache.commons.httpclient.ProxyClient;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;

public class VTHttpProxySocket extends VTProxySocket
{
  private ProxyClient httpProxyClient;
  //private Socket socket;
  
  public VTHttpProxySocket(VTProxy currentProxy, Socket currentSocket)
  {
    super(currentProxy, currentSocket);
  }
  
  public void connectSocket(String host, int port, int timeout) throws IOException
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
        }
        httpProxyClient.getHostConfiguration().setHost(host, port);
        httpProxyClient.getParams().setConnectionManagerTimeout(timeout);
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
