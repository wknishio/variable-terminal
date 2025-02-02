package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;

import javax.net.SocketFactory;

import org.vash.vate.proxy.client.VTProxy;

public class VTRemoteClientSocketFactory extends SocketFactory
{
  private static final VTProxy PROXY_NONE = new VTProxy(VTProxy.VTProxyType.GLOBAL, "", 0, "", "");
  private final VTRemoteSocketFactory remoteSocketFactory;
  private final int remoteConnectTimeout;
  private final int remoteDataTimeout;
  private final VTProxy remoteProxy;
  private final String bindHost;
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = 0;
    this.remoteDataTimeout = 0;
    this.remoteProxy = PROXY_NONE;
    this.bindHost = "";
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = 0;
    this.remoteProxy = PROXY_NONE;
    this.bindHost = "";
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteProxy = PROXY_NONE;
    this.bindHost = "";
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout, VTProxy proxy)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteProxy = proxy;
    this.bindHost = "";
  }
  
  public VTRemoteClientSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout, String bind, VTProxy proxy)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteProxy = proxy;
    this.bindHost = bind;
  }
  
  public Socket createSocket(String remoteHost, int remotePort) throws IOException
  {
    return remoteSocketFactory.connectSocket(bindHost, remoteHost, remotePort, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(InetAddress remoteHost, int remotePort) throws IOException
  {
    return remoteSocketFactory.connectSocket(bindHost, remoteHost.getHostAddress(), remotePort, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(String remoteHost, int remotePort, InetAddress bindHost, int bindPort) throws IOException
  {
    return remoteSocketFactory.connectSocket(bindHost.getHostAddress(), remoteHost, remotePort, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(InetAddress remoteHost, int remotePort, InetAddress bindHost, int bindPort) throws IOException
  {
    return remoteSocketFactory.connectSocket(bindHost.getHostAddress(), remoteHost.getHostAddress(), remotePort, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(String remoteHost, int remotePort, String bindHost, int bindPort) throws IOException
  {
    return remoteSocketFactory.connectSocket(bindHost, remoteHost, remotePort, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
  
  public Socket createSocket(String remoteHost, int remotePort, String bindHost) throws IOException
  {
    return remoteSocketFactory.connectSocket(bindHost, remoteHost, remotePort, remoteConnectTimeout, remoteDataTimeout, remoteProxy);
  }
}