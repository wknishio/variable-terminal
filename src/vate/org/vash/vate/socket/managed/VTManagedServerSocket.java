package org.vash.vate.socket.managed;

import java.io.IOException;
import java.net.Socket;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.vash.vate.VT;
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
  private Thread interruptible;
  private VTManagedSocketListener socketListener;
  private Map<VTServerSession, VTManagedSocket> sessions = new LinkedHashMap<VTServerSession, VTManagedSocket>();
  
  private class VTCloseableServerConnection implements VTManagedConnection
  {
    private VTServerSession session;
    private VTServerConnection connection;
    private VTManagedSocketPingListener pingListener;
    
    private VTCloseableServerConnection(VTServerSession session)
    {
      this.session = session;
      this.connection = session.getConnection();
      try
      {
        connection.getConnectionSocket().setSoLinger(true, 0);
      }
      catch (Throwable t)
      {
        
      }
      this.pingListener = new VTManagedSocketPingListener(session.getExecutorService(), this);
      this.session.addPingListener(pingListener);
    }
    
    public void close() throws IOException
    {
      connection.closeSockets();
    }
    
    public VTLinkableDynamicMultiplexedInputStream getInputStream(int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedInputStream stream = connection.getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, connection.getAvailableInputChannel() + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedOutputStream stream = connection.getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, connection.getAvailableOutputChannel() + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedInputStream stream = connection.getMultiplexedConnectionInputStream().linkInputStream(type, connection.getAvailableInputChannel() + number);
      return stream;
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, int number)
    {
      if (number < 0)
      {
        number = 0;
      }
      VTLinkableDynamicMultiplexedOutputStream stream = connection.getMultiplexedConnectionOutputStream().linkOutputStream(type, connection.getAvailableOutputChannel() + number);
      return stream;
    }
    
    public Class<VTServerSession> getConnectionSessionClass()
    {
      return VTServerSession.class;
    }
    
    public <T> T getConnectionSession(Class<T> clazz)
    {
      return clazz.cast(session);
    }
    
    public Socket getConnectionSocket()
    {
      return connection.getConnectionSocket();
    }
    
    public void pingConnection()
    {
      session.ping();
    }
    
    public long ping()
    {
      return pingListener.ping();
    }
    
    public long ping(long timeoutNanoSeconds)
    {
      return pingListener.ping(timeoutNanoSeconds);
    }
    
    public long getOutputRateBytesPerSecond()
    {
      long bytesPerSecond = connection.getMultiplexedConnectionOutputStream().getBytesPerSecond();
      if (bytesPerSecond < Long.MAX_VALUE)
      {
        return bytesPerSecond;
      }
      else
      {
        return 0;
      }
    }
    
    public void setOutputRateBytesPerSecond(long bytesPerSecond)
    {
      if (bytesPerSecond > 0)
      {
        connection.getMultiplexedConnectionOutputStream().setBytesPerSecond(bytesPerSecond);
      }
      else
      {
        connection.getMultiplexedConnectionOutputStream().setBytesPerSecond(Long.MAX_VALUE);
      }
    }
  }
  
  private class VTManagedServerSocketServerSessionListener implements VTServerSessionListener
  {
    public void sessionStarted(VTServerSession session)
    {
      // System.out.println("server.session.started()");
      VTLinkableDynamicMultiplexedInputStream input = session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, session.getConnection().getAvailableInputChannel());
      VTLinkableDynamicMultiplexedOutputStream output = session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, session.getConnection().getAvailableOutputChannel());
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableServerConnection(session), input, output);
      session.addSessionCloseable(socket);
      sessions.put(session, socket);
      if (socketListener != null)
      {
        try
        {
          socketListener.connected(socket);
        }
        catch (Throwable t)
        {
          
        }
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
        try
        {
          socketListener.disconnected(socket);
        }
        catch (Throwable t)
        {
          
        }
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
  
  public void loadServerSettingsProperties(Properties properties) throws Exception
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
  
//  public void setDataTimeout(int timeout)
//  {
//    vtserver.setDataTimeout(timeout);
//  }
//  
//  public void setPingInterval(int interval)
//  {
//    vtserver.setPingInterval(interval);
//  }
  
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
//        System.out.println("server.ping():" + socket.getConnection().ping(500));
//        long time = System.currentTimeMillis();
//        writer.write("server.message:" + time + "\r\n");
//        writer.flush();
//        System.out.println("server.readLine():" + reader.readLine());
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
