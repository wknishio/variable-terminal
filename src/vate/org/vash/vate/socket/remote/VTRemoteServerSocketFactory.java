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
  private final String remoteBind;
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = 0;
    this.remoteDataTimeout = 0;
    this.remoteBind = "";
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = 0;
    this.remoteBind = "";
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteBind = "";
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout, String bind)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.remoteBind = bind;
  }
  
  public ServerSocket createServerSocket() throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), remoteBind, 0, remoteConnectTimeout, remoteDataTimeout);
  }

  public ServerSocket createServerSocket(int port) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), remoteBind, port, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int port, int backlog) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), remoteBind, port, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int port, int backlog, InetAddress bind) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bind.getHostAddress(), port, remoteConnectTimeout, remoteDataTimeout);
  }
}