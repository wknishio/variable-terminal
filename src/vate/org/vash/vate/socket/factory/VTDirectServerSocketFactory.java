package org.vash.vate.socket.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

public class VTDirectServerSocketFactory extends ServerSocketFactory
{
  public ServerSocket createServerSocket() throws IOException
  {
    ServerSocket socket = new ServerSocket();
    return socket;
  }
  
  public ServerSocket createServerSocket(int port) throws IOException
  {
    ServerSocket socket = new ServerSocket();
    socket.bind(new InetSocketAddress(port));
    return socket;
  }
  
  public ServerSocket createServerSocket(int port, int backlog) throws IOException
  {
    ServerSocket socket = new ServerSocket();
    socket.bind(new InetSocketAddress(port), backlog);
    return socket;
  }
  
  public ServerSocket createServerSocket(int port, int backlog, InetAddress bind) throws IOException
  {
    ServerSocket socket = new ServerSocket();
    socket.bind(new InetSocketAddress(bind.getHostName(), port), backlog);
    return socket;
  }
}
