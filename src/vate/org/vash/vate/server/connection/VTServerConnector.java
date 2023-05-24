package org.vash.vate.server.connection;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.network.nat.mapping.VTNATPortMappingResultNotify;
import org.vash.vate.network.nat.mapping.VTNATSinglePortMappingManagerMKII;
import org.vash.vate.security.VTBlake3DigestRandom;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.session.VTServerSessionListener;
import org.vash.vate.socket.factory.VTDefaultProxy;
import org.vash.vate.socket.factory.VTDefaultProxyAuthenticator;
import org.vash.vate.socket.factory.VTHTTPConnectTunnelSocket;

public class VTServerConnector implements Runnable
{
  private volatile boolean passive;
  private volatile boolean connecting = false;
  private volatile boolean running = true;
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
  private List<VTServerConnectionHandler> connectionHandlers;
  private VTNATSinglePortMappingManagerMKII portMappingManager;
  private VTServerConnectorNATPortMappingResultNotify natNotify = new VTServerConnectorNATPortMappingResultNotify();
  private List<VTServerSessionListener> listeners = new ArrayList<VTServerSessionListener>();
  private VTBlake3DigestRandom secureRandom;
  
  public VTServerConnector(VTServer server, VTBlake3DigestRandom secureRandom)
  {
    this.server = server;
    this.secureRandom = secureRandom;
    this.connectionHandlers = Collections.synchronizedList(new ArrayList<VTServerConnectionHandler>());
    portMappingManager = new VTNATSinglePortMappingManagerMKII(3, 300);
    portMappingManager.start();
  }
  
  private class VTServerConnectorNATPortMappingResultNotify implements VTNATPortMappingResultNotify
  {
    public void result(List<String> externalHosts)
    {
      StringBuilder natHosts = new StringBuilder("[");
      for (String address : externalHosts)
      {
        natHosts.append(address + ",");
      }
      if (natHosts.length() > 1)
      {
        natHosts.deleteCharAt(natHosts.length() - 1);
      }
      natHosts.append("]");
      if (externalHosts.size() > 0 && passive && connecting)
      {
        VTConsole.print("\rVT>Configured NAT hosts:" + natHosts + "\nVT>");
      }
    }
  }
  
  public void stop()
  {
    this.running = false;
    interruptConnector();
    try
    {
      for (VTServerConnectionHandler handler : connectionHandlers.toArray(new VTServerConnectionHandler[] {}))
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
  
  public List<VTServerConnectionHandler> getConnectionHandlers()
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
  
  /*
   * public net.sourceforge.jsocks.socks.Proxy buildSocksProxy() { String
   * proxy_host = proxyAddress; String proxy_port = String.valueOf(proxyPort);
   * String proxy_user = proxyUser; String proxy_password = proxyPassword; if
   * (!UseProxyAuthentication) { proxy_user = null; proxy_password = null; }
   * return net.sourceforge.jsocks.socks.Proxy.buildProxy(proxy_host,
   * proxy_port, proxy_user, proxy_password); }
   */
  
  public boolean setServerSocket(String address, Integer port)
  {
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
      /*
       * if (proxyType.toUpperCase().startsWith("S")) {
       * net.sourceforge.jsocks.socks.Proxy proxy = buildSocksProxy(); String
       * nullHost = null; connectionServerSocket = new
       * net.sourceforge.jsocks.socks.SocksServerSocket(proxy, nullHost, port);
       * return true; } else { connectionServerSocket = new ServerSocket();
       * //vtServerSocket.setPerformancePreferences(1, 3, 2);
       * connectionServerSocket.bind(new InetSocketAddress(port)); return true;
       * }
       */
      return true;
    }
    catch (SecurityException e)
    {
      VTConsole.print("\rVT>Security error detected!\nVT>");
      // return false;
    }
    catch (Throwable e)
    {
      // VTTerminal.print("\rVT>TCP port [" + port + "] is already in
      // use!\nVT>");
      // e.printStackTrace();
      VTConsole.print("\rVT>Listening to connection in port [" + port + "] failed!\nVT>");
      // return false;
    }
    return false;
  }
  
  public void resetSockets(VTServerConnection connection) throws SocketException
  {
    if (proxyType != null)
    {
      //Authenticator.setDefault(VTDefaultProxyAuthenticator.getInstance());
    }
    if (proxyType == null)
    {
      connection.setConnectionSocket(new Socket());
      if (proxyAddress != null && proxyPort != null)
      {
        VTDefaultProxyAuthenticator.removeProxy(proxyAddress, proxyPort);
      }
    }
    else if (!passive && proxyType.toUpperCase().startsWith("H") && proxyAddress != null && proxyPort != null)
    {
      if (proxyType != null && proxyAddress != null && proxyPort != null && proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
      {
        VTDefaultProxyAuthenticator.putProxy(proxyAddress, proxyPort, new VTDefaultProxy(Proxy.Type.HTTP, proxyAddress, proxyPort, proxyUser, proxyPassword));
      }
      else
      {
        VTDefaultProxyAuthenticator.removeProxy(proxyAddress, proxyPort);
      }
      Proxy proxy = new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort != null && proxyPort > 0 ? proxyPort : 8080));
      
      Socket socket = null;
      
      try
      {
        socket = new Socket(proxy);
      }
      catch (RuntimeException e)
      {
        //java 1.7 and earlier cannot do http connect tunneling natively
        socket = new VTHTTPConnectTunnelSocket(proxyAddress, proxyPort, proxyUser, proxyPassword);
      }
      
      connection.setConnectionSocket(socket);
    }
    else if (!passive && proxyType.toUpperCase().startsWith("S") && proxyAddress != null && proxyPort != null)
    {
      if (proxyType != null && proxyAddress != null && proxyPort != null && proxyUser != null && proxyPassword != null && proxyUser.length() > 0 && proxyPassword.length() > 0)
      {
        VTDefaultProxyAuthenticator.putProxy(proxyAddress, proxyPort, new VTDefaultProxy(Proxy.Type.SOCKS, proxyAddress, proxyPort, proxyUser, proxyPassword));
      }
      else
      {
        VTDefaultProxyAuthenticator.removeProxy(proxyAddress, proxyPort);
      }
      Proxy proxy = new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyAddress, proxyPort != null && proxyPort > 0 ? proxyPort : 1080));
      
