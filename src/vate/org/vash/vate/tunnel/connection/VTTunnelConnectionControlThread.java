package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.socket.VTProxy;
import org.vash.vate.socket.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.session.VTTunnelCloseableSocket;
import org.vash.vate.tunnel.session.VTTunnelPipedSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSocksSessionHandler;

public class VTTunnelConnectionControlThread implements Runnable
{
  private final VTTunnelConnection connection;
  private final ExecutorService threads;
  private boolean closed = false;
  private static final String SESSION_SEPARATOR = "\f";
  private static final char SESSION_MARK = '\b';
  
  public VTTunnelConnectionControlThread(VTTunnelConnection connection, ExecutorService threads)
  {
    this.connection = connection;
    this.threads = threads;
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
              
              if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                final String host = parts[3];
                final int port = Integer.parseInt(parts[4]);
                String proxyTypeLetter = parts[5];
                String proxyHost = parts[6];
                int proxyPort = Integer.parseInt(parts[7]);
                String proxyUser = parts[8];
                String proxyPassword = parts[9];
                
                if (parts.length > 10 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[10].equals("*"))
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
                        remoteSocket = connect(host, port, proxy);
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
                          threads.execute(handler);
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
                  threads.execute(tcpConnectThread);
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
                String socksUsername = parts[3];
                String socksPassword = parts[4];
                String proxyTypeLetter = parts[5];
                String proxyHost = parts[6];
                int proxyPort = Integer.parseInt(parts[7]);
                String proxyUser = parts[8];
                String proxyPassword = parts[9];
                
                if (parts.length > 10 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[10].equals("*"))
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
                VTTunnelSocksSessionHandler handler = new VTTunnelSocksSessionHandler(session, connection.getResponseChannel(), socksUsername, socksPassword, proxy, null);
                
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
                  threads.execute(handler);
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
                      threads.execute(handler);
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
  
  public Socket connect(String host, int port, VTProxy proxy)
  {
    try
    {
      if (host.length() == 0 || host.equals("*"))
      {
        host = "";
      }
      else
      {
        
      }
      
      Socket socket = VTProxy.connect(host, port, null, proxy);
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}