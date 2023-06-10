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
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.server.session.VTServerSessionListener;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedServerSocket
{
  private VTServer vtserver;
  private BlockingQueue<VTManagedSocket> queue = new LinkedBlockingQueue<VTManagedSocket>();
  private volatile Thread interruptible;
  private VTManagedSocketListener socketListener;
  private Map<VTServerSession, VTManagedSocket> sessions = new LinkedHashMap<VTServerSession, VTManagedSocket>();
  
  private class VTCloseableServerConnection implements VTManagedConnection
  {
    private VTServerSession session;
    private VTServerConnection connection;
    
    private VTCloseableServerConnection(VTServerSession session)
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
      VTLinkableDynamicMultiplexedInputStream stream = connection.getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12 + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedOutputStream stream = connection.getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12 + number);
      return stream;
    }
    
    public VTClientSession getClientSession()
    {
      return null;
    }
    
    public VTServerSession getServerSession()
    {
      return session;
    }
  }
  
  private class VTManagedServerSocketServerSessionListener implements VTServerSessionListener
  {
    public void sessionStarted(VTServerSession session)
    {
      // System.out.println("server.session.started()");
      InputStream input = session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12);
      OutputStream output = session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED, 12);
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableServerConnection(session), input, output);
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
    
    public void sessionFinished(VTServerSession session)
    {
      // System.out.println("server.session.finished()");
      VTManagedSocket socket = sessions.remove(session);
      if (socketListener != null && socket != null)
      {
        socketListener.disconnected(socket);
      }
    }
  }
  
  public VTManagedServerSocket()
  {
    this.vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
  }
  
  public VTServer getServer()
  {
    return vtserver;
  }
  
  public void loadServerSettingsFile(String settingsFile) throws Exception
  {
    vtserver.loadServerSettingsFile(settingsFile);
  }
  
  public void loadServerSettings(Properties properties) throws Exception
  {
    vtserver.loadServerSettingsProperties(properties);
  }
  
  public void start()
  {
    vtserver.startThread();
  }
  
  public void stop()
  {
    vtserver.stop();
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
//    VTManagedServerSocket managed = new VTManagedServerSocket();
//    managed.start();
//    try
//    {
//      VTManagedSocket socket = managed.connect();
//      System.out.println("server.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      int i = 5;
//      while (socket.isConnected())
//      {
//        long time = System.currentTimeMillis();
//        writer.write("server.message:" + time + "\r\n");
//        writer.flush();
//        System.out.println(reader.readLine());
//        Thread.sleep(1000);
//        if (i > 0)
//        {
//          i--;
//        }
//        else
//        {
//          socket.close();
//        }
//      }
//    }
//    catch (Throwable t)
//    {
//      t.printStackTrace();
//    }
//    System.out.println("server.socket.disconnected()");
//  }
}
