package org.vash.vate.server.session;

import java.io.Closeable;
import java.io.File;
import java.io.IOException;
import java.io.Reader;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

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
import org.vash.vate.server.filetransfer.VTFileTransferServer;
import org.vash.vate.server.graphicsdevices.VTServerGraphicsDeviceResolver;
import org.vash.vate.server.graphicslink.VTGraphicsLinkServer;
import org.vash.vate.server.network.VTServerHostResolver;
import org.vash.vate.server.network.VTServerNetworkInterfaceResolver;
import org.vash.vate.server.opticaldrive.VTServerOpticalDriveOperation;
import org.vash.vate.server.print.VTServerPrintDataTask;
import org.vash.vate.server.print.VTServerPrintServiceResolver;
import org.vash.vate.server.runtime.VTServerRuntimeExecutor;
import org.vash.vate.server.screenshot.VTServerScreenshotTask;
import org.vash.vate.shell.adapter.VTShellAdapter;
import org.vash.vate.shell.adapter.VTShellProcessor;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.connection.VTTunnelConnectionHandler;

import com.martiansoftware.jsap.CommandLineTokenizerMKII;

public class VTServerSession
{
  private boolean stoppingShell;
  private boolean restartingShell;
  private boolean echoCommands;
  private int echoState;
  private long sessionLocalNanoDelay;
  private long sessionRemoteNanoDelay;
  private VTShellAdapter shellAdapter;
  private String user;
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
  private VTGraphicsLinkServer graphicsServer;
  private VTServerFileScanOperation fileScanOperation;
  private VTServerFileModifyOperation fileModifyOperation;
  // private VTServerZipFileOperation zipFileOperation;
  private VTServerHostResolver hostResolver;
  private VTServerNetworkInterfaceResolver networkInterfaceResolver;
//  private VTServerURLInvoker urlInvoker;
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
  private VTTunnelConnectionHandler tunnelsHandler;
  // private VTTunnelConnectionHandler socksTunnelsHandler;
  private VTNanoPingService pingServiceClient;
  private VTNanoPingService pingServiceServer;
  private Collection<Closeable> sessionCloseables;
  private ExecutorService executorService;
  
  public VTServerSession(VTServer server, VTServerConnection connection)
  {
    this.server = server;
    this.connection = connection;
    this.executorService = server.getExecutorService();
    this.sessionCloseables = new ConcurrentLinkedQueue<Closeable>();
    this.shellAdapter = new VTShellAdapter(executorService);
  }
  
