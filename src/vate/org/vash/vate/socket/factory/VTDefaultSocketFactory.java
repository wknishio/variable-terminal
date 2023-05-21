package org.vash.vate.socket.factory;

import java.io.IOException;
import java.net.Authenticator;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import org.vash.vate.VT;

public class VTDefaultSocketFactory extends VTAuthenticatedProxySocketFactory
{
  public Socket createSocket()
  {
    Socket socket = new Socket();
    return socket;
  }
  
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind, local));
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind, local));
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port) throws IOException, UnknownHostException
  {
    Proxy proxy = Proxy.NO_PROXY;
    InetSocketAddress connectAddress = null;
    if (proxyType != Proxy.Type.DIRECT)
    {
      proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
      connectAddress = InetSocketAddress.createUnresolved(host, port);
    }
    else
    {
      connectAddress = new InetSocketAddress(host, port);
    }
    Socket socket = new Socket(proxy);
    if (proxyType != Proxy.Type.DIRECT && proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      Authenticator.setDefault(new VTDefaultProxyAuthenticator(proxyUser, proxyPassword));
    }
    socket.connect(connectAddress);
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port) throws IOException
  {
    Proxy proxy = Proxy.NO_PROXY;
    InetSocketAddress connectAddress = null;
    if (proxyType != Proxy.Type.DIRECT)
    {
      proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
      connectAddress = InetSocketAddress.createUnresolved(host.getHostName(), port);
    }
    else
    {
      connectAddress = new InetSocketAddress(host, port);
    }
    Socket socket = new Socket(proxy);
    if (proxyType != Proxy.Type.DIRECT && proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      Authenticator.setDefault(new VTDefaultProxyAuthenticator(proxyUser, proxyPassword));
    }
    socket.connect(connectAddress);
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    Proxy proxy = Proxy.NO_PROXY;
    InetSocketAddress connectAddress = null;
    if (proxyType != Proxy.Type.DIRECT)
    {
      proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
      connectAddress = InetSocketAddress.createUnresolved(host, port);
    }
    else
    {
      connectAddress = new InetSocketAddress(host, port);
    }
    Socket socket = new Socket(proxy);
    if (proxyType != Proxy.Type.DIRECT && proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      Authenticator.setDefault(new VTDefaultProxyAuthenticator(proxyUser, proxyPassword));
    }
    socket.connect(connectAddress);
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    Proxy proxy = Proxy.NO_PROXY;
    InetSocketAddress connectAddress = null;
    if (proxyType != Proxy.Type.DIRECT)
    {
      proxy = new Proxy(proxyType, new InetSocketAddress(proxyHost, proxyPort));
      connectAddress = InetSocketAddress.createUnresolved(host.getHostName(), port);
    }
    else
    {
      connectAddress = new InetSocketAddress(host, port);
    }
    Socket socket = new Socket(proxy);
    if (proxyType != Proxy.Type.DIRECT && proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
    {
      Authenticator.setDefault(new VTDefaultProxyAuthenticator(proxyUser, proxyPassword));
    }
    socket.connect(connectAddress);
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
}