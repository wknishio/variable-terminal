package org.vash.vate.socket.proxy.launcher;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.Collection;
import java.util.Properties;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.vash.vate.parser.VTArgumentParser;
import org.vash.vate.parser.VTPropertiesBuilder;
import org.vash.vate.security.VTCredential;
import org.vash.vate.socket.proxy.VTSocksHttpProxyAuthenticatorNone;
import org.vash.vate.socket.proxy.VTSocksHttpProxyAuthenticatorUsernamePassword;
import org.vash.vate.socket.proxy.VTSocksMultipleUserValidation;
import org.vash.vate.socket.proxy.VTSocksProxyServer;

public class VTSocksPlusHttpProxyServer
{
  private int port = 1080;
  private String bind = null;
  private String host = null;
  private final Collection<VTCredential> userCredentials = new ConcurrentLinkedQueue<VTCredential>();
  private final ExecutorService executorService;
  
  public VTSocksPlusHttpProxyServer()
  {
    this.executorService = Executors.newCachedThreadPool(new ThreadFactory()
    {
      public Thread newThread(Runnable runnable)
      {
        Thread created = new Thread(null, runnable, runnable.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
  }
  
  public void loadSettingsFile(String settingsFile) throws Exception
  {
    File proxySettingsFile = new File(settingsFile);
    InputStream proxySettingsReader = new FileInputStream(proxySettingsFile);
    Properties fileProxySettings = VTPropertiesBuilder.loadProperties(proxySettingsReader, "UTF-8");
    if (fileProxySettings.getProperty("proxy.port") != null)
    {
      try
      {
        int proxyPort = Integer.parseInt(fileProxySettings.getProperty("proxy.port"));
        if (proxyPort > 0 && proxyPort < 65536)
        {
          port = proxyPort;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    if (fileProxySettings.getProperty("proxy.bind") != null)
    {
      try
      {
        String proxyBind = fileProxySettings.getProperty("proxy.bind");
        if (proxyBind != null && proxyBind.length() > 0)
        {
          bind = proxyBind;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    if (fileProxySettings.getProperty("proxy.host") != null)
    {
      try
      {
        String proxyHost = fileProxySettings.getProperty("proxy.host");
        if (proxyHost != null && proxyHost.length() > 0)
        {
          host = proxyHost;
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    if (fileProxySettings.getProperty("proxy.auth") != null)
    {
      try
      {
        String proxyAuth = fileProxySettings.getProperty("proxy.auth");
        setMultipleUserCredentials(proxyAuth);
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
          loadSettingsFile(parameterName);
        }
        catch (Throwable t)
        {
          
        }
      }
      if (parameterName.contains("-P"))
      {
        parameterValue = parameters[++i];
        try
        {
          int proxyPort = Integer.parseInt(parameterValue);
          if (proxyPort > 0 && proxyPort < 65536)
          {
            port = proxyPort;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      if (parameterName.contains("-B"))
      {
        parameterValue = parameters[++i];
        try
        {
          String proxyBind = parameterValue;
          if (proxyBind != null && proxyBind.length() > 0)
          {
            bind = proxyBind;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      if (parameterName.contains("-H"))
      {
        parameterValue = parameters[++i];
        try
        {
          String proxyHost = parameterValue;
          if (proxyHost != null && proxyHost.length() > 0)
          {
            host = proxyHost;
          }
        }
        catch (Throwable e)
        {
          
        }
      }
      if (parameterName.contains("-A"))
      {
        parameterValue = parameters[++i];
        setMultipleUserCredentials(parameterValue);
      }
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
      return false;
    }
  }
  
  public void addUserCredential(String user, String password)
  {
    userCredentials.add(new VTCredential(user, password));
  }
  
  public static void main(String[] args)
  {
    VTSocksPlusHttpProxyServer socksPlusHttpProxyServer = new VTSocksPlusHttpProxyServer();
    try
    {
      socksPlusHttpProxyServer.parseParameters(args);
    }
    catch (Throwable t)
    {
      
    }
    VTSocksMultipleUserValidation validation = null;
    if (socksPlusHttpProxyServer.userCredentials.size() > 0)
    {
      String[] usernames = new String[socksPlusHttpProxyServer.userCredentials.size()];
      String[] passwords = new String[socksPlusHttpProxyServer.userCredentials.size()];
      int i = 0;
      for (VTCredential credential : socksPlusHttpProxyServer.userCredentials)
      {
        usernames[i] = credential.getUser();
        passwords[i] = credential.getPassword();
        i++;
      }
      validation = new VTSocksMultipleUserValidation(usernames, passwords); 
    }
    else
    {
      validation = null;
    }
    if (validation != null)
    {
      VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorUsernamePassword(validation, socksPlusHttpProxyServer.executorService, null, null, 0, socksPlusHttpProxyServer.bind), socksPlusHttpProxyServer.executorService, false, true, null, null, 0);
      socksServer.start(socksPlusHttpProxyServer.port, 5, socksPlusHttpProxyServer.host, socksPlusHttpProxyServer.bind);
    }
    else
    {
      VTSocksProxyServer socksServer = new VTSocksProxyServer(new VTSocksHttpProxyAuthenticatorNone(socksPlusHttpProxyServer.executorService, null, null, 0, socksPlusHttpProxyServer.bind), socksPlusHttpProxyServer.executorService, false, true, null, null, 0);
      socksServer.start(socksPlusHttpProxyServer.port, 5, socksPlusHttpProxyServer.host, socksPlusHttpProxyServer.bind);
    }
  }
}