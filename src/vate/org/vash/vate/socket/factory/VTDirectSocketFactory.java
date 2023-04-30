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
  public Socket createSocket(String arg0, int arg1) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(arg0, arg1));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress arg0, int arg1) throws IOException
  {
    Socket socket = new Socket();
    socket.connect(new InetSocketAddress(arg0, arg1));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(String arg0, int arg1, InetAddress arg2, int arg3) throws IOException, UnknownHostException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(arg2, arg3));
    socket.connect(new InetSocketAddress(arg0, arg1));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
  
  public Socket createSocket(InetAddress arg0, int arg1, InetAddress arg2, int arg3) throws IOException
  {
    Socket socket = new Socket();
    socket.bind(new InetSocketAddress(arg2, arg3));
    socket.connect(new InetSocketAddress(arg0, arg1));
    socket.setTcpNoDelay(true);
    socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
    return socket;
  }
}
