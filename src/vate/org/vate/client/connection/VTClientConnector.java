package org.vate.client.connection;

import java.net.Authenticator;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.console.VTConsole;
import org.vate.network.nat.mapping.VTNATSinglePortMappingManagerMKII;

public class VTClientConnector implements Runnable
{
	private volatile boolean active;
	private String hostAddress;
	private Integer hostPort;
	private Integer natPort;
	private String proxyType;
	private String proxyAddress;
	private Integer proxyPort;
	private boolean useProxyAuthentication;
	private String proxyUser;
	private String proxyPassword;
	private String encryptionType;
	private byte[] encryptionKey;
	private String sessionCommands;
	private ServerSocket connectionServerSocket;
	private VTClient client;
	private VTClientConnection connection;
	private VTClientConnectionHandler handler;
	private volatile boolean skipConfiguration;
	//private volatile boolean connectedOnce;
	private volatile boolean timeoutEnabled;
	private VTNATSinglePortMappingManagerMKII portMappingManager;
	private VTConnectionRetryTimeoutTask connectionRetryTimeoutTask = new VTConnectionRetryTimeoutTask();
	private volatile boolean retry = false;
	private volatile boolean dialog = false;
	
	public VTClientConnector(VTClient client)
	{
		this.client = client;
		this.connection = new VTClientConnection();
		this.handler = new VTClientConnectionHandler(client, connection);
		portMappingManager = new VTNATSinglePortMappingManagerMKII(3, 60);
		portMappingManager.start();
	}
	
	private class VTConnectionRetryTimeoutTask implements Runnable
	{
		//private volatile boolean finished = true;
		//private Thread timeoutThread;
		
		public void start()
		{
			timeoutEnabled = true;
			//finished = false;
			//timeoutThread = new Thread(this, getClass().getSimpleName());
			//timeoutThread.start();
			client.getClientThreads().execute(this);
		}
		
