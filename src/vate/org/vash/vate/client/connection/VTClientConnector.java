package org.vash.vate.client.connection;

import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.console.VTConsole;
import org.vash.vate.network.nat.mapping.VTNATPortMappingResultNotify;
import org.vash.vate.network.nat.mapping.VTNATSinglePortMappingManagerMKII;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.security.VTBlake3DigestRandom;
import org.vash.vate.socket.VTDefaultProxy;
import org.vash.vate.socket.VTDefaultProxyAuthenticator;
import org.vash.vate.socket.VTHTTPConnectTunnelSocket;

public class VTClientConnector implements Runnable
{
  private volatile boolean active;
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
  private String sessionCommands;
  // private String sessionLines;
  private String sessionShell = "";
  private ServerSocket connectionServerSocket;
  private VTClient client;
  private VTClientConnection connection;
  private VTClientConnectionHandler handler;
  private volatile boolean skipConfiguration;
  // private volatile boolean connectedOnce;
  private volatile boolean timeoutEnabled;
  private VTNATSinglePortMappingManagerMKII portMappingManager;
  private VTConnectionRetryTimeoutTask connectionRetryTimeoutTask = new VTConnectionRetryTimeoutTask();
  private volatile boolean retry = false;
  private volatile boolean dialog = false;
  // private volatile boolean dialogLine = false;
  private VTClientConnectorNATPortMappingResultNotify natNotify = new VTClientConnectorNATPortMappingResultNotify();
  private List<VTClientSessionListener> listeners = new ArrayList<VTClientSessionListener>();
  private VTBlake3DigestRandom secureRandom;
  
  public VTClientConnector(VTClient client, VTBlake3DigestRandom secureRandom)
  {
    this.client = client;
    this.secureRandom = secureRandom;
    this.connection = new VTClientConnection(this.secureRandom);
    this.handler = new VTClientConnectionHandler(client, connection);
    portMappingManager = new VTNATSinglePortMappingManagerMKII(3, 300);
    portMappingManager.start();
  }
  
