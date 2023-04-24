package org.vash.vate.tunnel.connection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.channel.VTTunnelChannel;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSocksSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelVirtualSocket;

public class VTTunnelConnectionControlThread implements Runnable
{
  private VTTunnelConnection connection;
  private ExecutorService threads;
  private static final String SESSION_SEPARATOR = "\f\b";
  private static final char SESSION_MARK = 'C';
  
  public VTTunnelConnectionControlThread(VTTunnelConnection connection, ExecutorService threads)
  {
    this.connection = connection;
    this.threads = threads;
  }
  
  /*
   * public void setTunnelType(int tunnelType) { this.tunnelType = tunnelType; }
   */
  
  public void run()
  {
    while (true)
    {
      try
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
                try
                {
                  Socket socket = new Socket();
                  session = new VTTunnelSession(connection, socket, false);
                  if (host.length() == 0 || host.equals("*"))
                  {
                    socket.connect(new InetSocketAddress(port));
                  }
                  else
                  {
                    socket.connect(new InetSocketAddress(host, port));
                  }
                  socket.setTcpNoDelay(true);
                  socket.setSoLinger(true, 1);
                  VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, null);
                  VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(channelType, handler);
                  
                  if (output != null)
                  {
                    int outputNumber = output.number();
                    session.setOutputNumber(outputNumber);
                    session.setInputNumber(inputNumber);
                    session.setTunnelOutputStream(output);
                    session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                    session.getTunnelInputStream().setDirectOutputStream(session.getSocket().getOutputStream());
                    session.getTunnelInputStream().open();
                    session.getTunnelOutputStream().open();
                    // response message sent with ok
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                  }
                  else
                  {
                    // response message sent with error
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                    if (session != null)
                    {
                      session.close();
                    }
                  }
                }
                catch (Throwable e)
                {
                  //e.printStackTrace();
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "T" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  if (session != null)
                  {
                    session.close();
                  }
                }
              }
              // else if (connection.getTunnelType() ==
              // VTTunnelConnection.TUNNEL_TYPE_SOCKS)
              else if (tunnelType == VTTunnelChannel.TUNNEL_TYPE_SOCKS)
              {
                int channelType = Integer.parseInt(parts[0]);
                int inputNumber = Integer.parseInt(parts[1]);
                String socksUsername = parts[2];
                String socksPassword = parts[3];
                VTTunnelSession session = null;
                try
                {
                  VTTunnelVirtualSocket virtual = new VTTunnelVirtualSocket();
                  session = new VTTunnelSession(connection, virtual, false);
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
                    virtual.setOutputStream(output);
                    session.setTunnelOutputStream(output);
                    session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                    session.getTunnelInputStream().setDirectOutputStream(virtual.getInputStreamSource());
                    session.getTunnelInputStream().open();
                    session.getTunnelOutputStream().open();
                    // response message sent with ok
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                  }
                  else
                  {
                    // response message sent with error
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                    if (session != null)
                    {
                      session.close();
                    }
                  }
                }
                catch (Throwable e)
                {
                  //e.printStackTrace();
                  // response message sent with error
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + "S" + channelType + SESSION_SEPARATOR + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  if (session != null)
                  {
                    session.close();
                  }
                }
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
                VTTunnelSessionHandler handler = (VTTunnelSessionHandler) (connection.getOutputStream(channelType, outputNumber).getLink());
                if (handler != null)
                {
                  VTTunnelSession session = handler.getSession();
                  if (session != null)
                  {
                    if (session.isOriginator())
                    {
                      // response message received ok
                      session.setInputNumber(inputNumber);
                      session.setTunnelInputStream(connection.getInputStream(channelType, inputNumber, handler));
                      session.getTunnelInputStream().setDirectOutputStream(session.getSocket().getOutputStream());
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
                      //System.out.println("tunnel input:[" + inputNumber + "] output:[" + outputNumber + "]");
                      threads.execute(handler);
                    }
                    else
                    {
                      // ack message received
                      //System.out.println("tunnel input:[" + inputNumber + "] output:[" + outputNumber + "]");
                      threads.execute(handler);
                    }
                  }
                }
              }
              else
              {
                // response message received has error
                VTTunnelSessionHandler handler = (VTTunnelSessionHandler) (connection.getOutputStream(channelType, outputNumber).getLink());
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
      catch (Throwable e)
      {
        //e.printStackTrace();
        return;
      }
    }
  }
}