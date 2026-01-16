package org.vash.vate.socket.remote;

import java.io.IOException;
import java.io.OutputStream;
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
  public abstract DatagramSocket createSocket(String bind, String host, int port, int dataTimeout) throws IOException;
  public abstract Socket pipeSocket(String bind, int type, boolean originator) throws IOException;
  public abstract Socket pipeSocket(String bind, int type, boolean originator, OutputStream out) throws IOException;
  //public abstract DatagramSocket createSocket(InetAddress address, int port, int dataTimeout) throws IOException;
}