  private class VTClientConnectorNATPortMappingResultNotify implements VTNATPortMappingResultNotify
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
      if (externalHosts.size() > 0 && !active && connecting)
      {
        VTConsole.print("\nVT>Configured NAT hosts:" + natHosts);
      }
    }
  }
  
  private class VTConnectionRetryTimeoutTask implements Runnable
  {
    // private volatile boolean finished = true;
    // private Thread timeoutThread;
    
    public void start()
    {
      timeoutEnabled = true;
      // finished = false;
      // timeoutThread = new Thread(this, getClass().getSimpleName());
      // timeoutThread.start();
      client.getClientThreads().execute(this);
    }
    
    public void run()
    {
      try
      {
        // Thread.currentThread().setName(getClass().getSimpleName());
        // timeoutThread = Thread.currentThread();
        // Thread.sleep(180000);
        if (timeoutEnabled)
        {
          synchronized (this)
          {
            wait(VT.VT_CLIENT_RECONNECTION_TIMEOUT_MILLISECONDS);
          }
        }
        // VTConsole.print("\nVT>AuthenticationTimeout");
      }
      catch (Throwable e)
      {
        // VTConsole.print("\nVT>InterruptedTimeout");
      }
      try
      {
        if (timeoutEnabled)
        {
          timeoutEnabled = false;
          setSkipConfiguration(true);
          try
          {
            // VTConsole.println("");
            VTConsole.interruptReadLine();
          }
          catch (Throwable t)
          {
            
          }
          if ((!retry || dialog))
          {
            // System.out.println("dialog == true");
            VTConsole.print("VT>Retrying connection with server...");
            // retry = true;
          }
          else
          {
            // System.out.println("dialog == false");
            VTConsole.print("\nVT>Retrying connection with server...");
            // retry = true;
          }
          if (client.getConnectionDialog() != null)
          {
            if (client.getConnectionDialog().isVisible())
            {
              client.getConnectionDialog().close();
            }
          }
        }
      }
      catch (Throwable e)
      {
        
      }
      // finished = true;
    }
    
    public void trigger()
    {
      // System.out.println("trigger");
      try
      {
        if (timeoutEnabled)
        {
          timeoutEnabled = false;
          synchronized (this)
          {
            notifyAll();
          }
        }
        else
        {
          setSkipConfiguration(true);
          try
          {
            // VTConsole.println("");
            VTConsole.interruptReadLine();
          }
          catch (Throwable t)
          {
            // t.printStackTrace();
          }
//          if (dialog)
//          {
//            VTConsole.print("VT>Retrying connection with server...");
//          }
//          else
//          {
//            VTConsole.print("\nVT>Retrying connection with server...");
//          }
//          if (client.getConnectionDialog() != null)
//          {
//            if (client.getConnectionDialog().isVisible())
//            {
//              client.getConnectionDialog().close();
//            }
//          }
        }
      }
      catch (Throwable t)
      {
        // t.printStackTrace();
      }
    }
    
    public void stop()
    {
      if (timeoutEnabled)
      {
        timeoutEnabled = false;
        try
        {
          synchronized (this)
          {
            notifyAll();
          }
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    
  }
  
  public void stop()
  {
    this.running = false;
    connectionRetryTimeoutTask.stop();
    interruptConnector();
    connection.closeSockets();
  }
  
  public void setSkipConfiguration(boolean skipConfiguration)
  {
    this.skipConfiguration = skipConfiguration;
  }
  
  public boolean isSkipConfiguration()
  {
    return this.skipConfiguration;
  }
  
  public VTClient getClient()
  {
    return client;
  }
  
  public void setActive(boolean active)
  {
    this.active = active;
  }
  
  public void setAddress(String address)
  {
    this.hostAddress = address;
  }
  
  public void setPort(Integer port)
  {
    if (port != null && (port < 1 || port > 65535))
    {
      return;
    }
    this.hostPort = port;
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
  
  public void setProxyType(String proxyType)
  {
    this.proxyType = proxyType;
  }
  
  public void setProxyAddress(String proxyAddress)
  {
    this.proxyAddress = proxyAddress;
  }
  
  public void setProxyPort(Integer proxyPort)
  {
    if (proxyPort != null && (proxyPort < 1 || proxyPort > 65535))
    {
      return;
    }
    this.proxyPort = proxyPort;
  }
  
//  public void setUseProxyAuthentication(boolean useProxyAuthentication)
//  {
//    this.useProxyAuthentication = useProxyAuthentication;
//  }
  
  public void setProxyUser(String proxyUser)
  {
    this.proxyUser = proxyUser;
  }
  
  public void setProxyPassword(String proxyPassword)
  {
    this.proxyPassword = proxyPassword;
  }
  
  public void setEncryptionType(String encryptionType)
  {
    this.encryptionType = encryptionType;
  }
  
  public void setEncryptionKey(byte[] encryptionKey)
  {
    this.encryptionKey = encryptionKey;
  }
  
  public void setSessionCommands(String sessionCommands)
  {
    this.sessionCommands = sessionCommands;
  }
  
  public VTClientConnectionHandler getHandler()
  {
    return handler;
  }
  
  public void setHandler(VTClientConnectionHandler handler)
  {
    this.handler = handler;
  }
  
  public boolean isActive()
  {
    return active;
  }
  
  public String getAddress()
  {
    return hostAddress;
  }
  
  public Integer getPort()
  {
    return hostPort;
  }
  
  public Integer getNatPort()
  {
    return natPort;
  }
  
  public String getProxyType()
  {
    return proxyType;
  }
  
  public String getProxyAddress()
  {
    return proxyAddress;
  }
  
  public Integer getProxyPort()
  {
    return proxyPort;
  }
  
//  public boolean isUseProxyAuthentication()
//  {
//    return useProxyAuthentication;
//  }
  
  public String getProxyUser()
  {
    return proxyUser;
  }
  
  public String getProxyPassword()
  {
    return proxyPassword;
  }
  
  public String getEncryptionType()
  {
    return encryptionType;
  }
  
  public byte[] getEncryptionKey()
  {
    return encryptionKey;
  }
  
  public String getSessionCommands()
  {
    return sessionCommands;
  }
  
  public void setClient(VTClient client)
  {
    this.client = client;
  }
  
  public void setConnection(VTClientConnection connection)
  {
    this.connection = connection;
  }
  
  public VTClientConnection getConnection()
  {
    return connection;
  }
  
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
      VTConsole.print("\nVT>Listening to connection in port [" + port + "] failed!");
    }
    try
    {
      Thread.sleep(250);
    }
    catch (Throwable t)
    {
      
    }
    return false;
  }
  
  public void resetSockets(VTClientConnection connection) throws SocketException
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
    else if (proxyType.toUpperCase().startsWith("H") && proxyAddress != null && proxyPort != null)
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
    else if (proxyType.toUpperCase().startsWith("S") && proxyAddress != null && proxyPort != null)
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
  
  public boolean listenConnection(VTClientConnection connection)
  {
    if ((!retry || dialog))
    {
      VTConsole.print("VT>Listening to connection with server, interrupt with enter...");
      retry = true;
    }
    else
    {
      VTConsole.print("\nVT>Listening to connection with server, interrupt with enter...");
    }
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
      VTConsole.createInterruptibleReadline(false, new Runnable()
      {
        public void run()
        {
          try
          {
            connectionServerSocket.close();
          }
          catch (Throwable e)
          {
            
          }
        }
      });
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
      try
      {
        VTConsole.interruptReadLine();
      }
      catch (Throwable t)
      {
        
      }
      //connectionServerSocket.close();
      VTConsole.print("\nVT>Connection with server established!");
      return true;
    }
    catch (Throwable e)
    {
      //e.printStackTrace();
      VTConsole.print("\nVT>Connection with server interrupted!");
      connection.closeSockets();
    }
    return false;
  }
  
  public boolean establishConnection(VTClientConnection connection, String address, Integer port)
  {
    if (port == null)
    {
      port = 6060;
    }
    if ((!retry || dialog))
    {
      VTConsole.print("VT>Establishing connection with server...");
      retry = true;
    }
    else
    {
      VTConsole.print("\nVT>Establishing connection with server...");
    }
    try
    {
      resetSockets(connection);
      portMappingManager.deletePortMapping();
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
      VTConsole.print("\nVT>Connection with server established!");
      return true;
    }
    // catch (UnknownHostException e)
    // {
    // VTConsole.print("\nVT>Host [" + address + "] not found!");
    // return false;
    // }
    catch (Throwable e)
    {
      //e.printStackTrace();
      VTConsole.print("\nVT>Connection with server failed!");
    }
    return false;
  }
  
  public void interruptConnector()
  {
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
  }
  
  private void startConnectionRetryTimeoutThread()
  {
    connectionRetryTimeoutTask.start();
  }
  
  public void triggerConnectionRetryTimeoutThread()
  {
    connectionRetryTimeoutTask.trigger();
  }
  
  public void stopConnectionRetryTimeoutThread()
  {
    connectionRetryTimeoutTask.stop();
  }
  
  public boolean retryConnection()
  {
    retry = true;
    dialog = false;
    // dialogLine = false;
    if (VTConsole.isDaemon())
    {
      synchronized (this)
      {
        try
        {
          wait(VT.VT_DAEMON_RECONNECTION_TIMEOUT_MILLISECONDS);
        }
        catch (Throwable e)
        {
          
        }
      }
      return true;
    }
    startConnectionRetryTimeoutThread();
    if (skipConfiguration)
    {
      skipConfiguration = false;
      return true;
    }
    else
    {
      
    }
    try
    {
      VTConsole.print("\nVT>Retry connection with server?(Y/N, default:Y):");
      String line = VTConsole.readLine(true);
      if (line == null)
      {
        VTRuntimeExit.exit(0);
        return false;
      }
      else if (skipConfiguration)
      {
        return true;
      }
      if (line.toUpperCase().startsWith("N"))
      {
        VTRuntimeExit.exit(0);
        return false;
      }
//      if (client.getInputMenuBar() != null)
//      {
//        client.getInputMenuBar().setEnabledDialogMenu(true);
//      }
      if (client.getConnectionDialog() != null && !client.getConnectionDialog().isVisible())
      {
        // dialogLine = true;
        dialog = true;
        // retry = false;
        client.getConnectionDialog().open();
      }
      if (skipConfiguration)
      {
        // System.out.println("skipConfiguration");
        skipConfiguration = false;
        return true;
      }
      if (dialog)
      {
        VTConsole.print("VT>Repeat current connection settings?(Y/N, default:Y):");
        dialog = false;
      }
      else
      {
        VTConsole.print("\nVT>Repeat current connection settings?(Y/N, default:Y):");
      }
      // dialog = false;
      // dialogLine = false;
      line = VTConsole.readLine(true);
      if (line == null)
      {
        VTRuntimeExit.exit(0);
        return false;
      }
      else if (skipConfiguration)
      {
        return true;
      }
      if (!line.toUpperCase().startsWith("N"))
      {
        VTConsole.print("VT>Repeat current session user and password?(Y/N, default:Y):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        if (!line.toUpperCase().startsWith("N"))
        {
          retry = false;
          return true;
        }
        else
        {
          VTConsole.print("VT>Enter session shell(null for default):");
          String shell = VTConsole.readLine(true);
          if (shell == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          setSessionShell(shell);
          VTConsole.print("VT>Enter session user:");
          String user = VTConsole.readLine(false);
          if (user == null)
          {
            VTRuntimeExit.exit(0);
            return false;
          }
          else if (skipConfiguration)
          {
            return true;
          }
          client.setUser(user);
          VTConsole.print("VT>Enter session password:");
          String password = VTConsole.readLine(false);
          if (password == null)
          {
            VTRuntimeExit.exit(0);
            return false;
          }
          else if (skipConfiguration)
          {
            return true;
          }
          client.setPassword(password);
          retry = false;
        }
        return true;
      }
      else
      {
        VTConsole.print("VT>Enter settings file(if available):");
        try
        {
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          else if (line.length() > 0)
          {
            client.loadClientSettingsFile(line);
            return true;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      VTConsole.print("VT>Enter connection mode(active as A or passive as P, default:A):");
      line = VTConsole.readLine(true);
      if (line == null)
      {
        VTRuntimeExit.exit(0);
        return false;
      }
      else if (skipConfiguration)
      {
        return true;
      }
      if (line.toUpperCase().startsWith("P"))
      {
        active = false;
        VTConsole.print("VT>Enter host address(default:any):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        hostAddress = line;
        VTConsole.print("VT>Enter listening port(from 1 to 65535, default:6060):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        if (line.length() > 0)
        {
          hostPort = Integer.parseInt(line);
        }
        else
        {
          hostPort = 6060;
        }
        if (hostPort > 65535 || hostPort < 1)
        {
          VTConsole.print("VT>Invalid port!");
          return false;
        }
        else
        {
          VTConsole.print("VT>Use nat port in connection?(Y/N, default:N):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          if (line.toUpperCase().startsWith("Y"))
          {
            VTConsole.print("VT>Enter connection nat port(from 1 to 65535, default:" + hostPort + "):");
            line = VTConsole.readLine(true);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            if (line.length() > 0)
            {
              natPort = Integer.parseInt(line);
            }
            else
            {
              natPort = hostPort;
            }
            if (natPort > 65535 || natPort < 1)
            {
              VTConsole.print("VT>Invalid port!\n");
              natPort = null;
              hostPort = null;
            }
          }
          VTConsole.print("VT>Use encryption in connection?(Y/N, default:N):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          if (line.toUpperCase().startsWith("Y"))
          {
            VTConsole.print("VT>Enter encryption type(RC4(R)/AES(A)/ISAAC(I)/SALSA(S)/HC256(H)/GRAIN(G)):");
            line = VTConsole.readLine(false);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            encryptionType = "RC4";
            if (line.toUpperCase().startsWith("A"))
            {
              encryptionType = "AES";
            }
            // if (line.toUpperCase().startsWith("B"))
            // {
            // encryptionType = "BLOWFISH";
            // }
            if (line.toUpperCase().startsWith("S"))
            {
              encryptionType = "SALSA";
            }
            if (line.toUpperCase().startsWith("H"))
            {
              encryptionType = "HC256";
            }
            if (line.toUpperCase().startsWith("G"))
            {
              encryptionType = "GRAIN";
            }
            if (line.toUpperCase().startsWith("I"))
            {
              encryptionType = "ISAAC";
            }
            VTConsole.print("VT>Enter encryption password:");
            line = VTConsole.readLine(false);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            encryptionKey = line.getBytes("UTF-8");
          }
          else
          {
            encryptionType = "None";
          }
        }
      }
      else
      {
        active = true;
        VTConsole.print("VT>Enter host address(default:any):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        hostAddress = line;
        VTConsole.print("VT>Enter host port(from 1 to 65535, default:6060):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        if (line.length() > 0)
        {
          hostPort = Integer.parseInt(line);
        }
        else
        {
          hostPort = 6060;
        }
        if (hostPort > 65535 || hostPort < 1)
        {
          VTConsole.print("VT>Invalid port!");
          return false;
        }
        VTConsole.print("VT>Use encryption in connection?(Y/N, default:N):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
        }
        else if (skipConfiguration)
        {
          return true;
        }
        if (line.toUpperCase().startsWith("Y"))
        {
          VTConsole.print("VT>Enter encryption type(RC4(R)/AES(A)/ISAAC(I)/SALSA(S)/HC256(H)/GRAIN(G)):");
          line = VTConsole.readLine(false);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          encryptionType = "RC4";
          if (line.toUpperCase().startsWith("A"))
          {
            encryptionType = "AES";
          }
          // if (line.toUpperCase().startsWith("B"))
          // {
          // encryptionType = "BLOWFISH";
          // }
          if (line.toUpperCase().startsWith("S"))
          {
            encryptionType = "SALSA";
          }
          if (line.toUpperCase().startsWith("H"))
          {
            encryptionType = "HC256";
          }
          if (line.toUpperCase().startsWith("G"))
          {
            encryptionType = "GRAIN";
          }
          if (line.toUpperCase().startsWith("I"))
          {
            encryptionType = "ISAAC";
          }
          VTConsole.print("VT>Enter encryption password:");
          line = VTConsole.readLine(false);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          encryptionKey = line.getBytes("UTF-8");
        }
        else
        {
          encryptionType = "None";
        }
        VTConsole.print("VT>Use proxy in connection?(Y/N, default:N):");
        line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
        }
        else if (skipConfiguration)
        {
          return true;
        }
        if (line.toUpperCase().startsWith("Y"))
        {
          VTConsole.print("VT>Enter proxy type(SOCKS as S, HTTP as H, default:S):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          if (line.toUpperCase().startsWith("H"))
          {
            proxyType = "HTTP";
          }
          else
          {
            proxyType = "SOCKS";
          }
          VTConsole.print("VT>Enter proxy host address(default:any):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          proxyAddress = line;
          if (proxyType.equals("SOCKS"))
          {
            VTConsole.print("VT>Enter proxy port(from 1 to 65535, default:1080):");
            line = VTConsole.readLine(true);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            if (line.length() > 0)
            {
              proxyPort = Integer.parseInt(line);
            }
            else
            {
              proxyPort = 1080;
            }
          }
          else if (proxyType.equals("HTTP"))
          {
            VTConsole.print("VT>Enter proxy port(from 1 to 65535, default:8080):");
            line = VTConsole.readLine(true);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            if (line.length() > 0)
            {
              proxyPort = Integer.parseInt(line);
            }
            else
            {
              proxyPort = 8080;
            }
          }
          if (proxyPort > 65535 || proxyPort < 1)
          {
            VTConsole.print("VT>Invalid port!");
            proxyPort = null;
            //useProxyAuthentication = false;
            return false;
          }
          if (proxyPort != null && hostPort != null)
          {
            VTConsole.print("VT>Use authentication for proxy?(Y/N, default:N):");
            line = VTConsole.readLine(true);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            if (line.toUpperCase().startsWith("Y"))
            {
              //useProxyAuthentication = true;
              VTConsole.print("VT>Enter proxy username:");
              line = VTConsole.readLine(false);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return true;
              }
              proxyUser = line;
              VTConsole.print("VT>Enter proxy password:");
              line = VTConsole.readLine(false);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return true;
              }
              proxyPassword = line;
            }
            else
            {
              //useProxyAuthentication = false;
              proxyUser = null;
              proxyPassword = null;
            }
          }
          else
          {
            //useProxyAuthentication = false;
            proxyUser = null;
            proxyPassword = null;
          }
        }
        else
        {
          proxyType = "None";
        }
      }
      VTConsole.print("VT>Repeat current session user and password?(Y/N, default:Y):");
      line = VTConsole.readLine(true);
      if (line == null)
      {
        VTRuntimeExit.exit(0);
        return false;
      }
      else if (skipConfiguration)
      {
        return true;
      }
      if (!line.toUpperCase().startsWith("N"))
      {
        retry = false;
        return true;
      }
      else
      {
        VTConsole.print("VT>Enter session shell(null for default):");
        String shell = VTConsole.readLine(true);
        if (shell == null)
        {
          VTRuntimeExit.exit(0);
        }
        else if (skipConfiguration)
        {
          return true;
        }
        setSessionShell(shell);
        VTConsole.print("VT>Enter session user:");
        String user = VTConsole.readLine(false);
        if (user == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        client.setUser(user);
        VTConsole.print("VT>Enter session password:");
        String password = VTConsole.readLine(false);
        if (password == null)
        {
          VTRuntimeExit.exit(0);
          return false;
        }
        else if (skipConfiguration)
        {
          return true;
        }
        client.setPassword(password);
        retry = false;
      }
      VTConsole.print("VT>Enter session commands:");
      String commands = VTConsole.readLine(true);
      if (commands == null)
      {
        VTRuntimeExit.exit(0);
      }
      else if (skipConfiguration)
      {
        return true;
      }
      setSessionCommands(commands);
      // VTConsole.print("VT>Enter session lines:");
      // String lines = VTConsole.readLine(true);
      // if (lines == null)
      // {
      // VTExit.exit(0);
      // }
      // else if (skipConfiguration)
      // {
      // return true;
      // }
      // setSessionLines(lines);
      retry = false;
      return true;
    }
    catch (NumberFormatException e)
    {
      VTConsole.print("VT>Invalid port!");
      hostPort = null;
      proxyPort = null;
      //useProxyAuthentication = false;
      return false;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  public void run()
  {
    Thread.currentThread().setName(this.getClass().getSimpleName());
    while (running)
    {
      stopConnectionRetryTimeoutThread();
      if (active)
      {
        if (establishConnection(connection, hostAddress, hostPort != null && hostPort > 0 ? hostPort : 6060))
        {
          try
          {
            if (client.getConnectionDialog() != null && client.getConnectionDialog().isVisible())
            {
              client.getConnectionDialog().close();
            }
          }
          catch (Throwable t)
          {
            
          }
          handler.run();
        }
        else
        {
          
        }
      }
      else
      {
        if (listenConnection(connection))
        {
          try
          {
            if (client.getConnectionDialog() != null && client.getConnectionDialog().isVisible())
            {
              client.getConnectionDialog().close();
            }
          }
          catch (Throwable t)
          {
            
          }
          handler.run();
        }
        else
        {
          
        }
      }
      if (running)
      {
        while (!retryConnection())
        {
          stopConnectionRetryTimeoutThread();
        }
        if (skipConfiguration)
        {
          skipConfiguration = false;
        }
      }
      connecting = false;
    }
    
  }
  
  public void addSessionListener(VTClientSessionListener listener)
  {
    listeners.add(listener);
    handler.setSessionListeners(listeners);
  }
  
  public void removeSessionListener(VTClientSessionListener listener)
  {
    listeners.remove(listener);
    handler.setSessionListeners(listeners);
  }
  
  // public String getSessionLines()
  // {
  // return sessionLines;
  // }
  
  // public void setSessionLines(String sessionLines)
  // {
  // this.sessionLines = sessionLines;
  // }
  
  public String getSessionShell()
  {
    return sessionShell;
  }
  
  public void setSessionShell(String sessionShell)
  {
    this.sessionShell = sessionShell;
  }
  
//	public void setConnectedOnce(boolean connectedOnce)
//	{
//		this.connectedOnce = connectedOnce;
//	}
}