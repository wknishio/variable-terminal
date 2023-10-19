package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

public abstract class VTAuthenticatedProxySocketFactory extends SocketFactory
{  
  public abstract Socket createSocket(String host, int port, Socket proxyConnection, VTProxy... proxies) throws IOException, UnknownHostException;
}