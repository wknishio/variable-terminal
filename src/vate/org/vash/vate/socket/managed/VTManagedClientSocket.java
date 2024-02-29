package org.vash.vate.socket.managed;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedClientSocket
{
  private VTClient vtclient;
  private BlockingQueue<VTManagedSocket> queue = new LinkedBlockingQueue<VTManagedSocket>();
  private Thread interruptible;
  private VTManagedSocketListener socketListener;
  private Map<VTClientSession, VTManagedSocket> sessions = new LinkedHashMap<VTClientSession, VTManagedSocket>();
  
  private class VTCloseableClientConnection implements VTManagedConnection
  {
    private VTClientSession session;
    private VTClientConnection connection;
    
    private VTCloseableClientConnection(VTClientSession session)
    {
      this.session = session;
      this.connection = session.getConnection();
    }
    
    public void close() throws IOException
    {
      connection.closeSockets();
    }
    
    public boolean isConnected()
    {
      return connection.isConnected();
    }
    
    public boolean isClosed()
    {
      return !connection.isConnected();
    }
    
    public boolean isBound()
    {
      return connection.isConnected();
    }
    
    public VTLinkableDynamicMultiplexedInputStream getInputStream(int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedInputStream stream = connection.getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 12 + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedOutputStream stream = connection.getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 12 + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedInputStream getInputStream(int number, int type)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedInputStream stream = connection.getMultiplexedConnectionInputStream().linkInputStream(type, 12 + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number, int type)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedOutputStream stream = connection.getMultiplexedConnectionOutputStream().linkOutputStream(type, 12 + number);
      return stream;
    }
    
    public VTClientSession getClientSession()
    {
      return session;
    }
    
    public VTServerSession getServerSession()
    {
      return null;
    }
  }
  
  private class VTManagedClientSocketClientSessionListener implements VTClientSessionListener
  {
    public void sessionStarted(VTClientSession session)
    {
      // System.out.println("client.session.started()");
      InputStream input = session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 12);
      OutputStream output = session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, 12);
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableClientConnection(session), input, output);
      session.addSessionCloseable(this.getClass().getSimpleName(), socket);
      sessions.put(session, socket);
      if (socketListener != null)
      {
        socketListener.connected(socket);
      }
      else
      {
        queue.offer(socket);
      }
    }
    
    public void sessionFinished(VTClientSession session)
    {
      VTManagedSocket socket = sessions.remove(session);
      if (socketListener != null && socket != null)
      {
        socketListener.disconnected(socket);
      }
      // System.out.println("client.session.finished()");
    }
  }
  
  public VTManagedClientSocket()
  {
    this.vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
  }
  
  public VTClient getClient()
  {
    return vtclient;
  }
  
  public void loadClientSettingsFile(String settingsFile) throws Exception
  {
    vtclient.loadClientSettingsFile(settingsFile);
  }
  
  public void loadClientSettingsProperties(Properties properties) throws Exception
  {
    vtclient.loadClientSettingsProperties(properties);
  }
  
  public void start()
  {
    vtclient.startThread();
  }
  
  public void stop()
  {
    vtclient.stop();
  }
  
  public VTManagedSocket connect() throws InterruptedException
  {
    interruptible = Thread.currentThread();
    VTManagedSocket socket = queue.take();
    interruptible = null;
    return socket;
  }
  
  public void interrupt()
  {
    try
    {
      if (interruptible != null)
      {
        interruptible.interrupt();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close()
  {
    interrupt();
    stop();
  }
  
  public void setManagedSocketListener(VTManagedSocketListener socketListener)
  {
    this.socketListener = socketListener;
  }
  
//  public static void main(String[] args)
//  {
//    VTManagedClientSocket managed = new VTManagedClientSocket();
//    managed.start();
//    try
//    {
//      VTManagedSocket socket = managed.connect();
//      System.out.println("client.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      
//      while (socket.isConnected())
//      {
//        long time = System.currentTimeMillis();
//        writer.write("client.message:" + time + "\r\n");
//        writer.flush();
//        System.out.println(reader.readLine());
//        Thread.sleep(1000);
//      }
//    }
//    catch (Throwable t)
//    {
//      t.printStackTrace();
//    }
//    System.out.println("client.socket.disconnected()");
//  }
}
