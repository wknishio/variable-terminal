package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.vash.vate.VTSystem;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.proxy.client.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream.VTMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream.VTMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.session.VTTunnelCloseableServerSocket;
import org.vash.vate.tunnel.session.VTTunnelCloseableSocket;
import org.vash.vate.tunnel.session.VTTunnelDatagramSocket;
import org.vash.vate.tunnel.session.VTTunnelFTPSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelPipedSocket;
import org.vash.vate.tunnel.session.VTTunnelRunnableSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSocksSessionHandler;

public class VTTunnelConnectionControlThread implements Runnable
{
  private final VTTunnelConnection connection;
  private volatile boolean closed = false;
  private static final String SESSION_SEPARATOR = "\f";
  private static final char SESSION_MARK = '\b';
  private final byte[] packet = new byte[VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES];
  private final Map<String, VTTunnelCloseableServerSocket> serverSockets = new ConcurrentHashMap<String, VTTunnelCloseableServerSocket>();
  
  public VTTunnelConnectionControlThread(VTTunnelConnection connection)
  {
    this.connection = connection;
  }
  
  public void run()
  {
    try
    {
      int length = 0;
      while (!closed)
      {
        length = connection.getControlInputStream().readData(packet);
        if (packet[0] == 'U')
        {
          if (packet[1] == SESSION_MARK)
          {
            //final char tunnelChar = (char) packet[2];
            final char tunnelType = (char) packet[2];
            
            String text = new String(packet, 3, length - 3, "UTF-8");
            String[] parts = text.split(SESSION_SEPARATOR);
            if (parts.length >= 8)
            {
              // request message received
              final int channelType = Integer.parseInt(parts[0]);
              final int outputNumber = Integer.parseInt(parts[1]);
              final int inputNumber = Integer.parseInt(parts[2]);
              final int connectTimeout = Integer.parseInt(parts[3]);
              final int dataTimeout = Integer.parseInt(parts[4]);
              
              if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                final String bind = parts[5];
                final String host = parts[6];
                final int port = Integer.parseInt(parts[7]);
                String proxyTypeLetter = parts[8];
                String proxyHost = parts[9];
                int proxyPort = Integer.parseInt(parts[10]);
                String proxyUser = parts[11];
                String proxyPassword = parts[12];
                
                if (parts.length > 13 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[13].equals("*"))
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
                else if (proxyTypeLetter.toUpperCase().startsWith("P"))
                {
                  proxyType = VTProxyType.PLUS;
                }
                final VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                final boolean unbound = proxyTypeLetter.toUpperCase().startsWith("U") ? true : false;
                final boolean bound = proxyTypeLetter.toUpperCase().startsWith("B") ? true : false;
                final boolean accept = proxyTypeLetter.toUpperCase().startsWith("A") ? true : false;
                
                if (unbound)
                {
                  unbind(bind);
                  continue;
                }
                
                final VTTunnelSession session = new VTTunnelSession(connection, false);
                final VTTunnelSessionHandler handler = new VTTunnelSessionHandler(connection.getResponseChannel(channelType), session);
                
                VTMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                VTMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                
                if (output != null && input != null)
                {
                  session.setTunnelInputStream(input);
                  session.setTunnelOutputStream(output);
                  handler.open();
                  
                  Runnable tcpTunnelThread = new Runnable()
                  {
                    public void run()
                    {
                      Socket remoteSocket = null;
                      InputStream socketInputStream = null;
                      OutputStream socketOutputStream = null;
                      
                      try
                      {
                        if (bound)
                        {
                          if (handler != null)
                          {
                            handler.close();
                          }
                          ServerSocket serverSocket = bind(bind, host, port);
                          if (serverSocket != null)
                          {
                            int localPort = serverSocket.getLocalPort();
                            //InetAddress localAddress = serverSocket.getInetAddress();
                            
//                            if (localAddress.getHostAddress().equals("0.0.0.0") || localAddress.getHostAddress().equals("::")
//                            || localAddress.getHostAddress().equals("::0") || localAddress.getHostAddress().equals("0:0:0:0:0:0:0:0")
//                            || localAddress.getHostAddress().equals("00:00:00:00:00:00:00:00")
//                            || localAddress.getHostAddress().equals("0000:0000:0000:0000:0000:0000:0000:0000"))
//                            {
//                              try
//                              {
//                                InetAddress localHost = InetAddress.getLocalHost();
//                                localAddress = localHost;
//                              }
//                              catch (Throwable t)
//                              {
//                                
//                              }
//                            }
                            // response message sent with ok
                            connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + host + SESSION_SEPARATOR + localPort).getBytes("UTF-8"));
                            connection.getControlOutputStream().flush();
                          }
                          else
                          {
                            connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                            connection.getControlOutputStream().flush();
                          }
                          return;
                        }
                        
                        if (accept)
                        {
                          remoteSocket = accept(bind, host, port, connectTimeout, dataTimeout);
                        }
                        else
                        {
                          remoteSocket = connect(bind, host, port, connectTimeout, dataTimeout, proxy);
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
                          
                          session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream(), new VTTunnelCloseableSocket(session.getSocket()));
                          session.getTunnelOutputStream().open();
                          // response message sent with ok
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + remoteSocket.getInetAddress().getHostAddress() + SESSION_SEPARATOR + remoteSocket.getPort()).getBytes("UTF-8"));
                          connection.getControlOutputStream().flush();
                          connection.getExecutorService().execute(handler);
                          session.setResult(true);
                        }
                        else
                        {
                          if (handler != null)
                          {
                            handler.close();
                          }
                          // response message sent with error
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                          connection.getControlOutputStream().flush();
                        }
                      }
                      catch (Throwable t)
                      {
                        //t.printStackTrace();
                      }
                    }
                  };
                  connection.getExecutorService().execute(tcpTunnelThread);
                }
                else
                {
                  if (handler != null)
                  {
                    handler.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
              {
                String bind = parts[5];
                String username = parts[6];
                String password = parts[7];
                String proxyTypeLetter = parts[8];
                String proxyHost = parts[9];
                int proxyPort = Integer.parseInt(parts[10]);
                String proxyUser = parts[11];
                String proxyPassword = parts[12];
                
                if (parts.length > 13 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[13].equals("*"))
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
                else if (proxyTypeLetter.toUpperCase().startsWith("P"))
                {
                  proxyType = VTProxyType.PLUS;
                }
                VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                VTTunnelSession session = new VTTunnelSession(connection, false);
                VTTunnelSocksSessionHandler handler = new VTTunnelSocksSessionHandler(connection.getResponseChannel(channelType), session, username, password, bind, connectTimeout, dataTimeout, proxy);
                
                VTMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                VTMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                
                VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(null);
                session.setSocket(pipedSocket);
                
                if (output != null && input != null)
                {
                  pipedSocket.setOutputStream(output);
                  session.setSocketInputStream(pipedSocket.getInputStream());
                  session.setSocketOutputStream(pipedSocket.getOutputStream());
                  
                  input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                  output.open();
                  
                  session.setTunnelInputStream(input);
                  session.setTunnelOutputStream(output);
                  handler.open();
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  connection.getExecutorService().execute(handler);
                  session.setResult(true);
                }
                else
                {
                  if (handler != null)
                  {
                    handler.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_UDP)
              {
                //final String bind = parts[5];
                final String host = parts[6];
                final int port = Integer.parseInt(parts[7]);
                
                VTTunnelSession session = new VTTunnelSession(connection, false);
                VTTunnelDatagramSocket datagramSocket = null;
                
                VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(null);
                session.setSocket(pipedSocket);
                
                try
                {
                  if (host != null && host.length() > 0)
                  {
                    datagramSocket = new VTTunnelDatagramSocket(pipedSocket, connection.getExecutorService(), host, port);
                    datagramSocket.setSoTimeout(dataTimeout);
                  }
                  else
                  {
                    datagramSocket = new VTTunnelDatagramSocket(pipedSocket, connection.getExecutorService(), port);
                    datagramSocket.setSoTimeout(dataTimeout);
                  }
                }
                catch (Throwable t)
                {
                  
                }
                
                VTTunnelRunnableSessionHandler handler = new VTTunnelRunnableSessionHandler(connection.getResponseChannel(channelType), session, datagramSocket);
                
                VTMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                VTMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                  
                if (output != null && input != null)
                {
                  pipedSocket.setOutputStream(output);
                  session.setSocketInputStream(pipedSocket.getInputStream());
                  session.setSocketOutputStream(pipedSocket.getOutputStream());
                  
                  input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                  output.open();
                  
                  session.setTunnelInputStream(input);
                  session.setTunnelOutputStream(output);
                  handler.open();
                  
                  if (datagramSocket != null)
                  {
                    datagramSocket.setTunnelInputStream(pipedSocket.getInputStream());
                    datagramSocket.setTunnelOutputStream(pipedSocket.getOutputStream());
                    
                    int localPort = datagramSocket.getLocalPort();
                    InetAddress localAddress = datagramSocket.getLocalAddress();
                    String hostAddress = localAddress.getHostAddress();
                    
                    if (hostAddress.equals("0.0.0.0") || hostAddress.equals("::")
                    || hostAddress.equals("::0") || hostAddress.equals("0:0:0:0:0:0:0:0")
                    || hostAddress.equals("00:00:00:00:00:00:00:00")
                    || hostAddress.equals("0000:0000:0000:0000:0000:0000:0000:0000"))
                    {
                      try
                      {
                        InetAddress localHost = InetAddress.getLocalHost();
                        localAddress = localHost;
                      }
                      catch (Throwable t)
                      {
                        
                      }
                    }
                    // response message sent with ok
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber + SESSION_SEPARATOR + localAddress.getHostAddress() + SESSION_SEPARATOR + localPort).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                    connection.getExecutorService().execute(handler);
                    session.setResult(true);
                  }
                  else
                  {
                    if (handler != null)
                    {
                      handler.close();
                    }
                    // response message sent with error
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                  }
                }
                else
                {
                  if (handler != null)
                  {
                    handler.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_FTP)
              {
                String bind = parts[5];
                String username = parts[6];
                String password = parts[7];
                String proxyTypeLetter = parts[8];
                String proxyHost = parts[9];
                int proxyPort = Integer.parseInt(parts[10]);
                String proxyUser = parts[11];
                String proxyPassword = parts[12];
                
                if (parts.length > 13 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[13].equals("*"))
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
                else if (proxyTypeLetter.toUpperCase().startsWith("P"))
                {
                  proxyType = VTProxyType.PLUS;
                }
                VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                VTTunnelSession session = new VTTunnelSession(connection, false);
                VTTunnelFTPSessionHandler handler = new VTTunnelFTPSessionHandler(connection.getResponseChannel(channelType), session, username, password, bind, connectTimeout, dataTimeout, proxy);
                
                VTMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                VTMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                
                VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(null);
                session.setSocket(pipedSocket);
                
                if (output != null && input != null)
                {
                  pipedSocket.setOutputStream(output);
                  session.setSocketInputStream(pipedSocket.getInputStream());
                  session.setSocketOutputStream(pipedSocket.getOutputStream());
                  
                  input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                  output.open();
                  
                  session.setTunnelInputStream(input);
                  session.setTunnelOutputStream(output);
                  handler.open();
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  connection.getExecutorService().execute(handler);
                  session.setResult(true);
                }
                else
                {
                  if (handler != null)
                  {
                    handler.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_PIPE)
              {
                String bind = parts[5];
                
                final VTTunnelSession session = new VTTunnelSession(connection, false);
                final VTTunnelSessionHandler handler = new VTTunnelSessionHandler(connection.getPipedChannel(), session, bind);
                
                VTMultiplexedInputStream input = connection.getInputStream(channelType, inputNumber, handler);
                VTMultiplexedOutputStream output = connection.getOutputStream(channelType, outputNumber, handler);
                
                VTTunnelPipedSocket pipedSocket = new VTTunnelPipedSocket(session, input, output);
                session.setSocket(pipedSocket);
                
                if (output != null && input != null)
                {
                  //pipedSocket.setOutputStream(output);
                  session.setSocketInputStream(pipedSocket.getInputStream());
                  session.setSocketOutputStream(pipedSocket.getOutputStream());
                  
                  input.close();
                  //input.setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                  //output.open();
                  
                  session.setTunnelInputStream(input);
                  session.setTunnelOutputStream(output);
                  handler.open();
                  
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  session.setResult(true);
                }
                else
                {
                  if (handler != null)
                  {
                    handler.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + tunnelType + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else
              {
                //closed = true;
              }
            }
            else if (parts.length >= 3)
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
                  if (parts.length >= 5)
                  {
                    session.setRemoteHost(parts[3]);
                    session.setRemotePort(Integer.parseInt(parts[4]));
                  }
                  if (session.isOriginator())
                  {
                    // response message received ok
                    Socket sessionSocket = session.getSocket();
                    if (!(sessionSocket instanceof VTTunnelPipedSocket))
                    {
                      connection.getExecutorService().execute(handler);
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
                  handler.close();
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
  
  private Socket connect(String bind, String host, int port, int connectTimeout, int dataTimeout, VTProxy proxy)
  {
    Socket connectionSocket = null;
    try
    {
      if (host == null || host.length() == 0 || host.equals("*"))
      {
        host = "";
      }
      if (bind == null || bind.length() == 0 || bind.equals("*"))
      {
        bind = "";
      }
      
      connectionSocket = VTProxy.connect(bind, host, port, connectTimeout, connectionSocket, proxy);
      if (dataTimeout > 0)
      {
        connectionSocket.setSoTimeout(dataTimeout);
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
      if (connectionSocket != null)
      {
        try
        {
          connectionSocket.close();
        }
        catch (Throwable e)
        {
          
        }
      }
      connectionSocket = null;
    }
    return connectionSocket;
  }
  
  private Socket accept(String bind, String host, int port, int connectTimeout, int dataTimeout)
  {
    VTTunnelCloseableServerSocket serverSocket = null;
    Socket connectionSocket = null;
    boolean bound = false;
    try
    {
      if (host == null || host.length() == 0 || host.equals("*"))
      {
        host = "";
      }
      else
      {
        
      }
      
      if (bind != null && bind.length() > 0)
      {
        serverSocket = serverSockets.get(bind);
      }
      
      if (serverSocket == null)
      {
        serverSocket = new VTTunnelCloseableServerSocket(new ServerSocket());
        serverSocket.bind(new InetSocketAddress(host, port));
        connection.getCloseables().add(serverSocket);
      }
      else
      {
        bound = true;
      }
      
      if (connectTimeout > 0)
      {
        serverSocket.setSoTimeout(connectTimeout);
      }
      else
      {
        serverSocket.setSoTimeout(0);
      }
      
      connectionSocket = serverSocket.accept();
      if (dataTimeout > 0)
      {
        connectionSocket.setSoTimeout(dataTimeout);
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
      if (connectionSocket != null)
      {
        try
        {
          connectionSocket.close();
        }
        catch (Throwable e)
        {
          
        }
      }
    }
    finally
    {
      if (!bound && serverSocket != null)
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
    return connectionSocket;
  }
  
  private VTTunnelCloseableServerSocket bind(String bind, String host, int port)
  {
    try
    {
      VTTunnelCloseableServerSocket serverSocket = null;
      serverSocket = new VTTunnelCloseableServerSocket(new ServerSocket());
      serverSocket.bind(new InetSocketAddress(host, port));
      connection.getCloseables().add(serverSocket);
      serverSockets.put(bind, serverSocket);
      return serverSocket;
    }
    catch (Throwable t)
    {
      
    }
    return null;
  }
  
  private boolean unbind(String bind)
  {
    try
    {
      VTTunnelCloseableServerSocket serverSocket = null;
      serverSocket = serverSockets.remove(bind);
      if (serverSocket != null)
      {
        connection.getCloseables().remove(serverSocket);
        serverSocket.close();
      }
      return true;
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
}