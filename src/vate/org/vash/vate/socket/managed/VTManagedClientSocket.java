package org.vash.vate.socket.managed;

import java.io.BufferedInputStream;
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
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.stream.filter.VTBufferedOutputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vash.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTManagedClientSocket
{
  private final VTClient vtclient;
  private final BlockingQueue<VTManagedSocket> queue = new LinkedBlockingQueue<VTManagedSocket>();
  private final ConcurrentMap<VTClientSession, VTManagedSocket> sessions = new ConcurrentHashMap<VTClientSession, VTManagedSocket>();
  private Thread acceptThread;
  private VTManagedSocketListener socketListener;
  
  private class VTCloseableClientConnection implements VTManagedConnection
  {
    private VTClientSession session;
    private VTClientConnection connection;
    private VTManagedSocketPingListener pingListener;
    
    private VTCloseableClientConnection(VTClientSession session)
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
    
    public Class<VTClientSession> getSessionClass()
    {
      return VTClientSession.class;
    }
    
    public <T> T getSession(Class<T> clazz)
    {
      if (clazz != null && clazz.isAssignableFrom(VTClientSession.class))
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
    
    public InputStream createBufferedInputStream(int number)
    {
      return new BufferedInputStream(getInputStream(number), VT.VT_STANDARD_BUFFER_SIZE_BYTES);
    }
    
    public OutputStream createBufferedOutputStream(int number)
    {
      return new VTBufferedOutputStream(getOutputStream(number), VT.VT_STANDARD_BUFFER_SIZE_BYTES, true);
    }
    
    public InputStream createBufferedInputStream(int type, int number)
    {
      return new BufferedInputStream(getInputStream(type & (VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY), number), VT.VT_STANDARD_BUFFER_SIZE_BYTES);
    }
    
    public OutputStream createBufferedOutputStream(int type, int number)
    {
      return new VTBufferedOutputStream(getOutputStream(type & (VT.VT_MULTIPLEXED_CHANNEL_TYPE_RATE_UNLIMITED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_ENABLED | VT.VT_MULTIPLEXED_CHANNEL_TYPE_COMPRESSION_MODE_HEAVY), number), VT.VT_STANDARD_BUFFER_SIZE_BYTES, true);
    }
  }
  
  private class VTManagedClientSocketClientSessionListener implements VTClientSessionListener
  {
    public void sessionStarted(VTClientSession session)
    {
      VTManagedSocket socket = new VTManagedSocket(new VTCloseableClientConnection(session));
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
    
    public void sessionFinished(VTClientSession session)
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
  
  public VTManagedClientSocket()
  {
    vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
  }
  
  public VTManagedClientSocket(String host, int port)
  {
    vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
  }
  
  public VTManagedClientSocket(String host, int port, String type, String key)
  {
    vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
    vtclient.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtclient.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedClientSocket(String host, int port, String type, String key, String user, String password)
  {
    vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
    vtclient.setUser(user);
    vtclient.setPassword(password);
    vtclient.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtclient.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedClientSocket(boolean active, String host, int port)
  {
    vtclient = new VTClient();
    vtclient.setActive(active);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
  }
  
  public VTManagedClientSocket(boolean active, String host, int port, String type, String key)
  {
    vtclient = new VTClient();
    vtclient.setActive(active);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
    vtclient.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtclient.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedClientSocket(boolean active, String host, int port, String type, String key, String user, String password)
  {
    vtclient = new VTClient();
    vtclient.setActive(active);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
    vtclient.setUser(user);
    vtclient.setPassword(password);
    vtclient.setEncryptionType(type);
    try
    {
      if (key != null)
      {
        vtclient.setEncryptionKey(key.getBytes("UTF-8"));
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public VTManagedClientSocket(Properties properties)
  {
    vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.loadClientSettingsProperties(properties);
  }
  
  public VTManagedClientSocket(String settingsFile) throws IOException
  {
    vtclient = new VTClient();
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.loadClientSettingsFile(settingsFile);
  }
  
  public VTClient getClient()
  {
    return vtclient;
  }
  
  public void loadClientSettingsFile(String settingsFile) throws IOException
  {
    vtclient.loadClientSettingsFile(settingsFile);
  }
  
  public void loadClientSettingsProperties(Properties properties)
  {
    vtclient.loadClientSettingsProperties(properties);
  }
  
  public int getPingInterval()
  {
    return vtclient.getPingIntervalMilliseconds();
  }
  
  public void setPingInterval(int interval)
  {
    vtclient.setPingInterval(interval);
  }
  
  public int getPingLimit()
  {
    return vtclient.getPingLimitMilliseconds();
  }
  
  public void setPingLimit(int limit)
  {
    vtclient.setPingLimit(limit);
  }
  
  public void start()
  {
    vtclient.startThread();
  }
  
  public void stop()
  {
    vtclient.stop();
    interrupt();
  }
  
  public VTManagedSocket accept() throws IOException
  {
    if (!vtclient.isRunning())
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
    if (!vtclient.isRunning())
    {
      start();
    }
  }
  
//  public void setDataTimeout(int timeout)
//  {
//    vtclient.setDataTimeout(timeout);
//  }
//  
//  public void setPingInterval(int interval)
//  {
//    vtclient.setPingInterval(interval);
//  }
  
//  public static void main(String[] args)
//  {
//    VTManagedClientSocket managed = new VTManagedClientSocket();
//    managed.start();
//    try
//    {
//      VTManagedSocket socket = managed.accept();
//      System.out.println("client.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      
//      while (socket.isConnected())
//      {
//        System.out.println("client.ping():" + socket.getConnection().checkPing(500));
//        long time = System.currentTimeMillis();
//        writer.write("client.message:" + time + "\r\n");
//        writer.flush();
//        System.out.println("client.readLine():" + reader.readLine());
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