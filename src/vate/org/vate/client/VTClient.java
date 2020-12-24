package org.vate.client;

import java.awt.Toolkit;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.imageio.ImageIO;

import org.vate.VT;
import org.vate.audio.VTAudioSystem;
import org.vate.client.connection.VTClientConnector;
import org.vate.client.console.remote.VTClientRemoteGraphicalConsoleMenuBar;
import org.vate.client.dialog.VTClientConfigurationDialog;
import org.vate.console.VTConsole;
import org.vate.exception.VTUncaughtExceptionHandler;
import org.vate.help.VTHelpManager;
import org.vate.nativeutils.VTNativeUtils;
import org.vate.network.ssl.SSLVerificationDisabler;
import org.vate.parser.VTConfigurationProperties;
import org.vate.parser.VTPropertiesBuilder;

public class VTClient implements Runnable
{
	private boolean active = true;
	private String hostAddress = "";
	private Integer hostPort = null;
	private Integer natPort = null;
	private String proxyType = "None";
	private String proxyAddress = "";
	private Integer proxyPort = null;
	private boolean useProxyAuthentication = false;
	private String proxyUser = "";
	private String proxyPassword = "";
	private String encryptionType = "None";
	private byte[] encryptionKey = new byte[] {};
	private String authenticationLogin = "";
	private String authenticationPassword = "";
	private String sessionCommands = "";
	private final String vtURL = System.getenv("VT_PATH");
	private final Runtime runtime = Runtime.getRuntime();
	private File clientSettingsFile;
	private VTConfigurationProperties fileClientSettings;
	private InputStream clientSettingsReader;
	private VTClientConnector clientConnector;
	private VTClientRemoteGraphicalConsoleMenuBar inputMenuBar;
	private VTAudioSystem audioSystem;
	private VTClientConfigurationDialog connectionDialog;
	private ExecutorService threads;
	private volatile boolean skipConfiguration;
	private volatile boolean retry = false;
	private volatile boolean manual = false;
	private static final String VT_CLIENT_SETTINGS_COMMENTS = 
	"Variable-Terminal client settings file, supports UTF-8\r\n" + 
	"#vate.client.connection.mode      values: A(active, default) or P(passive)\r\n" + 
	"#vate.client.proxy.type           values: N(none, default), S(SOCKS) or H(HTTP)\r\n" + 
	"#vate.client.proxy.authentication values: D(disabled, default) or E(enabled)\r\n" +
	"#vate.client.encryption.type      values: N(none, default), R(RC4) or A(AES)\r\n" +
	"#vate.client.session.commands     format: cmd1*;cmd2*;cmd3*;...";
	
	static
	{
		ImageIO.setUseCache(false);
		VTHelpManager.initialize();
		SSLVerificationDisabler.install();
		com.github.luben.zstd.util.Native.load();
	}
	
	public VTClient()
	{
		//VTClientRemoteConsoleCommandSelector.initialize();
		this.threads = Executors.newCachedThreadPool(new ThreadFactory()
		{
			public Thread newThread(Runnable r)
			{
				Thread created = new Thread(null, r, r.getClass().getSimpleName());
				created.setDaemon(true);
				return created;
			}
		});
		this.audioSystem = new VTAudioSystem(threads);
		try
		{
			Toolkit.getDefaultToolkit().setDynamicLayout(false);
		}
		catch (Throwable t)
		{
			
		}
		loadClientSettingsFile();
	}
	
	public ExecutorService getClientThreads()
	{
		return threads;
	}
	
	public void setSkipConfiguration(boolean skipConfiguration)
	{
		this.skipConfiguration = skipConfiguration;
		// System.out.println("skipConfiguration = " + skipConfiguration);
	}
	
	public void setManual(boolean manual)
	{
		this.manual = manual;
	}
	
	public boolean isManual()
	{
		return manual;
	}
	
	public VTClientConnector getClientConnector()
	{
		return clientConnector;
	}
	
	public VTAudioSystem getAudioSystem()
	{
		return audioSystem;
	}
	
	public void setActive(boolean active)
	{
		this.active = active;
	}
	
	/* public String getAddress() { return address; } */
	
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
	
