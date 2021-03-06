package org.vate.tunnel.connection;

import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;
import org.vate.tunnel.session.VTTunnelSession;
import org.vate.tunnel.session.VTTunnelSessionHandler;
import org.vate.tunnel.session.VTTunnelSocksSessionHandler;
import org.vate.tunnel.session.VTTunnelVirtualSocket;

public class VTTunnelConnectionControlThread implements Runnable
{
  private VTTunnelConnection connection;
  private ExecutorService threads;
  // private int tunnelType = VTTunnelConnection.TUNNEL_TYPE_TCP;

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
        String line = connection.getControlReader().readLine();
        // System.out.println(line);
        String[] data = line.split(";:;");
        if (data.length >= 3)
        {
          // first message received
          if (connection.getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_TCP)
          {
            int number = Integer.parseInt(data[0]);
            String host = data[1];
            int port = Integer.parseInt(data[2]);
            VTTunnelSession session = new VTTunnelSession(connection, connection.getInputStream(number));
            VTTunnelSessionHandler handler = new VTTunnelSessionHandler(session, null);
            try
            {
              Socket socket = new Socket();
              // socket.setReuseAddress(true);
              // System.out.println("host:" + host);
              //socket.setReceiveBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE - 1);
              //socket.setSendBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE - 1);
              if (host.length() == 0 || host.equals("*"))
              {
                socket.connect(new InetSocketAddress(port));
              }
              else
              {
                socket.connect(new InetSocketAddress(host, port));
              }
              socket.setTcpNoDelay(true);
              socket.setKeepAlive(true);
              //socket.setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
              // socket.setSoLinger(true, 0);
              session.setSocket(socket);
              VTLinkableDynamicMultiplexedOutputStream stream = connection.getOutputStream(number, handler);
              if (stream != null)
              {
                session.setTunnelOutputStream(stream);
                session.getTunnelOutputStream().open();
                session.getTunnelInputStream().setDirectOutputStream(session.getSocketOutputStream());
                // second message sent with ok
                connection.getControlWriter().write(number + ";:;" + session.getTunnelOutputStream().number() + "\n");
                connection.getControlWriter().flush();
                threads.execute(handler);
              }
              else
              {
                session.close();
                // second message sent with error
                connection.getControlWriter().write(number + ";:;-1\n");
                connection.getControlWriter().flush();
              }
            }
            catch (Throwable e)
            {
              session.close();
              // second message sent with error
              connection.getControlWriter().write(number + ";:;-1\n");
              connection.getControlWriter().flush();
            }
          }
          else if (connection.getTunnelType() == VTTunnelConnection.TUNNEL_TYPE_SOCKS)
          {
            int number = Integer.parseInt(data[0]);
            String socksUsername = data[1];
            String socksPassword = data[2];
            VTTunnelSession session = new VTTunnelSession(connection, connection.getInputStream(number));
            VTTunnelSocksSessionHandler handler = null;
            if (data.length == 4)
            {
              handler = new VTTunnelSocksSessionHandler(session, null);
            }
            else
            {
              handler = new VTTunnelSocksSessionHandler(session, null, socksUsername, socksPassword);
            }
            try
            {
              VTLinkableDynamicMultiplexedOutputStream stream = connection.getOutputStream(number, handler);
              VTTunnelVirtualSocket socket = new VTTunnelVirtualSocket(stream);
              session.setSocket(socket);
              if (stream != null)
              {
                session.setTunnelOutputStream(stream);
                session.getTunnelOutputStream().open();
                session.getTunnelInputStream().setDirectOutputStream(socket.getInputStreamSource());
                // second message sent with ok
                connection.getControlWriter().write(number + ";:;" + session.getTunnelOutputStream().number() + "\n");
                connection.getControlWriter().flush();
                threads.execute(handler);
              }
              else
              {
                session.close();
                // second message sent with error
                connection.getControlWriter().write(number + ";:;-1\n");
                connection.getControlWriter().flush();
              }
            }
            catch (Throwable e)
            {
              session.close();
              // second message sent with error
              connection.getControlWriter().write(number + ";:;-1\n");
              connection.getControlWriter().flush();
            }
          }
        }
        else if (data.length == 2)
        {
          // second or third message received
          int number = Integer.parseInt(data[0]);
          int result = Integer.parseInt(data[1]);
          VTTunnelSessionHandler handler = (VTTunnelSessionHandler) (connection.getOutputStream(number).getLink());
          VTTunnelSession session = handler.getSession();
          if (result > -1)
          {
            if (session.isOriginator())
            {
              // second message ok
              // session.setTunnelInputStream(connection.getInputStream(number));
              // session.getTunnelInputStream().setOutputStream(session.getSocketOutputStream());
              // third message sent
              // connection.getControlWriter().write(channel + ";:;" + result + "\n");
              // connection.getControlWriter().flush();
              threads.execute(handler);
            }
            else
            {
              // third message received
              // threads.execute(handler);
              // Thread handlerThread = new Thread(handler,
              // handler.getClass().getSimpleName());
              // handlerThread.start();
            }
          }
          else
          {
            // second message has error
            session.close();
          }
        }
        else if (data.length == 1)
        {
          // fourth message received
          // int output = Integer.parseInt(data[0]);
          // VTTunnelSessionHandler handler = (VTTunnelSessionHandler)
          // (connection.getOutputStream(output).getLink());
          // VTTunnelSession session = handler.getSession();
          // fourth message received
          // threads.execute(handler);
          // Thread handlerThread = new Thread(handler,
          // handler.getClass().getSimpleName());
          // handlerThread.start();
        }
        else
        {
          // unable to handle
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        return;
      }
    }
  }
}