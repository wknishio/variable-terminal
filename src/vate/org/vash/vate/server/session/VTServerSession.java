package org.vash.vate.server.session;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.vash.vate.VT;
import org.vash.vate.console.VTConsole;
import org.vash.vate.graphics.capture.VTAWTScreenCaptureProvider;
import org.vash.vate.graphics.clipboard.VTClipboardTransferTask;
import org.vash.vate.graphics.control.VTAWTControlProvider;
import org.vash.vate.ping.VTNanoPingListener;
import org.vash.vate.ping.VTNanoPingService;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.console.remote.VTServerRemoteConsoleReader;
//import org.vash.vate.server.console.shell.VTServerShellErrorWriter;
import org.vash.vate.server.console.shell.VTServerShellExitListener;
import org.vash.vate.server.console.shell.VTServerShellOutputWriter;
import org.vash.vate.server.filesystem.VTServerFileModifyOperation;
import org.vash.vate.server.filesystem.VTServerFileScanOperation;
import org.vash.vate.server.filesystem.VTServerFileSystemRootsResolver;
import org.vash.vate.server.filesystem.VTServerZipFileOperation;
import org.vash.vate.server.filetransfer.VTFileTransferServer;
import org.vash.vate.server.graphicsdevices.VTServerGraphicsDeviceResolver;
import org.vash.vate.server.graphicsmode.VTGraphicsModeServer;
import org.vash.vate.server.network.VTServerHostResolver;
import org.vash.vate.server.network.VTServerNetworkInterfaceResolver;
import org.vash.vate.server.network.VTServerURLInvoker;
import org.vash.vate.server.opticaldrive.VTServerOpticalDriveOperation;
import org.vash.vate.server.print.VTServerPrintDataTask;
import org.vash.vate.server.print.VTServerPrintServiceResolver;
import org.vash.vate.server.runtime.VTServerRuntimeExecutor;
import org.vash.vate.server.screenshot.VTServerScreenshotTask;
import org.vash.vate.shell.adapter.VTShellAdapter;
import org.vash.vate.shell.adapter.VTShellProcessor;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.connection.VTTunnelConnectionHandler;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTServerSession
{
  //private volatile boolean supressEchoShell;
  private volatile boolean stoppingShell;
  private volatile boolean restartingShell;
  //private volatile boolean runningAudio;
  
  private volatile boolean echoCommands;
  private volatile int echoState;
  private volatile long sessionLocalNanoDelay;
  private volatile long sessionRemoteNanoDelay;
  
  private VTShellAdapter shellAdapter;
    
  private String user;
  private VTServer server;
  private VTServerConnection connection;
  private VTAWTControlProvider controlProvider;
  private VTAWTScreenCaptureProvider viewProvider;
  private VTAWTScreenCaptureProvider screenshotProvider;
  private VTServerRemoteConsoleReader clientReader;
  private VTServerShellOutputWriter shellOutputWriter;
  //private VTServerShellErrorWriter shellErrorWriter;
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
  private VTServerURLInvoker urlInvoker;
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

  public VTServerSession(VTServer server, VTServerConnection connection)
  {
    this.server = server;
    this.connection = connection;
    this.sessionResources = Collections.synchronizedMap(new LinkedHashMap<String, Closeable>());
    this.shellAdapter = new VTShellAdapter();
  }

  public void initialize()
  {
    this.setEchoState(0);
    this.setEchoCommands(false);
    this.shellAdapter.setShellEncoding(null);
    this.threads = Executors.newCachedThreadPool(new ThreadFactory()
    {
      public Thread newThread(Runnable r)
      {
        Thread created = new Thread(null, r, r.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    this.shellAdapter.setThreads(threads);
    
//    try
//    {
//      int supressEchoShell = getConnection().getShellInputStream().read();
//      this.supressEchoShell = (supressEchoShell == 1);
//    }
//    catch (Throwable e)
//    {
//      // TODO Auto-generated catch block
//      //e.printStackTrace();
//    }
//    this.shellAdapter.setSuppressEchoShell(supressEchoShell);
    
    this.stoppingShell = false;
    this.restartingShell = false;
    //this.runningAudio = false;
    this.echoCommands = false;

    this.clientReader = new VTServerRemoteConsoleReader(this);
    this.shellOutputWriter = new VTServerShellOutputWriter(this);
    //this.shellErrorWriter = new VTServerShellErrorWriter(this);
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
    this.urlInvoker = new VTServerURLInvoker(this);
    this.networkInterfaceResolver = new VTServerNetworkInterfaceResolver(this);
    this.printServiceResolver = new VTServerPrintServiceResolver(this);
    this.connectionListViewer = new VTServerSessionListViewer(this);
    this.fileSystemRootsResolver = new VTServerFileSystemRootsResolver(this);
    this.clipboardTransferTask = new VTClipboardTransferTask();
    this.graphicsDeviceResolver = new VTServerGraphicsDeviceResolver(this);
    // this.printTextTask = new VTServerPrintTextTask(this);
    // this.printFileTask = new VTServerPrintFileTask(this);
    this.printDataTask = new VTServerPrintDataTask(this);
    this.tcpTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(threads), threads);
    this.socksTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(threads), threads);
    this.pingService = new VTNanoPingService(VT.VT_PING_SERVICE_INTERVAL_MILLISECONDS, true);
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
    setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
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
    return shellAdapter.setShellEncoding(shellEncoding);
  }

  public void setShellBuilder(String[] command, String[] names, String[] values)
  {
    shellAdapter.setShellBuilder(command, names, values);
  }

  public long getLocalNanoDelay()
  {
    return sessionLocalNanoDelay;
  }

  public long getRemoteNanoDelay()
  {
    return sessionRemoteNanoDelay;
  }

  public String getUser()
  {
    return user;
  }

  public void setUser(String user)
  {
    this.user = user;
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
  
  public void setRuntimeBuilderWorkingDirectory(File runtimeDirectory)
  {
    this.runtimeExecutor.setRuntimeBuilderWorkingDirectory(runtimeDirectory);
  }

  public VTShellProcessor getShell()
  {
    return shellAdapter.getShell();
  }

  public Map<String, String> getShellEnvironment()
  {
    return shellAdapter.getShellEnvironment();
  }

  public File getShellDirectory()
  {
    return shellAdapter.getShellDirectory();
  }

  public boolean setShellDirectory(File shellDirectory)
  {
    return shellAdapter.setShellDirectory(shellDirectory);
  }
  
  public Reader getShellOutputReader()
  {
    return shellAdapter.getShellOutputReader();
  }

  public Reader getShellErrorReader()
  {
    return shellAdapter.getShellErrorReader();
  }

  public Writer getShellCommandExecutor()
  {
    return shellAdapter.getShellCommandExecutor();
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
  
  public VTServerURLInvoker getURLInvoker()
  {
    return urlInvoker;
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

  public boolean isStopped()
  {
    return clientReader.isStopped() || !connection.isConnected();
    /* || outputWriter.isStopped() || exitListener.isStopped() */
    // || !connection.isConnected();
  }

  public void stopTasks()
  {
    connection.closeSockets();
    // System.out.println("connection.closeSockets");
    clientReader.setStopped(true);
    // System.out.println("clientReader.setStopped");
    shellOutputWriter.setStopped(true);
    //shellErrorWriter.setStopped(stopped);
    // System.out.println("shellOutputWriter.setStopped");
    shellExitListener.setStopped(true);
    // System.out.println("shellExitListener.setStopped");
    fileTransferServer.getHandler().getSession().getTransaction().setStopped(true);
    // System.out.println("fileTransferServer.setStopped");
    // runtimeExecutor.setStopped(stopped);
    graphicsServer.setStopped(true);
    // System.out.println("graphicsServer.setStopped");
    // printTextTask.setStopped(stopped);
    // System.out.println("printTextTask.setStopped");
    // printFileTask.setStopped(stopped);
    // System.out.println("printFileTask.setStopped");
    printDataTask.setStopped(true);
    pingService.setStopped(true);
    urlInvoker.close();
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
    setRestartingShell(true);
    stopShell();
    waitShell();
    tryStopShellThreads();
    waitShellThreads();
    startShell();
    restartShellThreads();
  }
  
  public void negotiateShell() throws IOException
  {
    String clientShell = connection.getCommandReader().readLine();
    String serverShell = server.getServerConnector().getSessionShell();
    if (clientShell != null && clientShell.length() > 0)
    {
      if (clientShell.trim().equalsIgnoreCase("B"))
      {
        setShellType(VTShellProcessor.SHELL_TYPE_BEANSHELL);
      }
      else
      {
        setShellBuilder(CommandLineTokenizer.tokenize(clientShell), null, null);
      }
    }
    else if (serverShell != null && serverShell.length() > 0)
    {
      if (serverShell.trim().equalsIgnoreCase("B"))
      {
        setShellType(VTShellProcessor.SHELL_TYPE_BEANSHELL);
      }
      else
      {
        setShellBuilder(CommandLineTokenizer.tokenize(serverShell), null, null);
      }
    }
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
      shellAdapter.startShell();
      
      started = true;
      /*
       * if (!Platform.isWindows()) { VTNativeUtils.system("set +m"); }
       */
    }
    catch (IOException e)
    {
      //e.printStackTrace();
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
      //e.printStackTrace();
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
    catch (Throwable e)
    {
      //e.printStackTrace();
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

  public void startSession() throws UnsupportedEncodingException
  {
    // screenshotProvider.initialize();
    //runningAudio = false;
    clientReader.setStopped(false);
    shellOutputWriter.setStopped(false);
    //shellErrorWriter.setStopped(false);
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
    //shellErrorWriter.startThread();
    shellExitListener.startThread();
    tcpTunnelsHandler.startThread();
    socksTunnelsHandler.startThread();
  }

  public void restartShellThreads()
  {
    shellOutputWriter.setStopped(false);
    //shellErrorWriter.setStopped(false);
    shellExitListener.setStopped(false);
    shellOutputWriter.startThread();
    //shellErrorWriter.startThread();
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
//    try
//    {
//      synchronized (shellProcess)
//      {
//        while (!shellOutputWriter.isStopped() && !shellExitListener.isStopped())
//        {
//          shellProcess.wait();
//        }
//      }
//    }
//    catch (Throwable e)
//    {
//      return;
//    }
    shellAdapter.waitShell();
  }

  public void tryStopShellThreads()
  {
    shellOutputWriter.setStopped(true);
    //shellErrorWriter.setStopped(true);
    shellExitListener.setStopped(true);
  }

  public void tryStopSessionThreads()
  {
    // System.out.println("tryStopSessionThreads start");
    stopTasks();
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
    if (urlInvoker.aliveThread())
    {
      urlInvoker.interruptThread();
      urlInvoker.stopThread();
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
  
  public String getShellEncoding()
  {
    return shellAdapter.getShellEncoding();
  }
  
  //public void changeShellCharset(Charset shellCharset)
  //{
    //shellAdapter.changeShellCharset(shellCharset);
  //}

  public void stopShell()
  {
    shellAdapter.stopShell();
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
      //shellErrorWriter.joinThread();
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
      urlInvoker.joinThread();
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
      //shellErrorWriter.joinThread();
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
  
  public int getEchoState()
  {
    return echoState;
  }
  
  public void setEchoState(int echoState)
  {
    this.echoState = echoState;
  }
  
  public void setShellType(int shellType)
  {
    this.shellAdapter.setShellType(shellType);
  }
}