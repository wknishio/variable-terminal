package org.vash.vate.socket;

import java.io.IOException;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public abstract class VTAuthenticatedProxySocketFactory extends SocketFactory
{  
  public abstract Socket createSocket(String host, int port, Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword) throws IOException, UnknownHostException;
  public abstract Socket createSocket(String host, int port, VTDefaultProxy proxy) throws IOException, UnknownHostException;
}