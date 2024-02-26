package org.vash.vate.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import javax.sound.sampled.AudioFormat;

import org.vash.vate.VT;
import org.vash.vate.audio.VTAudioSystem;
import org.vash.vate.console.VTConsole;
import org.vash.vate.exception.VTUncaughtExceptionHandler;
import org.vash.vate.graphics.message.VTTrayIconInterface;
import org.vash.vate.parser.VTArgumentParser;
import org.vash.vate.parser.VTConfigurationProperties;
import org.vash.vate.parser.VTPropertiesBuilder;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.security.VTBlake3SecureRandom;
import org.vash.vate.server.connection.VTServerConnector;
import org.vash.vate.server.console.local.VTServerLocalConsoleReader;
import org.vash.vate.server.console.local.VTServerLocalGraphicalConsoleMenuBar;
import org.vash.vate.server.dialog.VTServerSettingsDialog;
import org.vash.vate.server.session.VTServerSessionListener;

public class VTServer implements Runnable
{
  private boolean passive = true;
  private String hostAddress = "";
  private Integer hostPort = null;
  private Integer natPort = null;
  private String proxyType = "None";
  private String proxyAddress = "";
  private Integer proxyPort = null;
  //private boolean useProxyAuthentication = false;
  private String proxyUser = "";
  private String proxyPassword = "";
  private String encryptionType = "None";
  private byte[] encryptionKey = new byte[] {};
  private boolean daemon = false;
  private String sessionAccounts = "";
  private Integer sessionsMaximum;
  private String sessionShell = "";
  private final String vtURL = System.getenv("VT_PATH");
  // private MessageDigest sha256Digester;
  //private VTBlake3MessageDigest blake3Digest;
  // private File userDatabaseFile;
  private File serverSettingsFile;
  private final List<Credential> userCredentials = new ArrayList<Credential>();
  // private Properties fileUserCredentials;
  // private Properties argumentsServerSettings = new Properties();
  private VTConfigurationProperties fileServerSettings;
  private final Runtime runtime = Runtime.getRuntime();
  // private Thread consoleThread;
  private InputStream userCredentialsReader;
  private InputStream serverSettingsReader;
  private VTServerConnector serverConnector;
  private VTServerLocalConsoleReader consoleReader;
  private VTServerLocalGraphicalConsoleMenuBar inputMenuBar;
  private VTAudioSystem[] audioSystem;
  private VTServerSettingsDialog connectionDialog;
  private ExecutorService threads;
  private VTTrayIconInterface trayIconInterface;
  private volatile boolean skipConfiguration;
  private volatile boolean echoCommands = false;
  private volatile boolean running = true;
  private volatile boolean reconfigure = false;
  private List<VTServerSessionListener> listeners = new ArrayList<VTServerSessionListener>();
  private static final String VT_SERVER_SETTINGS_COMMENTS = 
  "Variable-Terminal server settings file, supports UTF-8\r\n" + 
  "#vate.server.connection.mode      values: default passive(P), active(A)\r\n" + 
  "#vate.server.proxy.type           values: default none, DIRECT(D), AUTO(A), SOCKS(S), HTTP(H)\r\n" + 
  "#vate.server.encryption.type      values: default none/RC4(R)/ISAAC(I)/SALSA(S)/HC256(H)/LEA(L)\r\n" + 
  "#vate.server.session.accounts     format: user1/password1;user2/password2;...";
  
  static
  {
    VT.initialize();
  }
  
  public class Credential
  {
    private String user;
    private String password;
    
    public Credential(String user, String password)
    {
      this.user = user;
      this.password = password;
    }
    
    public String getUser()
    {
      return user;
    }
    
    public String getPassword()
    {
      return password;
    }
  }
  
