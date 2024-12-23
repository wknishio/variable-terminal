package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.Socket;

import org.vash.vate.socket.proxy.VTProxy;

public abstract class VTRemoteSocketFactory
{  
  public abstract Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy... proxies) throws IOException;
  public abstract Socket acceptSocket(String host, int port, int connectTimeout, int dataTimeout) throws IOException;
}