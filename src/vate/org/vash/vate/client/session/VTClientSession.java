package org.vash.vate.client.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.charset.Charset;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.vash.vate.VT;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.console.remote.VTClientRemoteConsoleReader;
import org.vash.vate.client.console.remote.VTClientRemoteConsoleWriter;
import org.vash.vate.client.filesystem.VTClientZipFileOperation;
import org.vash.vate.client.filetransfer.VTFileTransferClient;
import org.vash.vate.client.graphicsmode.VTGraphicsModeClient;
import org.vash.vate.graphics.clipboard.VTClipboardTransferTask;
import org.vash.vate.ping.VTNanoPingListener;
import org.vash.vate.ping.VTNanoPingService;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.connection.VTTunnelConnectionHandler;

public class VTClientSession
{
  private volatile long sessionLocalNanoDelay;
  private volatile long sessionRemoteNanoDelay;
  //private volatile boolean runningAudio;
  // private File workingDirectory;
  private VTClient client;
  private VTClientConnection connection;

  private VTClientRemoteConsoleReader serverReader;
  private VTClientRemoteConsoleWriter clientWriter;
  private VTFileTransferClient fileTransferClient;
  private VTGraphicsModeClient graphicsClient;
  private VTClipboardTransferTask clipboardTransferTask;
  private VTClientZipFileOperation zipFileOperation;
  private VTTunnelConnectionHandler tunnelsHandler;
  //private VTTunnelConnectionHandler socksTunnelsHandler;
  private VTNanoPingService pingService;

  private Map<String, Closeable> sessionResources;

  private ExecutorService threads;

  public VTClientSession(VTClient client, VTClientConnection connection)
  {
    this.client = client;
    this.connection = connection;
    this.sessionResources = Collections.synchronizedMap(new LinkedHashMap<String, Closeable>());
  }

  public void initialize()
  {
    this.threads = Executors.newCachedThreadPool(new ThreadFactory()
    {
      public Thread newThread(Runnable r)
      {
        Thread created = new Thread(null, r, r.getClass().getSimpleName());
        created.setDaemon(true);
        return created;
      }
    });
    
//    try
//    {
//      int supressEchoShell = 1;
//      if (VTConsole.isGraphical() && !VTConsole.isDaemon())
//      {
//        supressEchoShell = 0;
//      }
//      getConnection().getShellOutputStream().write(supressEchoShell);
//      getConnection().getShellOutputStream().flush();
//    }
//    catch (Throwable e)
//    {
//      //e.printStackTrace();
//    }
    
    //this.runningAudio = false;
    this.serverReader = new VTClientRemoteConsoleReader(this);
    this.clientWriter = new VTClientRemoteConsoleWriter(this);
    this.fileTransferClient = new VTFileTransferClient(this);
    this.graphicsClient = new VTGraphicsModeClient(this);
    this.clipboardTransferTask = new VTClipboardTransferTask();
    this.zipFileOperation = new VTClientZipFileOperation(this);
    this.tunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(threads), threads);
    //this.socksTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(threads), threads);
    this.pingService = new VTNanoPingService(VT.VT_PING_SERVICE_INTERVAL_MILLISECONDS, false);
    this.pingService.addListener(new VTNanoPingListener()
    {
      public void pingObtained(long localNanoDelay, long remoteNanoDelay)
      {
        sessionLocalNanoDelay = localNanoDelay;
        sessionRemoteNanoDelay = remoteNanoDelay;
        // VTConsole.println("client localNanoDelay:" +
        // localNanoDelay);
        // VTConsole.println("client remoteNanoDelay:" +
        // remoteNanoDelay);
      }
    });
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

  public VTGraphicsModeClient getGraphicsClient()
  {
    return graphicsClient;
  }

  public VTClipboardTransferTask getClipboardTransferTask()
  {
    return clipboardTransferTask;
  }

  public void setZipFileOperation(VTClientZipFileOperation zipFileOperation)
  {
    this.zipFileOperation = zipFileOperation;
  }

  public VTClientZipFileOperation getZipFileOperation()
  {
    return zipFileOperation;
  }

  public VTNanoPingService getNanoPingService()
  {
    return pingService;
  }

  public VTTunnelConnectionHandler getTunnelsHandler()
  {
    return tunnelsHandler;
  }

  //public VTTunnelConnectionHandler getSOCKSTunnelsHandler()
  //{
    //return socksTunnelsHandler;
  //}
  
  public void setCommandInputStream(InputStream in, Charset charset)
  {
    clientWriter.setCommandInputStream(in, charset);
  }

  public boolean isStopped()
  {
    return serverReader.isStopped() || clientWriter.isStopped() || !connection.isConnected();
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
    pingService.setStopped(true);
    pingService.ping();
  }

  public void startSession() throws UnsupportedEncodingException
  {
    //runningAudio = false;
    serverReader.setStopped(false);
    clientWriter.setStopped(false);
    tunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlInputStream());
    tunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlOutputStream());
    tunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    tunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    //socksTunnelsHandler.getConnection().setControlInputStream(connection.getSocksControlInputStream());
    //socksTunnelsHandler.getConnection().setControlOutputStream(connection.getSocksControlOutputStream());
    //socksTunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    //socksTunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    pingService.setInputStream(connection.getPingInputStream());
    pingService.setOutputStream(connection.getPingOutputStream());
    // tunnelHandler.getConnection().start();
  }

  public void startSessionThreads()
  {
    // session.getServerReader().setStopped(false);
    pingService.startThread();
    serverReader.startThread();
    clientWriter.startThread();
    tunnelsHandler.startThread();
    //socksTunnelsHandler.startThread();
    if (client.getInputMenuBar() != null)
    {
      client.getInputMenuBar().setEnabled(true);
    }
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

  public void tryStopSessionThreads()
  {
    // VTTerminal.println("\nSession over!");
    stopTasks();
    // if (writerThread != null && writerThread.isAlive())
    // {
    // System.out.println("interrupting writerThread...");
    // writerThread.interrupt();
    // }
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

    if (clipboardTransferTask.aliveThread())
    {
      clipboardTransferTask.interruptThread();
      // clipboardTransferThread.stop();
    }
    if (zipFileOperation.aliveThread())
    {
      zipFileOperation.interruptThread();
      zipFileOperation.stopThread();
    }
    if (pingService.aliveThread())
    {
      pingService.interruptThread();
      pingService.stopThread();
    }
    tunnelsHandler.getConnection().close();
    //socksTunnelsHandler.getConnection().close();
  }

  public void waitThreads()
  {
    /*
     * while (readerThread.isAlive() || writerThread.isAlive() ||
     * fileTransferThread.isAlive() || graphicsThread.isAlive()) { try {
     * Thread.sleep(1); } catch (Throwable e) { return; } }
     */
    sessionResources.clear();
    try
    {
      serverReader.joinThread();
      clientWriter.joinThread();
      fileTransferClient.joinThread();
      graphicsClient.joinThread();
      clipboardTransferTask.joinThread();
      zipFileOperation.joinThread();
      tunnelsHandler.joinThread();
      //socksTunnelsHandler.joinThread();
      pingService.joinThread();
    }
    catch (Throwable e)
    {
      // return;
    }
  }

  public void negotiateShell() throws IOException
  {
    String clientShell = client.getClientConnector().getSessionShell();
    
    clientShell = clientShell.replace('\n', ' ');
    
    connection.getCommandWriter().write(clientShell + "\n");
    connection.getCommandWriter().flush();
  }
}