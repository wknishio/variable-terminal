package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Proxy.Type;
import java.net.Socket;
import java.net.UnknownHostException;

import org.vash.vate.VT;

public class VTSocketFactory extends VTAuthenticatedProxySocketFactory
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
  
  public Socket createSocket(String host, int port, Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) throws IOException, UnknownHostException
  {
    return VTProxy.connect(host, port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword, null);
  }
  
  public Socket createSocket(String host, int port, Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, Socket proxyConnection) throws IOException, UnknownHostException
  {
    return VTProxy.connect(host, port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword, proxyConnection);
  }
  
  public Socket createSocket(String host, int port, VTProxy proxy) throws IOException, UnknownHostException
  {
    return VTProxy.connect(host, port, proxy);
  }
}