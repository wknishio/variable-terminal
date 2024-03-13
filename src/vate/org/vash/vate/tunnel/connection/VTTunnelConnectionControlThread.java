package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.socket.VTProxy;
import org.vash.vate.socket.VTProxy.VTProxyType;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.session.VTTunnelCloseableSocket;
import org.vash.vate.tunnel.session.VTTunnelPipedSocket;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSocksSessionHandler;

public class VTTunnelConnectionControlThread implements Runnable
{
  private VTTunnelConnection connection;
  private ExecutorService threads;
  private static final String SESSION_SEPARATOR = "\f\b";
  private static final char SESSION_MARK = 'C';
  private boolean closed = false;
  
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
            int tunnelType = packet[2] == 'S' ? VTTunnelChannel.TUNNEL_TYPE_SOCKS : VTTunnelChannel.TUNNEL_TYPE_TCP;
            String text = new String(packet, 3, packet.length - 3, "UTF-8");
            String[] parts = text.split(SESSION_SEPARATOR);
            if (parts.length >= 4)
            {
              // request message received
              if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                final int channelType = Integer.parseInt(parts[0]);
                final int inputNumber = Integer.parseInt(parts[1]);
                final String host = parts[2];
                final int port = Integer.parseInt(parts[3]);
                String proxyTypeLetter = parts[4];
                String proxyHost = parts[5];
                int proxyPort = Integer.parseInt(parts[6]);
                String proxyUser = parts[7];
                String proxyPassword = parts[8];
                
                if (parts.length > 9 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[9].equals("*"))
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
                //System.out.println("received.request:input=" + inputNumber);
                
                final VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                final VTTunnelSession session = new VTTunnelSession(connection, false);
                
                final VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, null);
                
                VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, handler);
                
                if (output != null)
                {
                  final int outputNumber = output.number();
                  session.setOutputNumber(outputNumber);
                  session.setInputNumber(inputNumber);
                  session.setTunnelOutputStream(output);
                  session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                  
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
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + packet[2] + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                          connection.getControlOutputStream().flush();
                          //System.out.println("sent.response:input=" + inputNumber + " output=" + outputNumber);
                        }
                        else
                        {
                          if (session != null)
                          {
                            session.close();
                          }
                          // response message sent with error
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + packet[2] + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
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
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + packet[2] + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
              {
                //int additional = 0;
                int channelType = Integer.parseInt(parts[0]);
                int inputNumber = Integer.parseInt(parts[1]);
                String socksUsername = parts[2];
                String socksPassword = parts[3];
                String proxyTypeLetter = parts[4];
                String proxyHost = parts[5];
                int proxyPort = Integer.parseInt(parts[6]);
                String proxyUser = parts[7];
                String proxyPassword = parts[8];
                
                if (parts.length > 9 && proxyUser.equals("*") && proxyPassword.equals("*") && parts[9].equals("*"))
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
                
                VTTunnelSocksSessionHandler handler = new VTTunnelSocksSessionHandler(session, null, socksUsername, socksPassword, proxy, null);
                VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, handler);
                
                if (output != null)
                {
                  pipedSocket.setOutputStream(output);
                  session.setSocketInputStream(pipedSocket.getInputStream());
                  session.setSocketOutputStream(pipedSocket.getOutputStream());
                  int outputNumber = output.number();
                  session.setOutputNumber(outputNumber);
                  session.setInputNumber(inputNumber);
                  session.setTunnelOutputStream(output);
                  session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                  session.getTunnelOutputStream().open();
                  //session.getTunnelInputStream().open();
                  session.getTunnelInputStream().setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + packet[2] + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                }
                else
                {
                  if (session != null)
                  {
                    session.close();
                  }
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + packet[2] + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
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
              int channelType = Integer.parseInt(parts[0]);
              int outputNumber = Integer.parseInt(parts[1]);
              int inputNumber = Integer.parseInt(parts[2]);
              //System.out.println("received.response:input=" + inputNumber + " output=" + outputNumber);
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
                    session.setInputNumber(inputNumber);
                    session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                    session.getTunnelOutputStream().open();
                    //session.getTunnelInputStream().open();
                    Socket sessionSocket = session.getSocket();
                    VTTunnelPipedSocket pipedSocket = null;
                    if (sessionSocket instanceof VTTunnelPipedSocket)
                    {
                      pipedSocket = (VTTunnelPipedSocket) sessionSocket;
                      session.getTunnelInputStream().setOutputStream(pipedSocket.getInputStreamSource(), pipedSocket);
                    }
                    else
                    {
                      session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream(), new VTTunnelCloseableSocket(sessionSocket));
                    }
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + packet[2] + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                    //System.out.println("sent.response:input=" + inputNumber + " output=" + outputNumber);
                    if (pipedSocket == null)
                    {
                      threads.execute(handler);
                    }
                    session.setResult(true);
                  }
                  else
                  {
                    // response message received ok
                    threads.execute(handler);
                    session.setResult(true);
                  }
                }
                else
                {
                  
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
            else if (parts.length == 2)
            {
              
            }
          }
          else
          {
            // unable to handle
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