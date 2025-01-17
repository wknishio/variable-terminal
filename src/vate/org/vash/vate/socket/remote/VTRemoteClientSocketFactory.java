package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.vash.vate.socket.proxy.VTProxy;

public class VTRemoteClientSocketFactory extends SocketFactory
{
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  private final VTRemoteSocketFactory remoteSocketFactory;
  private final int remoteConnectTimeout;
  private final int remoteDataTimeout;
  private final VTProxy remoteProxy;
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = 0;
    this.remoteDataTimeout = 0;
    this.remoteProxy = PROXY_NONE;
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = 0;
    this.remoteProxy = PROXY_NONE;
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteProxy = PROXY_NONE;
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout, VTProxy proxy)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteProxy = proxy;
  }
  
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    return remoteSocketFactory.connectSocket("", host, port, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    return remoteSocketFactory.connectSocket("", host.getHostAddress(), port, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(String host, int port, InetAddress localHost, int localPort) throws IOException, UnknownHostException
  {
    return remoteSocketFactory.connectSocket(localHost.getHostAddress(), host, port, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(InetAddress address, int port, InetAddress localAddress, int localPort) throws IOException
  {
    return remoteSocketFactory.connectSocket(localAddress.getHostAddress(), address.getHostAddress(), port, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
}