  public VTServer()
  {
    // VTServerLocalConsoleCommandSelector.initialize();
    // VTServerRemoteConsoleCommandSelector.initialize();
    // try
    // {
    // sha256Digester = MessageDigest.getInstance("SHA-256");
    // }
    // catch (NoSuchAlgorithmException e)
    // {
    // e.printStackTrace();
    // }
    //this.blake3Digest = new VTBlake3MessageDigest();
    //byte[] seed = new byte[64];
    //new SecureRandom().nextBytes(seed);
    //this.blake3Digest.setSeed(seed);
    this.threads = Executors.newCachedThreadPool(new ThreadFactory()
    {
      public Thread newThread(Runnable runnable)
      {
        Thread created = new Thread(null, runnable, runnable.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    this.audioSystem = new VTAudioSystem[5];
    this.audioSystem[0] = new VTAudioSystem(threads);
    this.audioSystem[1] = new VTAudioSystem(threads);
    this.audioSystem[2] = new VTAudioSystem(threads);
    this.audioSystem[3] = new VTAudioSystem(threads);
    this.audioSystem[4] = new VTAudioSystem(threads);
    
    loadServerSettingsFile();
  }
  
  public void stop()
  {
    try
    {
      running = false;
      if (trayIconInterface != null)
      {
        trayIconInterface.remove();
      }
      serverConnector.stop();
      threads.shutdownNow();
    }
    catch (Throwable t)
    {
      // t.printStackTrace();
    }
  }
  
  public boolean isRunning()
  {
    return running;
  }
  
  public ExecutorService getServerThreads()
  {
    return threads;
  }
  
  public void setSkipConfiguration(boolean skipConfiguration)
  {
    this.skipConfiguration = skipConfiguration;
  }
  
  public VTAudioSystem getAudioSystem(AudioFormat format)
  {
    if (format.getSampleRate() == VT.VT_AUDIO_FORMAT_8000.getSampleRate())
    {
      return this.audioSystem[0];
    }
    if (format.getSampleRate() == VT.VT_AUDIO_FORMAT_16000.getSampleRate())
    {
      return this.audioSystem[1];
    }
    if (format.getSampleRate() == VT.VT_AUDIO_FORMAT_48000.getSampleRate())
    {
      return this.audioSystem[2];
    }
    if (format.getSampleRate() == VT.VT_AUDIO_FORMAT_24000.getSampleRate())
    {
      return this.audioSystem[3];
    }
    if (format.getSampleRate() == VT.VT_AUDIO_FORMAT_32000.getSampleRate())
    {
      return this.audioSystem[4];
    }
    
    return null;
  }
  
  public Runtime getRuntime()
  {
    return runtime;
  }
  
  public List<Credential> getUserCredentials()
  {
    return userCredentials;
  }
  
  public VTServerConnector getServerConnector()
  {
    return serverConnector;
  }
  
  public void setPassive(boolean passive)
  {
    this.passive = passive;
  }
  
  public void setDaemon(boolean daemon)
  {
    this.daemon = daemon;
    if (daemon && hostPort == null)
    {
      hostPort = 6060;
    }
    VTConsole.setDaemon(daemon);
  }
  
  public void setSessionsMaximum(Integer sessionsMaximum)
  {
    this.sessionsMaximum = sessionsMaximum;
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
  
  public InputStream getUserCredentialsReader()
  {
    return userCredentialsReader;
  }
  
  public void setUserCredentialsReader(InputStream userCredentialsReader)
  {
    this.userCredentialsReader = userCredentialsReader;
  }
  
  public InputStream getServerSettingsReader()
  {
    return serverSettingsReader;
  }
  
  public void setServerSettingsReader(InputStream serverSettingsReader)
  {
    this.serverSettingsReader = serverSettingsReader;
  }
  
  public VTServerLocalConsoleReader getConsoleReader()
  {
    return consoleReader;
  }
  
  public void setConsoleReader(VTServerLocalConsoleReader consoleReader)
  {
    this.consoleReader = consoleReader;
  }
  
  public boolean isPassive()
  {
    return passive;
  }
  
  public boolean isDaemon()
  {
    return daemon;
  }
  
  public Integer getSessionsMaximum()
  {
    return sessionsMaximum;
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
  
  public String getVTURL()
  {
    return vtURL;
  }
  
  public void setServerConnector(VTServerConnector serverConnector)
  {
    this.serverConnector = serverConnector;
  }
  
  public void setInputMenuBar(VTServerLocalGraphicalConsoleMenuBar inputMenuBar)
  {
    this.inputMenuBar = inputMenuBar;
  }
  
  public VTServerLocalGraphicalConsoleMenuBar getInputMenuBar()
  {
    return inputMenuBar;
  }
  
  public void setUniqueUserCredential(String user, String password)
  {
    //System.out.println("user=[" + user + "]");
    //System.out.println("password=[" + password + "]");
    try
    {
      userCredentials.clear();
      userCredentials.add(new Credential(user, password));
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public boolean setMultipleUserCredentials(String multipleUsers)
  {
    userCredentials.clear();
    boolean added = false;
    try
    {
      if (multipleUsers != null && multipleUsers.length() > 0)
      {
        String[] users = VTArgumentParser.parseParameter(multipleUsers, ';');
        for (String user : users)
        {
          String[] credentials = VTArgumentParser.parseParameter(user, '/');
          if (credentials.length >= 2)
          {
            addUserCredential(credentials[0], credentials[1]);
            added = true;
          }
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    if (added)
    {
      return true;
    }
    else
    {
      setUniqueUserCredential("", "");
      return false;
    }
  }
  
  public void addUserCredential(String user, String password)
  {
    userCredentials.add(new Credential(user, password));
  }
  
  public void displayTrayIconMessage(String caption, String text)
  {
    if (trayIconInterface != null)
    {
      trayIconInterface.displayMessage(caption, text);
    }
  }
  
  public void enableTrayIcon()
  {
    if (trayIconInterface != null)
    {
      trayIconInterface.install(VTConsole.getFrame(), "Variable-Terminal - Server");
    }
  }
  
  public void disableTrayIcon()
  {
    if (trayIconInterface != null)
    {
      trayIconInterface.remove();
    }
  }
  
  public void saveServerSettingsFile(String settingsFile) throws Exception
  {
    loadFromConnectorToServer();
    if (vtURL != null)
    {
      serverSettingsFile = new File(vtURL, settingsFile);
      if (!serverSettingsFile.exists())
      {
        serverSettingsFile = new File(settingsFile);
      }
    }
    else
    {
      serverSettingsFile = new File(settingsFile);
    }
    
    if (fileServerSettings == null)
    {
      fileServerSettings = new VTConfigurationProperties();
    }
    
    fileServerSettings.clear();
    fileServerSettings.setProperty("vate.server.connection.mode", passive ? "Passive" : "Active");
    fileServerSettings.setProperty("vate.server.connection.port", hostPort != null ? String.valueOf(hostPort) : "");
    fileServerSettings.setProperty("vate.server.connection.host", hostAddress);
    fileServerSettings.setProperty("vate.server.connection.nat.port", natPort != null ? String.valueOf(natPort) : "");
    fileServerSettings.setProperty("vate.server.proxy.type", proxyType);
    fileServerSettings.setProperty("vate.server.proxy.host", proxyAddress);
    fileServerSettings.setProperty("vate.server.proxy.port", proxyPort != null ? String.valueOf(proxyPort) : "");
    //fileServerSettings.setProperty("vate.server.proxy.authentication", useProxyAuthentication ? "Enabled" : "Disabled");
    fileServerSettings.setProperty("vate.server.proxy.user", proxyUser);
    fileServerSettings.setProperty("vate.server.proxy.password", proxyPassword);
    fileServerSettings.setProperty("vate.server.encryption.type", encryptionType);
    fileServerSettings.setProperty("vate.server.encryption.password", new String(encryptionKey, "UTF-8"));
    fileServerSettings.setProperty("vate.server.session.shell", sessionShell);
    fileServerSettings.setProperty("vate.server.session.maximum", String.valueOf(sessionsMaximum == null ? "" : sessionsMaximum));
    fileServerSettings.setProperty("vate.server.session.accounts", sessionAccounts);
    
    FileOutputStream out = new FileOutputStream(settingsFile);
    VTPropertiesBuilder.saveProperties(out, fileServerSettings, VT_SERVER_SETTINGS_COMMENTS, "UTF-8");
    out.flush();
    out.close();
  }
  
  public void loadServerSettingsFile(String settingsFile) throws Exception
  {
    loadFromConnectorToServer();
    if (vtURL != null)
    {
      serverSettingsFile = new File(vtURL, settingsFile);
      if (!serverSettingsFile.exists())
      {
        serverSettingsFile = new File(settingsFile);
      }
    }
    else
    {
      serverSettingsFile = new File(settingsFile);
    }
    serverSettingsReader = new FileInputStream(serverSettingsFile);
    fileServerSettings = VTPropertiesBuilder.loadProperties(serverSettingsReader, "UTF-8");
    // rawSecuritySettings.load(securitySettingsReader);
    serverSettingsReader.close();
    
    sessionAccounts = fileServerSettings.getProperty("vate.server.session.accounts", null);
    
    setMultipleUserCredentials(sessionAccounts);
    
    if (fileServerSettings.getProperty("vate.server.connection.mode") != null)
    {
      try
      {
        String mode = fileServerSettings.getProperty("vate.server.connection.mode");
        if (mode.toUpperCase().startsWith("A"))
        {
          passive = false;
        }
        else
        {
          passive = true;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.connection.port") != null)
    {
      try
      {
        int filePort = Integer.parseInt(fileServerSettings.getProperty("vate.server.connection.port"));
        if (filePort > 0 && filePort < 65536)
        {
          hostPort = filePort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.connection.host") != null)
    {
      try
      {
        hostAddress = fileServerSettings.getProperty("vate.server.connection.host", hostAddress);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.connection.nat.port") != null)
    {
      try
      {
        int fileNatPort = Integer.parseInt(fileServerSettings.getProperty("vate.server.connection.nat.port"));
        if (fileNatPort > 0 && fileNatPort < 65536)
        {
          natPort = fileNatPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.encryption.type") != null)
    {
      try
      {
        encryptionType = fileServerSettings.getProperty("vate.server.encryption.type", encryptionType);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.encryption.password") != null)
    {
      try
      {
        encryptionKey = fileServerSettings.getProperty("vate.server.encryption.password", "").getBytes("UTF-8");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.proxy.type") != null)
    {
      try
      {
        proxyType = fileServerSettings.getProperty("vate.server.proxy.type", proxyType);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.proxy.host") != null)
    {
      try
      {
        proxyAddress = fileServerSettings.getProperty("vate.server.proxy.host", proxyAddress);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.proxy.port") != null)
    {
      try
      {
        int fileProxyPort = Integer.parseInt(fileServerSettings.getProperty("vate.server.proxy.port"));
        if (fileProxyPort > 0 && fileProxyPort < 65536)
        {
          proxyPort = fileProxyPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
//    if (fileServerSettings.getProperty("vate.server.proxy.authentication") != null)
//    {
//      try
//      {
//        if (fileServerSettings.getProperty("vate.server.proxy.authentication").toUpperCase().startsWith("E"))
//        {
//          useProxyAuthentication = true;
//        }
//        else
//        {
//          useProxyAuthentication = false;
//        }
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
    
    if (fileServerSettings.getProperty("vate.server.proxy.user") != null)
    {
      try
      {
        proxyUser = fileServerSettings.getProperty("vate.server.proxy.user");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.proxy.password") != null)
    {
      try
      {
        proxyPassword = fileServerSettings.getProperty("vate.server.proxy.password");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.session.limit") != null)
    {
      try
      {
        int fileSessionsMaximum = Integer.parseInt(fileServerSettings.getProperty("vate.server.session.maximum"));
        if (fileSessionsMaximum < 0)
        {
          sessionsMaximum = null;
        }
        else
        {
          sessionsMaximum = fileSessionsMaximum;
        }
      }
      catch (Throwable e)
      {
        sessionsMaximum = null;
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.session.shell") != null)
    {
      try
      {
        sessionShell = fileServerSettings.getProperty("vate.server.session.shell");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    saveFromServerToConnector();
  }
  
  private void loadServerSettingsFile()
  {
    if (fileServerSettings != null)
    {
      return;
    }
    try
    {
      if (vtURL != null)
      {
        serverSettingsFile = new File(vtURL, "vate-server.properties");
        if (!serverSettingsFile.exists())
        {
          serverSettingsFile = new File("vate-server.properties");
        }
      }
      else
      {
        serverSettingsFile = new File("vate-server.properties");
      }
      serverSettingsReader = new FileInputStream(serverSettingsFile);
      fileServerSettings = VTPropertiesBuilder.loadProperties(serverSettingsReader, "UTF-8");
      // rawSecuritySettings.load(securitySettingsReader);
      serverSettingsReader.close();
      
      sessionAccounts = fileServerSettings.getProperty("vate.server.session.accounts", null);
      
      setMultipleUserCredentials(sessionAccounts);
      
      if (fileServerSettings.getProperty("vate.server.connection.mode") != null)
      {
        try
        {
          String mode = fileServerSettings.getProperty("vate.server.connection.mode");
          if (mode.toUpperCase().startsWith("A"))
          {
            passive = false;
          }
          else
          {
            passive = true;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.connection.port") != null)
      {
        try
        {
          int filePort = Integer.parseInt(fileServerSettings.getProperty("vate.server.connection.port"));
          if (filePort > 0 && filePort < 65536)
          {
            hostPort = filePort;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.connection.host") != null)
      {
        try
        {
          hostAddress = fileServerSettings.getProperty("vate.server.connection.host", hostAddress);
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.connection.nat.port") != null)
      {
        try
        {
          int fileNatPort = Integer.parseInt(fileServerSettings.getProperty("vate.server.connection.nat.port"));
          if (fileNatPort > 0 && fileNatPort < 65536)
          {
            natPort = fileNatPort;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.encryption.type") != null)
      {
        try
        {
          encryptionType = fileServerSettings.getProperty("vate.server.encryption.type", encryptionType);
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.encryption.password") != null)
      {
        try
        {
          encryptionKey = fileServerSettings.getProperty("vate.server.encryption.password", "").getBytes("UTF-8");
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.proxy.type") != null)
      {
        try
        {
          proxyType = fileServerSettings.getProperty("vate.server.proxy.type", proxyType);
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.proxy.host") != null)
      {
        try
        {
          proxyAddress = fileServerSettings.getProperty("vate.server.proxy.host", proxyAddress);
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.proxy.port") != null)
      {
        try
        {
          int fileProxyPort = Integer.parseInt(fileServerSettings.getProperty("vate.server.proxy.port"));
          if (fileProxyPort > 0 && fileProxyPort < 65536)
          {
            proxyPort = fileProxyPort;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      
//      if (fileServerSettings.getProperty("vate.server.proxy.authentication") != null)
//      {
//        try
//        {
//          if (fileServerSettings.getProperty("vate.server.proxy.authentication").toUpperCase().startsWith("E"))
//          {
//            useProxyAuthentication = true;
//          }
//          else
//          {
//            useProxyAuthentication = false;
//          }
//        }
//        catch (Throwable e)
//        {
//          
//        }
//      }
      
      if (fileServerSettings.getProperty("vate.server.proxy.user") != null)
      {
        try
        {
          proxyUser = fileServerSettings.getProperty("vate.server.proxy.user");
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.proxy.password") != null)
      {
        try
        {
          proxyPassword = fileServerSettings.getProperty("vate.server.proxy.password");
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.session.limit") != null)
      {
        try
        {
          int fileSessionsMaximum = Integer.parseInt(fileServerSettings.getProperty("vate.server.session.maximum"));
          if (fileSessionsMaximum < 0)
          {
            sessionsMaximum = null;
          }
          else
          {
            sessionsMaximum = fileSessionsMaximum;
          }
        }
        catch (Throwable e)
        {
          sessionsMaximum = null;
        }
      }
      
      if (fileServerSettings.getProperty("vate.server.session.shell") != null)
      {
        try
        {
          sessionShell = fileServerSettings.getProperty("vate.server.session.shell");
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
  
  public void loadServerSettingsProperties(Properties properties) throws Exception
  {
    loadFromConnectorToServer();
    
    sessionAccounts = properties.getProperty("vate.server.session.accounts", null);
    
    setMultipleUserCredentials(sessionAccounts);
    
    if (properties.getProperty("vate.server.connection.mode") != null)
    {
      try
      {
        String mode = properties.getProperty("vate.server.connection.mode");
        if (mode.toUpperCase().startsWith("A"))
        {
          passive = false;
        }
        else
        {
          passive = true;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.connection.port") != null)
    {
      try
      {
        int filePort = Integer.parseInt(properties.getProperty("vate.server.connection.port"));
        if (filePort > 0 && filePort < 65536)
        {
          hostPort = filePort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.connection.host") != null)
    {
      try
      {
        hostAddress = properties.getProperty("vate.server.connection.host", hostAddress);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.connection.nat.port") != null)
    {
      try
      {
        int fileNatPort = Integer.parseInt(properties.getProperty("vate.server.connection.nat.port"));
        if (fileNatPort > 0 && fileNatPort < 65536)
        {
          natPort = fileNatPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.encryption.type") != null)
    {
      try
      {
        encryptionType = properties.getProperty("vate.server.encryption.type", encryptionType);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.encryption.password") != null)
    {
      try
      {
        encryptionKey = properties.getProperty("vate.server.encryption.password", "").getBytes("UTF-8");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.proxy.type") != null)
    {
      try
      {
        proxyType = properties.getProperty("vate.server.proxy.type", proxyType);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.proxy.host") != null)
    {
      try
      {
        proxyAddress = properties.getProperty("vate.server.proxy.host", proxyAddress);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.proxy.port") != null)
    {
      try
      {
        int fileProxyPort = Integer.parseInt(properties.getProperty("vate.server.proxy.port"));
        if (fileProxyPort > 0 && fileProxyPort < 65536)
        {
          proxyPort = fileProxyPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
//    if (properties.getProperty("vate.server.proxy.authentication") != null)
//    {
//      try
//      {
//        if (properties.getProperty("vate.server.proxy.authentication").toUpperCase().startsWith("E"))
//        {
//          useProxyAuthentication = true;
//        }
//        else
//        {
//          useProxyAuthentication = false;
//        }
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
    
    if (properties.getProperty("vate.server.proxy.user") != null)
    {
      try
      {
        proxyUser = properties.getProperty("vate.server.proxy.user");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.proxy.password") != null)
    {
      try
      {
        proxyPassword = properties.getProperty("vate.server.proxy.password");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.server.session.limit") != null)
    {
      try
      {
        int fileSessionsMaximum = Integer.parseInt(properties.getProperty("vate.server.session.maximum"));
        if (fileSessionsMaximum < 0)
        {
          sessionsMaximum = null;
        }
        else
        {
          sessionsMaximum = fileSessionsMaximum;
        }
      }
      catch (Throwable e)
      {
        sessionsMaximum = null;
      }
    }
    
    if (fileServerSettings.getProperty("vate.server.session.shell") != null)
    {
      try
      {
        sessionShell = fileServerSettings.getProperty("vate.server.session.shell");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    saveFromServerToConnector();
  }
  
  public void reconfigure()
  {
    reconfigure = true;
    skipConfiguration = false;
    loadFromConnectorToServer();
    hostPort = null;
    configure();
    saveFromServerToConnector();
    reconfigure = false;
  }
  
  private void configure()
  {
    while ((passive && hostPort == null) || (!passive && (hostAddress == null || hostPort == null)))
    {
      if (skipConfiguration)
      {
        skipConfiguration = false;
        return;
      }
      
      if (!reconfigure)
      {
        VTConsole.print("VT>Press enter to start server:");
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
          VTRuntimeExit.exit(0);
        }
      }
      else
      {
        // if (inputMenuBar != null)
        // {
        // inputMenuBar.setEnabledDialogMenu(true);
        // }
      }
      
      if (connectionDialog != null)
      {
        if ((passive && hostPort == null) || (!passive && (hostAddress == null || hostPort == null)))
        {
          connectionDialog.open();
          if (skipConfiguration)
          {
            skipConfiguration = false;
            if (reconfigure)
            {
              reconfigure = false;
              loadFromConnectorToServer();
            }
            return;
          }
        }
      }
      if (reconfigure)
      {
        reconfigure = false;
        VTConsole.print("\r");
      }
      VTConsole.print("VT>Enter settings file(if available):");
      try
      {
        String line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
        }
        else if (skipConfiguration)
        {
          return;
        }
        else if (line.length() > 0)
        {
          loadServerSettingsFile(line);
          return;
        }
      }
      catch (Throwable e)
      {
        
      }
      if (!(userCredentials.size() > 0))
      {
        if (!daemon)
        {
          try
          {
            VTConsole.print("VT>Configure a session user account on server?(Y/N, default:Y):");
            String line = VTConsole.readLine(true);
            if (line == null || line.toUpperCase().startsWith("N"))
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return;
            }
            VTConsole.print("VT>Enter session user:");
            String user = VTConsole.readLine(false);
            if (user == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return;
            }
            VTConsole.print("VT>Enter session password:");
            String password = VTConsole.readLine(false);
            if (password == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return;
            }
            addUserCredential(user, password);
          }
          catch (InterruptedException e)
          {
            if (skipConfiguration)
            {
              return;
            }
          }
          catch (Throwable e)
          {
            VTRuntimeExit.exit(0);
          }
        }
        else
        {
          // VTExit.exit(0);
          // try
          // {
          // addUserCredential("", "");
          // }
          // catch (UnsupportedEncodingException e)
          // {
          
          // }
        }
      }
      VTConsole.print("VT>Enter connection mode(active as A or passive as P, default:P):");
      try
      {
        String line = VTConsole.readLine(true);
        if (line == null)
        {
          VTRuntimeExit.exit(0);
        }
        else if (skipConfiguration)
        {
          return;
        }
        if (line.toUpperCase().startsWith("A"))
        {
          passive = false;
          VTConsole.print("VT>Enter host address(default:any):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return;
          }
          hostAddress = line;
          VTConsole.print("VT>Enter host port(from 1 to 65535, default:6060):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
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
            VTConsole.print("VT>Invalid port!\n");
            hostPort = null;
          }
          if (hostPort != null)
          {
            if (proxyType == null)
            {
              VTConsole.print("VT>Use proxy in connection?(Y/N, default:N):");
              line = VTConsole.readLine(true);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return;
              }
              if (line.toUpperCase().startsWith("Y"))
              {
                VTConsole.print("VT>Enter proxy type(DIRECT as D, AUTO as A, SOCKS as S, HTTP as H, default:A):");
                line = VTConsole.readLine(true);
                if (line == null)
                {
                  VTRuntimeExit.exit(0);
                }
                else if (skipConfiguration)
                {
                  return;
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
                  proxyType = "AUTO";
                }
                if ("AUTO".equals(proxyType) || "HTTP".equals(proxyType) || "SOCKS".equals(proxyType))
                {
                  VTConsole.print("VT>Enter proxy host address(default:any):");
                  line = VTConsole.readLine(true);
                  if (line == null)
                  {
                    VTRuntimeExit.exit(0);
                  }
                  else if (skipConfiguration)
                  {
                    return;
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
                else if (proxyType.equals("HTTP") || proxyType.equals("AUTO"))
                {
                  VTConsole.print("VT>Enter proxy port(from 1 to 65535, default:8080):");
                  line = VTConsole.readLine(true);
                  if (line == null)
                  {
                    VTRuntimeExit.exit(0);
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
                  //useProxyAuthentication = false;
                  hostPort = null;
                }
                if (("AUTO".equals(proxyType) || "HTTP".equals(proxyType) || "SOCKS".equals(proxyType)) && proxyPort != null && hostPort != null)
                {
                  VTConsole.print("VT>Use authentication for proxy?(Y/N, default:N):");
                  line = VTConsole.readLine(true);
                  if (line == null)
                  {
                    VTRuntimeExit.exit(0);
                  }
                  else if (skipConfiguration)
                  {
                    return;
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
                      return;
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
                      return;
                    }
                    proxyPassword = line;
                  }
                  else
                  {
                    proxyUser = null;
                    proxyPassword = null;
                    //useProxyAuthentication = false;
                  }
                }
                else
                {
                  proxyUser = null;
                  proxyPassword = null;
                  //useProxyAuthentication = false;
                }
              }
              else
              {
                proxyType = "None";
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
              return;
            }
            if (line.toUpperCase().startsWith("Y"))
            {
              VTConsole.print("VT>Enter encryption type(RC4(R)/ISAAC(I)/SALSA(S)/HC256(H)/LEA(L)):");
              line = VTConsole.readLine(false);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return;
              }
              encryptionType = "RC4";
              if (line.toUpperCase().startsWith("L"))
              {
                encryptionType = "LEA";
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
//              if (line.toUpperCase().startsWith("G"))
//              {
//                encryptionType = "GRAIN";
//              }
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
        else
        {
          passive = true;
          sessionsMaximum = null;
          // hostAddress = "";
          VTConsole.print("VT>Enter host address(default:any):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return;
          }
          hostAddress = line;
          VTConsole.print("VT>Enter listening port(from 1 to 65535, default:6060):");
          line = VTConsole.readLine(true);
          if (line == null)
          {
            VTRuntimeExit.exit(0);
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
            VTConsole.print("VT>Invalid port!\n");
            hostPort = null;
            sessionsMaximum = null;
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
              return;
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
            VTConsole.print("VT>Use encryption in connection?(Y/N, default:N):");
            line = VTConsole.readLine(true);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return;
            }
            if (line.toUpperCase().startsWith("Y"))
            {
              VTConsole.print("VT>Enter encryption type(RC4(R)/ISAAC(I)/SALSA(S)/HC256(H)/LEA(L)):");
              line = VTConsole.readLine(false);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return;
              }
              encryptionType = "RC4";
              if (line.toUpperCase().startsWith("L"))
              {
                encryptionType = "LEA";
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
//              if (line.toUpperCase().startsWith("G"))
//              {
//                encryptionType = "GRAIN";
//              }
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
                return;
              }
              encryptionKey = line.getBytes("UTF-8");
            }
            else
            {
              encryptionType = "None";
            }
            VTConsole.print("VT>Enter session shell(null for default):");
            line = VTConsole.readLine(false);
            if (line == null)
            {
              VTRuntimeExit.exit(0);
            }
            else if (skipConfiguration)
            {
              return;
            }
            sessionShell = line;
            try
            {
              VTConsole.print("VT>Enter session maximum(from 0 to 65535, default:0):");
              line = VTConsole.readLine(true);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return;
              }
              if (!line.trim().equals(""))
              {
                sessionsMaximum = Integer.parseInt(line);
                if (sessionsMaximum < 0)
                {
                  sessionsMaximum = null;
                }
              }
              else
              {
                sessionsMaximum = null;
              }
            }
            catch (NumberFormatException e)
            {
              sessionsMaximum = null;
            }
            catch (Throwable e)
            {
              sessionsMaximum = null;
            }
          }
//          if (sessionsMaximum == null || sessionsMaximum < 0)
//          {
//            VTConsole.print("VT>Invalid session maximum!\n");
//          }
        }
      }
      catch (NumberFormatException e)
      {
        VTConsole.print("VT>Invalid port!\n");
        hostPort = null;
        proxyPort = null;
        //useProxyAuthentication = false;
      }
      catch (Throwable e)
      {
        
      }
      if (hostAddress == null || hostPort == null)
      {
        VTConsole.print("VT>Try configuring again?(Y/N, default:N):");
        try
        {
          String line = VTConsole.readLine(true);
          if (line == null || !line.toUpperCase().startsWith("Y"))
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return;
          }
        }
        catch (Throwable e)
        {
          VTRuntimeExit.exit(0);
        }
      }
    }
  }
  
  public void parseParameters(String[] parameters) throws Exception
  {
    int i;
    String parameterName;
    String parameterValue;
    String parameterUser = null;
    String parameterPassword = null;
    for (i = 0; i < parameters.length; i++)
    {
      parameterName = parameters[i].toUpperCase();
      if (!parameterName.startsWith("-"))
      {
        try
        {
          loadServerSettingsFile(parameterName);
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
          loadServerSettingsFile(parameterValue);
        }
        catch (Throwable t)
        {
          
        }
      }
      if (parameterName.contains("-CM"))
      {
        parameterValue = parameters[++i];
        if (parameterValue.toUpperCase().startsWith("A"))
        {
          passive = false;
        }
        else
        {
          passive = true;
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
      if (parameterName.contains("-CN"))
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
      if (parameterName.contains("-EK"))
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
//      if (parameterName.contains("-PA"))
//      {
//        parameterValue = parameters[++i];
//        if (parameterValue.toUpperCase().startsWith("E"))
//        {
//          useProxyAuthentication = true;
//        }
//        else
//        {
//          useProxyAuthentication = false;
//        }
//      }
      if (parameterName.contains("-PU"))
      {
        parameterValue = parameters[++i];
        proxyUser = parameterValue;
      }
      if (parameterName.contains("-PK"))
      {
        parameterValue = parameters[++i];
        proxyPassword = parameterValue;
      }
      if (parameterName.contains("-SM"))
      {
        parameterValue = parameters[++i];
        try
        {
          int intValue = Integer.parseInt(parameterValue);
          if (intValue > 0)
          {
            sessionsMaximum = intValue;
          }
        }
        catch (Throwable t)
        {
          
        }
      }
      if (parameterName.contains("-SU"))
      {
        parameterValue = parameters[++i];
        parameterUser = parameterValue;
        if (parameterPassword != null)
        {
          setUniqueUserCredential(parameterUser, parameterPassword);
        }
      }
      if (parameterName.contains("-SK"))
      {
        parameterValue = parameters[++i];
        parameterPassword = parameterValue;
        if (parameterUser != null)
        {
          setUniqueUserCredential(parameterUser, parameterPassword);
        }
      }
      if (parameterName.contains("-SS"))
      {
        parameterValue = parameters[++i];
        sessionShell = parameterValue;
      }
    }
  }
  
  public void startThread()
  {
    threads.execute(new Runnable()
    {
      public void run()
      {
        start();
      }
    });
  }
  
  public void start()
  {
    Thread.setDefaultUncaughtExceptionHandler(new VTUncaughtExceptionHandler());
    // loadFileServerSettings();
    if (!VTConsole.isDaemon() && VTConsole.isGraphical())
    {
      VTConsole.initialize();
      VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Server - Console");
      if (!VTConsole.isDaemon() && !daemon)
      {
        connectionDialog = new VTServerSettingsDialog(VTConsole.getFrame(), "Variable-Terminal " + VT.VT_VERSION + " - Server - Connection", true, this);
        inputMenuBar = new VTServerLocalGraphicalConsoleMenuBar(connectionDialog);
        VTConsole.getFrame().setMenuBar(inputMenuBar);
        VTConsole.getFrame().pack();
        try
        {
          trayIconInterface = new VTTrayIconInterface();
          trayIconInterface.install(VTConsole.getFrame(), "Variable-Terminal - Server");
        }
        catch (Throwable t)
        {
          trayIconInterface = null;
        }
      }
    }
    else
    {
      VTConsole.initialize();
      VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Server - Console");
    }
    VTConsole.clear();
    VTConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + " - Server\n" + 
    "VT>Copyright (c) " + VT.VT_YEAR + " - wknishio@gmail.com\n" + 
    "VT>This software is under MIT license, see license.txt!\n" + 
    "VT>This software comes with no warranty, use at your own risk!\n");
    if (!VTConsole.isDaemon() && !daemon)
    {
      configure();
    }
    if (skipConfiguration)
    {
      skipConfiguration = false;
    }
    Thread.currentThread().setName(this.getClass().getSimpleName());
    run();
  }
  
  private void loadFromConnectorToServer()
  {
    if (serverConnector != null)
    {
      this.passive = serverConnector.isPassive();
      this.hostAddress = serverConnector.getAddress();
      this.hostPort = serverConnector.getPort();
      this.natPort = serverConnector.getNatPort();
      this.proxyType = serverConnector.getProxyType();
      this.proxyAddress = serverConnector.getProxyAddress();
      this.proxyPort = serverConnector.getProxyPort();
      //this.useProxyAuthentication = serverConnector.isUseProxyAuthentication();
      this.proxyUser = serverConnector.getProxyUser();
      this.proxyPassword = serverConnector.getProxyPassword();
      this.encryptionType = serverConnector.getEncryptionType();
      this.encryptionKey = serverConnector.getEncryptionKey();
      this.sessionsMaximum = serverConnector.getSessionsMaximum();
      this.sessionShell = serverConnector.getSessionShell();
    }
  }
  
  private void saveFromServerToConnector()
  {
    if (serverConnector != null)
    {
      serverConnector.setPassive(passive);
      serverConnector.setAddress(hostAddress);
      serverConnector.setPort(hostPort);
      serverConnector.setNatPort(natPort);
      serverConnector.setProxyType(proxyType);
      serverConnector.setProxyAddress(proxyAddress);
      serverConnector.setProxyPort(proxyPort);
      //serverConnector.setUseProxyAuthentication(useProxyAuthentication);
      serverConnector.setProxyUser(proxyUser);
      serverConnector.setProxyPassword(proxyPassword);
      serverConnector.setSessionsMaximum(sessionsMaximum);
      serverConnector.setSessionShell(sessionShell);
      if (encryptionType != null && encryptionKey != null)
      {
        serverConnector.setEncryptionType(encryptionType);
        serverConnector.setEncryptionKey(encryptionKey);
      }
      else
      {
        serverConnector.setEncryptionType("None");
        serverConnector.setEncryptionKey(encryptionKey);
      }
      if (!(userCredentials.size() > 0))
      {
        this.setUniqueUserCredential("", "");
      }
    }
  }
  
  public void run()
  {
    serverConnector = new VTServerConnector(this, new VTBlake3SecureRandom());
    serverConnector.setPassive(passive);
    serverConnector.setAddress(hostAddress);
    serverConnector.setPort(hostPort);
    serverConnector.setNatPort(natPort);
    serverConnector.setProxyType(proxyType);
    serverConnector.setProxyAddress(proxyAddress);
    serverConnector.setProxyPort(proxyPort);
    //serverConnector.setUseProxyAuthentication(useProxyAuthentication);
    serverConnector.setProxyUser(proxyUser);
    serverConnector.setProxyPassword(proxyPassword);
    serverConnector.setSessionsMaximum(sessionsMaximum);
    serverConnector.setSessionShell(sessionShell);
    if (encryptionType != null && encryptionKey != null)
    {
      serverConnector.setEncryptionType(encryptionType);
      serverConnector.setEncryptionKey(encryptionKey);
    }
    else
    {
      serverConnector.setEncryptionType("None");
      serverConnector.setEncryptionKey(encryptionKey);
    }
    if (!(userCredentials.size() > 0))
    {
      this.setUniqueUserCredential("", "");
    }
    if (!daemon)
    {
      consoleReader = new VTServerLocalConsoleReader(this);
      consoleReader.startThread();
    }
    for (VTServerSessionListener listener : listeners)
    {
      serverConnector.addSessionListener(listener);
    }
    serverConnector.run();
  }
  
  public void setEchoCommands(boolean echoCommands)
  {
    this.echoCommands = echoCommands;
  }
  
  public boolean isEchoCommands()
  {
    return echoCommands;
  }
  
  public boolean isReconfigure()
  {
    return reconfigure;
  }
  
  public void addSessionListener(VTServerSessionListener listener)
  {
    if (serverConnector != null)
    {
      serverConnector.addSessionListener(listener);
    }
    else
    {
      listeners.add(listener);
    }
  }
  
  public void removeSessionListener(VTServerSessionListener listener)
  {
    if (serverConnector != null)
    {
      serverConnector.removeSessionListener(listener);
    }
    else
    {
      listeners.add(listener);
    }
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