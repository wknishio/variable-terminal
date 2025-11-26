package org.vash.vate.server.connection;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.ConcurrentLinkedQueue;

import org.vash.vate.VTSystem;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.nat.mapping.VTNATPortMappingResultNotify;
import org.vash.vate.nat.mapping.VTNATSinglePortMappingManagerMKII;
import org.vash.vate.proxy.client.VTProxy;
import org.vash.vate.proxy.client.VTProxy.VTProxyType;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.session.VTServerSessionListener;

public class VTServerConnector implements Runnable
{
  private boolean passive;
  //private boolean connecting = false;
  private boolean running = true;
  private String hostAddress;
  private Integer hostPort;
  private Integer natPort;
  private String proxyType;
  private String proxyAddress;
  private Integer proxyPort;
  //private boolean useProxyAuthentication;
  private String proxyUser;
  private String proxyPassword;
  private String encryptionType;
  private byte[] encryptionKey;
  private Integer sessionsMaximum;
  private String sessionShell = "";
  private ServerSocket connectionServerSocket;
  private VTServer server;
  private Collection<VTServerConnectionHandler> connectionHandlers;
  private VTNATSinglePortMappingManagerMKII portMappingManager;
  private VTServerConnectorNATPortMappingResultNotify natNotify = new VTServerConnectorNATPortMappingResultNotify();
  private Collection<VTServerSessionListener> listeners = new ConcurrentLinkedQueue<VTServerSessionListener>();
  private final VTBlake3SecureRandom secureRandom;
  private final VTProxy[] proxies;
  
  public VTServerConnector(VTServer server, VTBlake3SecureRandom secureRandom, VTProxy... proxies)
  {
    this.server = server;
    this.secureRandom = secureRandom;
    this.proxies = proxies;
    this.connectionHandlers = new ConcurrentLinkedQueue<VTServerConnectionHandler>();
    portMappingManager = new VTNATSinglePortMappingManagerMKII(5, 300, server.getExecutorService());
    portMappingManager.start();
  }
  
  private class VTServerConnectorNATPortMappingResultNotify implements VTNATPortMappingResultNotify
  {
    public void result(List<String> externalHosts)
    {
//      StringBuilder natHosts = new StringBuilder("[");
//      for (String address : externalHosts)
//      {
//        natHosts.append(address + ",");
//      }
//      if (natHosts.length() > 1)
//      {
//        natHosts.deleteCharAt(natHosts.length() - 1);
//      }
//      natHosts.append("]");
//      if (externalHosts.size() > 0 && passive && connecting)
//      {
//        VTConsole.print("\rVT>Configured NAT hosts:" + natHosts + "\nVT>");
//      }
    }
  }
  
