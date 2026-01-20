package org.vash.vate.socket.managed;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.vash.vate.VTSystem;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;

public class VTManagedClientSocket
{
  private final VTClient vtclient;
  private final BlockingQueue<VTManagedSocket> queue = new LinkedBlockingQueue<VTManagedSocket>();
  private final ConcurrentMap<VTClientSession, VTManagedSocket> sessions = new ConcurrentHashMap<VTClientSession, VTManagedSocket>();
  private Thread acceptThread;
  private VTManagedSocketListener socketListener;
  
  private class VTManagedClientConnection implements VTManagedConnection
  {
    private VTClientSession session;
    private VTClientConnection connection;
    private VTManagedSocketPingListener pingListener;
    
    private VTManagedClientConnection(VTClientSession session)
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
    
    public VTMultiplexingInputStream getMultiplexedConnectionInputStream()
    {
      return connection.getMultiplexedConnectionInputStream();
    }
    
    public VTMultiplexingOutputStream getMultiplexedConnectionOutputStream()
    {
      return connection.getMultiplexedConnectionOutputStream();
    }
    
    public VTRemotePipedSocketFactory getRemotePipedSocketFactory()
    {
      return session.getRemotePipedSocketFactory();
    }
  }
  
  private class VTManagedClientSocketClientSessionListener implements VTClientSessionListener
  {
    public void sessionStarted(VTClientSession session)
    {
      VTManagedSocket socket = new VTManagedSocket(new VTManagedClientConnection(session), session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, session.getConnection().getAvailableInputChannel()), session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, session.getConnection().getAvailableOutputChannel()));
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
  
  public VTManagedClientSocket(VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
  }
  
  public VTManagedClientSocket(String host, int port, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
  }
  
  public VTManagedClientSocket(String host, int port, String type, String key, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
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
  
  public VTManagedClientSocket(String host, int port, String type, String key, String user, String password, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
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
  
  public VTManagedClientSocket(boolean active, String host, int port, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
    vtclient.setActive(active);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.setAddress(host);
    vtclient.setPort(port);
  }
  
  public VTManagedClientSocket(boolean active, String host, int port, String type, String key, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
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
  
  public VTManagedClientSocket(boolean active, String host, int port, String type, String key, String user, String password, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
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
  
  public VTManagedClientSocket(Properties properties, VTProxy proxy)
  {
    vtclient = new VTClient(proxy, true);
    vtclient.setDaemon(true);
    vtclient.addSessionListener(new VTManagedClientSocketClientSessionListener());
    vtclient.setSessionShell("N");
    vtclient.loadClientSettingsProperties(properties);
  }
  
  public VTManagedClientSocket(String settingsFile, VTProxy proxy) throws IOException
  {
    vtclient = new VTClient(proxy, true);
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
  
  public VTManagedSocket connect() throws IOException
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
  
  public void setProxy(VTProxy proxy)
  {
    vtclient.setProxy(proxy);
  }
  
//  public static void main(String[] args)
//  {
//    VTManagedClientSocket managed = new VTManagedClientSocket(null);
//    managed.start();
//    try
//    {
//      VTManagedSocket socket = managed.connect();
//      System.out.println("client.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      VTRemotePipedSocketFactory remoteSocketFactory = socket.getConnection().getRemotePipedSocketFactory();
//      
//      int i = 5;
//      while (socket.isConnected())
//      {
//        System.out.println("client.ping():" + socket.getConnection().checkPing(500000000));
//        String clientUUID = java.util.UUID.randomUUID().toString();
//        System.out.println("client.socketLocal():" + clientUUID);
//        writer.write(clientUUID + "\r\n");
//        writer.flush();
//        String serverUUID = reader.readLine();
//        System.out.println("client.socketRemote():" + serverUUID);
//        Socket pipeClient = remoteSocketFactory.pipeSocket(clientUUID, 0, true);
//        Socket pipeServer = remoteSocketFactory.pipeSocket(serverUUID, 0, false);
//        System.out.println("client.writing()");
//        pipeClient.getOutputStream().write(1);
//        pipeServer.getOutputStream().write(2);
//        pipeClient.getOutputStream().flush();
//        pipeServer.getOutputStream().flush();
//        System.out.println("client.written()");
//        System.out.println("client.reading()");
//        System.out.println("client.readRemote():" + pipeServer.getInputStream().read());
//        System.out.println("client.readLocal():" + pipeClient.getInputStream().read());
//        pipeClient.close();
//        pipeServer.close();
//        System.out.println("client.readed()");
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
//    System.out.println("client.socket.disconnected()");
//  }
}