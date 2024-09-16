package org.vash.vate.client;

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

import org.vash.vate.VT;
import org.vash.vate.audio.VTAudioSystem;
import org.vash.vate.client.connection.VTClientConnector;
import org.vash.vate.client.console.remote.VTClientRemoteGraphicalConsoleMenuBar;
import org.vash.vate.client.dialog.VTClientConfigurationDialog;
import org.vash.vate.client.session.VTClientSessionListener;
import org.vash.vate.console.VTConsole;
import org.vash.vate.exception.VTUncaughtExceptionHandler;
import org.vash.vate.parser.VTConfigurationProperties;
import org.vash.vate.parser.VTPropertiesBuilder;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.security.VTBlake3SecureRandom;

public class VTClient implements Runnable
{
  private boolean active = true;
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
  private String sessionUser = "";
  private String sessionPassword = "";
  private String sessionCommands = "";
  // private String sessionLines = "";
  private String sessionShell = "";
  private boolean daemon = false;
  private final String vtURL = System.getenv("VT_PATH");
  private final Runtime runtime = Runtime.getRuntime();
  private File clientSettingsFile;
  private VTConfigurationProperties fileClientSettings;
  private InputStream clientSettingsReader;
  private VTClientConnector clientConnector;
  private VTClientRemoteGraphicalConsoleMenuBar inputMenuBar;
  private VTAudioSystem audioSystem;
  private VTClientConfigurationDialog connectionDialog;
  private ExecutorService executorService;
  // private VTTrayIconInterface trayIconInterface;
  private boolean skipConfiguration;
  private boolean retry = false;
  private boolean manual = false;
  private List<VTClientSessionListener> listeners = new ArrayList<VTClientSessionListener>();
  private static final String VT_CLIENT_SETTINGS_COMMENTS = 
  "Variable-Terminal client settings file, supports UTF-8\r\n" + 
  "#vate.client.connection.mode  values: default active(A), passive(P)\r\n" + 
  "#vate.client.proxy.type       values: DIRECT(D)/SOCKS(S)/HTTP(H)/ANY(A)\r\n" + 
  "#vate.client.encryption.type  values: ISAAC(I)/VMPC(V)/SALSA(S)/HC(H)/ZUC(Z)\r\n" + 
  "#vate.client.session.commands format: cmd1*;cmd2*;cmd3*;...\r\n";
  
  static
  {
    VT.initialize();
  }
  
