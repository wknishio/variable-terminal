package org.vash.vate.client.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.console.remote.VTClientRemoteConsoleReader;
import org.vash.vate.client.console.remote.VTClientRemoteConsoleWriter;
import org.vash.vate.client.filetransfer.VTFileTransferClient;
import org.vash.vate.client.graphicslink.VTGraphicsLinkClient;
import org.vash.vate.graphics.clipboard.VTClipboardTransferTask;
import org.vash.vate.ping.VTNanoPingListener;
import org.vash.vate.ping.VTNanoPingService;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.connection.VTTunnelConnectionHandler;

public class VTClientSession
{
  private long sessionLocalNanoDelay;
  private long sessionRemoteNanoDelay;
  // private File workingDirectory;
  private VTClient client;
  private VTClientConnection connection;
  private VTClientRemoteConsoleReader serverReader;
  private VTClientRemoteConsoleWriter clientWriter;
  private VTFileTransferClient fileTransferClient;
  private VTGraphicsLinkClient graphicsClient;
  private VTClipboardTransferTask clipboardTransferTask;
  // private VTClientZipFileOperation zipFileOperation;
  private VTTunnelConnectionHandler tunnelsHandler;
  // private VTTunnelConnectionHandler socksTunnelsHandler;
  private VTNanoPingService pingServiceClient;
  private VTNanoPingService pingServiceServer;
  private Collection<Closeable> sessionCloseables;
  private ExecutorService executorService;
  
  public VTClientSession(VTClient client, VTClientConnection connection)
  {
    this.client = client;
    this.connection = connection;
    this.executorService = client.getExecutorService();
    this.sessionCloseables = new ConcurrentLinkedQueue<Closeable>();
  }
  