		public void run()
		{
			try
			{
				//Thread.currentThread().setName(getClass().getSimpleName());
				//timeoutThread = Thread.currentThread();
				//Thread.sleep(180000);
				if (timeoutEnabled)
				{
					synchronized (this)
					{
						wait(VT.VT_RECONNECTION_TIMEOUT_MILLISECONDS);
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
						//VTConsole.println("");
						VTConsole.interruptReadLine();
					}
					catch (Throwable t)
					{
						
					}
					if (dialog)
					{
						//System.out.println("dialog == true");
						VTConsole.print("VT>Retrying connection with server...");
						//retry = true;
					}
					else
					{
						//System.out.println("dialog == false");
						VTConsole.print("\nVT>Retrying connection with server...");
						//retry = true;
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
			//finished = true;
		}
		
		public void trigger()
		{
			//System.out.println("trigger");
			try
			{
				if (timeoutEnabled)
				{
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
						//VTConsole.println("");
						VTConsole.interruptReadLine();
					}
					catch (Throwable t)
					{
						
					}
					if (dialog)
					{
						VTConsole.print("VT>Retrying connection with server...");
					}
					else
					{
						VTConsole.print("\nVT>Retrying connection with server...");
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
			catch (Throwable t)
			{
				
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
	
	public void setSkipConfiguration(boolean skipConfiguration)
	{
		this.skipConfiguration = skipConfiguration;
	}
	
	public boolean isSkipConfiguration()
	{
		return this.skipConfiguration;
	}
	
	/* public void setRetryOnce(boolean retryOnce) { this.retryOnce = retryOnce;
	 * } */
	
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
			portMappingManager.setPortMapping(hostPort != null && hostPort > 0 ? hostPort : 6060, null, natPort, "TCP", "Variable-Terminal-Port-Mapping");
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
	
	public void setUseProxyAuthentication(boolean useProxyAuthentication)
	{
		this.useProxyAuthentication = useProxyAuthentication;
	}
	
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
	
	public boolean isUseProxyAuthentication()
	{
		return useProxyAuthentication;
	}
	
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
	
	/* public net.sourceforge.jsocks.socks.Proxy buildSocksProxy() { String
	 * proxy_host = proxyAddress; String proxy_port = String.valueOf(proxyPort);
	 * String proxy_user = proxyUser; String proxy_password = proxyPassword; if
	 * (!UseProxyAuthentication) { proxy_user = null; proxy_password = null; }
	 * return net.sourceforge.jsocks.socks.Proxy.buildProxy(proxy_host,
	 * proxy_port, proxy_user, proxy_password); } */
	
	public boolean setServerSocket(String address, Integer port)
	{
		try
		{
			if (connectionServerSocket != null && (connectionServerSocket.isClosed() || !connectionServerSocket.isBound() || connectionServerSocket.getLocalPort() != port))
			{
				connectionServerSocket.close();
				connectionServerSocket = new ServerSocket();
				//connectionServerSocket.setReuseAddress(true);
				if (port != null)
				{
					if (address != null && address.length() > 0)
					{
						connectionServerSocket.bind(new InetSocketAddress(address, port), 1);
					}
					else
					{
						connectionServerSocket.bind(new InetSocketAddress(port), 1);
					}
				}
				else
				{
					if (address != null && address.length() > 0)
					{
						connectionServerSocket.bind(new InetSocketAddress(address, 6060), 1);
					}
					else
					{
						connectionServerSocket.bind(new InetSocketAddress(6060), 1);
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
						connectionServerSocket.bind(new InetSocketAddress(address, port), 1);
					}
					else
					{
						connectionServerSocket.bind(new InetSocketAddress(port), 1);
					}
				}
				else
				{
					if (address != null && address.length() > 0)
					{
						connectionServerSocket.bind(new InetSocketAddress(address, 6060), 1);
					}
					else
					{
						connectionServerSocket.bind(new InetSocketAddress(6060), 1);
					}
				}
			}
			/* if (proxyType.toUpperCase().startsWith("S")) {
			 * net.sourceforge.jsocks.socks.Proxy proxy = buildSocksProxy();
			 * String nullHost = null; connectionServerSocket = new
			 * SocksServerSocket(proxy, nullHost, port); return true; } else {
			 * connectionServerSocket = new ServerSocket();
			 * //vtServerSocket.setPerformancePreferences(1, 3, 2);
			 * connectionServerSocket.bind(new InetSocketAddress(port)); return
			 * true; } */
			return true;
		}
		catch (Throwable e)
		{
			// VTTerminal.print("VT>TCP port [" + port + "] is already in
			// use!\n");
			// e.printStackTrace();
			VTConsole.print("\nVT>Listening to connection in port [" + port + "] failed!");
			// return false;
		}
//		try
//		{
//			Thread.sleep(1000);
//		}
//		catch (Throwable e1)
//		{
//			
//		}
		return false;
	}
	
	public void resetSockets(VTClientConnection connection) throws SocketException
	{
		if (useProxyAuthentication)
		{
			Authenticator.setDefault(new VTClientConnectionProxyAuthenticator(this));
		}
		else
		{
			Authenticator.setDefault(null);
		}
		if (proxyType == null)
		{
			connection.setConnectionSocket(new Socket());
		}
		else if (proxyType.toUpperCase().startsWith("H"))
		{
			connection.setConnectionSocket(new Socket(new Proxy(Proxy.Type.HTTP, new InetSocketAddress(proxyAddress, proxyPort != null && proxyPort > 0 ? proxyPort : 8080))));
		}
		else if (proxyType.toUpperCase().startsWith("S"))
		{
			connection.setConnectionSocket(new Socket(new Proxy(Proxy.Type.SOCKS, new InetSocketAddress(proxyAddress, proxyPort != null && proxyPort > 0 ? proxyPort : 1080))));
		}
		else
		{
			connection.setConnectionSocket(new Socket());
		}
		//connection.getConnectionSocket().setReuseAddress(true);
	}
	
	public boolean listenConnection(VTClientConnection connection)
	{
		if (retry)
		{
			VTConsole.print("\nVT>Listening to connection with server, interrupt with ENTER...");
		}
		else
		{
			retry = true;
			VTConsole.print("VT>Listening to connection with server, interrupt with ENTER...");
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
				portMappingManager.setPortMapping(hostPort != null && hostPort > 0 ? hostPort : 6060, null, natPort, "TCP", "Variable-Terminal-Port-Mapping");
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
			connection.setConnectionSocket(connectionServerSocket.accept());
			connection.getConnectionSocket().setTcpNoDelay(true);
			connection.getConnectionSocket().setKeepAlive(true);
			connection.getConnectionSocket().setSoLinger(true, 0);
			//connection.getConnectionSocket().setSoLinger(false, 5000);
			connection.getConnectionSocket().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
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
			/* else if (encryptionType.toUpperCase().startsWith("H")) {
			 * connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC128); }
			 * else if (encryptionType.toUpperCase().startsWith("G")) {
			 * connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN128)
			 * ; } */
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
			connectionServerSocket.close();
			VTConsole.print("\nVT>Connection with server established!");
			return true;
		}
		catch (Throwable e)
		{
			VTConsole.print("\nVT>Connection with server interrupted!");
			connection.closeSockets();
		}
//		try
//		{
//			Thread.sleep(1000);
//		}
//		catch (Throwable e1)
//		{
//			
//		}
		return false;
	}
	
	public boolean establishConnection(VTClientConnection connection, String address, Integer port)
	{
		if (port == null)
		{
			port = 6060;
		}
		if (retry)
		{
			VTConsole.print("\nVT>Establishing connection with server...");
		}
		else
		{
			retry = true;
			VTConsole.print("VT>Establishing connection with server...");
		}
		try
		{
			resetSockets(connection);
			portMappingManager.deletePortMapping();
			InetSocketAddress socketAddress = null;
			if (proxyType.toUpperCase().startsWith("H")
			|| proxyType.toUpperCase().startsWith("S"))
			{
				socketAddress = InetSocketAddress.createUnresolved(address, port);
			}
			else
			{
				socketAddress = new InetSocketAddress(address, port);
			}
			
			// connection.getShellSocket().setPerformancePreferences(1, 3, 2);
			connection.getConnectionSocket().connect(socketAddress);
			connection.getConnectionSocket().setTcpNoDelay(true);
			connection.getConnectionSocket().setKeepAlive(true);
			connection.getConnectionSocket().setSoLinger(true, 0);
			//connection.getConnectionSocket().setSoLinger(false, 5000);
			connection.getConnectionSocket().setSoTimeout(VT.VT_CONNECTION_DATA_TIMEOUT_MILLISECONDS);
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
			/* else if (encryptionType.toUpperCase().startsWith("H")) {
			 * connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_HC128); }
			 * else if (encryptionType.toUpperCase().startsWith("G")) {
			 * connection.setEncryptionType(VT.VT_CONNECTION_ENCRYPT_GRAIN128)
			 * ; } */
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
			VTConsole.print("\nVT>Connection with server failed!");
		}
//		try
//		{
//			Thread.sleep(1000);
//		}
//		catch (Throwable e1)
//		{
//			
//		}
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
	
	private void stopConnectionRetryTimeoutThread()
	{
		connectionRetryTimeoutTask.stop();
	}
	
	public boolean retryConnection()
	{
		dialog = false;
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
				System.exit(0);
				return false;
			}
			else if (skipConfiguration)
			{
				return true;
			}
			if (line.toUpperCase().startsWith("N"))
			{
				System.exit(0);
				return false;
			}
			if (client.getConnectionDialog() != null)
			{
				dialog = true;
				client.getConnectionDialog().open();
			}
			if (skipConfiguration)
			{
				//System.out.println("skipConfiguration");
				skipConfiguration = false;
				return true;
			}
			dialog = false;
			VTConsole.print("VT>Repeat current connection settings?(Y/N, default:Y):");
			line = VTConsole.readLine(true);
			if (line == null)
			{
				System.exit(0);
				return false;
			}
			else if (skipConfiguration)
			{
				return true;
			}
			if (!line.toUpperCase().startsWith("N"))
			{
				VTConsole.print("VT>Repeat current authentication login and password?(Y/N, default:Y):");
				line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
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
					VTConsole.print("VT>Enter the authentication login:");
					String login = VTConsole.readLine(false);
					if (login == null)
					{
						System.exit(0);
						return false;
					}
					else if (skipConfiguration)
					{
						return true;
					}
					client.setLogin(login);
					VTConsole.print("VT>Enter the authentication password:");
					String password = VTConsole.readLine(false);
					if (password == null)
					{
						System.exit(0);
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
				VTConsole.print("VT>Enter the settings file(if available):");
				try
				{
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
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
			VTConsole.print("VT>Enter the connection mode(active as A or passive as P, default:A):");
			line = VTConsole.readLine(true);
			if (line == null)
			{
				System.exit(0);
				return false;
			}
			else if (skipConfiguration)
			{
				return true;
			}
			if (line.toUpperCase().startsWith("P"))
			{
				active = false;
				VTConsole.print("VT>Enter the host address(default:any):");
				line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
					return false;
				}
				else if (skipConfiguration)
				{
					return true;
				}
				hostAddress = line;
				VTConsole.print("VT>Enter the listening port(from 1 to 65535, default:6060):");
				line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
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
					VTConsole.print("VT>Use nat port mapping in connection?(Y/N, default:N):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return true;
					}
					if (line.toUpperCase().startsWith("Y"))
					{
						VTConsole.print("VT>Enter the nat port(from 1 to 65535, default:" + hostPort + "):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
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
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return true;
					}
					if (line.toUpperCase().startsWith("Y"))
					{
						VTConsole.print("VT>Enter the encryption type(RC4 as R, AES as A, default:R):");
						line = VTConsole.readLine(false);
						if (line == null)
						{
							System.exit(0);
						}
						else if (skipConfiguration)
						{
							return true;
						}
						if (line.toUpperCase().startsWith("A"))
						{
							encryptionType = "AES";
						}
						/* else if (line.toUpperCase().startsWith("H")) {
						 * encryptionType = "H"; } */
						else
						{
							encryptionType = "RC4";
						}
						VTConsole.print("VT>Enter the encryption password:");
						line = VTConsole.readLine(false);
						if (line == null)
						{
							System.exit(0);
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
					/* VTTerminal.
					 * print("VT>Use SOCKS proxy to connect?(Y/N, default:N):"
					 * ); line = VTTerminal.readLine(true); if (line == null) {
					 * System.exit(0); } if (line.toUpperCase().startsWith("Y"))
					 * { proxyType = "SOCKS"; VTTerminal.
					 * print("VT>Enter the proxy host address(default:localhost):"
					 * ); line = VTTerminal.readLine(true); if (line == null) {
					 * System.exit(0); } proxyAddress = line; if
					 * (proxyType.equals("SOCKS")) { VTTerminal.
					 * print("VT>Enter the proxy port(from 1 to 65535, default:1080):"
					 * ); line = VTTerminal.readLine(true); if (line == null) {
					 * System.exit(0); } if (line.length() > 0) { proxyPort =
					 * Integer.parseInt(line); } else { proxyPort = 1080; } } if
					 * (proxyPort > 65535 || proxyPort < 1) {
					 * VTTerminal.print("VT>Invalid port!"); proxyPort = null;
					 * UseProxyAuthentication = false; return false; } if
					 * (proxyPort != null && port != null) { VTTerminal.
					 * print("VT>Use authentication for proxy?(Y/N, default:N):"
					 * ); line = VTTerminal.readLine(true); if (line == null) {
					 * System.exit(0); } if (line.toUpperCase().startsWith("Y"))
					 * { UseProxyAuthentication = true;
					 * VTTerminal.print("VT>Enter the proxy username:"); line
					 * = VTTerminal.readLine(true); if (line == null) {
					 * System.exit(0); } proxyUser = line;
					 * VTTerminal.print("VT>Enter the proxy password:"); line
					 * = VTTerminal.readLine(true); if (line == null) {
					 * System.exit(0); } proxyPassword = line; } else {
					 * UseProxyAuthentication = false; } } else {
					 * UseProxyAuthentication = false; } } else { proxyType =
					 * "None"; } */
				}
			}
			else
			{
				active = true;
				VTConsole.print("VT>Enter the host address(default:any):");
				line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
					return false;
				}
				else if (skipConfiguration)
				{
					return true;
				}
				hostAddress = line;
				VTConsole.print("VT>Enter the host port(from 1 to 65535, default:6060):");
				line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
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
					System.exit(0);
				}
				else if (skipConfiguration)
				{
					return true;
				}
				if (line.toUpperCase().startsWith("Y"))
				{
					VTConsole.print("VT>Enter the encryption type(RC4 as R, AES as A, default:R):");
					line = VTConsole.readLine(false);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return true;
					}
					if (line.toUpperCase().startsWith("A"))
					{
						encryptionType = "AES";
					}
					/* else if (line.toUpperCase().startsWith("H")) {
					 * encryptionType = "H"; } */
					else
					{
						encryptionType = "RC4";
					}
					VTConsole.print("VT>Enter the encryption password:");
					line = VTConsole.readLine(false);
					if (line == null)
					{
						System.exit(0);
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
					System.exit(0);
				}
				else if (skipConfiguration)
				{
					return true;
				}
				if (line.toUpperCase().startsWith("Y"))
				{
					VTConsole.print("VT>Enter the proxy type(SOCKS as S, HTTP as H, default:S):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
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
					VTConsole.print("VT>Enter the proxy host address(default:any):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return true;
					}
					proxyAddress = line;
					if (proxyType.equals("SOCKS"))
					{
						VTConsole.print("VT>Enter the proxy port(from 1 to 65535, default:1080):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
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
						VTConsole.print("VT>Enter the proxy port(from 1 to 65535, default:8080):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
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
						useProxyAuthentication = false;
						return false;
					}
					if (proxyPort != null && hostPort != null)
					{
						VTConsole.print("VT>Use authentication for proxy?(Y/N, default:N):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
						}
						else if (skipConfiguration)
						{
							return true;
						}
						if (line.toUpperCase().startsWith("Y"))
						{
							useProxyAuthentication = true;
							VTConsole.print("VT>Enter the proxy username:");
							line = VTConsole.readLine(false);
							if (line == null)
							{
								System.exit(0);
							}
							else if (skipConfiguration)
							{
								return true;
							}
							proxyUser = line;
							VTConsole.print("VT>Enter the proxy password:");
							line = VTConsole.readLine(false);
							if (line == null)
							{
								System.exit(0);
							}
							else if (skipConfiguration)
							{
								return true;
							}
							proxyPassword = line;
						}
						else
						{
							useProxyAuthentication = false;
						}
					}
					else
					{
						useProxyAuthentication = false;
					}
				}
				else
				{
					proxyType = "None";
				}
			}
			VTConsole.print("VT>Repeat current authentication login and password?(Y/N, default:Y):");
			line = VTConsole.readLine(true);
			if (line == null)
			{
				System.exit(0);
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
				VTConsole.print("VT>Enter the authentication login:");
				String login = VTConsole.readLine(false);
				if (login == null)
				{
					System.exit(0);
					return false;
				}
				else if (skipConfiguration)
				{
					return true;
				}
				client.setLogin(login);
				VTConsole.print("VT>Enter the authentication password:");
				String password = VTConsole.readLine(false);
				if (password == null)
				{
					System.exit(0);
					return false;
				}
				else if (skipConfiguration)
				{
					return true;
				}
				client.setPassword(password);
				retry = false;
			}
			VTConsole.print("VT>Enter the session commands:");
			String commands = VTConsole.readLine(true);
			if (commands == null)
			{
				System.exit(0);
			}
			else if (skipConfiguration)
			{
				return true;
			}
			setSessionCommands(commands);
			retry = false;
			return true;
		}
		catch (NumberFormatException e)
		{
			VTConsole.print("VT>Invalid port!");
			hostPort = null;
			proxyPort = null;
			useProxyAuthentication = false;
			return false;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
	
	public void run()
	{
		while (true)
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
			}
			while (!retryConnection())
			{
				stopConnectionRetryTimeoutThread();
			}
			if (skipConfiguration)
			{
				skipConfiguration = false;
			}
//			try
//			{
//				Thread.sleep(1000);
//			}
//			catch (Throwable e1)
//			{
//				
//			}
		}
	}
	
//	public void setConnectedOnce(boolean connectedOnce)
//	{
//		this.connectedOnce = connectedOnce;
//	}
}