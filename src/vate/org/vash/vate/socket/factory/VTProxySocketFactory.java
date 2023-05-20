package org.vash.vate.socket.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.Proxy;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public abstract class VTProxySocketFactory extends SocketFactory
{
  public abstract Socket createSocket(Proxy proxy);
  
  public abstract Socket createSocket(Proxy proxy, String host, int port) throws IOException, UnknownHostException;
  
  public abstract Socket createSocket(Proxy proxy, InetAddress host, int port) throws IOException;
  
  public abstract Socket createSocket(Proxy proxy, String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException;
  
  public abstract Socket createSocket(Proxy proxy, InetAddress host, int port, InetAddress bind, int local) throws IOException;
}