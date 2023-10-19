package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Proxy;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.socket.VTProxy;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
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
  private volatile boolean closed = false;
  
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
        byte[] packet = connection.getControlInputStream().readData();
        if (packet[0] == 'U')
        {
          if (packet[1] == SESSION_MARK)
          {
            int tunnelType = packet[2] == 'S' ? VTTunnelChannel.TUNNEL_TYPE_SOCKS : VTTunnelChannel.TUNNEL_TYPE_TCP;
            String text = new String(packet, 3, packet.length - 3, "UTF-8");
            final String[] parts = text.split(SESSION_SEPARATOR);
            if (parts.length >= 4)
            {
              // request message received
              if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                //attempt tcp connect in another thread
                Runnable tcpConnect = new Runnable()
                {
                  public void run()
                  {
                    try
                    {
                      int channelType = Integer.parseInt(parts[0]);
                      int inputNumber = Integer.parseInt(parts[1]);
                      String host = parts[2];
                      int port = Integer.parseInt(parts[3]);
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
                      
                      Proxy.Type proxyType = Proxy.Type.DIRECT;
                      if (proxyTypeLetter.toUpperCase().startsWith("H"))
                      {
                        proxyType = Proxy.Type.HTTP;
                      }
                      else if (proxyTypeLetter.toUpperCase().startsWith("S"))
                      {
                        proxyType = Proxy.Type.SOCKS;
                      }
                      else if (proxyTypeLetter.toUpperCase().startsWith("A"))
                      {
                        proxyType = null;
                      }
                      
                      VTTunnelSession session = null;
                      VTTunnelSessionHandler handler = null;
                      Socket socket = null;
                      InputStream socketInputStream = null;
                      OutputStream socketOutputStream = null;
                      
                      try
                      {
                        socket = connect(host, port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                        socketInputStream = socket.getInputStream();
                        socketOutputStream = socket.getOutputStream();
                      }
                      catch (Throwable t)
                      {
                        //t.printStackTrace();
                      }
                      if (socketInputStream != null && socketOutputStream != null)
                      {
                        session = new VTTunnelSession(connection, socket, socketInputStream, socketOutputStream, false);
                        handler = new VTTunnelSessionHandler(session, null);
                        VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, handler);
                        if (output != null)
                        {
                          int outputNumber = output.number();
                          session.setOutputNumber(outputNumber);
                          session.setInputNumber(inputNumber);
                          session.setTunnelOutputStream(output);
                          session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                          session.getTunnelOutputStream().open();
                          session.getTunnelInputStream().open();
                          session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream(), session.getSocket());
                          // response message sent with ok
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
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
                          connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                          connection.getControlOutputStream().flush();
                        }
                      }
                      else
                      {
                        // response message sent with error
                        connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                        connection.getControlOutputStream().flush();
                      }
                    }
                    catch (Throwable t)
                    {
                      
                    }
                  }
                };
                threads.execute(tcpConnect);
              }
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
              {
                int additional = 0;
                int channelType = Integer.parseInt(parts[0]);
                int inputNumber = Integer.parseInt(parts[1]);
                String socksUsername = parts[2];
                String socksPassword = parts[3];
                if (parts.length > 4 && socksUsername.equals("*") && socksPassword.equals("*") && parts[4].equals("*"))
                {
                  socksUsername = null;
                  socksPassword = null;
                  additional = 1;
                }
                String proxyTypeLetter = parts[4 + additional];
                String proxyHost = parts[5 + additional];
                int proxyPort = Integer.parseInt(parts[6 + additional]);
                String proxyUser = parts[7 + additional];
                String proxyPassword = parts[8 + additional];
                
                if (parts.length > (9 + additional) && proxyUser.equals("*") && proxyPassword.equals("*") && parts[9 + additional].equals("*"))
                {
                  proxyUser = null;
                  proxyPassword = null;
                }
                
                Proxy.Type proxyType = Proxy.Type.DIRECT;
                if (proxyTypeLetter.toUpperCase().startsWith("H"))
                {
                  proxyType = Proxy.Type.HTTP;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("S"))
                {
                  proxyType = Proxy.Type.SOCKS;
                }
                else if (proxyTypeLetter.toUpperCase().startsWith("A"))
                {
                  proxyType = null;
                }
                VTProxy proxy = new VTProxy(proxyType, proxyHost, proxyPort, proxyUser, proxyPassword);
                
                VTTunnelSession session = null;
                VTTunnelPipedSocket piped = new VTTunnelPipedSocket();
                session = new VTTunnelSession(connection, piped, piped.getInputStream(), piped.getOutputStream(), false);
                VTTunnelSocksSessionHandler handler = new VTTunnelSocksSessionHandler(session, null, socksUsername, socksPassword, proxy);
                VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, handler);
                
                if (output != null)
                {
                  piped.setOutputStream(output);
                  int outputNumber = output.number();
                  session.setOutputNumber(outputNumber);
                  session.setInputNumber(inputNumber);
                  session.setTunnelOutputStream(output);
                  session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                  session.getTunnelOutputStream().open();
                  session.getTunnelInputStream().open();
                  session.getTunnelInputStream().setOutputStream(piped.getInputStreamSource(), piped);
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
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
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
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
                    session.getTunnelInputStream().open();
                    Socket socket = session.getSocket();
                    if (socket instanceof VTTunnelPipedSocket)
                    {
                      VTTunnelPipedSocket piped = (VTTunnelPipedSocket) socket;
                      session.getTunnelInputStream().setOutputStream(piped.getInputStreamSource(), piped);
                    }
                    else
                    {
                      session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream(), session.getSocket());
                    }
                    threads.execute(handler);
                    session.setResult(true);
                  }
                  else
                  {
                    
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
  
  public Socket connect(String host, int port, Proxy.Type proxyType, String proxyHost, int proxyPort, String proxyUser, String proxyPassword)
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
      
      Socket socket = VTProxy.connect(host, port, proxyType, proxyHost, proxyPort, proxyUser, proxyPassword, null);
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}