package org.vash.vate.socket.factory;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.UnknownHostException;

import javax.net.SocketFactory;

import org.vash.vate.VT;

public class VTDirectSocketFactory extends SocketFactory
{
  public Socket createSocket(String host, int port) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port) throws IOException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(String host, int port, InetAddress bind, int local) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind, local));
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress host, int port, InetAddress bind, int local) throws IOException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(bind, local));
    socket.connect(new InetSocketAddress(host, port));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
    return socket;
  }
}
