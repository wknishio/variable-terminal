package org.vate.server.session;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vate.graphics.clipboard.VTClipboardTransferTask;
import org.vate.graphics.control.VTAWTControlProvider;
import org.vate.nativeutils.VTNativeUtils;
import org.vate.ping.VTNanoPingListener;
import org.vate.ping.VTNanoPingService;
import org.vate.server.VTServer;
import org.vate.server.connection.VTServerConnection;
import org.vate.server.console.remote.VTServerRemoteConsoleReader;
import org.vate.server.console.shell.VTServerShellExitListener;
import org.vate.server.console.shell.VTServerShellOutputWriter;
import org.vate.server.filesystem.VTServerFileModifyOperation;
import org.vate.server.filesystem.VTServerFileScanOperation;
import org.vate.server.filesystem.VTServerFileSystemRootsResolver;
import org.vate.server.filesystem.VTServerZipFileOperation;
import org.vate.server.filetransfer.VTFileTransferServer;
import org.vate.server.graphicsdevices.VTServerGraphicsDeviceResolver;
import org.vate.server.graphicsmode.VTGraphicsModeServer;
import org.vate.server.network.VTServerHostResolver;
import org.vate.server.network.VTServerNetworkInterfaceResolver;
import org.vate.server.opticaldrive.VTServerOpticalDriveOperation;
import org.vate.server.print.VTServerPrintDataTask;
import org.vate.server.print.VTServerPrintServiceResolver;
import org.vate.server.runtime.VTServerRuntimeExecutor;
import org.vate.server.screenshot.VTServerScreenshotTask;
import org.vate.tunnel.connection.VTTunnelConnection;
import org.vate.tunnel.connection.VTTunnelConnectionHandler;

import com.sun.jna.Platform;

public class VTServerSession
{
  private volatile boolean stoppingShell;
  private volatile boolean restartingShell;
  private volatile boolean runningAudio;
  private volatile boolean echoCommands;
  private volatile long sessionLocalNanoDelay;
  private volatile long sessionRemoteNanoDelay;
  private Process shellProcess;
  private ProcessBuilder shellBuilder;
  private Map<String, String> shellEnvironment;
  private File runtimeDirectory;
  private File shellDirectory;
  private String login;
  private InputStream shellInputStream;
  private InputStream shellErrorStream;
  private OutputStream shellOutputStream;
  private Reader shellOutputReader;
  private Reader shellErrorReader;
  private Writer shellCommandExecutor;
  private VTServer server;
  private VTServerConnection connection;
  private VTAWTControlProvider controlProvider;
  private VTAWTScreenCaptureProvider viewProvider;
  private VTAWTScreenCaptureProvider screenshotProvider;
  private VTServerRemoteConsoleReader clientReader;
  private VTServerShellOutputWriter shellOutputWriter;
  // private VTServerShellErrorWriter shellErrorWriter;
  private VTServerShellExitListener shellExitListener;
  private VTFileTransferServer fileTransferServer;
  private VTServerScreenshotTask screenshotTask;
  private VTServerRuntimeExecutor runtimeExecutor;
  private VTGraphicsModeServer graphicsServer;
  private VTServerFileScanOperation fileScanOperation;
  private VTServerFileModifyOperation fileModifyOperation;
  private VTServerZipFileOperation zipFileOperation;
  private VTServerHostResolver hostResolver;
  private VTServerNetworkInterfaceResolver networkInterfaceResolver;
  private VTServerPrintServiceResolver printServiceResolver;
  private VTServerOpticalDriveOperation opticalDriveOperation;
  private VTServerSessionListViewer connectionListViewer;
  private VTServerFileSystemRootsResolver fileSystemRootsResolver;
  // private VTServerPrintTextTask printTextTask;
  // private VTServerPrintFileTask printFileTask;
  private VTServerPrintDataTask printDataTask;
  // private VTServerDefaultPrintServiceResolver defaultPrintServiceResolver;
  private VTClipboardTransferTask clipboardTransferTask;
  private VTServerGraphicsDeviceResolver graphicsDeviceResolver;
  private VTTunnelConnectionHandler tcpTunnelsHandler;
  private VTTunnelConnectionHandler socksTunnelsHandler;
  private VTNanoPingService pingService;

  private Map<String, Closeable> sessionResources;

