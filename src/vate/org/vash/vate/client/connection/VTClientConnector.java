package org.vash.vate.client.connection;

import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.console.VTConsole;
import org.vash.vate.nat.mapping.VTNATPortMappingResultNotify;
import org.vash.vate.nat.mapping.VTNATSinglePortMappingManagerMKII;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.socket.proxy.VTProxy;
import org.vash.vate.socket.proxy.VTProxy.VTProxyType;

public class VTClientConnector implements Runnable
{
  private boolean active;
  //private boolean connecting = false;
  private boolean running = true;
  private boolean retry = false;
  private boolean dialog = false;
  private boolean skipConfiguration;
  private boolean timeoutEnabled;
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
  private VTNATSinglePortMappingManagerMKII portMappingManager;
  private VTConnectionRetryTimeoutTask connectionRetryTimeoutTask = new VTConnectionRetryTimeoutTask();
  private VTClientConnectorNATPortMappingResultNotify natNotify = new VTClientConnectorNATPortMappingResultNotify();
  private List<VTClientSessionListener> listeners = new ArrayList<VTClientSessionListener>();
  private final VTBlake3SecureRandom secureRandom;
  
  public VTClientConnector(VTClient client, VTBlake3SecureRandom secureRandom)
  {
    this.client = client;
    this.secureRandom = secureRandom;
    this.connection = new VTClientConnection(client.getExecutorService());
    this.handler = new VTClientConnectionHandler(client, connection);
    portMappingManager = new VTNATSinglePortMappingManagerMKII(3, 300, client.getExecutorService());
    portMappingManager.start();
  }
  
