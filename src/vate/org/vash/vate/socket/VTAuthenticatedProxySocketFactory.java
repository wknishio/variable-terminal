package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public abstract class VTAuthenticatedProxySocketFactory extends SocketFactory
{  
  public abstract Socket createSocket(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port) throws IOException, UnknownHostException;
  
  public abstract Socket createSocket(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port) throws IOException;
  
  public abstract Socket createSocket(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException;
  
  public abstract Socket createSocket(Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword, InetAddress host, int port, InetAddress bind, int local) throws IOException;
}