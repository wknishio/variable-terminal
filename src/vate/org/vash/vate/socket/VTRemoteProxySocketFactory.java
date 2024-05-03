package org.vash.vate.socket;

import java.io.IOException;
import java.net.Socket;
import java.net.UnknownHostException;

public abstract class VTRemoteProxySocketFactory
{  
  public abstract Socket createSocket(String host, int port, Socket proxyConnection, VTProxy... proxies) throws IOException, UnknownHostException;
  public abstract Socket acceptSocket(String host, int port, int timeout) throws IOException, UnknownHostException;
}