  public void initialize()
  {
    // this.runningAudio = false;
    this.serverReader = new VTClientRemoteConsoleReader(this);
    this.clientWriter = new VTClientRemoteConsoleWriter(this);
    this.fileTransferClient = new VTFileTransferClient(this);
    this.graphicsClient = new VTGraphicsLinkClient(this);
    this.clipboardTransferTask = new VTClipboardTransferTask(executorService);
    // this.zipFileOperation = new VTClientZipFileOperation(this);
    this.tunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(executorService, sessionCloseables));
    // this.socksTunnelsHandler = new VTTunnelConnectionHandler(new
    // VTTunnelConnection(executor), executor);
    this.pingServiceClient = new VTNanoPingService(client.getPingIntervalMilliseconds(), 0, false, executorService);
    this.pingServiceClient.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long nanoDelay)
      {
        sessionLocalNanoDelay = nanoDelay;
      }
    });
    this.pingServiceServer = new VTNanoPingService(client.getPingIntervalMilliseconds(), 0, true, executorService);
    this.pingServiceServer.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long nanoDelay)
      {
        sessionRemoteNanoDelay = nanoDelay;
      }
    });
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
  
  public boolean isRunningAudio()
  {
    return client.getAudioSystem().isRunning();
  }
  
  public long getLocalNanoDelay()
  {
    return sessionLocalNanoDelay;
  }
  
  public long getRemoteNanoDelay()
  {
    return sessionRemoteNanoDelay;
  }
  
  /*
   * public File getWorkingDirectory() { return workingDirectory; } public void
   * setWorkingDirectory(File workingDirectory) { this.workingDirectory =
   * workingDirectory; }
   */
  
  public VTClient getClient()
  {
    return client;
  }
  
  public VTClientRemoteConsoleWriter getClientWriter()
  {
    return clientWriter;
  }
  
  public VTClientConnection getConnection()
  {
    return connection;
  }
  
  public VTFileTransferClient getFileTransferClient()
  {
    return fileTransferClient;
  }
  
  public VTGraphicsLinkClient getGraphicsClient()
  {
    return graphicsClient;
  }
  
  public VTClipboardTransferTask getClipboardTransferTask()
  {
    return clipboardTransferTask;
  }
  
  // public void setZipFileOperation(VTClientZipFileOperation zipFileOperation)
  // {
  // .zipFileOperation = zipFileOperation;
  // }
  
  // public VTClientZipFileOperation getZipFileOperation()
  // {
  // return zipFileOperation;
  // }
  
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
  
  public void setCommandInputStream(InputStream in, String charsetName)
  {
    clientWriter.setCommandInputStream(in, charsetName);
  }
  
  public boolean isStopped()
  {
    //return serverReader.isStopped() || clientWriter.isStopped() || !connection.isConnected();
    return serverReader.isStopped() || !connection.isConnected();
    // return serverReader.isStopped() || clientWriter.isStopped() ||
    // !connection.isConnected();
  }
  
  public void stopTasks()
  {
    connection.closeSockets();
    client.getAudioSystem().stop();
    serverReader.setStopped(true);
    clientWriter.setStopped(true);
    fileTransferClient.getHandler().getSession().getTransaction().setStopped(true);
    graphicsClient.setStopped(true);
    pingServiceClient.setStopped(true);
    pingServiceServer.setStopped(true);
    pingServiceClient.ping();
    pingServiceServer.ping();
  }
  
  public void startSession() throws UnsupportedEncodingException
  {
    // runningAudio = false;
    serverReader.setStopped(false);
    clientWriter.setStopped(false);
    tunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlInputStream());
    tunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlOutputStream());
    tunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    tunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    // socksTunnelsHandler.getConnection().setControlInputStream(connection.getSocksControlInputStream());
    // socksTunnelsHandler.getConnection().setControlOutputStream(connection.getSocksControlOutputStream());
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
    serverReader.startThread();
    clientWriter.startThread();
    tunnelsHandler.startThread();
    client.enableInputMenuBar();
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
  
  @SuppressWarnings("unchecked")
  public void tryStopSessionThreads()
  {
    // VTTerminal.println("\nSession over!");
    stopTasks();
//    synchronized (this)
//    {
//      notifyAll();
//    }
    // if (writerThread != null && writerThread.isAlive())
    // {
    // System.out.println("interrupting writerThread...");
    // writerThread.interrupt();
    // }
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
    
    if (clipboardTransferTask.aliveThread())
    {
      clipboardTransferTask.interruptThread();
      // clipboardTransferThread.stop();
    }
    // if (zipFileOperation.aliveThread())
    // {
    // zipFileOperation.interruptThread();
    // zipFileOperation.stopThread();
    // }
    tunnelsHandler.getConnection().close();
    // socksTunnelsHandler.getConnection().close();
  }
  
  public void waitThreads()
  {
    /*
     * while (readerThread.isAlive() || writerThread.isAlive() ||
     * fileTransferThread.isAlive() || graphicsThread.isAlive()) { try {
     * Thread.sleep(1); } catch (Throwable e) { return; } }
     */
    //sessionResources.clear();
    try
    {
      serverReader.joinThread();
      //System.out.println("serverReader.joinThread()");
      clientWriter.joinThread();
      //System.out.println("clientWriter.joinThread()");
      fileTransferClient.joinThread();
      //System.out.println("fileTransferClient.joinThread()");
      graphicsClient.joinThread();
      //System.out.println("graphicsClient.joinThread()");
      clipboardTransferTask.joinThread();
      //System.out.println("clipboardTransferTask.joinThread()");
      // zipFileOperation.joinThread();
      tunnelsHandler.joinThread();
      //System.out.println("tunnelsHandler.joinThread()");
      // socksTunnelsHandler.joinThread();
      pingServiceClient.joinThread();
      pingServiceServer.joinThread();
      //System.out.println("pingService.joinThread()");
    }
    catch (Throwable e)
    {
      // return;
    }
  }
  
  public void negotiateShell() throws IOException
  {
    String clientShell = client.getClientConnector().getSessionShell();
    clientShell = clientShell.replace("\r\n", "").replace("\n", "");
    connection.getCommandWriter().writeLine(clientShell);
    connection.getCommandWriter().flush();
  }
}