package org.vash.vate.socket.managed;

import java.io.IOException;
import java.net.Socket;
import java.util.Properties;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.LinkedBlockingQueue;

import org.vash.vate.VTSystem;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.server.session.VTServerSessionListener;
import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;
import org.vash.vate.stream.multiplex.VTMultiplexingInputStream;
import org.vash.vate.stream.multiplex.VTMultiplexingOutputStream;

public class VTManagedServerSocket
{
  private final VTServer vtserver;
  private final BlockingQueue<VTManagedSocket> queue = new LinkedBlockingQueue<VTManagedSocket>();
  private final ConcurrentMap<VTServerSession, VTManagedSocket> sessions = new ConcurrentHashMap<VTServerSession, VTManagedSocket>();
  private Thread acceptThread;
  private VTManagedSocketListener socketListener;
  
  private class VTManagedServerConnection implements VTManagedConnection
  {
    private VTServerSession session;
    private VTServerConnection connection;
    private VTManagedSocketPingListener pingListener;
    
    private VTManagedServerConnection(VTServerSession session)
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
  
  private class VTManagedServerSocketServerSessionListener implements VTServerSessionListener
  {
    public void sessionStarted(VTServerSession session)
    {
      VTManagedSocket socket = new VTManagedSocket(new VTManagedServerConnection(session), session.getConnection().getMultiplexedConnectionInputStream().linkInputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, session.getConnection().getAvailableInputChannel()), session.getConnection().getMultiplexedConnectionOutputStream().linkOutputStream(VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_BUFFERED, session.getConnection().getAvailableOutputChannel()));
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
  
  public VTManagedServerSocket(VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
  }
  
  public VTManagedServerSocket(String host, int port, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
  }
  
  public VTManagedServerSocket(String host, int port, String type, String key, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
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
  
  public VTManagedServerSocket(String host, int port, String type, String key, String user, String password, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
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
  
  public VTManagedServerSocket(boolean passive, String host, int port, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
    vtserver.setPassive(passive);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.setAddress(host);
    vtserver.setPort(port);
  }
  
  public VTManagedServerSocket(boolean passive, String host, int port, String type, String key, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
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
  
  public VTManagedServerSocket(boolean passive, String host, int port, String type, String key, String user, String password, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
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
  
  public VTManagedServerSocket(Properties properties, VTProxy proxy)
  {
    vtserver = new VTServer(proxy, true);
    vtserver.setDaemon(true);
    vtserver.addSessionListener(new VTManagedServerSocketServerSessionListener());
    vtserver.setSessionShell("N");
    vtserver.loadServerSettingsProperties(properties);
  }
  
  public VTManagedServerSocket(String settingsFile, VTProxy proxy) throws IOException
  {
    vtserver = new VTServer(proxy, true);
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
  
  public void setProxy(VTProxy proxy)
  {
    vtserver.setProxy(proxy);
  }
  
//  public static void main(String[] args)
//  {
//    VTManagedServerSocket managed = new VTManagedServerSocket(null);
//    managed.start();
//    try
//    {
//      VTManagedSocket socket = managed.connect();
//      System.out.println("server.socket.connected()");
//      java.io.BufferedWriter writer = new java.io.BufferedWriter(new java.io.OutputStreamWriter(socket.getOutputStream()));
//      java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.InputStreamReader(socket.getInputStream()));
//      VTRemoteSocketFactory remoteSocketFactory = socket.getConnection().getRemotePipedSocketFactory();
//      
//      int i = 5;
//      while (socket.isConnected())
//      {
//        System.out.println("server.ping():" + socket.getConnection().checkPing(500000000));
//        String serverUUID = java.util.UUID.randomUUID().toString();
//        System.out.println("server.socketLocal():" + serverUUID);
//        writer.write(serverUUID + "\r\n");
//        writer.flush();
//        String clientUUID = reader.readLine();
//        System.out.println("server.socketRemote():" + clientUUID);
//        Socket pipeClient = remoteSocketFactory.pipeSocket(clientUUID, 0, false);
//        Socket pipeServer = remoteSocketFactory.pipeSocket(serverUUID, 0, true);
//        System.out.println("server.writing()");
//        pipeServer.getOutputStream().write(1);
//        pipeClient.getOutputStream().write(2);
//        pipeServer.getOutputStream().flush();
//        pipeClient.getOutputStream().flush();
//        System.out.println("server.written()");
//        System.out.println("server.reading()");
//        System.out.println("server.readRemote():" + pipeClient.getInputStream().read());
//        System.out.println("server.readLocal():" + pipeServer.getInputStream().read());
//        System.out.println("server.reading()");
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