	public String getLogin()
	{
		return authenticationLogin;
	}
	
	public void setLogin(String login)
	{
		this.authenticationLogin = login;
	}
	
	public String getPassword()
	{
		return authenticationPassword;
	}
	
	public void setPassword(String password)
	{
		this.authenticationPassword = password;
	}
	
	public String getSessionCommands()
	{
		return sessionCommands;
	}
	
	public void setSessionCommands(String sessionCommands)
	{
		this.sessionCommands = sessionCommands;
	}
	
	/* public MessageDigest getSha256Digester() { return sha256Digester; } */
	
	/* public SecureRandom getSecureRandom() { return secureRandom; } */
	
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
	
	public String getVTURL()
	{
		return vtURL;
	}
	
	public void setClientConnector(VTClientConnector clientConnector)
	{
		this.clientConnector = clientConnector;
	}
	
	public void setInputMenuBar(VTClientRemoteGraphicalConsoleMenuBar inputMenuBar)
	{
		this.inputMenuBar = inputMenuBar;
	}
	
	public void setAudioSystem(VTAudioSystem audioSystem)
	{
		this.audioSystem = audioSystem;
	}
	
	public Runtime getRuntime()
	{
		return runtime;
	}
	
	public VTClientRemoteGraphicalConsoleMenuBar getInputMenuBar()
	{
		return inputMenuBar;
	}
	
	public VTClientConfigurationDialog getConnectionDialog()
	{
		return connectionDialog;
	}
	
	public void saveClientSettingsFile(String settingsFile) throws Exception
	{
		loadFromConnectorToClient();
		if (vtURL != null)
		{
			clientSettingsFile = new File(vtURL, settingsFile);
			if (!clientSettingsFile.exists())
			{
				clientSettingsFile = new File(settingsFile);
			}
		}
		else
		{
			clientSettingsFile = new File(settingsFile);
		}
		
		if (fileClientSettings == null)
		{
			fileClientSettings = new VTConfigurationProperties();
		}
		
		fileClientSettings.clear();
		fileClientSettings.setProperty("vate.client.connection.mode", active ? "Active" : "Passive");
		fileClientSettings.setProperty("vate.client.connection.port", hostPort != null ? String.valueOf(hostPort) : "");
		fileClientSettings.setProperty("vate.client.connection.host", hostAddress);
		fileClientSettings.setProperty("vate.client.connection.nat.port", natPort != null ? String.valueOf(natPort) : "");
		fileClientSettings.setProperty("vate.client.connection.login", authenticationLogin);
		fileClientSettings.setProperty("vate.client.connection.password", authenticationPassword);
		fileClientSettings.setProperty("vate.client.encryption.type", encryptionType);
		fileClientSettings.setProperty("vate.client.encryption.password", new String(encryptionKey, "UTF-8"));
		fileClientSettings.setProperty("vate.client.proxy.type", proxyType);
		fileClientSettings.setProperty("vate.client.proxy.host", proxyAddress);
		fileClientSettings.setProperty("vate.client.proxy.port", proxyPort != null ? String.valueOf(proxyPort) : "");
		fileClientSettings.setProperty("vate.client.proxy.authentication", useProxyAuthentication ? "Enabled" : "Disabled");
		fileClientSettings.setProperty("vate.client.proxy.user", proxyUser);
		fileClientSettings.setProperty("vate.client.proxy.password", proxyPassword);
		fileClientSettings.setProperty("vate.client.session.commands", sessionCommands);
		
		FileOutputStream out = new FileOutputStream(settingsFile);
		VTPropertiesBuilder.saveProperties(out, fileClientSettings, VT_CLIENT_SETTINGS_COMMENTS, "UTF-8");
		out.flush();
		out.close();
	}
	
