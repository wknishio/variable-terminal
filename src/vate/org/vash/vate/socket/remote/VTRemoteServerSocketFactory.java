package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.util.UUID;

import javax.net.ServerSocketFactory;

public class VTRemoteServerSocketFactory extends ServerSocketFactory
{
  private final VTRemoteSocketFactory remoteSocketFactory;
  private final int remoteConnectTimeout;
  private final int remoteDataTimeout;
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = 0;
    this.remoteDataTimeout = 0;
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = 0;
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
  }
  
  public ServerSocket createServerSocket() throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), "", 0, remoteConnectTimeout, remoteDataTimeout);
  }

  public ServerSocket createServerSocket(int port) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), "", port, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int port, int backlog) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), "", port, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int port, int backlog, InetAddress bind) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bind.getHostAddress(), port, remoteConnectTimeout, remoteDataTimeout);
  }
}