package org.vash.vate.socket.remote;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.ServerSocket;
import java.net.Socket;

import org.vash.vate.proxy.client.VTProxy;

public abstract class VTRemoteSocketFactory
{  
  public abstract Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy proxy) throws IOException;
  public abstract Socket connectSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException;
  public abstract Socket acceptSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException;
  public abstract ServerSocket bindSocket(String bind, String host, int port, int connectTimeout, int dataTimeout) throws IOException;
  public abstract void unbindSocket(String bind) throws IOException;
  public abstract DatagramSocket createSocket(String host, int port, int dataTimeout) throws IOException;
  //public abstract DatagramSocket createSocket(InetAddress address, int port, int dataTimeout) throws IOException;
}