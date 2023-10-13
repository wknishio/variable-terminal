package org.vash.vate.socket;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;

import javax.net.ServerSocketFactory;

public class VTServerSocketFactory extends ServerSocketFactory
{
  public ServerSocket createServerSocket() throws IOException
  {
    ServerSocket serverSocket = new ServerSocket();
    return new VTServerSocket(serverSocket);
  }
  
  public ServerSocket createServerSocket(int port) throws IOException
  {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress(port));
    return new VTServerSocket(serverSocket);
  }
  
  public ServerSocket createServerSocket(int port, int backlog) throws IOException
  {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress(port), backlog);
    return new VTServerSocket(serverSocket);
  }
  
  public ServerSocket createServerSocket(int port, int backlog, InetAddress bind) throws IOException
  {
    ServerSocket serverSocket = new ServerSocket();
    serverSocket.bind(new InetSocketAddress(bind.getHostName(), port), backlog);
    return new VTServerSocket(serverSocket);
  }
}
