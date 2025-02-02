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
  private final String bindHost;
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = 0;
    this.remoteDataTimeout = 0;
    this.bindHost = "";
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = 0;
    this.bindHost = "";
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.bindHost = "";
  }
  
  public VTRemoteServerSocketFactory(VTRemoteSocketFactory factory, int connectTimeout, int dataTimeout, String bind)
  {
    this.remoteSocketFactory = factory;
    this.remoteConnectTimeout = connectTimeout;
    this.remoteDataTimeout = dataTimeout;
    this.bindHost = bind;
  }
  
  public ServerSocket createServerSocket() throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bindHost, 0, remoteConnectTimeout, remoteDataTimeout);
  }

  public ServerSocket createServerSocket(int bindPort) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bindHost, bindPort, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int bindPort, int backlog) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bindHost, bindPort, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int bindPort, int backlog, InetAddress bindHost) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bindHost.getHostAddress(), bindPort, remoteConnectTimeout, remoteDataTimeout);
  }
  
  public ServerSocket createServerSocket(int bindPort, int backlog, String bindHost) throws IOException
  {
    return remoteSocketFactory.bindSocket(UUID.randomUUID().toString(), bindHost, bindPort, remoteConnectTimeout, remoteDataTimeout);
  }
}