	public void loadClientSettingsFile(String settingsFile) throws Exception
	{
		loadFromConnectorToClient();
		if (vtURL != null)
		{
			clientSettingsFile = new File(vtURL, settingsFile);
			if (!clientSettingsFile.exists())
			{
				clientSettingsFile = new File(settingsFile);
			}
		}
		else
		{
			clientSettingsFile = new File(settingsFile);
		}
		clientSettingsReader = new FileInputStream(clientSettingsFile);
		fileClientSettings = VTPropertiesBuilder.loadProperties(clientSettingsReader, "UTF-8");
		// rawSecuritySettings.load(securitySettingsReader);
		clientSettingsReader.close();
		
		if (fileClientSettings.getProperty("vate.client.connection.mode") != null)
		{
			try
			{
				String mode = fileClientSettings.getProperty("vate.client.connection.mode");
				if (mode.toUpperCase().startsWith("P"))
				{
					active = false;
				}
				else
				{
					active = true;
				}
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.connection.port") != null)
		{
			try
			{
				int filePort = Integer.parseInt(fileClientSettings.getProperty("vate.client.connection.port"));
				if (filePort > 0 && filePort < 65536)
				{
					hostPort = filePort;
				}
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.connection.host") != null)
		{
			try
			{
				hostAddress = fileClientSettings.getProperty("vate.client.connection.host", hostAddress);
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.connection.login") != null)
		{
			try
			{
				authenticationLogin = fileClientSettings.getProperty("vate.client.connection.login", authenticationLogin);
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.connection.password") != null)
		{
			try
			{
				authenticationPassword = fileClientSettings.getProperty("vate.client.connection.password", authenticationPassword);
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.connection.nat.port") != null)
		{
			try
			{
				int fileNatPort = Integer.parseInt(fileClientSettings.getProperty("vate.client.connection.nat.port"));
				if (fileNatPort > 0 && fileNatPort < 65536)
				{
					natPort = fileNatPort;
				}
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.encryption.type") != null)
		{
			try
			{
				encryptionType = fileClientSettings.getProperty("vate.client.encryption.type", encryptionType);
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.encryption.password") != null)
		{
			try
			{
				encryptionKey = fileClientSettings.getProperty("vate.client.encryption.password", "").getBytes("UTF-8");
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.proxy.type") != null)
		{
			try
			{
				proxyType = fileClientSettings.getProperty("vate.client.proxy.type", proxyType);
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.proxy.host") != null)
		{
			try
			{
				proxyAddress = fileClientSettings.getProperty("vate.client.proxy.host", proxyAddress);
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.proxy.port") != null)
		{
			try
			{
				int fileProxyPort = Integer.parseInt(fileClientSettings.getProperty("vate.client.proxy.port"));
				if (fileProxyPort > 0 && fileProxyPort < 65536)
				{
					proxyPort = fileProxyPort;
				}
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.proxy.authentication") != null)
		{
			try
			{
				if (fileClientSettings.getProperty("vate.client.proxy.authentication").toUpperCase().startsWith("E"))
				{
					useProxyAuthentication = true;
				}
				else
				{
					useProxyAuthentication = false;
				}
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.proxy.user") != null)
		{
			try
			{
				proxyUser = fileClientSettings.getProperty("vate.client.proxy.user");
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.proxy.password") != null)
		{
			try
			{
				proxyPassword = fileClientSettings.getProperty("vate.client.proxy.password");
			}
			catch (Throwable e)
			{
				
			}
		}
		
		if (fileClientSettings.getProperty("vate.client.session.commands") != null)
		{
			try
			{
				sessionCommands = fileClientSettings.getProperty("vate.client.session.commands");
			}
			catch (Throwable e)
			{
				
			}
		}
		
		saveFromClientToConnector();
	}
	
	private void loadClientSettingsFile()
	{
		if (fileClientSettings != null)
		{
			return;
		}
		try
		{
			if (vtURL != null)
			{
				clientSettingsFile = new File(vtURL, "variable-terminal-client.properties");
				if (!clientSettingsFile.exists())
				{
					clientSettingsFile = new File("variable-terminal-client.properties");
				}
			}
			else
			{
				clientSettingsFile = new File("variable-terminal-client.properties");
			}
			clientSettingsReader = new FileInputStream(clientSettingsFile);
			fileClientSettings = VTPropertiesBuilder.loadProperties(clientSettingsReader, "UTF-8");
			// rawSecuritySettings.load(securitySettingsReader);
			clientSettingsReader.close();
			
			if (fileClientSettings.getProperty("vate.client.connection.mode") != null)
			{
				try
				{
					String mode = fileClientSettings.getProperty("vate.client.connection.mode");
					if (mode.toUpperCase().startsWith("P"))
					{
						active = false;
					}
					else
					{
						active = true;
					}
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.connection.port") != null)
			{
				try
				{
					int filePort = Integer.parseInt(fileClientSettings.getProperty("vate.client.connection.port"));
					if (filePort > 0 && filePort < 65536)
					{
						hostPort = filePort;
					}
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.connection.host") != null)
			{
				try
				{
					hostAddress = fileClientSettings.getProperty("vate.client.connection.host", hostAddress);
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.connection.login") != null)
			{
				try
				{
					authenticationLogin = fileClientSettings.getProperty("vate.client.connection.login", authenticationLogin);
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.connection.password") != null)
			{
				try
				{
					authenticationPassword = fileClientSettings.getProperty("vate.client.connection.password", authenticationPassword);
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.connection.nat.port") != null)
			{
				try
				{
					int fileNatPort = Integer.parseInt(fileClientSettings.getProperty("vate.client.connection.nat.port"));
					if (fileNatPort > 0 && fileNatPort < 65536)
					{
						natPort = fileNatPort;
					}
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.encryption.type") != null)
			{
				try
				{
					encryptionType = fileClientSettings.getProperty("vate.client.encryption.type", encryptionType);
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.encryption.password") != null)
			{
				try
				{
					encryptionKey = fileClientSettings.getProperty("vate.client.encryption.password", "").getBytes("UTF-8");
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.proxy.type") != null)
			{
				try
				{
					proxyType = fileClientSettings.getProperty("vate.client.proxy.type", proxyType);
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.proxy.host") != null)
			{
				try
				{
					proxyAddress = fileClientSettings.getProperty("vate.client.proxy.host", proxyAddress);
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.proxy.port") != null)
			{
				try
				{
					int fileProxyPort = Integer.parseInt(fileClientSettings.getProperty("vate.client.proxy.port"));
					if (fileProxyPort > 0 && fileProxyPort < 65536)
					{
						proxyPort = fileProxyPort;
					}
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.proxy.authentication") != null)
			{
				try
				{
					if (fileClientSettings.getProperty("vate.client.proxy.authentication").toUpperCase().startsWith("E"))
					{
						useProxyAuthentication = true;
					}
					else
					{
						useProxyAuthentication = false;
					}
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.proxy.user") != null)
			{
				try
				{
					proxyUser = fileClientSettings.getProperty("vate.client.proxy.user");
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.proxy.password") != null)
			{
				try
				{
					proxyPassword = fileClientSettings.getProperty("vate.client.proxy.password");
				}
				catch (Throwable e)
				{
					
				}
			}
			
			if (fileClientSettings.getProperty("vate.client.session.commands") != null)
			{
				try
				{
					sessionCommands = fileClientSettings.getProperty("vate.client.session.commands");
				}
				catch (Throwable e)
				{
					
				}
			}
		}
		catch (Throwable e)
		{
			
		}
	}
	
	private void configure()
	{
		while ((active && (hostAddress == null || hostPort == null)) || (!active && hostPort == null))
		{
			if (skipConfiguration)
			{
				skipConfiguration = false;
				return;
			}
			if (retry)
			{
				VTConsole.print("\nVT>Retry connection with server?(Y/N, default:N):");
				try
				{
					String line = VTConsole.readLine(true);
					if (line == null || !line.toUpperCase().startsWith("Y"))
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
					}
				}
				catch (Throwable e)
				{
					System.exit(0);
				}
			}
			else
			{
				VTConsole.print("VT>Press enter to start client:");
				//VTConsole.print("\nVT>Press enter to try connecting with server");
				try
				{
					if (inputMenuBar != null)
					{
						inputMenuBar.setEnabledDialogMenu(false);
					}
					VTConsole.readLine(true);
					
					if (inputMenuBar != null)
					{
						inputMenuBar.setEnabledDialogMenu(true);
					}
					if (skipConfiguration)
					{
						return;
					}
				}
				catch (Throwable e)
				{
					System.exit(0);
				}
			}
			manual = false;
			if (connectionDialog != null)
			{
				connectionDialog.open();
				if (skipConfiguration)
				{
					skipConfiguration = false;
					return;
				}
			}
			manual = true;
			if (retry)
			{
				VTConsole.print("\nVT>Enter the settings file(if available):");
			}
			else
			{
				VTConsole.print("VT>Enter the settings file(if available):");
			}
			retry = true;
			try
			{
				String line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
				}
				else if (skipConfiguration)
				{
					return;
				}
				else if (line.length() > 0)
				{
					loadClientSettingsFile(line);
					return;
				}
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
			}
			VTConsole.print("VT>Enter the connection mode(active as A or passive as P, default:A):");
			try
			{
				String line = VTConsole.readLine(true);
				if (line == null)
				{
					System.exit(0);
				}
				else if (skipConfiguration)
				{
					return;
				}
				if (line.toUpperCase().startsWith("P"))
				{
					active = false;
					//hostAddress = "";
					VTConsole.print("VT>Enter the host address(default:any):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
					}
					hostAddress = line;
					VTConsole.print("VT>Enter the listening port(from 1 to 65535, default:6060):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
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
						hostPort = null;
					}
					else
					{
						VTConsole.print("VT>Use nat port in connection?(Y/N, default:N):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
						}
						else if (skipConfiguration)
						{
							return;
						}
						if (line.toUpperCase().startsWith("Y"))
						{
							VTConsole.print("VT>Enter the connection nat port(from 1 to 65535, default:" + hostPort + "):");
							line = VTConsole.readLine(true);
							if (line == null)
							{
								System.exit(0);
							}
							else if (skipConfiguration)
							{
								return;
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
					}
					if (hostPort != null)
					{
						VTConsole.print("VT>Use encryption in connection?(Y/N, default:N):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
						}
						else if (skipConfiguration)
						{
							return;
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
								return;
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
								return;
							}
							encryptionKey = line.getBytes("UTF-8");
						}
						else
						{
							encryptionType = "None";
						}
						
					}
					/* if (port != null) { VTTerminal.
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
					 * VTTerminal.print("VT>Invalid port!\n"); proxyPort =
					 * null; UseProxyAuthentication = false; port = null; } if
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
					 * "Disabled"; } } */
				}
				else
				{
					active = true;
					VTConsole.print("VT>Enter the host address(default:any):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
					}
					hostAddress = line;
					VTConsole.print("VT>Enter the host port(from 1 to 65535, default:6060):");
					line = VTConsole.readLine(true);
					if (line == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
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
						hostPort = null;
					}
					if (hostPort != null)
					{
						VTConsole.print("VT>Use proxy in connection?(Y/N, default:N):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
						}
						else if (skipConfiguration)
						{
							return;
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
								return;
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
								return;
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
									return;
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
									return;
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
								VTConsole.print("VT>Invalid port!\n");
								proxyPort = null;
								useProxyAuthentication = false;
								hostPort = null;
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
									return;
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
										return;
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
										return;
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
					if (hostPort != null)
					{
						VTConsole.print("VT>Use encryption in connection?(Y/N, default:N):");
						line = VTConsole.readLine(true);
						if (line == null)
						{
							System.exit(0);
						}
						else if (skipConfiguration)
						{
							return;
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
								return;
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
								return;
							}
							encryptionKey = line.getBytes("UTF-8");
						}
						else
						{
							encryptionType = "None";
						}
					}
				}
				if ((hostAddress != null && hostPort != null) && (authenticationLogin == null || authenticationPassword == null || authenticationLogin.length() == 0 || authenticationPassword.length() == 0))
				{
					VTConsole.print("VT>Enter the connection login:");
					String login = VTConsole.readLine(false);
					if (login == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
					}
					setLogin(login);
					VTConsole.print("VT>Enter the connection password:");
					String password = VTConsole.readLine(false);
					if (password == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
					}
					setPassword(password);
				}
				if (hostPort != null)
				{
					VTConsole.print("VT>Enter the session commands:");
					String command = VTConsole.readLine(true);
					if (command == null)
					{
						System.exit(0);
					}
					else if (skipConfiguration)
					{
						return;
					}
					setSessionCommands(command);
				}
			}
			catch (NumberFormatException e)
			{
				VTConsole.print("VT>Invalid port!");
				hostPort = null;
				proxyPort = null;
				useProxyAuthentication = false;
			}
			catch (Throwable e)
			{
				
			}
		}
	}
		
	public void parseParameters(String[] parameters) throws Exception
	{
		int i;
		String parameterName;
		String parameterValue;
		for (i = 0; i < parameters.length; i++)
		{
			parameterName = parameters[i].toUpperCase();
			if (!parameterName.startsWith("-"))
			{
				try
				{
					loadClientSettingsFile(parameterName);
				}
				catch (Throwable t)
				{
					
				}
			}
			if (parameterName.contains("-LF"))
			{
				parameterValue = parameters[++i];
				try
				{
					loadClientSettingsFile(parameterValue);
				}
				catch (Throwable t)
				{
					
				}
			}
			if (parameterName.contains("-CM"))
			{
				parameterValue = parameters[++i];
				if (parameterValue.toUpperCase().startsWith("P"))
				{
					active = false;
				}
				else
				{
					active = true;
				}
			}
			if (parameterName.contains("-CH"))
			{
				parameterValue = parameters[++i];
				hostAddress = parameterValue;
			}
			if (parameterName.contains("-CP"))
			{
				parameterValue = parameters[++i];
				try
				{
					int intValue = Integer.parseInt(parameterValue);
					if (intValue > 0 && intValue < 65536)
					{
						hostPort = intValue;
					}
				}
				catch (Throwable t)
				{
					
				}
			}
			if (parameterName.contains("-NP"))
			{
				parameterValue = parameters[++i];
				try
				{
					int intValue = Integer.parseInt(parameterValue);
					if (intValue > 0 && intValue < 65536)
					{
						natPort = intValue;
					}
				}
				catch (Throwable t)
				{
					
				}
			}
			if (parameterName.contains("-ET"))
			{
				parameterValue = parameters[++i];
				encryptionType = parameterValue;
			}
			if (parameterName.contains("-ES"))
			{
				parameterValue = parameters[++i];
				encryptionKey = parameterValue.getBytes("UTF-8");
			}
			if (parameterName.contains("-PT"))
			{
				parameterValue = parameters[++i];
				proxyType = parameterValue;
			}
			if (parameterName.contains("-PH"))
			{
				parameterValue = parameters[++i];
				proxyAddress = parameterValue;
			}
			if (parameterName.contains("-PP"))
			{
				parameterValue = parameters[++i];
				try
				{
					int intValue = Integer.parseInt(parameterValue);
					if (intValue > 0 && intValue < 65536)
					{
						proxyPort = intValue;
					}
				}
				catch (Throwable t)
				{
					
				}
			}
			if (parameterName.contains("-PA"))
			{
				parameterValue = parameters[++i];
				if (parameterValue.toUpperCase().startsWith("E"))
				{
					useProxyAuthentication = true;
				}
				else
				{
					useProxyAuthentication = false;
				}
			}
			if (parameterName.contains("-PU"))
			{
				parameterValue = parameters[++i];
				proxyUser = parameterValue;
			}
			if (parameterName.contains("-PS"))
			{
				parameterValue = parameters[++i];
				proxyPassword = parameterValue;
			}
			if (parameterName.contains("-SC"))
			{
				parameterValue = parameters[++i];
				sessionCommands = parameterValue;
			}
			if (parameterName.contains("-AL"))
			{
				parameterValue = parameters[++i];
				authenticationLogin = parameterValue;
			}
			if (parameterName.contains("-AS"))
			{
				parameterValue = parameters[++i];
				authenticationPassword = parameterValue;
			}
		}
	}
	
	public void start()
	{
		Thread.setDefaultUncaughtExceptionHandler(new VTUncaughtExceptionHandler());
		// loadFileClientSettings();
		VTNativeUtils.initialize();
		if (VTConsole.isGraphical())
		{
			VTConsole.initialize();
			VTConsole.setTitle("Variable-Terminal Client " + VT.VT_VERSION + " - Console");
			connectionDialog = new VTClientConfigurationDialog(VTConsole.getFrame(), "Variable-Terminal Client " + VT.VT_VERSION + " - Connection", true, this);
			inputMenuBar = new VTClientRemoteGraphicalConsoleMenuBar(connectionDialog);
			VTConsole.getFrame().setMenuBar(inputMenuBar);
			VTConsole.getFrame().pack();
		}
		else
		{
//			if (!GraphicsEnvironment.isHeadless())
//			{
//				Frame invisible = new Frame();
//				invisible.setUndecorated(true);
//				invisible.pack();
//				connectionDialog = new VTClientConnectionDialog(invisible, "Variable-Terminal Client " + VT.VT_VERSION + " - Connection", false, this);
//			}
			VTConsole.initialize();
			VTConsole.setTitle("Variable-Terminal Client " + VT.VT_VERSION + " - Console");
		}
		VTConsole.clear();
		if (vtURL != null)
		{
			System.setProperty("java.library.path", vtURL);
		}
		else
		{
			// System.setProperty("java.library.path", "lib/native");
		}
		VTConsole.print("VT>Variable-Terminal Client " + VT.VT_VERSION + "\n"
		+ "VT>Copyright (c) " + VT.VT_YEAR + " - wknishio@gmail.com\n"
		+ "VT>This software is under MIT license, see license.txt!\n"
		+ "VT>This software comes with no warranty, use at your own risk!\n");
		//+ "VT>Press enter to start client:");
		configure();
		if (skipConfiguration)
		{
			skipConfiguration = false;
		}
		Thread.currentThread().setName(this.getClass().getSimpleName());
		run();
	}
	
	private void loadFromConnectorToClient()
	{
		if (clientConnector != null)
		{
			this.active = clientConnector.isActive();
			this.hostAddress = clientConnector.getAddress();
			this.hostPort = clientConnector.getPort();
			this.natPort = clientConnector.getNatPort();
			this.proxyType = clientConnector.getProxyType();
			this.proxyAddress = clientConnector.getProxyAddress();
			this.proxyPort = clientConnector.getProxyPort();
			this.useProxyAuthentication = clientConnector.isUseProxyAuthentication();
			this.proxyUser = clientConnector.getProxyUser();
			this.proxyPassword = clientConnector.getProxyPassword();
			this.encryptionType = clientConnector.getEncryptionType();
			this.encryptionKey = clientConnector.getEncryptionKey();
			this.sessionCommands = clientConnector.getSessionCommands();
		}
	}
	
	private void saveFromClientToConnector()
	{
		if (clientConnector != null)
		{
			clientConnector.setActive(active);
			clientConnector.setAddress(hostAddress);
			clientConnector.setPort(hostPort);
			clientConnector.setNatPort(natPort);
			clientConnector.setProxyType(proxyType);
			clientConnector.setProxyAddress(proxyAddress);
			clientConnector.setProxyPort(proxyPort);
			clientConnector.setUseProxyAuthentication(useProxyAuthentication);
			clientConnector.setProxyUser(proxyUser);
			clientConnector.setProxyPassword(proxyPassword);
			clientConnector.setEncryptionType(encryptionType);
			clientConnector.setEncryptionKey(encryptionKey);
			clientConnector.setSessionCommands(sessionCommands);
		}
	}
	
	public void run()
	{
		clientConnector = new VTClientConnector(this);
		clientConnector.setActive(active);
		clientConnector.setAddress(hostAddress);
		clientConnector.setPort(hostPort);
		clientConnector.setNatPort(natPort);
		clientConnector.setProxyType(proxyType);
		clientConnector.setProxyAddress(proxyAddress);
		clientConnector.setProxyPort(proxyPort);
		clientConnector.setUseProxyAuthentication(useProxyAuthentication);
		clientConnector.setProxyUser(proxyUser);
		clientConnector.setProxyPassword(proxyPassword);
		clientConnector.setEncryptionType(encryptionType);
		clientConnector.setEncryptionKey(encryptionKey);
		clientConnector.setSessionCommands(sessionCommands);
		clientConnector.run();
	}
}