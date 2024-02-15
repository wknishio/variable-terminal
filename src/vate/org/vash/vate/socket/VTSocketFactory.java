package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

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
    socket.setKeepAlive(true);
    //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host.getHostName(), port));
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
    //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind.getHostName(), local));
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
    //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind.getHostName(), local));
    socket.connect(new InetSocketAddress(host.getHostName(), port));
    socket.setTcpNoDelay(true);
    socket.setKeepAlive(true);
    //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(String host, int port, Socket proxyConnection, VTProxy... proxies) throws IOException, UnknownHostException
  {
    return VTProxy.connect(host, port, proxyConnection, proxies);
  }
}