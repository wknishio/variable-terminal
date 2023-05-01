package org.vash.vate.tunnel.connection;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VT;
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
            String[] parts = text.split(SESSION_SEPARATOR);
            if (parts.length >= 4)
            {
              // request message received
              if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_TCP)
              {
                int channelType = Integer.parseInt(parts[0]);
                int inputNumber = Integer.parseInt(parts[1]);
                String host = parts[2];
                int port = Integer.parseInt(parts[3]);
                VTTunnelSession session = null;
                VTTunnelSessionHandler handler = null;
                Socket socket = null;
                InputStream socketInputStream = null;
                OutputStream socketOutputStream = null;
                try
                {
                  socket = connect(host, port);
                  socketInputStream = socket.getInputStream();
                  socketOutputStream = socket.getOutputStream();
                }
                catch (Throwable t)
                {
                  
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
                    //session.getTunnelInputStream().addPropagated(session);
                    session.getTunnelInputStream().setDirectOutputStream(session.getSocketOutputStream(), session.getSocket());
                    session.getTunnelInputStream().open();
                    session.getTunnelOutputStream().open();
                    // response message sent with ok
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
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
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
              {
                int channelType = Integer.parseInt(parts[0]);
                int inputNumber = Integer.parseInt(parts[1]);
                String socksUsername = parts[2];
                String socksPassword = parts[3];
                VTTunnelSession session = null;
                
                VTTunnelPipedSocket piped = new VTTunnelPipedSocket();
                session = new VTTunnelSession(connection, piped, piped.getInputStream(), piped.getOutputStream(), false);
                VTTunnelSocksSessionHandler handler = null;
                if (parts.length > 4 && socksUsername.equals("*") && socksPassword.equals("*") && parts[4].equals("*"))
                {
                  handler = new VTTunnelSocksSessionHandler(session, null);
                }
                else
                {
                  handler = new VTTunnelSocksSessionHandler(session, null, socksUsername, socksPassword);
                }
                VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, handler);
                
                if (output != null)
                {
                  int outputNumber = output.number();
                  session.setOutputNumber(outputNumber);
                  session.setInputNumber(inputNumber);
                  session.setTunnelOutputStream(output);
                  session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                  piped.setOutputStream(output);
                  session.getTunnelInputStream().setDirectOutputStream(piped.getInputStreamSource(), piped);
                  session.getTunnelInputStream().open();
                  session.getTunnelOutputStream().open();
                  // response message sent with ok
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
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
                    //session.getTunnelInputStream().addPropagated(session);
                    Socket socket = session.getSocket();
                    if (socket instanceof VTTunnelPipedSocket)
                    {
                      VTTunnelPipedSocket piped = (VTTunnelPipedSocket) socket;
                      session.getTunnelInputStream().setDirectOutputStream(piped.getInputStreamSource(), piped);
                    }
                    else
                    {
                      session.getTunnelInputStream().setDirectOutputStream(session.getSocketOutputStream(), session.getSocket());
                    }
                    session.getTunnelInputStream().open();
                    session.getTunnelOutputStream().open();
                    // ack message sent~
                    if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
                    {
                      connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                      connection.getControlOutputStream().flush();
                    }
                    else
                    {
                      connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                      connection.getControlOutputStream().flush();
                    }
                    threads.execute(handler);
                    session.setResult(true);
                  }
                  else
                  {
                    // ack message received
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
  
  public Socket connect(String host, int port)
  {
    try
    {
      
      if (host.length() == 0 || host.equals("*"))
      {
        host = "";
      }
      else
      {
        //socket.connect(InetSocketAddress.createUnresolved(host, port));
      }
      Socket socket = new Socket();
      socket.connect(new InetSocketAddress(host, port));
      socket.setTcpNoDelay(true);
      //socket.setSoLinger(true, 5);
      socket.setSoTimeout(VT.VT_TIMEOUT_NETWORK_CONNECTION_MILLISECONDS);
      return socket;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return null;
  }
}