      Socket socket = new Socket(proxy);
      
      connection.setConnectionSocket(socket);
    }
    else
    {
      connection.setConnectionSocket(new Socket());
      if (proxyAddress != null && proxyPort != null)
      {
        VTDefaultProxyAuthenticator.removeProxy(proxyAddress, proxyPort);
      }
    }
    
    // connection.getConnectionSocket().setReuseAddress(true);
  }
  
  public boolean listenConnection(VTServerConnection connection)
  {
    VTConsole.print("\rVT>Listening to connection with client...\nVT>");
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
      // connectionServerSocket.setReceiveBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE
      // - 1);
      connecting = true;
      connection.setConnectionSocket(connectionServerSocket.accept());
      // connection.getConnectionSocket().setSendBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE
      // - 1);
      connection.getConnectionSocket().setTcpNoDelay(true);
      //connection.getConnectionSocket().setSendBufferSize(1024 * 64);
      //connection.getConnectionSocket().setReceiveBufferSize(1024 * 64);
      //connection.getConnectionSocket().setSoLinger(true, 5);
      // connection.getConnectionSocket().setReuseAddress(true);
      // connection.getConnectionSocket().setKeepAlive(true);
      connection.getConnectionSocket().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
      connecting = false;
      if (encryptionType == null)
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
      }
      else if (encryptionType.toUpperCase().startsWith("A"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
      }
      else if (encryptionType.toUpperCase().startsWith("R"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
      }
      // else if (encryptionType.toUpperCase().startsWith("B"))
      // {
      // connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
      // }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
      }
      else if (encryptionType.toUpperCase().startsWith("I"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
      }
      else if (encryptionType.toUpperCase().startsWith("G"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
      }
      else
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
      }
      connection.setEncryptionKey(encryptionKey);
      //connectionServerSocket.close();
      VTConsole.print("\rVT>Connection with client established!\nVT>");
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
    if (port == null)
    {
      port = 6060;
    }
    VTConsole.print("\rVT>Establishing connection with client...\nVT>");
    portMappingManager.deletePortMapping();
    connection.closeSockets();
    try
    {
      resetSockets(connection);
      InetSocketAddress socketAddress = null;
      if (proxyType.toUpperCase().startsWith("H") || proxyType.toUpperCase().startsWith("S"))
      {
        socketAddress = InetSocketAddress.createUnresolved(address, port);
      }
      else
      {
        socketAddress = new InetSocketAddress(address, port);
      }
      // connection.getShellSocket().setPerformancePreferences(1, 3, 2);
      connecting = true;
      // connection.getConnectionSocket().setReceiveBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE
      // - 1);
      // connection.getConnectionSocket().setSendBufferSize(VT.VT_NETWORK_PACKET_BUFFER_SIZE
      // - 1);
      connection.getConnectionSocket().connect(socketAddress);
      connection.getConnectionSocket().setTcpNoDelay(true);
      //connection.getConnectionSocket().setSendBufferSize(1024 * 64);
      //connection.getConnectionSocket().setReceiveBufferSize(1024 * 64);
      //connection.getConnectionSocket().setSoLinger(true, 5);
      // connection.getConnectionSocket().setReuseAddress(true);
      // connection.getConnectionSocket().setKeepAlive(true);
      connection.getConnectionSocket().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
      connecting = false;
      if (encryptionType == null)
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
      }
      else if (encryptionType.toUpperCase().startsWith("A"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_AES);
      }
      else if (encryptionType.toUpperCase().startsWith("R"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_RC4);
      }
      // else if (encryptionType.toUpperCase().startsWith("B"))
      // {
      // connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_BLOWFISH);
      // }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_SALSA);
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC256);
      }
      else if (encryptionType.toUpperCase().startsWith("I"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_ISAAC);
      }
      else if (encryptionType.toUpperCase().startsWith("G"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN);
      }
      else
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_NONE);
      }
      connection.setEncryptionKey(encryptionKey);
      VTConsole.print("\rVT>Connection with client established!\nVT>");
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
    if (server.getInputMenuBar() != null)
    {
      server.getInputMenuBar().setEnabled(true);
    }
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
        VTServerConnection connection = new VTServerConnection(secureRandom);
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
              server.getServerThreads().execute(handler);
            }
//						if (sessionsLimit > 0 || connectionHandlers.size() < sessionsLimit)
//						{
//							try
//							{
//								connectionServerSocket.close();
//							}
//							catch (Throwable t)
//							{
//								
//							}
//						}
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
              server.getServerThreads().execute(handler);
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
      connecting = false;
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