  private ExecutorService threads;

  private volatile Charset shellCharset;

  public VTServerSession(VTServer server, VTServerConnection connection)
  {
    this.server = server;
    this.connection = connection;
    this.sessionResources = Collections.synchronizedMap(new LinkedHashMap<String, Closeable>());
  }

  public void initialize()
  {
    this.shellCharset = null;
    this.threads = Executors.newCachedThreadPool(new ThreadFactory()
    {
      public Thread newThread(Runnable r)
      {
        Thread created = new Thread(null, r, r.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    this.stoppingShell = false;
    this.restartingShell = false;
    this.runningAudio = false;
    this.echoCommands = false;

    this.clientReader = new VTServerRemoteConsoleReader(this);
    this.shellOutputWriter = new VTServerShellOutputWriter(this);
    // this.shellErrorWriter = new VTServerShellErrorWriter(this);
    this.shellExitListener = new VTServerShellExitListener(this);

    this.controlProvider = new VTAWTControlProvider();
    this.viewProvider = new VTAWTScreenCaptureProvider();
    this.screenshotProvider = new VTAWTScreenCaptureProvider();
    this.fileTransferServer = new VTFileTransferServer(this);
    this.screenshotTask = new VTServerScreenshotTask(this);
    this.runtimeExecutor = new VTServerRuntimeExecutor(this);
    this.graphicsServer = new VTGraphicsModeServer(this);
    this.fileScanOperation = new VTServerFileScanOperation(this);
    this.fileModifyOperation = new VTServerFileModifyOperation(this);
    this.zipFileOperation = new VTServerZipFileOperation(this);
    this.opticalDriveOperation = new VTServerOpticalDriveOperation(this);
    this.hostResolver = new VTServerHostResolver(this);
    this.networkInterfaceResolver = new VTServerNetworkInterfaceResolver(this);
    this.printServiceResolver = new VTServerPrintServiceResolver(this);
    this.connectionListViewer = new VTServerSessionListViewer(this);
    this.fileSystemRootsResolver = new VTServerFileSystemRootsResolver(this);
    this.clipboardTransferTask = new VTClipboardTransferTask();
    this.graphicsDeviceResolver = new VTServerGraphicsDeviceResolver(this);
    // this.printTextTask = new VTServerPrintTextTask(this);
    // this.printFileTask = new VTServerPrintFileTask(this);
    this.printDataTask = new VTServerPrintDataTask(this);
    this.tcpTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(VTTunnelConnection.TUNNEL_TYPE_TCP, threads), threads);
    this.socksTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(VTTunnelConnection.TUNNEL_TYPE_SOCKS, threads), threads);
    this.pingService = new VTNanoPingService(VT.VT_PING_SERVICE_INTERVAL, true);
    this.pingService.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long localNanoDelay, long remoteNanoDelay)
      {
        sessionLocalNanoDelay = localNanoDelay;
        sessionRemoteNanoDelay = remoteNanoDelay;
        // VTConsole.println("server localNanoDelay:" +
        // localNanoDelay);
        // VTConsole.println("server remoteNanoDelay:" +
        // remoteNanoDelay);
      }
    });
    setShellBuilder(null, null, null);
  }

  public ExecutorService getSessionThreads()
  {
    return threads;
  }

  public Closeable getSessionResource(String key)
  {
    return sessionResources.get(key);
  }

  public void addSessionResource(String key, Closeable value)
  {
    sessionResources.put(key, value);
  }

  public Closeable removeSessionResource(String key)
  {
    return sessionResources.remove(key);
  }

  public boolean setShellEncoding(String shellEncoding)
  {
    if (shellEncoding == null)
    {
      shellCharset = null;
      return true;
    }
    try
    {
      shellCharset = Charset.forName(shellEncoding);
      return true;
    }
    catch (Throwable e)
    {

    }
    return false;
  }

  public void setShellBuilder(String[] command, String[] names, String[] values)
  {
    if (command == null)
    {
      if (Platform.isWindows())
      {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS 95") || System.getProperty("os.name").toUpperCase().startsWith("WINDOWS 98"))
        {
          // almost impossible to enter here now
          this.shellBuilder = new ProcessBuilder("command.com", "/A", "/E:1900");
          // this.shellBuilder.directory(this.getRuntimeBuilderWorkingDirectory());
          this.shellDirectory = shellBuilder.directory();
          if (this.shellDirectory == null)
          {
            this.shellDirectory = new File(System.getProperty("user.dir"));
          }
          this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
          this.shellEnvironment = this.shellBuilder.environment();
          if (this.shellEnvironment != null)
          {
            this.shellEnvironment.remove("PROMPT");
            this.shellEnvironment.put("PROMPT", "$P$G");
            // this.shellEnvironment.put("PROMPT", "VT>");
          }
          // shell = server.getRun().exec(new String[]{"command.com",
          // "/E:1900"},
          // VTSystemUtils.changeEnvArray(VTSystemUtils.getEnvMapAsArray(System.getenv()),
          // "PROMPT", "$P$G"));
          // shell = server.getRun().exec(new String[]{"command.com",
          // "/E:1900"});
          // shell = server.getRun().exec(new String[]{"sh", "-s",
          // "-i"},
          // VTSystemUtils.changeEnvArray(VTSystemUtils.changeEnvArray(VTSystemUtils.getEnvMapAsArray(System.getenv()),
          // "PS1", "$PWD>"), "PROMPT", "]pwd'>"));
        }
        else
        {
          // this.shellBuilder = new ProcessBuilder("cmd.exe", "/A",
          // "/E:ON", "/F:ON");
          // this.shellBuilder = new ProcessBuilder("cmd.exe",
          // "/E:ON", "/F:ON", "/Q", "/K", "chcp", "65001");
          // this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A",
          // "/K", "@chcp 65001");
          // this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/K",
          // "@chcp 65001>nul");
          this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A");
          // this.shellBuilder = new ProcessBuilder("sh", "-s", "-i",
          // "+m", "&");
          // this.shellBuilder.directory(this.getRuntimeBuilderWorkingDirectory());
          this.shellDirectory = shellBuilder.directory();
          if (this.shellDirectory == null)
          {
            this.shellDirectory = new File(System.getProperty("user.dir"));
          }
          this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
          this.shellEnvironment = this.shellBuilder.environment();
          if (this.shellEnvironment != null)
          {
            this.shellEnvironment.remove("PROMPT");
            this.shellEnvironment.put("PROMPT", "$P$G");
            // this.shellEnvironment.put("PROMPT", "VT>");
          }
          // shell = server.getRun().exec(new String[]{"cmd.exe",
          // "/E:ON", "/F:ON"},
          // VTSystemUtils.changeEnvArray(VTSystemUtils.getEnvMapAsArray(System.getenv()),
          // "PROMPT", "$P$G"));
          // shell = server.getRun().exec(new String[]{"cmd.exe",
          // "/E:ON", "/F:ON", "/Q"});
          // shell = server.getRun().exec(new String[]{"sh", "-s",
          // "-i"},
          // VTSystemUtils.changeEnvArray(VTSystemUtils.changeEnvArray(VTSystemUtils.getEnvMapAsArray(System.getenv()),
          // "PS1", "$PWD>"), "PROMPT", "]pwd'>"));
        }
      }
      else
      {
        // this.shellBuilder = new ProcessBuilder("/bin/sh", "-s", "-i", "-l");
        // this.shellBuilder = new ProcessBuilder("/bin/sh", "-s", "-i", "+m", "&");
        // this.shellBuilder = new ProcessBuilder("/bin/sh", "-l", "-s", "-i");
//				if (VTConsole.isDaemon())
//				{
//					this.shellBuilder = new ProcessBuilder("/bin/sh", "-l", "-s");
//				}
//				else
//				{
//					this.shellBuilder = new ProcessBuilder("/bin/sh", "-l", "-s");
//				}
        this.shellBuilder = new ProcessBuilder("/bin/sh", "-l", "-s");
        // this.shellBuilder.directory(this.getRuntimeBuilderWorkingDirectory());
        // this.shellBuilder = new ProcessBuilder("nohup", "/bin/sh",
        // "-s", "-i", "&");
        // this.shellBuilder = new ProcessBuilder("nohup /bin/sh -s
        // -i");
        this.shellDirectory = shellBuilder.directory();
        if (this.shellDirectory == null)
        {
          this.shellDirectory = new File(System.getProperty("user.dir"));
        }
        this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
        this.shellEnvironment = this.shellBuilder.environment();
        if (this.shellEnvironment != null)
        {
          this.shellEnvironment.remove("PS1");
          this.shellEnvironment.put("PS1", "$PWD>");
          this.shellEnvironment.remove("PROMPT");
          this.shellEnvironment.put("PROMPT", "'pwd'>");
//					this.shellEnvironment.remove("LANG");
//					this.shellEnvironment.put("LANG", "en_US.UTF-8");
//					this.shellEnvironment.remove("LANGUAGE");
//					this.shellEnvironment.put("LANGUAGE", "en_US.UTF-8");
//					this.shellEnvironment.remove("LC_TYPE");
//					this.shellEnvironment.put("LC_TYPE", "en_US.UTF-8");
//					this.shellEnvironment.remove("LC_ALL");
//					this.shellEnvironment.put("LC_ALL", "en_US.UTF-8");
        }
        // VTNativeUtils.system("set +m");
        // shell = server.getRun().exec(new String[]{"/bin/sh"});
        // shell = server.getRun().exec(new String[]{"/bin/sh", "-s",
        // "-i"},
        // VTSystemUtils.changeEnvArray(VTSystemUtils.changeEnvArray(VTSystemUtils.getEnvMapAsArray(System.getenv()),
        // "PS1", "\\w>"), "PROMPT", "]pwd'>"));
        // shell = server.getRun().exec(new String[]{"/bin/sh", "-s",
        // "-i"},
        // VTSystemUtils.changeEnvArray(VTSystemUtils.changeEnvArray(VTSystemUtils.getEnvMapAsArray(System.getenv()),
        // "PS1", "$PWD>"), "PROMPT", "]pwd'>"));
      }
    }
    else
    {
      this.shellBuilder = new ProcessBuilder(command);
      // this.shellBuilder.directory(this.getRuntimeBuilderWorkingDirectory());
      this.shellDirectory = shellBuilder.directory();
      if (this.shellDirectory == null)
      {
        this.shellDirectory = new File(System.getProperty("user.dir"));
      }
      this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
      if (names != null && values != null && (names.length == values.length))
      {
        for (int i = 0; i < names.length; i++)
        {
          this.shellEnvironment.remove(names[i]);
          this.shellEnvironment.put(names[i], values[i]);
        }
      }
    }
    this.shellBuilder.redirectErrorStream(true);
  }

  public long getLocalNanoDelay()
  {
    return sessionLocalNanoDelay;
  }

  public long getRemoteNanoDelay()
  {
    return sessionRemoteNanoDelay;
  }

  public String getLogin()
  {
    return login;
  }

  public void setLogin(String login)
  {
    this.login = login;
  }

  public boolean isStoppingShell()
  {
    return stoppingShell;
  }

  public void setStoppingShell(boolean stoppingShell)
  {
    this.stoppingShell = stoppingShell;
  }

  public boolean isRestartingShell()
  {
    return restartingShell;
  }

  public void setRestartingShell(boolean restartingShell)
  {
    this.restartingShell = restartingShell;
  }

  public VTServerRemoteConsoleReader getClientReader()
  {
    return clientReader;
  }

  public Process getShell()
  {
    return shellProcess;
  }

  public Map<String, String> getShellEnvironment()
  {
    return shellEnvironment;
  }

  public File getRuntimeBuilderWorkingDirectory()
  {
    return runtimeDirectory;
  }

  public void setRuntimeBuilderWorkingDirectory(File runtimeDirectory)
  {
    this.runtimeDirectory = runtimeDirectory;
  }

  public File getShellDirectory()
  {
    return shellDirectory;
  }

  public void setShellDirectory(File shellDirectory)
  {
    this.shellDirectory = shellDirectory;
  }

  public VTServerConnection getConnection()
  {
    return connection;
  }

  public VTAWTControlProvider getControlProvider()
  {
    return controlProvider;
  }

  public VTAWTScreenCaptureProvider getViewProvider()
  {
    return viewProvider;
  }

  public VTAWTScreenCaptureProvider getScreenshotProvider()
  {
    return screenshotProvider;
  }

  public VTServerShellExitListener getShellExitListener()
  {
    return shellExitListener;
  }

  public VTServerHostResolver getHostResolver()
  {
    return hostResolver;
  }

  public VTServerShellOutputWriter getOutputWriter()
  {
    return shellOutputWriter;
  }

  public VTServerRuntimeExecutor getRuntimeExecutor()
  {
    return runtimeExecutor;
  }

  public VTFileTransferServer getFileTransferServer()
  {
    return fileTransferServer;
  }

  public VTServer getServer()
  {
    return server;
  }

  public VTServerScreenshotTask getScreenshotTask()
  {
    return screenshotTask;
  }

  public InputStream getShellInputStream()
  {
    return shellInputStream;
  }

  public InputStream getShellErrorStream()
  {
    return shellErrorStream;
  }

  public OutputStream getShellOutputStream()
  {
    return shellOutputStream;
  }

  public Reader getShellOutputReader()
  {
    return shellOutputReader;
  }

  public Reader getShellErrorReader()
  {
    return shellErrorReader;
  }

  public Writer getShellCommandExecutor()
  {
    return shellCommandExecutor;
  }

  public VTGraphicsModeServer getGraphicsServer()
  {
    return graphicsServer;
  }

  public VTServerFileScanOperation getFileScanOperation()
  {
    return fileScanOperation;
  }

  public VTServerFileModifyOperation getFileModifyOperation()
  {
    return fileModifyOperation;
  }

  public VTServerZipFileOperation getZipFileOperation()
  {
    return zipFileOperation;
  }

  public VTServerNetworkInterfaceResolver getNetworkInterfaceResolver()
  {
    return networkInterfaceResolver;
  }

  public VTServerPrintServiceResolver getPrintServiceResolver()
  {
    return printServiceResolver;
  }

  public VTServerOpticalDriveOperation getOpticalDriveOperation()
  {
    return opticalDriveOperation;
  }

  public VTServerSessionListViewer getConnectionListViewer()
  {
    return connectionListViewer;
  }

  public VTServerFileSystemRootsResolver getFileSystemRootsResolver()
  {
    return fileSystemRootsResolver;
  }

  // public VTServerPrintTextTask getPrintTextTask()
  // {
  // return printTextTask;
  // }

  // public VTServerPrintFileTask getPrintFileTask()
  // {
  // return printFileTask;
  // }

  public VTServerPrintDataTask getPrintDataTask()
  {
    return printDataTask;
  }

  public VTClipboardTransferTask getClipboardTransferTask()
  {
    return clipboardTransferTask;
  }

  public VTServerGraphicsDeviceResolver getGraphicsDeviceResolver()
  {
    return graphicsDeviceResolver;
  }

  public VTNanoPingService getNanoPingService()
  {
    return pingService;
  }

  public VTTunnelConnectionHandler getTCPTunnelsHandler()
  {
    return tcpTunnelsHandler;
  }

  public VTTunnelConnectionHandler getSOCKSTunnelsHandler()
  {
    return socksTunnelsHandler;
  }

  public boolean isRunningAudio()
  {
    return runningAudio;
  }

  public void setRunningAudio(boolean runningAudio)
  {
    this.runningAudio = runningAudio;
  }

  public boolean isStopped()
  {
    return clientReader.isStopped() || !connection.isConnected();
    /* || outputWriter.isStopped() || exitListener.isStopped() */
    // || !connection.isConnected();
  }

  public void setStopped(boolean stopped)
  {
    connection.closeSockets();
    // System.out.println("connection.closeSockets");
    clientReader.setStopped(stopped);
    // System.out.println("clientReader.setStopped");
    shellOutputWriter.setStopped(stopped);
    // shellErrorWriter.setStopped(stopped);
    // System.out.println("shellOutputWriter.setStopped");
    shellExitListener.setStopped(stopped);
    // System.out.println("shellExitListener.setStopped");
    fileTransferServer.getHandler().getSession().getTransaction().setStopped(stopped);
    // System.out.println("fileTransferServer.setStopped");
    // runtimeExecutor.setStopped(stopped);
    graphicsServer.setStopped(stopped);
    // System.out.println("graphicsServer.setStopped");
    // printTextTask.setStopped(stopped);
    // System.out.println("printTextTask.setStopped");
    // printFileTask.setStopped(stopped);
    // System.out.println("printFileTask.setStopped");
    printDataTask.setStopped(stopped);
    pingService.setStopped(stopped);
    pingService.ping();
    // System.out.println("pingService.setStopped");
    /*
     * if (fileCopyOperation != null) { fileCopyOperation.setStopped(stopped); }
     */
  }

  public void restartShell()
  {
    /*
     * try { } catch (Throwable e) { }
     */
    stopShell();
    waitShell();
    tryStopShellThreads();
    waitShellThreads();
    startShell();
    restartShellThreads();
  }

  public void startShell()
  {
    boolean started = false;
    try
    {
      if (restartingShell)
      {
        /*
         * connection.getResultWriter(). write("\nVT>Starting remote shell...\nVT>");
         * connection.getResultWriter().flush();
         */
      }
      else
      {
        connection.getResultWriter().write("\nVT>Starting remote shell...");
        connection.getResultWriter().flush();
      }
      // shellBuilder.directory(getRuntimeBuilderWorkingDirectory());
      shellDirectory = shellBuilder.directory();
      if (shellDirectory == null)
      {
        shellDirectory = new File(System.getProperty("user.dir"));
      }
      shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
      shellEnvironment = shellBuilder.environment();
      shellProcess = shellBuilder.start();
      shellInputStream = shellProcess.getInputStream();
      shellErrorStream = shellProcess.getErrorStream();
      shellOutputStream = shellProcess.getOutputStream();

      // shellOutputReader = new BufferedReader(new
      // InputStreamReader(shellInputStream), 1024 * 8);
      // shellErrorReader = new BufferedReader(new
      // InputStreamReader(shellErrorStream), 1024 * 8);
      if (shellCharset != null)
      {
        shellOutputReader = new InputStreamReader(shellInputStream, shellCharset);
        shellErrorReader = new InputStreamReader(shellErrorStream, shellCharset);
        shellCommandExecutor = new OutputStreamWriter(shellOutputStream, shellCharset);
      }
      else
      {
        shellOutputReader = new InputStreamReader(shellInputStream);
        shellErrorReader = new InputStreamReader(shellErrorStream);
        shellCommandExecutor = new OutputStreamWriter(shellOutputStream);
      }

      // if (Platform.isWindows())
      // {
      // shellOutputReader = new BufferedReader(new
      // InputStreamReader(shellInputStream, "UTF-8"), 1024 * 8);
      // shellErrorReader = new BufferedReader(new
      // InputStreamReader(shellErrorStream, "UTF-8"), 1024 * 8);
      // shellCommandExecutor = new BufferedWriter(new
      // OutputStreamWriter(shellOutputStream, "UTF-8"), 1024 * 8);
      // }
      // else
      // {
      // shellOutputReader = new BufferedReader(new
      // InputStreamReader(shellInputStream), 1024 * 8);
      // shellErrorReader = new BufferedReader(new
      // InputStreamReader(shellErrorStream), 1024 * 8);
      // shellCommandExecutor = new BufferedWriter(new
      // OutputStreamWriter(shellOutputStream), 1024 * 8);
      // }

      started = true;
      /*
       * if (!Platform.isWindows()) { VTNativeUtils.system("set +m"); }
       */
    }
    catch (IOException e)
    {
      // e.printStackTrace();
      VTConsole.print("\rVT>Remote shell not available!");
      try
      {
        if (restartingShell)
        {
          connection.getResultWriter().write("\nVT>Remote shell not available!" + "\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Remote shell not available!" + "\nVT>Enter *VTHELP or *VTHLP to list available commands in client console\nVT>\n");
          connection.getResultWriter().flush();
        }
      }
      catch (Throwable e1)
      {

      }
      started = false;
    }
    catch (SecurityException e)
    {
      VTConsole.print("\rVT>Security error detected!");
      try
      {
        if (restartingShell)
        {
          connection.getResultWriter().write("\nVT>Security error detected!" + "\nVT>");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Security error detected!" + "\nVT>Enter *VTHELP or *VTHLP to list available commands in client console\nVT>\n");
          connection.getResultWriter().flush();
        }
      }
      catch (Throwable e1)
      {

      }
      started = false;
    }
    try
    {
      if (started)
      {
        if (restartingShell)
        {
          connection.getResultWriter().write("\nVT>Remote shell started!" + "\nVT>\n\n");
          connection.getResultWriter().flush();
        }
        else
        {
          connection.getResultWriter().write("\nVT>Remote shell started!" + "\nVT>Enter *VTHELP or *VTHLP to list available commands in client console\nVT>\n");
          connection.getResultWriter().flush();
        }
      }
      else
      {

      }
    }
    catch (Throwable e)
    {

    }
    restartingShell = false;
  }

  public void startSession()
  {
    // screenshotProvider.initialize();
    runningAudio = false;
    clientReader.setStopped(false);
    shellOutputWriter.setStopped(false);
    // shellErrorWriter.setStopped(false);
    shellExitListener.setStopped(false);
    tcpTunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlInputStream());
    tcpTunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlOutputStream());
    tcpTunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    tcpTunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    socksTunnelsHandler.getConnection().setControlOutputStream(connection.getSocksControlOutputStream());
    socksTunnelsHandler.getConnection().setControlInputStream(connection.getSocksControlInputStream());
    socksTunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    socksTunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    pingService.setInputStream(connection.getPingInputStream());
    pingService.setOutputStream(connection.getPingOutputStream());
    // tunnelHandler.getConnection().start();
  }

  public void startSessionThreads()
  {
    pingService.startThread();
    clientReader.startThread();
    shellOutputWriter.startThread();
    // shellErrorWriter.startThread();
    shellExitListener.startThread();
    tcpTunnelsHandler.startThread();
    socksTunnelsHandler.startThread();
  }

  public void restartShellThreads()
  {
    shellOutputWriter.setStopped(false);
    // shellErrorWriter.setStopped(false);
    shellExitListener.setStopped(false);
    shellOutputWriter.startThread();
    // shellErrorWriter.startThread();
    shellExitListener.startThread();
  }

  public void waitSession()
  {
    /*
     * while (!isStopped()) { try { Thread.sleep(1); } catch (Throwable e) { return;
     * } }
     */
    synchronized (this)
    {
      while (!isStopped())
      {
        try
        {
          wait();
        }
        catch (Throwable e)
        {
          return;
        }
      }
    }
  }

  public void waitShell()
  {
    try
    {
      synchronized (shellProcess)
      {
        while (!shellOutputWriter.isStopped() && !shellExitListener.isStopped())
        {
          shellProcess.wait();
        }
      }
    }
    catch (Throwable e)
    {
      return;
    }
  }

  public void tryStopShellThreads()
  {
    shellOutputWriter.setStopped(true);
    // shellErrorWriter.setStopped(true);
    shellExitListener.setStopped(true);
  }

  public void tryStopSessionThreads()
  {
    // System.out.println("tryStopSessionThreads start");
    setStopped(true);
    // System.out.println("tryStopSessionThreads middle");
    try
    {
      for (Entry<String, Closeable> resource : sessionResources.entrySet())
      {
        try
        {
          resource.getValue().close();
        }
        catch (Throwable t)
        {

        }
      }
    }
    catch (Throwable t)
    {

    }
    runtimeExecutor.clear();
    if (fileScanOperation.aliveThread())
    {
      fileScanOperation.interruptThread();
      fileScanOperation.stopThread();
    }
    if (fileModifyOperation.aliveThread())
    {
      fileModifyOperation.interruptThread();
      fileModifyOperation.stopThread();
    }
    if (zipFileOperation.aliveThread())
    {
      zipFileOperation.interruptThread();
      zipFileOperation.stopThread();
    }
    if (clipboardTransferTask.aliveThread())
    {
      clipboardTransferTask.interruptThread();
    }
//		if (printTextTask.aliveThread())
//		{
//			printTextTask.interruptThread();
//			printTextTask.stopThread();
//		}
//		if (printFileTask.aliveThread())
//		{
//			printFileTask.interruptThread();
//			printFileTask.stopThread();
//		}
    if (printDataTask.aliveThread())
    {
      printDataTask.interruptThread();
      printDataTask.stopThread();
    }
    if (printServiceResolver.aliveThread())
    {
      printServiceResolver.interruptThread();
      printServiceResolver.stopThread();
    }
    if (screenshotTask.aliveThread())
    {
      screenshotTask.interruptThread();
      screenshotTask.stopThread();
    }
    if (runtimeExecutor.aliveThread())
    {
      runtimeExecutor.interruptThread();
      runtimeExecutor.stopThread();
    }
    if (hostResolver.aliveThread())
    {
      hostResolver.interruptThread();
      hostResolver.stopThread();
    }
    if (networkInterfaceResolver.aliveThread())
    {
      networkInterfaceResolver.interruptThread();
      networkInterfaceResolver.stopThread();
    }
    if (connectionListViewer.aliveThread())
    {
      connectionListViewer.interruptThread();
      connectionListViewer.stopThread();
    }
    if (fileSystemRootsResolver.aliveThread())
    {
      fileSystemRootsResolver.interruptThread();
      fileSystemRootsResolver.stopThread();
    }
    if (graphicsDeviceResolver.aliveThread())
    {
      graphicsDeviceResolver.interruptThread();
      graphicsDeviceResolver.stopThread();
    }
    if (opticalDriveOperation.aliveThread())
    {
      opticalDriveOperation.interruptThread();
      opticalDriveOperation.stopThread();
    }
    if (pingService.aliveThread())
    {
      pingService.interruptThread();
      pingService.stopThread();
    }
    tcpTunnelsHandler.getConnection().close();
    socksTunnelsHandler.getConnection().close();
    // System.out.println("tryStopSessionThreads end");
  }

  public void stopShell()
  {
    try
    {
      shellProcess.destroy();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellProcess.waitFor();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellOutputReader.close();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellErrorReader.close();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellCommandExecutor.close();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellProcess.getInputStream().close();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellProcess.getErrorStream().close();
    }
    catch (Throwable e)
    {

    }
    try
    {
      shellProcess.getOutputStream().close();
    }
    catch (Throwable e)
    {

    }
  }

  public void waitThreads()
  {
    // System.out.println("waitThreads");
    sessionResources.clear();
    try
    {
      clientReader.joinThread();
      // System.out.println("clientReader.joinThread()");
      shellOutputWriter.joinThread();
      // shellErrorWriter.joinThread();
      // System.out.println("shellOutputWriter.joinThread()");
      shellExitListener.joinThread();
      // System.out.println("shellExitListener.joinThread()");
      fileTransferServer.joinThread();
      // System.out.println("fileTransferServer.joinThread()");
      screenshotTask.joinThread();
      // System.out.println("screenshotTask.joinThread()");
      runtimeExecutor.joinThread();
      // System.out.println("runtimeExecutor.joinThread()");
      graphicsServer.joinThread();
      // System.out.println("graphicsServer.joinThread()");
      fileScanOperation.joinThread();
      // System.out.println("fileScanOperation.joinThread()");
      fileModifyOperation.joinThread();
      // System.out.println("fileModifyOperation.joinThread()");
      zipFileOperation.joinThread();
      // System.out.println("zipFileCompressOperation.joinThread()");
      hostResolver.joinThread();
      // System.out.println("hostResolver.joinThread()");
      networkInterfaceResolver.joinThread();
      // System.out.println("networkInterfaceResolver.joinThread()");
      printServiceResolver.joinThread();
      // System.out.println("printServiceResolver.joinThread()");
      // this.cdOperationThread.join();
      connectionListViewer.joinThread();
      // System.out.println("connectionListViewer.joinThread()");
      fileSystemRootsResolver.joinThread();
      // System.out.println("fileSystemRootsResolver.joinThread()");
      // this.printTextTask.joinThread();
      // System.out.println("printTextTask.joinThread()");
      // this.printFileTask.joinThread();
      // System.out.println("printFileTask.joinThread()");
      printDataTask.joinThread();
      graphicsDeviceResolver.joinThread();
      // System.out.println("graphicsDeviceResolver.joinThread()");
      clipboardTransferTask.joinThread();
      // System.out.println("clipboardTransferTask.joinThread()");
      tcpTunnelsHandler.joinThread();
      socksTunnelsHandler.joinThread();
      pingService.joinThread();
      // System.out.println("pingService.joinThread()");
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      // return;
    }
    controlProvider.dispose();
    viewProvider.dispose();
    screenshotProvider.dispose();
    screenshotTask.dispose();
  }

  public void waitShellThreads()
  {
    try
    {
      shellOutputWriter.joinThread();
      // shellErrorWriter.joinThread();
      shellExitListener.joinThread();
    }
    catch (Throwable e)
    {
      // return;
    }
  }

  public boolean isEchoCommands()
  {
    return echoCommands;
  }

  public void setEchoCommands(boolean echoCommands)
  {
    this.echoCommands = echoCommands;
  }
}