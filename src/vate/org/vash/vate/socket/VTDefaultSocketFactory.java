package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
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
    socket.connect(new InetSocketAddress(host.getHostName(), port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind.getHostName(), local));
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind.getHostName(), local));
    socket.connect(new InetSocketAddress(host.getHostName(), port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port) throws IOException, UnknownHostException
  {
    return VTDefaultProxy.connect(host, port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port) throws IOException
  {
    return VTDefaultProxy.connect(host.getHostName(), port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    return VTDefaultProxy.connect(host, port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
  
  public Socket createSocket(Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    return VTDefaultProxy.connect(host.getHostName(), port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
  }
}