  public void stop()
  {
    this.running = false;
    interruptConnector();
    try
    {
      for (VTServerConnectionHandler handler : connectionHandlers)
      {
        try
        {
          handler.getConnection().closeSockets();
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
  
  public boolean isPassive()
  {
    return passive;
  }
  
  public void setPassive(boolean passive)
  {
    this.passive = passive;
  }
  
  public String getAddress()
  {
    return hostAddress;
  }
  
  public void setAddress(String address)
  {
    this.hostAddress = address;
  }
  
  public String getProxyType()
  {
    return proxyType;
  }
  
  public void setProxyType(String proxyType)
  {
    this.proxyType = proxyType;
  }
  
  public String getProxyAddress()
  {
    return proxyAddress;
  }
  
  public void setProxyAddress(String proxyAddress)
  {
    this.proxyAddress = proxyAddress;
  }
  
  public Integer getPort()
  {
    return hostPort;
  }
  
  public void setPort(Integer port)
  {
    if (port != null && (port < 1 || port > 65535))
    {
      return;
    }
    this.hostPort = port;
  }
  
  public Integer getProxyPort()
  {
    return proxyPort;
  }
  
  public Integer getNatPort()
  {
    return natPort;
  }
  
  public void setNatPort(Integer natPort)
  {
    if (natPort != null && (natPort < 1 || natPort > 65535))
    {
      return;
    }
    this.natPort = natPort;
    if (natPort != null && natPort > 0)
    {
      // portMappingManager.setPortMapping(hostPort != null && hostPort > 0 ?
      // hostPort
      // : 6060, null, natPort, "TCP", "Variable-Terminal-Port-Mapping");
    }
    else
    {
      portMappingManager.deletePortMapping();
    }
  }
  
  public void setProxyPort(Integer proxyPort)
  {
    if (proxyPort != null && (proxyPort < 1 || proxyPort > 65535))
    {
      return;
    }
    this.proxyPort = proxyPort;
  }
  
//  public boolean isUseProxyAuthentication()
//  {
//    return useProxyAuthentication;
//  }
  
//  public void setUseProxyAuthentication(boolean useProxyAuthentication)
//  {
//    this.useProxyAuthentication = useProxyAuthentication;
//  }
  
  public String getProxyUser()
  {
    return proxyUser;
  }
  
  public void setProxyUser(String proxyUser)
  {
    this.proxyUser = proxyUser;
  }
  
  public String getProxyPassword()
  {
    return proxyPassword;
  }
  
  public void setProxyPassword(String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
  }
  
  public String getEncryptionType()
  {
    return encryptionType;
  }
  
  public void setEncryptionType(String encryptionType)
  {
    this.encryptionType = encryptionType;
  }
  
  public byte[] getEncryptionKey()
  {
    return encryptionKey;
  }
  
  public void setEncryptionKey(byte[] encryptionKey)
  {
    this.encryptionKey = encryptionKey;
  }
  
  public Integer getSessionsMaximum()
  {
    return sessionsMaximum;
  }
  
  public void setSessionsMaximum(Integer sessionsMaximum)
  {
    this.sessionsMaximum = sessionsMaximum;
  }
  
  public Collection<VTServerConnectionHandler> getConnectionHandlers()
  {
    return connectionHandlers;
  }
  
  public boolean registerConnectionHandler(VTServerConnectionHandler handler)
  {
    handler.setSessionListeners(listeners);
    return connectionHandlers.add(handler);
  }
  
  public boolean unregisterConnectionHandler(VTServerConnectionHandler handler)
  {
    synchronized (this)
    {
      if (connectionHandlers.remove(handler))
      {
        notify();
        return true;
      }
      else
      {
        return false;
      }
    }
  }
  
  public void interruptConnector()
  {
    // interrupted = true;
    if (connectionServerSocket != null)
    {
      try
      {
        connectionServerSocket.close();
        // setServerSocket(port);
      }
      catch (Throwable t)
      {
        // VTTerminal.println(e.toString());
      }
    }
    
    synchronized (this)
    {
      notifyAll();
    }
  }
  
  public boolean setServerSocket(String address, Integer port)
  {
    int idx = address.indexOf(';');
    if (idx >= 0)
    {
      String[] split = address.split(";");
      address = split[0];
    }
    try
    {
      if (connectionServerSocket != null
      && (connectionServerSocket.isClosed()
      || !connectionServerSocket.isBound()
      || connectionServerSocket.getLocalPort() != port
      || (address != null && address.length() > 0 && !connectionServerSocket.getInetAddress().getHostName().equals(address))))
      {
        connectionServerSocket.close();
        connectionServerSocket = new ServerSocket();
        //connectionServerSocket.setReceiveBufferSize(VT.VT_CONNECTION_PACKET_BUFFER_SIZE_BYTES);
        // connectionServerSocket.setReuseAddress(true);
        if (port != null)
        {
          if (address != null && address.length() > 0)
          {
            connectionServerSocket.bind(new InetSocketAddress(address, port));
          }
          else
          {
            connectionServerSocket.bind(new InetSocketAddress(port));
          }
        }
        else
        {
          if (hostAddress != null && hostAddress.length() > 0)
          {
            connectionServerSocket.bind(new InetSocketAddress(address, 6060));
          }
          else
          {
            connectionServerSocket.bind(new InetSocketAddress(6060));
          }
        }
      }
      if (connectionServerSocket == null)
      {
        connectionServerSocket = new ServerSocket();
        if (port != null)
        {
          if (address != null && address.length() > 0)
          {
            connectionServerSocket.bind(new InetSocketAddress(address, port));
          }
          else
          {
            connectionServerSocket.bind(new InetSocketAddress(port));
          }
        }
        else
        {
          if (address != null && address.length() > 0)
          {
            connectionServerSocket.bind(new InetSocketAddress(address, 6060));
          }
          else
          {
            connectionServerSocket.bind(new InetSocketAddress(6060));
          }
        }
      }
      return true;
    }
    catch (Throwable e)
    {
      VTMainConsole.print("\rVT>Awaiting connection in port [" + port + "] failed!\nVT>");
    }
    try
    {
      Thread.sleep(125);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public void resetSockets(VTServerConnection connection) throws IOException
  {
    if (proxies != null && proxies.length > 0)
    {
      Socket socket = VTProxy.next(null, null, 0, proxies);
      
      connection.setConnectionSocket(socket);
      return;
    }
    if (proxyType != null)
    {
      //Authenticator.setDefault(VTDefaultProxyAuthenticator.getInstance());
    }
    if (proxyType == null)
    {
      connection.setConnectionSocket(new Socket());
    }
    else if (proxyType.toUpperCase().startsWith("G") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, null, 0, new VTProxy(VTProxyType.GLOBAL, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("D") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, null, 0, new VTProxy(VTProxyType.DIRECT, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("H") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, null, 0, new VTProxy(VTProxyType.HTTP, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("S") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, null, 0, new VTProxy(VTProxyType.SOCKS, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("P") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, null, 0, new VTProxy(VTProxyType.PLUS, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else
    {
      connection.setConnectionSocket(new Socket());
    }
    // connection.getConnectionSocket().setReuseAddress(true);
  }
  
  public boolean listenConnection(VTServerConnection connection)
  {
    VTMainConsole.print("\rVT>Awaiting connection with client...\nVT>");
    connection.closeSockets();
    if (!setServerSocket(hostAddress, hostPort != null && hostPort > 0 ? hostPort : 6060))
    {
      return false;
    }
    try
    {
      resetSockets(connection);
      if (natPort != null && natPort > 0)
      {
        portMappingManager.setPortMapping(hostPort != null && hostPort > 0 ? hostPort : 6060, null, natPort, 600, "TCP", "Variable-Terminal-Port-Mapping", natNotify);
      }
      else
      {
        portMappingManager.deletePortMapping();
      }
      connectionServerSocket.setSoTimeout(0);
//      connecting = true;
      connection.setConnectionSocket(connectionServerSocket.accept());
      connection.getConnectionSocket().setTcpNoDelay(true);
      //connection.getConnectionSocket().setSoLinger(true, 0);
      //connection.getConnectionSocket().setKeepAlive(true);
      connection.getConnectionSocket().setSoTimeout(server.getPingLimitMilliseconds());
//      connecting = false;
      if (encryptionType == null)
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_NONE);
      }
      else if (encryptionType.toUpperCase().startsWith("Z"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ZUC);
      }
//      else if (encryptionType.toUpperCase().startsWith("V"))
//      {
//        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_VMPC);
//      }
      else if (encryptionType.toUpperCase().startsWith("G"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN);
      }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_SALSA);
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_HC);
      }
      else if (encryptionType.toUpperCase().startsWith("R"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_RABBIT);
      }
      else
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_NONE);
      }
      connection.setEncryptionKey(encryptionKey);
      //connectionServerSocket.close();
      VTMainConsole.print("\rVT>Connection with client established!\nVT>");
      return true;
    }
    catch (Throwable e)
    {
      // VTTerminal.println(e.toString());
      //e.printStackTrace();
      connection.closeSockets();
    }
    /*
     * else { VTTerminal.print("\rVT>Connection listening interrupted!\nVT>"); }
     */
    return false;
  }
  
  public boolean establishConnection(VTServerConnection connection, String address, Integer port)
  {
    String bind = "";
    int idx = address.indexOf(';');
    if (idx >= 0)
    {
      String[] split = address.split(";");
      bind = split[0];
      address = split[1];
    }
    if (port == null)
    {
      port = 6060;
    }
    VTMainConsole.print("\rVT>Establishing connection with client...\nVT>");
    portMappingManager.deletePortMapping();
    connection.closeSockets();
    try
    {
      resetSockets(connection);
//      connecting = true;
      VTProxy.connect(bind, address, port, 0, connection.getConnectionSocket());
      connection.getConnectionSocket().setTcpNoDelay(true);
      //connection.getConnectionSocket().setSoLinger(true, 0);
      //connection.getConnectionSocket().setKeepAlive(true);
      connection.getConnectionSocket().setSoTimeout(server.getPingLimitMilliseconds());
//      connecting = false;
      if (encryptionType == null)
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_NONE);
      }
      else if (encryptionType.toUpperCase().startsWith("Z"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_ZUC);
      }
//      else if (encryptionType.toUpperCase().startsWith("V"))
//      {
//        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_VMPC);
//      }
      else if (encryptionType.toUpperCase().startsWith("G"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_GRAIN);
      }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_SALSA);
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_HC);
      }
      else if (encryptionType.toUpperCase().startsWith("R"))
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_RABBIT);
      }
      else
      {
        connection.setEncryptionType(VTSystem.VT_CONNECTION_ENCRYPTION_NONE);
      }
      connection.setEncryptionKey(encryptionKey);
      VTMainConsole.print("\rVT>Connection with client established!\nVT>");
      return true;
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
    }
    return false;
  }
  
  public void run()
  {
    /* if (passive) { if (!setServerSocket(socketAddress)) { return; } } */
    Thread.currentThread().setName(this.getClass().getSimpleName());
//    if (server.getInputMenuBar() != null)
//    {
//      server.getInputMenuBar().setEnabled(true);
//    }
    server.enableInputMenuBar();
    while (running)
    {
      // if (interrupted)
      // {
      // VTConsole.print("\rVT>Listening of connections
      // interrupted!\nVT>");
      // }
      // interrupted = false;
      // VTTerminal.print("\rVT>sessionLimit:" + sessionLimit +
      // "\nVT>");
      synchronized (this)
      {
        while (running && sessionsMaximum != null && sessionsMaximum > 0 && connectionHandlers.size() >= sessionsMaximum)
        {
          try
          {
            wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
      }
      if (sessionsMaximum == null || sessionsMaximum <= 0 || connectionHandlers.size() < sessionsMaximum)
      {
        VTServerConnection connection = new VTServerConnection(server.getExecutorService());
        byte[] secureSeed = new byte[64];
        secureRandom.nextBytes(secureSeed);
        connection.setSecureRandomSeed(secureSeed);
        if (passive)
        {
          if (listenConnection(connection))
          {
            synchronized (this)
            {
              VTServerConnectionHandler handler = new VTServerConnectionHandler(server, this, connection);
              // Thread handlerThread = new Thread(handler,
              // handler.getClass().getSimpleName());
              // handlerThread.start();
              server.getExecutorService().execute(handler);
            }
          }
          else
          {
            connection.closeSockets();
            // VTConsole.print("\rVT>Listening of connections
            // interrupted!\nVT>");
          }
        }
        else
        {
          if (establishConnection(connection, hostAddress, hostPort != null && hostPort > 0 ? hostPort : 6060))
          {
            synchronized (this)
            {
              VTServerConnectionHandler handler = new VTServerConnectionHandler(server, this, connection);
              // Thread handlerThread = new Thread(handler,
              // handler.getClass().getSimpleName());
              // handlerThread.start();
              server.getExecutorService().execute(handler);
            }
          }
          else
          {
            connection.closeSockets();
            // VTConsole.print("\rVT>Establishment of connections
            // interrupted!\nVT>");
          }
        }
      }
      try
      {
        //Thread.sleep(1000);
      }
      catch (Throwable t)
      {
        
      }
//      connecting = false;
    }
  }
  
  public void addSessionListener(VTServerSessionListener listener)
  {
    listeners.add(listener);
  }
  
  public void removeSessionListener(VTServerSessionListener listener)
  {
    listeners.remove(listener);
  }
  
  public String getSessionShell()
  {
    return sessionShell;
  }
  
  public void setSessionShell(String sessionShell)
  {
    this.sessionShell = sessionShell;
  }
}