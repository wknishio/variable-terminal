package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.socket.VTProxy;
import org.vash.vate.socket.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.session.VTTunnelCloseableServerSocket;
import org.vash.vate.tunnel.session.VTTunnelCloseableSocket;
import org.vash.vate.tunnel.session.VTTunnelPipedSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSocksSessionHandler;

public class VTTunnelConnectionControlThread implements Runnable
{
  private final VTTunnelConnection connection;
  private final ExecutorService executor;
  private boolean closed = false;
  private static final String SESSION_SEPARATOR = "\f";
  private static final char SESSION_MARK = '\b';
  
  public VTTunnelConnectionControlThread(VTTunnelConnection connection, ExecutorService executor)
  {
    this.connection = connection;
    this.executor = executor;
  }
  
  public void run()
  {
    try
    {
      while (!closed)
      {
        final byte[] packet = connection.getControlInputStream().readData();
        if (packet[0] == 'U')
        {
          if (packet[1] == SESSION_MARK)
          {
            final int tunnelType = packet[2] == 'S' ? VTTunnelChannel.TUNNEL_TYPE_SOCKS : VTTunnelChannel.TUNNEL_TYPE_TCP;
            final String tunnelChar = tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS ? "S" : "T";
            String text = new String(packet, 3, packet.length - 3, "UTF-8");
            String[] parts = text.split(SESSION_SEPARATOR);
            if (parts.length >= 5)
            {
              // request message received
              final int channelType = Integer.parseInt(parts[0]);
              final int outputNumber = Integer.parseInt(parts[1]);
              final int inputNumber = Integer.parseInt(parts[2]);
              final int connectTimeout = Integer.parseInt(parts[3]);
              final int dataTimeout = Integer.parseInt(parts[4]);
              
              if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                final String host = parts[5];
                final int port = Integer.parseInt(parts[6]);
                String proxyTypeLetter = parts[7];
                String proxyHost = parts[8];
                int proxyPort = Integer.parseInt(parts[9]);
                String proxyUser = parts[10];
                String proxyPassword = parts[11];
                
                if (parts.length > 12 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[12].equals("*"))
                {
                  proxyUser = null;
                  proxyPassword = null;
                }
                
                VTProxyType proxyType = VTProxyType.GLOBAL;
                if (proxyTypeLetter.toUpperCase().startsWith("G"))
                {
                  proxyType = VTProxyType.GLOBAL;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("D"))
                {
                  proxyType = VTProxyType.DIRECT;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("H"))
                {
                  proxyType = VTProxyType.HTTP;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("S"))
                {
                  proxyType = VTProxyType.SOCKS;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("A"))
                {
                  proxyType = VTProxyType.ANY;
                }
                final VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                final boolean connect = proxyTypeLetter.toUpperCase().startsWith("W") ? false : true;
                
                final VTTunnelSession session = new VTTunnelSession(connection, false);
                final VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, connection.getResponseChannel());
                
                VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                VTLinkableDynamicMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                
                if (output != null && input != null)
                {
                  session.setTunnelOutputStream(output);
                  session.setTunnelInputStream(input);
                  
                  Runnable tcpConnectThread = new Runnable()
                  {
                    public void run()
                    {
                      Socket remoteSocket = null;
                      InputStream socketInputStream = null;
                      OutputStream socketOutputStream = null;
                      
                      try
                      {
                        if (connect)
                        {
                          remoteSocket = connect(host, port, connectTimeout, dataTimeout, proxy);
                        }
                        else
                        {
                          remoteSocket = accept(host, port, connectTimeout, dataTimeout);
                        }
                        socketInputStream = remoteSocket.getInputStream();
                        socketOutputStream = remoteSocket.getOutputStream();
                      }
                      catch (Throwable t)
                      {
                        
                      }
                      
                      try
                      {
                        if (socketInputStream != null && socketOutputStream != null)
                        {
                          session.setSocket(remoteSocket);
                          session.setSocketInputStream(socketInputStream);
                          session.setSocketOutputStream(socketOutputStream);
                          
                          session.getTunnelOutputStream().open();
                          //session.getTunnelInputStream().open();
                          session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream(), new VTTunnelCloseableSocket(session.getSocket()));
                          // response message sent with ok
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelChar + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                          connection.getControlOutputStream().flush();
                          executor.execute(handler);
                          session.setResult(true);
                        }
                        else
                        {
                          if (session != null)
                          {
                            session.close();
                          }
                          // response message sent with error
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelChar + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                          connection.getControlOutputStream().flush();
                        }
                      }
                      catch (Throwable t)
                      {
                        //t.printStackTrace();
                      }
                    }
                  };
                  executor.execute(tcpConnectThread);
                }
                else
                {
                  if (session != null)
                  {
                    session.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelChar + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
              {
                String socksUsername = parts[5];
                String socksPassword = parts[6];
                String proxyTypeLetter = parts[7];
                String proxyHost = parts[8];
                int proxyPort = Integer.parseInt(parts[9]);
                String proxyUser = parts[10];
                String proxyPassword = parts[11];
                
                if (parts.length > 12 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[12].equals("*"))
                {
                  proxyUser = null;
                  proxyPassword = null;
                }
                
                VTProxyType proxyType = VTProxyType.GLOBAL;
                if (proxyTypeLetter.toUpperCase().startsWith("G"))
                {
                  proxyType = VTProxyType.GLOBAL;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("D"))
                {
                  proxyType = VTProxyType.DIRECT;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("H"))
                {
                  proxyType = VTProxyType.HTTP;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("S"))
                {
                  proxyType = VTProxyType.SOCKS;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("A"))
                {
                  proxyType = VTProxyType.ANY;
                }
                VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                VTTunnelSession session = new VTTunnelSession(connection, false);
                VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(null);
                session.setSocket(pipedSocket);
                VTTunnelSocksSessionHandler handler = new VTTunnelSocksSessionHandler(session, connection.getResponseChannel(), socksUsername, socksPassword, proxy, null, connectTimeout);
                
                VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                VTLinkableDynamicMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                
                if (output != null && input != null)
                {
                  pipedSocket.setOutputStream(output);
                  session.setSocketInputStream(pipedSocket.getInputStream());
                  session.setSocketOutputStream(pipedSocket.getOutputStream());
                  
                  session.setTunnelOutputStream(output);
                  session.setTunnelInputStream(input);
                  session.getTunnelOutputStream().open();
                  session.getTunnelInputStream().setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelChar + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  executor.execute(handler);
                  session.setResult(true);
                }
                else
                {
                  if (session != null)
                  {
                    session.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelChar + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else
              {
                //closed = true;
              }
            }
            else if (parts.length == 3)
            {
              // response message received
              final int channelType = Integer.parseInt(parts[0]);
              final int outputNumber = Integer.parseInt(parts[1]);
              final int inputNumber = Integer.parseInt(parts[2]);
              
              if (inputNumber > -1)
              {
                VTTunnelSessionHandler handler = null;
                Object link = connection.getOutputStream(channelType, outputNumber).getLink();
                if (link instanceof VTTunnelSessionHandler)
                {
                  handler = (VTTunnelSessionHandler) link;
                }
                if (handler != null)
                {
                  VTTunnelSession session = handler.getSession();
                  if (session.isOriginator())
                  {
                    // response message received ok
                    Socket sessionSocket = session.getSocket();
                    if (!(sessionSocket instanceof VTTunnelPipedSocket))
                    {
                      executor.execute(handler);
                    }
                    session.setResult(true);
                  }
                  else
                  {
                    // response message received ok
                  }
                }
                else
                {
                  // handler not found
                }
              }
              else
              {
                // response message received has error
                VTTunnelSessionHandler handler = null;
                Object link = connection.getOutputStream(channelType, outputNumber).getLink();
                if (link instanceof VTTunnelSessionHandler)
                {
                  handler = (VTTunnelSessionHandler) link;
                }
                if (handler != null)
                {
                  VTTunnelSession session = handler.getSession();
                  if (session != null)
                  {
                    session.close();
                  }
                }
              }
            }
            else
            {
              // message with unsupported parts
            }
          }
          else
          {
            // session mark not found
          }
        }
        else
        {
          // unable to handle
        }
      }
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      //return;
    }
    closed = true;
  }
  
  public Socket connect(String host, int port, int connectTimeout, int dataTimeout, VTProxy proxy)
  {
    try
    {
      if (host == null || host.length() == 0 || host.equals("*"))
      {
        host = "";
      }
      else
      {
        
      }
      Socket socket = VTProxy.connect(host, port, connectTimeout, null, proxy);
      if (dataTimeout > 0)
      {
        socket.setSoTimeout(dataTimeout);
      }
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
  
  public Socket accept(String host, int port, int connectTimeout, int dataTimeout)
  {
    VTTunnelCloseableServerSocket serverSocket = null;
    try
    {
      if (host == null || host.length() == 0 || host.equals("*"))
      {
        host = "";
      }
      else
      {
        
      }
      serverSocket = new VTTunnelCloseableServerSocket(new ServerSocket());
      serverSocket.bind(new InetSocketAddress(host, port));
      if (connectTimeout > 0)
      {
        serverSocket.setSoTimeout(connectTimeout);
      }
      else
      {
        serverSocket.setSoTimeout(0);
      }
      connection.getCloseables().add(serverSocket);
      Socket socket = serverSocket.accept();
      if (dataTimeout > 0)
      {
        socket.setSoTimeout(dataTimeout);
      }
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    finally
    {
      if (serverSocket != null)
      {
        try
        {
          serverSocket.close();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        connection.getCloseables().remove(serverSocket);
      }
    }
    return null;
  }
}