  public void initialize()
  {
    this.setEchoState(0);
    this.setEchoCommands(false);
    this.shellAdapter.setShellEncoding(null);
    
    this.stoppingShell = false;
    this.restartingShell = false;
    // this.runningAudio = false;
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
    this.graphicsServer = new VTGraphicsLinkServer(this);
    this.fileScanOperation = new VTServerFileScanOperation(this);
    this.fileModifyOperation = new VTServerFileModifyOperation(this);
    // this.zipFileOperation = new VTServerZipFileOperation(this);
    this.opticalDriveOperation = new VTServerOpticalDriveOperation(this);
    this.hostResolver = new VTServerHostResolver(this);
//    this.urlInvoker = new VTServerURLInvoker(this);
    this.networkInterfaceResolver = new VTServerNetworkInterfaceResolver(this);
    this.printServiceResolver = new VTServerPrintServiceResolver(this);
    this.connectionListViewer = new VTServerSessionListViewer(this);
    this.fileSystemRootsResolver = new VTServerFileSystemRootsResolver(this);
    this.clipboardTransferTask = new VTClipboardTransferTask(executorService);
    this.graphicsDeviceResolver = new VTServerGraphicsDeviceResolver(this);
    // this.printTextTask = new VTServerPrintTextTask(this);
    // this.printFileTask = new VTServerPrintFileTask(this);
    this.printDataTask = new VTServerPrintDataTask(this);
    this.tunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(executorService, sessionCloseables));
    // this.socksTunnelsHandler = new VTTunnelConnectionHandler(new
    // VTTunnelConnection(executor), executor);
    this.pingServiceClient = new VTNanoPingService(server.getPingIntervalMilliseconds(), server.getPingIntervalMilliseconds() / 2, false, executorService);
    this.pingServiceClient.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long nanoDelay)
      {
        sessionLocalNanoDelay = nanoDelay;
      }
    });
    this.pingServiceServer = new VTNanoPingService(server.getPingIntervalMilliseconds(), 0, true, executorService);
    this.pingServiceServer.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long nanoDelay)
      {
        sessionRemoteNanoDelay = nanoDelay;
      }
    });
    setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
    setShellBuilder(null, null, null);
  }
  
  public ExecutorService getExecutorService()
  {
    return executorService;
  }
  
  public void addSessionCloseable(Closeable value)
  {
    sessionCloseables.add(value);
  }
  
  public boolean removeSessionCloseable(Closeable value)
  {
    return sessionCloseables.remove(value);
  }
  
  public void clearSessionCloseables()
  {
    sessionCloseables.clear();
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
  
  public VTShellProcessor getShellProcessor()
  {
    return shellAdapter.getShellProcessor();
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
  
//  public VTServerURLInvoker getURLInvoker()
//  {
//    return urlInvoker;
//  }
  
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
  
  public VTGraphicsLinkServer getGraphicsServer()
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
  
  // public VTServerZipFileOperation getZipFileOperation()
  // {
  // return zipFileOperation;
  // }
  
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
  
  public void ping()
  {
    pingServiceClient.ping();
  }
  
  public void addPingListener(VTNanoPingListener listener)
  {
    pingServiceClient.addListener(listener);
  }
  
  public void removePingListener(VTNanoPingListener listener)
  {
    pingServiceClient.removeListener(listener);
  }
  
  public VTTunnelConnectionHandler getTunnelsHandler()
  {
    return tunnelsHandler;
  }
  
  // public VTTunnelConnectionHandler getSOCKSTunnelsHandler()
  // {
  // return socksTunnelsHandler;
  // }
  
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
    // shellErrorWriter.setStopped(stopped);
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
    pingServiceClient.setStopped(true);
    pingServiceServer.setStopped(true);
    pingServiceClient.ping();
    pingServiceServer.ping();
    // System.out.println("pingService.setStopped");
    /*
     * if (fileCopyOperation != null) { fileCopyOperation.setStopped(stopped); }
     */
  }
  
  public void restartShell()
  {
    setRestartingShell(true);
    stopShell();
    waitShell();
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
      else if (clientShell.trim().equalsIgnoreCase("N"))
      {
        setShellBuilder(new String[] {}, null, null);
      }
      else
      {
        setShellBuilder(CommandLineTokenizerMKII.tokenize(clientShell), null, null);
      }
    }
    else if (serverShell != null && serverShell.length() > 0)
    {
      if (serverShell.trim().equalsIgnoreCase("B"))
      {
        setShellType(VTShellProcessor.SHELL_TYPE_BEANSHELL);
      }
      else if (serverShell.trim().equalsIgnoreCase("N"))
      {
        setShellBuilder(new String[] {}, null, null);
      }
      else
      {
        setShellBuilder(CommandLineTokenizerMKII.tokenize(serverShell), null, null);
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
         * connection.getResultWriter().
         * write("\nVT>Starting remote shell...\nVT>");
         * connection.getResultWriter().flush();
         */
      }
      else
      {
        //connection.getResultWriter().write("\nVT>Starting remote shell...");
        //connection.getResultWriter().flush();
      }
      shellAdapter.startShell();
      
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
          connection.getResultWriter().write("\nVT>Remote shell not available!" + "\nVT>Enter *VTHELP or *VTHL to list available commands in client console\nVT>\n");
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
      // e.printStackTrace();
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
          connection.getResultWriter().write("\nVT>Security error detected!" + "\nVT>Enter *VTHELP or *VTHL to list available commands in client console\nVT>\n");
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
      // e.printStackTrace();
    }
    try
    {
      if (started)
      {
//        if (restartingShell)
//        {
//          connection.getResultWriter().write("\nVT>Remote shell started!" + "\nVT>\n\n");
//          connection.getResultWriter().flush();
//        }
//        else
//        {
//          connection.getResultWriter().write("\nVT>Remote shell started!" + "\nVT>Enter *VTHELP or *VTHL to list available commands in client console\nVT>\n");
//          connection.getResultWriter().flush();
//        }
      }
      else
      {
        
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void startSession() throws UnsupportedEncodingException
  {
    // screenshotProvider.initialize();
    // runningAudio = false;
    clientReader.setStopped(false);
    shellOutputWriter.setStopped(false);
    // shellErrorWriter.setStopped(false);
    shellExitListener.setStopped(false);
    tunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlInputStream());
    tunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlOutputStream());
    tunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    tunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    // socksTunnelsHandler.getConnection().setControlOutputStream(connection.getSocksControlOutputStream());
    // socksTunnelsHandler.getConnection().setControlInputStream(connection.getSocksControlInputStream());
    // socksTunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    // socksTunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    pingServiceClient.setInputStream(connection.getPingClientInputStream());
    pingServiceClient.setOutputStream(connection.getPingClientOutputStream());
    pingServiceServer.setInputStream(connection.getPingServerInputStream());
    pingServiceServer.setOutputStream(connection.getPingServerOutputStream());
    // tunnelHandler.getConnection().start();
  }
  
  public void startSessionThreads()
  {
    pingServiceServer.startThread();
    pingServiceClient.startThread();
    clientReader.startThread();
    shellOutputWriter.startThread();
    shellExitListener.startThread();
    tunnelsHandler.startThread();
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
     * while (!isStopped()) { try { Thread.sleep(1); } catch (Throwable e) {
     * return; } }
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
    // shellErrorWriter.setStopped(true);
    shellExitListener.setStopped(true);
  }
  
  @SuppressWarnings("unchecked")
  public void tryStopSessionThreads()
  {
    // System.out.println("tryStopSessionThreads start");
    stopTasks();
    // System.out.println("tryStopSessionThreads middle");
    try
    {
      for (Closeable closeable : sessionCloseables)
      {
        try
        {
          closeable.close();
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
    // if (zipFileOperation.aliveThread())
    // {
    // zipFileOperation.interruptThread();
    // zipFileOperation.stopThread();
    // }
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
//    if (urlInvoker.aliveThread())
//    {
//      urlInvoker.interruptThread();
//      urlInvoker.stopThread();
//    }
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
    tunnelsHandler.getConnection().close();
    // socksTunnelsHandler.getConnection().close();
    // System.out.println("tryStopSessionThreads end");
  }
  
  public String getShellEncoding()
  {
    return shellAdapter.getShellEncoding();
  }
  
  // public void changeShellCharset(Charset shellCharset)
  // {
  // shellAdapter.changeShellCharset(shellCharset);
  // }
  
  public void stopShell()
  {
    tryStopShellThreads();
    shellAdapter.stopShell();
  }
  
  public void waitThreads()
  {
    // System.out.println("waitThreads");
    //sessionResources.clear();
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
      // zipFileOperation.joinThread();
      // System.out.println("zipFileCompressOperation.joinThread()");
      hostResolver.joinThread();
//      urlInvoker.joinThread();
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
      tunnelsHandler.joinThread();
      // socksTunnelsHandler.joinThread();
      pingServiceClient.joinThread();
      pingServiceServer.joinThread();
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