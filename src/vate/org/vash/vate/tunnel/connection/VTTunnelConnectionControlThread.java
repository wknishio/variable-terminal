package org.vash.vate.tunnel.connection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vash.vate.tunnel.session.VTTunnelSession;
import org.vash.vate.tunnel.session.VTTunnelSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelSocksSessionHandler;
import org.vash.vate.tunnel.session.VTTunnelVirtualSocket;

public class VTTunnelConnectionControlThread implements Runnable
{
  private VTTunnelConnection connection;
  private ExecutorService threads;
  private static final String SESSION_SEPARATOR = "\f\b";
  private static final String SESSION_MARK = "SESS";

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
          String text = new String(packet, 1, packet.length - 1, "UTF-8");
          if (text.startsWith(SESSION_MARK))
          {
            text = text.substring(4);
            String[] parts = text.split(SESSION_SEPARATOR);
            if (parts.length >= 3)
            {
              // request message received
              if (connection.getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_TCP)
              {
                int inputNumber = Integer.parseInt(parts[0]);
                String host = parts[1];
                int port = Integer.parseInt(parts[2]);
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
                  VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, null);
                  VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(handler);
                  
                  if (output != null)
                  {
                    int outputNumber = output.number();
                    session.setOutputNumber(outputNumber);
                    session.setInputNumber(inputNumber);
                    session.setTunnelOutputStream(output);
                    session.setTunnelInputStream(connection.getInputStream(inputNumber, handler));
                    session.getTunnelInputStream().setDirectOutputStream(session.getSocket().getOutputStream());
                    session.getTunnelInputStream().open();
                    session.getTunnelOutputStream().open();
                    // response message sent with ok
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                  }
                  else
                  {
                    // response message sent with error
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
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
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  if (session != null)
                  {
                    session.close();
                  }
                }
              }
              else if (connection.getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_SOCKS)
              {
                int inputNumber = Integer.parseInt(parts[0]);
                String socksUsername = parts[1];
                String socksPassword = parts[2];
                VTTunnelSession session = null;
                try
                {
                  VTTunnelVirtualSocket virtual = new VTTunnelVirtualSocket();
                  session = new VTTunnelSession(connection, virtual, false);
                  VTTunnelSocksSessionHandler handler = null;
                  if (parts.length > 3 && socksUsername.equals("*") && socksPassword.equals("*") && parts[3].equals("*"))
                  {
                    handler = new VTTunnelSocksSessionHandler(session, null);
                  }
                  else
                  {
                    handler = new VTTunnelSocksSessionHandler(session, null, socksUsername, socksPassword);
                  }
                  VTLinkableDynamicMultiplexedOutputStream output = connection.getOutputStream(handler);
                  
                  if (output != null)
                  {
                    int outputNumber = output.number();
                    session.setOutputNumber(outputNumber);
                    session.setInputNumber(inputNumber);
                    virtual.setOutputStream(output);
                    session.setTunnelOutputStream(output);
                    session.setTunnelInputStream(connection.getInputStream(inputNumber, handler));
                    session.getTunnelInputStream().setDirectOutputStream(virtual.getInputStreamSource());
                    session.getTunnelInputStream().open();
                    session.getTunnelOutputStream().open();
                    // response message sent with ok
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                    connection.getControlOutputStream().flush();
                  }
                  else
                  {
                    // response message sent with error
                    connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
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
                  connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + "-1").getBytes("UTF-8"));
                  connection.getControlOutputStream().flush();
                  if (session != null)
                  {
                    session.close();
                  }
                }
              }
            }
            else if (parts.length == 2)
            {
              // response message received
              int outputNumber = Integer.parseInt(parts[0]);
              int inputNumber = Integer.parseInt(parts[1]);
              if (inputNumber > -1)
              {
                VTTunnelSessionHandler handler = (VTTunnelSessionHandler) (connection.getOutputStream(outputNumber).getLink());
                if (handler != null)
                {
                  VTTunnelSession session = handler.getSession();
                  if (session != null)
                  {
                    if (session.isOriginator())
                    {
                      // response message received ok
                      session.setInputNumber(inputNumber);
                      session.setTunnelInputStream(connection.getInputStream(inputNumber, handler));
                      session.getTunnelInputStream().setDirectOutputStream(session.getSocket().getOutputStream());
                      session.getTunnelInputStream().open();
                      session.getTunnelOutputStream().open();
                      // ack message sent
                      connection.getControlOutputStream().writeData(("U" + SESSION_MARK + inputNumber + SESSION_SEPARATOR + outputNumber).getBytes("UTF-8"));
                      connection.getControlOutputStream().flush();
                      threads.execute(handler);
                    }
                    else
                    {
                      // ack message received
                      threads.execute(handler);
                    }
                  }
                }
              }
              else
              {
                // response message received has error
                VTTunnelSessionHandler handler = (VTTunnelSessionHandler) (connection.getOutputStream(outputNumber).getLink());
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
            else if (parts.length == 1)
            {
              // unable to handle
            }
            else
            {
              // unable to handle
            }
          }
          else
          {
            
          }
        }
        else
        {
          
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