  public VTClient()
  {
    // VTClientRemoteConsoleCommandSelector.initialize();
    this.executorService = Executors.newCachedThreadPool(new ThreadFactory()
    {
      public Thread newThread(Runnable runnable)
      {
        Thread created = new Thread(null, runnable, runnable.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    this.audioSystem = new VTAudioSystem(executorService);
    loadClientSettingsFile();
  }
  
  public void stop()
  {
    try
    {
      clientConnector.stop();
    }
    catch (Throwable t)
    {
      
    }
    
    try
    {
      executorService.shutdownNow();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public ExecutorService getExecutorService()
  {
    return executorService;
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
  
  public void setDaemon(boolean daemon)
  {
    this.daemon = daemon;
    VTConsole.setDaemon(daemon);
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
  
  public String getUser()
  {
    return sessionUser;
  }
  
  public void setUser(String user)
  {
    this.sessionUser = user;
  }
  
  public String getPassword()
  {
    return sessionPassword;
  }
  
  public void setPassword(String password)
  {
    this.sessionPassword = password;
  }
  
  public String getSessionCommands()
  {
    return sessionCommands;
  }
  
  public void setSessionCommands(String sessionCommands)
  {
    this.sessionCommands = sessionCommands;
  }
  
  // public String getSessionLines()
  // {
  // return sessionLines;
  // }
  
  // public void setSessionLines(String sessionLines)
  // {
  // this.sessionLines = sessionLines;
  // }
  
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
  
  public boolean isDaemon()
  {
    return daemon;
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
  
  public void setClientConnector(VTClientConnector clientConnector)
  {
    this.clientConnector = clientConnector;
  }
  
  public Runtime getRuntime()
  {
    return runtime;
  }
  
  public void enableInputMenuBar()
  {
    if (inputMenuBar != null)
    {
      inputMenuBar.setEnabled(true);
    }
  }
  
  public void disableInputMenuBar()
  {
    if (inputMenuBar != null)
    {
      inputMenuBar.setEnabled(false);
    }
  }
  
  public boolean hasConnectionDialog()
  {
    return connectionDialog != null && !connectionDialog.isVisible();
  }
  
  public void openConnectionDialog()
  {
    try
    {
      if (connectionDialog != null)
      {
        if (!connectionDialog.isVisible())
        {
          connectionDialog.open();
        }
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void closeConnectionDialog()
  {
    try
    {
      if (connectionDialog != null)
      {
        if (connectionDialog.isVisible())
        {
          connectionDialog.close();
        }
      }
    }
    catch (Throwable t)
    {
      
    }
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
    fileClientSettings.setProperty("vate.client.proxy.type", proxyType);
    fileClientSettings.setProperty("vate.client.proxy.host", proxyAddress);
    fileClientSettings.setProperty("vate.client.proxy.port", proxyPort != null ? String.valueOf(proxyPort) : "");
    //fileClientSettings.setProperty("vate.client.proxy.authentication", useProxyAuthentication ? "Enabled" : "Disabled");
    fileClientSettings.setProperty("vate.client.proxy.user", proxyUser);
    fileClientSettings.setProperty("vate.client.proxy.password", proxyPassword);
    fileClientSettings.setProperty("vate.client.encryption.type", encryptionType);
    fileClientSettings.setProperty("vate.client.encryption.password", new String(encryptionKey, "UTF-8"));
    fileClientSettings.setProperty("vate.client.session.commands", sessionCommands);
    // fileClientSettings.setProperty("vate.client.session.lines",
    // sessionLines);
    fileClientSettings.setProperty("vate.client.session.shell", sessionShell);
    fileClientSettings.setProperty("vate.client.session.user", sessionUser);
    fileClientSettings.setProperty("vate.client.session.password", sessionPassword);
    
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
    
    if (fileClientSettings.getProperty("vate.client.session.user") != null)
    {
      try
      {
        sessionUser = fileClientSettings.getProperty("vate.client.session.user", sessionUser);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (fileClientSettings.getProperty("vate.client.session.password") != null)
    {
      try
      {
        sessionPassword = fileClientSettings.getProperty("vate.client.session.password", sessionPassword);
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
    
//    if (fileClientSettings.getProperty("vate.client.proxy.authentication") != null)
//    {
//      try
//      {
//        if (fileClientSettings.getProperty("vate.client.proxy.authentication").toUpperCase().startsWith("E"))
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
    
    // if (fileClientSettings.getProperty("vate.client.session.lines") != null)
    // {
    // try
    // {
    // sessionLines =
    // fileClientSettings.getProperty("vate.client.session.lines");
    // }
    // catch (Throwable e)
    // {
    
    // }
    // }
    
    if (fileClientSettings.getProperty("vate.client.session.shell") != null)
    {
      try
      {
        sessionShell = fileClientSettings.getProperty("vate.client.session.shell");
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
        clientSettingsFile = new File(vtURL, "vate-client.properties");
        if (!clientSettingsFile.exists())
        {
          clientSettingsFile = new File("vate-client.properties");
        }
      }
      else
      {
        clientSettingsFile = new File("vate-client.properties");
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
      
      if (fileClientSettings.getProperty("vate.client.session.user") != null)
      {
        try
        {
          sessionUser = fileClientSettings.getProperty("vate.client.session.user", sessionUser);
        }
        catch (Throwable e)
        {
          
        }
      }
      
      if (fileClientSettings.getProperty("vate.client.session.password") != null)
      {
        try
        {
          sessionPassword = fileClientSettings.getProperty("vate.client.session.password", sessionPassword);
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
      
//      if (fileClientSettings.getProperty("vate.client.proxy.authentication") != null)
//      {
//        try
//        {
//          if (fileClientSettings.getProperty("vate.client.proxy.authentication").toUpperCase().startsWith("E"))
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
      
      // if (fileClientSettings.getProperty("vate.client.session.lines") !=
      // null)
      // {
      // try
      // {
      // sessionLines =
      // fileClientSettings.getProperty("vate.client.session.lines");
      // }
      // catch (Throwable e)
      // {
      
      // }
      // }
      
      if (fileClientSettings.getProperty("vate.client.session.shell") != null)
      {
        try
        {
          sessionShell = fileClientSettings.getProperty("vate.client.session.shell");
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
  
  public void loadClientSettingsProperties(Properties properties)
  {
    loadFromConnectorToClient();
    
    if (properties.getProperty("vate.client.connection.mode") != null)
    {
      try
      {
        String mode = properties.getProperty("vate.client.connection.mode");
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
    
    if (properties.getProperty("vate.client.connection.port") != null)
    {
      try
      {
        int filePort = Integer.parseInt(properties.getProperty("vate.client.connection.port"));
        if (filePort > 0 && filePort < 65536)
        {
          hostPort = filePort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.connection.host") != null)
    {
      try
      {
        hostAddress = properties.getProperty("vate.client.connection.host", hostAddress);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.session.user") != null)
    {
      try
      {
        sessionUser = properties.getProperty("vate.client.session.user", sessionUser);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.session.password") != null)
    {
      try
      {
        sessionPassword = properties.getProperty("vate.client.session.password", sessionPassword);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.connection.nat.port") != null)
    {
      try
      {
        int fileNatPort = Integer.parseInt(properties.getProperty("vate.client.connection.nat.port"));
        if (fileNatPort > 0 && fileNatPort < 65536)
        {
          natPort = fileNatPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.encryption.type") != null)
    {
      try
      {
        encryptionType = properties.getProperty("vate.client.encryption.type", encryptionType);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.encryption.password") != null)
    {
      try
      {
        encryptionKey = properties.getProperty("vate.client.encryption.password", "").getBytes("UTF-8");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.proxy.type") != null)
    {
      try
      {
        proxyType = properties.getProperty("vate.client.proxy.type", proxyType);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.proxy.host") != null)
    {
      try
      {
        proxyAddress = properties.getProperty("vate.client.proxy.host", proxyAddress);
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.proxy.port") != null)
    {
      try
      {
        int fileProxyPort = Integer.parseInt(properties.getProperty("vate.client.proxy.port"));
        if (fileProxyPort > 0 && fileProxyPort < 65536)
        {
          proxyPort = fileProxyPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    
//    if (properties.getProperty("vate.client.proxy.authentication") != null)
//    {
//      try
//      {
//        if (properties.getProperty("vate.client.proxy.authentication").toUpperCase().startsWith("E"))
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
    
    if (properties.getProperty("vate.client.proxy.user") != null)
    {
      try
      {
        proxyUser = properties.getProperty("vate.client.proxy.user");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.proxy.password") != null)
    {
      try
      {
        proxyPassword = properties.getProperty("vate.client.proxy.password");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    if (properties.getProperty("vate.client.session.commands") != null)
    {
      try
      {
        sessionCommands = properties.getProperty("vate.client.session.commands");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    // if (properties.getProperty("vate.client.session.lines") != null)
    // {
    // try
    // {
    // sessionLines = properties.getProperty("vate.client.session.lines");
    // }
    // catch (Throwable e)
    // {
    
    // }
    // }
    
    if (properties.getProperty("vate.client.session.shell") != null)
    {
      try
      {
        sessionShell = properties.getProperty("vate.client.session.shell");
      }
      catch (Throwable e)
      {
        
      }
    }
    
    saveFromClientToConnector();
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
      else
      {
        VTConsole.print("VT>Press enter to start client:");
        // VTConsole.print("\nVT>Press enter to try connecting with server");
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
      manual = false;
      if (connectionDialog != null && !connectionDialog.isVisible())
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
        VTConsole.print("\nVT>Enter settings file(if available):");
      }
      else
      {
        VTConsole.print("VT>Enter settings file(if available):");
      }
      retry = true;
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
          loadClientSettingsFile(line);
          return;
        }
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
      }
      VTConsole.print("VT>Enter connection mode(active as A or passive as P, default:A):");
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
        if (line.toUpperCase().startsWith("P"))
        {
          active = false;
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
            VTConsole.print("VT>Invalid port!");
            hostPort = null;
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
          }
          if (hostPort != null)
          {
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
              VTConsole.print("VT>Enter encryption type(ISAAC(I)/VMPC(V)/SALSA(S)/HC(H)/ZUC(Z)):");
              line = VTConsole.readLine(false);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return;
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
          active = true;
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
            VTConsole.print("VT>Invalid port!");
            hostPort = null;
          }
          if (hostPort != null)
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
              VTConsole.print("VT>Enter proxy type(DIRECT as D, SOCKS as S, HTTP as H, ANY as A, default:A):");
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
          if (hostPort != null)
          {
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
              VTConsole.print("VT>Enter encryption type(ISAAC(I)/VMPC(V)/SALSA(S)/HC(H)/ZUC(Z)):");
              line = VTConsole.readLine(false);
              if (line == null)
              {
                VTRuntimeExit.exit(0);
              }
              else if (skipConfiguration)
              {
                return;
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
        if ((hostAddress != null && hostPort != null) && (sessionUser == null || sessionPassword == null || sessionUser.length() == 0 || sessionPassword.length() == 0))
        {
          VTConsole.print("VT>Enter session shell(null for default):");
          String shell = VTConsole.readLine(true);
          if (shell == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return;
          }
          setSessionShell(shell);
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
          setUser(user);
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
          setPassword(password);
        }
        if (hostPort != null)
        {
          VTConsole.print("VT>Enter session commands:");
          String command = VTConsole.readLine(true);
          if (command == null)
          {
            VTRuntimeExit.exit(0);
          }
          else if (skipConfiguration)
          {
            return;
          }
          setSessionCommands(command);
          // VTConsole.print("VT>Enter session lines:");
          // String lines = VTConsole.readLine(true);
          // if (lines == null)
          // {
          // VTExit.exit(0);
          // }
          // else if (skipConfiguration)
          // {
          // return;
          // }
          // setSessionLines(lines);
        }
      }
      catch (NumberFormatException e)
      {
        VTConsole.print("VT>Invalid port!");
        hostPort = null;
        proxyPort = null;
        //useProxyAuthentication = false;
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
      if (parameterName.contains("-SC"))
      {
        parameterValue = parameters[++i];
        sessionCommands = parameterValue;
      }
      // if (parameterName.contains("-SL"))
      // {
      // parameterValue = parameters[++i];
      // sessionLines = parameterValue;
      // }
      if (parameterName.contains("-SU"))
      {
        parameterValue = parameters[++i];
        sessionUser = parameterValue;
      }
      if (parameterName.contains("-SK"))
      {
        parameterValue = parameters[++i];
        sessionPassword = parameterValue;
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
    executorService.execute(new Runnable()
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
    // loadFileClientSettings();
    if (!VTConsole.isDaemon() && VTConsole.isGraphical())
    {
      VTConsole.initialize();
      VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Client - Console");
      connectionDialog = new VTClientConfigurationDialog(VTConsole.getFrame(), "Variable-Terminal " + VT.VT_VERSION + " - Client - Connection", true, this);
      inputMenuBar = new VTClientRemoteGraphicalConsoleMenuBar(connectionDialog);
      VTConsole.getFrame().setMenuBar(inputMenuBar);
      VTConsole.getFrame().pack();
//      try
//      {
//        trayIconInterface = new VTTrayIconInterface();
//        trayIconInterface.install(VTConsole.getFrame(), "Variable-Terminal - Client");
//      }
//      catch (Throwable t)
//      {
//        trayIconInterface = null;
//      }
    }
    else
    {
      VTConsole.initialize();
      VTConsole.setTitle("Variable-Terminal " + VT.VT_VERSION + " - Client - Console");
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
    VTConsole.print("VT>Variable-Terminal " + VT.VT_VERSION + " - Client - (c) " + VT.VT_YEAR + " wknishio@gmail.com\n" + 
    "VT>This software is under MIT license with no warranty, use at your own risk!\n");
    // + "VT>Press enter to start client:");
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
      //this.useProxyAuthentication = clientConnector.isUseProxyAuthentication();
      this.proxyUser = clientConnector.getProxyUser();
      this.proxyPassword = clientConnector.getProxyPassword();
      this.encryptionType = clientConnector.getEncryptionType();
      this.encryptionKey = clientConnector.getEncryptionKey();
      this.sessionCommands = clientConnector.getSessionCommands();
      // this.sessionLines = clientConnector.getSessionLines();
      this.sessionShell = clientConnector.getSessionShell();
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
      //clientConnector.setUseProxyAuthentication(useProxyAuthentication);
      clientConnector.setProxyUser(proxyUser);
      clientConnector.setProxyPassword(proxyPassword);
      clientConnector.setEncryptionType(encryptionType);
      clientConnector.setEncryptionKey(encryptionKey);
      clientConnector.setSessionCommands(sessionCommands);
      // clientConnector.setSessionLines(sessionLines);
      clientConnector.setSessionShell(sessionShell);
    }
  }
  
  public void run()
  {
    clientConnector = new VTClientConnector(this, new VTBlake3SecureRandom());
    clientConnector.setActive(active);
    clientConnector.setAddress(hostAddress);
    clientConnector.setPort(hostPort);
    clientConnector.setNatPort(natPort);
    clientConnector.setProxyType(proxyType);
    clientConnector.setProxyAddress(proxyAddress);
    clientConnector.setProxyPort(proxyPort);
    //clientConnector.setUseProxyAuthentication(useProxyAuthentication);
    clientConnector.setProxyUser(proxyUser);
    clientConnector.setProxyPassword(proxyPassword);
    clientConnector.setEncryptionType(encryptionType);
    clientConnector.setEncryptionKey(encryptionKey);
    clientConnector.setSessionCommands(sessionCommands);
    // clientConnector.setSessionLines(sessionLines);
    clientConnector.setSessionShell(sessionShell);
    for (VTClientSessionListener listener : listeners)
    {
      clientConnector.addSessionListener(listener);
    }
    clientConnector.run();
  }
  
  public void addSessionListener(VTClientSessionListener listener)
  {
    if (clientConnector != null)
    {
      clientConnector.addSessionListener(listener);
    }
    else
    {
      listeners.add(listener);
    }
  }
  
  public void removeSessionListener(VTClientSessionListener listener)
  {
    if (clientConnector != null)
    {
      clientConnector.removeSessionListener(listener);
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
  
//  public void enableTrayIcon()
//  {
//    if (trayIconInterface != null)
//    {
//      trayIconInterface.install(VTConsole.getFrame(), "Variable-Terminal - Client");
//    }
//  }
//
//  public void disableTrayIcon()
//  {
//    if (trayIconInterface != null)
//    {
//      trayIconInterface.removeTrayIcon();
//    }
//  }
//
//  public void displayTrayIconMessage(String caption, String text)
//  {
//    if (trayIconInterface != null)
//    {
//      trayIconInterface.displayMessage(caption, text);
//    }
//  }
}