  private class VTClientConnectorNATPortMappingResultNotify implements VTNATPortMappingResultNotify
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
//      if (externalHosts.size() > 0 && !active && connecting)
//      {
//        VTConsole.print("\nVT>Configured NAT hosts:" + natHosts);
//      }
    }
  }
  
  private class VTConnectionRetryTimeoutTask implements Runnable
  {
    // private Thread timeoutThread;
    
    public void start()
    {
      timeoutEnabled = true;
      // finished = false;
      // timeoutThread = new Thread(this, getClass().getSimpleName());
      // timeoutThread.start();
      client.getExecutorService().execute(this);
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
            wait(client.getReconnectTimeout());
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
          client.closeConnectionDialog();
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
      VTConsole.print("\nVT>Awaiting connection in port [" + port + "] failed!");
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
  
  public void resetSockets(VTClientConnection connection) throws SocketException
  {
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
      Socket socket = VTProxy.next(null, 0, new VTProxy(VTProxyType.GLOBAL, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("D") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, 0, new VTProxy(VTProxyType.DIRECT, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("H") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, 0, new VTProxy(VTProxyType.HTTP, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("S") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, 0, new VTProxy(VTProxyType.SOCKS, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else if (proxyType.toUpperCase().startsWith("A") && proxyAddress != null && proxyPort != null)
    {
      Socket socket = VTProxy.next(null, 0, new VTProxy(VTProxyType.ANY, proxyAddress, proxyPort, proxyUser, proxyPassword));
      
      connection.setConnectionSocket(socket);
    }
    else
    {
      connection.setConnectionSocket(new Socket());
    }
    // connection.getConnectionSocket().setReuseAddress(true);
  }
  
  public boolean listenConnection(VTClientConnection connection)
  {
    if ((!retry || dialog))
    {
      VTConsole.print("VT>Awaiting connection with server, interrupt with enter...");
      retry = true;
    }
    else
    {
      VTConsole.print("\nVT>Awaiting connection with server, interrupt with enter...");
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
      VTConsole.createInterruptibleReadline(false, client.getExecutorService(), new Runnable()
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
//      connecting = true;
      connection.setConnectionSocket(connectionServerSocket.accept());
      connection.getConnectionSocket().setTcpNoDelay(true);
      //connection.getConnectionSocket().setSoLinger(true, 0);
      //connection.getConnectionSocket().setKeepAlive(true);
      connection.getConnectionSocket().setSoTimeout(client.getDataTimeout());
//      connecting = false;
      if (encryptionType == null)
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_NONE);
      }
      else if (encryptionType.toUpperCase().startsWith("Z"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_ZUC);
      }
      else if (encryptionType.toUpperCase().startsWith("V"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_VMPC);
      }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_SALSA);
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_HC);
      }
      else if (encryptionType.toUpperCase().startsWith("I"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_ISAAC);
      }
      else
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_NONE);
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
//      connecting = true;
      VTProxy.connect(address, port, 0, connection.getConnectionSocket());
      connection.getConnectionSocket().setTcpNoDelay(true);
      //connection.getConnectionSocket().setSoLinger(true, 0);
      //connection.getConnectionSocket().setKeepAlive(true);
      connection.getConnectionSocket().setSoTimeout(client.getDataTimeout());
//      connecting = false;
      if (encryptionType == null)
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_NONE);
      }
      else if (encryptionType.toUpperCase().startsWith("Z"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_ZUC);
      }
      else if (encryptionType.toUpperCase().startsWith("V"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_VMPC);
      }
      else if (encryptionType.toUpperCase().startsWith("S"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_SALSA);
      }
      else if (encryptionType.toUpperCase().startsWith("H"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_HC);
      }
      else if (encryptionType.toUpperCase().startsWith("I"))
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_ISAAC);
      }
      else
      {
        connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPTION_NONE);
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
          wait(VT.VT_DAEMON_RECONNECT_TIMEOUT_MILLISECONDS);
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
      if (client.hasConnectionDialog())
      {
        // dialogLine = true;
        dialog = true;
        // retry = false;
        client.openConnectionDialog();
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
            VTConsole.print("VT>Enter encryption type(ISAAC(I)/VMPC(V)/SALSA(S)/HC(H)/ZUC(Z)):");
            line = VTConsole.readLine(false);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return true;
            }
            encryptionType = "ISAAC";
            if (line.toUpperCase().startsWith("Z"))
            {
              encryptionType = "ZUC";
            }
            if (line.toUpperCase().startsWith("S"))
            {
              encryptionType = "SALSA";
            }
            if (line.toUpperCase().startsWith("H"))
            {
              encryptionType = "HC";
            }
            if (line.toUpperCase().startsWith("V"))
            {
              encryptionType = "VMPC";
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
            encryptionType = "NONE";
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
          VTConsole.print("VT>Enter encryption type(ISAAC(I)/VMPC(V)/SALSA(S)/HC(H)/ZUC(Z)):");
          line = VTConsole.readLine(false);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          encryptionType = "ISAAC";
          if (line.toUpperCase().startsWith("Z"))
          {
            encryptionType = "ZUC";
          }
          if (line.toUpperCase().startsWith("S"))
          {
            encryptionType = "SALSA";
          }
          if (line.toUpperCase().startsWith("H"))
          {
            encryptionType = "HC";
          }
          if (line.toUpperCase().startsWith("V"))
          {
            encryptionType = "VMPC";
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
          encryptionType = "NONE";
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
          VTConsole.print("VT>Enter proxy type(DIRECT as D, SOCKS as S, HTTP as H, ANY as A, default:A):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return true;
          }
          if (line.toUpperCase().startsWith("D"))
          {
            proxyType = "DIRECT";
          }
          else if (line.toUpperCase().startsWith("H"))
          {
            proxyType = "HTTP";
          }
          else if (line.toUpperCase().startsWith("S"))
          {
            proxyType = "SOCKS";
          }
          else
          {
            proxyType = "ANY";
          }
          if ("ANY".equals(proxyType) || "HTTP".equals(proxyType) || "SOCKS".equals(proxyType))
          {
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
          }
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
          else if (proxyType.equals("HTTP") || proxyType.equals("ANY"))
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
          if (("ANY".equals(proxyType) || "HTTP".equals(proxyType) || "SOCKS".equals(proxyType)) && proxyPort != null && hostPort != null)
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
          proxyType = "NONE";
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
      byte[] secureSeed = new byte[64];
      secureRandom.nextBytes(secureSeed);
      connection.setSecureRandomSeed(secureSeed);
      if (active)
      {
        if (establishConnection(connection, hostAddress, hostPort != null && hostPort > 0 ? hostPort : 6060))
        {
          client.closeConnectionDialog();
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
          client.closeConnectionDialog();
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
//      connecting = false;
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
}