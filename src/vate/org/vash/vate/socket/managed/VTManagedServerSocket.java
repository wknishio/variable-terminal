package org.vash.vate.socket.managed;

import java.io.BufferedInputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.vash.vate.VT;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.server.session.VTServerSessionListener;
import org.vash.vate.stream.filter.VTBufferedOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedServerSocket
{
  private final VTServer vtserver;
  private final BlockingQueue<VTManagedSocket> queue = new LinkedBlockingQueue<VTManagedSocket>();
  private final ConcurrentMap<VTServerSession, VTManagedSocket> sessions = new ConcurrentHashMap<VTServerSession, VTManagedSocket>();
  private Thread acceptThread;
  private VTManagedSocketListener socketListener;
  
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
    
    public VTLinkableDynamicMultiplexedInputStream getInputStream(Object link)
    {
      return getInputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, link);
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(Object link)
    {
      return getOutputStream(VT.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, link);
    }
    
    public VTLinkableDynamicMultiplexedInputStream getInputStream(int type, Object link)
    {
      return connection.getMultiplexedConnectionInputStream().linkInputStream(type, link);
    }
    
    public VTLinkableDynamicMultiplexedOutputStream getOutputStream(int type, Object link)
    {
      return connection.getMultiplexedConnectionOutputStream().linkOutputStream(type, link);
    }
    
    public int setInputStreamOutputStream(Object link, OutputStream outputStream, Closeable closeable)
    {
      VTLinkableDynamicMultiplexedInputStream stream = getInputStream(link);
      stream.setOutputStream(outputStream, closeable);
      return stream.number();
    }
    
    public int setInputStreamOutputStream(int type, Object link, OutputStream outputStream, Closeable closeable)
    {
      VTLinkableDynamicMultiplexedInputStream stream = getInputStream(type, link);
      stream.setOutputStream(outputStream, closeable);
      return stream.number();
    }
    
    public Class<VTServerSession> getSessionClass()
    {
      return VTServerSession.class;
    }
    
    public <T> T getSession(Class<T> clazz)
    {
      if (clazz != null && clazz.isAssignableFrom(VTServerSession.class))
      {
        return clazz.cast(session);
      }
      return null;
    }
    
    public Socket getSocket()
    {
      return connection.getConnectionSocket();
    }
    
    public void requestPing()
    {
      session.ping();
    }
    
    public long checkPing()
    {
      return pingListener.checkPing();
    }
    
    public long checkPing(long timeoutNanoSeconds)
    {
      return pingListener.checkPing(timeoutNanoSeconds);
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
    
    public InputStream createBufferedInputStream(Object link)
    {
      return new BufferedInputStream(getInputStream(link), VT.VT_STANDARD_BUFFER_SIZE_BYTES);
    }
    
    public OutputStream createBufferedOutputStream(Object link)
    {
      return new VTBufferedOutputStream(getOutputStream(link), VT.VT_STANDARD_BUFFER_SIZE_BYTES, true);
    }
    
    public InputStream createBufferedInputStream(int type, Object link)
    {
      return new BufferedInputStream(getInputStream(type, link), VT.VT_STANDARD_BUFFER_SIZE_BYTES);
    }
    
    public OutputStream createBufferedOutputStream(int type, Object link)
    {
      return new VTBufferedOutputStream(getOutputStream(type, link), VT.VT_STANDARD_BUFFER_SIZE_BYTES, true);
    }
    
    public void releaseInputStream(VTLinkableDynamicMultiplexedInputStream stream)
    {
      connection.getMultiplexedConnectionInputStream().releaseInputStream(stream);
    }
    
    public void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
    {
      connection.getMultiplexedConnectionOutputStream().releaseOutputStream(stream);
    }
    
    public VTLinkableDynamicMultiplexingInputStream getMultiplexedConnectionInputStream()
    {
      return connection.getMultiplexedConnectionInputStream();
    }
    
    public VTLinkableDynamicMultiplexingOutputStream getMultiplexedConnectionOutputStream()
    {
      return connection.getMultiplexedConnectionOutputStream();
    }
    
    public int getInputStreamIndexStart()
    {
      return connection.getAvailableInputChannel();
    }
    
    public int getOutputStreamIndexStart()
    {
      return connection.getAvailableOutputChannel();
    }
  }
  
  private class VTManagedServerSocketServerSessionListener implements VTServerSessionListener
  {
    public void sessionStarted(VTServerSession session)
    {
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableServerConnection(session));
      //session.addSessionCloseable(socket);
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
    vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
  }
  
  public VTManagedServerSocket(String host, int port)
  {
    vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
  }
  
  public VTManagedServerSocket(String host, int port, String type, String key)
  {
    vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
    vtserver.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtserver.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedServerSocket(String host, int port, String type, String key, String user, String password)
  {
    vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
    vtserver.setUniqueUserCredential(user, password);
    vtserver.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtserver.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedServerSocket(boolean passive, String host, int port)
  {
    vtserver = new VTServer();
    vtserver.setPassive(passive);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
  }
  
  public VTManagedServerSocket(boolean passive, String host, int port, String type, String key)
  {
    vtserver = new VTServer();
    vtserver.setPassive(passive);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
    vtserver.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtserver.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedServerSocket(boolean passive, String host, int port, String type, String key, String user, String password)
  {
    vtserver = new VTServer();
    vtserver.setPassive(passive);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
    vtserver.setUniqueUserCredential(user, password);
    vtserver.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtserver.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedServerSocket(Properties properties)
  {
    vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.loadServerSettingsProperties(properties);
  }
  
  public VTManagedServerSocket(String settingsFile) throws IOException
  {
    vtserver = new VTServer();
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.loadServerSettingsFile(settingsFile);
  }
  
  public VTServer getServer()
  {
    return vtserver;
  }
  
  public void loadServerSettingsFile(String settingsFile) throws IOException
  {
    vtserver.loadServerSettingsFile(settingsFile);
  }
  
  public void loadServerSettingsProperties(Properties properties)
  {
    vtserver.loadServerSettingsProperties(properties);
  }
  
  public int getPingInterval()
  {
    return vtserver.getPingIntervalMilliseconds();
  }
  
  public void setPingInterval(int interval)
  {
    vtserver.setPingInterval(interval);
  }
  
  public int getPingLimit()
  {
    return vtserver.getPingLimitMilliseconds();
  }
  
  public void setPingLimit(int limit)
  {
    vtserver.setPingLimit(limit);
  }
  
  public void start()
  {
    vtserver.startThread();
  }
  
  public void stop()
  {
    vtserver.stop();
    interrupt();
  }
  
  public VTManagedSocket connect() throws IOException
  {
    if (!vtserver.isRunning())
    {
      start();
    }
    acceptThread = Thread.currentThread();
    VTManagedSocket socket = null;
    try 
    {
      socket = queue.take();
    }
    catch (InterruptedException e)
    { 
      throw new IOException(e.getMessage());
    }
    acceptThread = null;
    return socket;
  }
  
  public void interrupt()
  {
    try
    {
      if (acceptThread != null)
      {
        acceptThread.interrupt();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close()
  {
    stop();
    try
    {
      for (VTManagedSocket socket : sessions.values())
      {
        try
        {
          socket.close();
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void setManagedSocketListener(VTManagedSocketListener socketListener)
  {
    this.socketListener = socketListener;
    if (!vtserver.isRunning())
    {
      start();
    }
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
//      VTManagedSocket socket = managed.accept();
//      System.out.println("server.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      int i = 5;
//      while (socket.isConnected())
//      {
//        System.out.println("server.ping():" + socket.getConnection().checkPing(500000000));
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