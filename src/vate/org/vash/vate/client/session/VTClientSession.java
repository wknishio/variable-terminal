package org.vash.vate.client.session;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VTSystem;
import org.vash.vate.client.VTClient;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.console.remote.VTClientRemoteConsoleReader;
import org.vash.vate.client.console.remote.VTClientRemoteConsoleWriter;
import org.vash.vate.client.filetransfer.VTFileTransferClient;
import org.vash.vate.client.graphicslink.VTGraphicsLinkClient;
import org.vash.vate.graphics.clipboard.VTClipboardTransferTask;
import org.vash.vate.ping.VTNanoPingListener;
import org.vash.vate.ping.VTNanoPingService;
import org.vash.vate.socket.remote.VTRemotePipedSocketFactory;
import org.vash.vate.socket.remote.VTRemoteSocketFactory;
import org.vash.vate.tunnel.connection.VTTunnelConnection;
import org.vash.vate.tunnel.connection.VTTunnelConnectionHandler;

public class VTClientSession
{
  private long sessionLocalNanoDelay;
  private long sessionRemoteNanoDelay;
  private String shellEncoding;
  private VTClient client;
  private VTClientConnection connection;
  private VTClientRemoteConsoleReader serverReader;
  private VTClientRemoteConsoleWriter clientWriter;
  private VTFileTransferClient fileTransferClient;
  private VTGraphicsLinkClient graphicsClient;
  private VTClipboardTransferTask clipboardTransferTask;
  private VTTunnelConnectionHandler tunnelsHandler;
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
    this.shellEncoding = null;
    this.serverReader = new VTClientRemoteConsoleReader(this);
    this.clientWriter = new VTClientRemoteConsoleWriter(this);
    this.fileTransferClient = new VTFileTransferClient(this);
    this.graphicsClient = new VTGraphicsLinkClient(this);
    this.clipboardTransferTask = new VTClipboardTransferTask(executorService);
    this.tunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(executorService, sessionCloseables, connection.getSecureRandom()));
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
    clientWriter.setCommandInputStream(client.getCommandInputStream());
    serverReader.setCommandOutputStream(client.getCommandOutputStream());
    serverReader.setStopped(false);
    clientWriter.setStopped(false);
    tunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlDataInputStream());
    tunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlDataOutputStream());
    tunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
    tunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
    pingServiceClient.setInputStream(connection.getPingClientInputStream());
    pingServiceClient.setOutputStream(connection.getPingClientOutputStream());
    pingServiceServer.setInputStream(connection.getPingServerInputStream());
    pingServiceServer.setOutputStream(connection.getPingServerOutputStream());
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
  
  public void setShellEncoding(String shellEncoding)
  {
    this.shellEncoding = shellEncoding;
  }
  
  public String getShellEncoding()
  {
    return shellEncoding;
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
  
  public VTRemotePipedSocketFactory createRemotePipedSocketFactory(int type)
  {
    return tunnelsHandler.getConnection().createRemotePipedSocketFactory(type);
  }
  
  public VTRemoteSocketFactory createRemoteSocketFactory(int type)
  {
    type |= VTSystem.VT_MULTIPLEXED_CHANNEL_TYPE_PIPE_DIRECT;
    return tunnelsHandler.getConnection().createRemoteSocketFactory(tunnelsHandler.getConnection().getResponseChannel(type));
  }
  
  public void setCommandInputStream(InputStream in, String charsetName)
  {
    clientWriter.setCommandInputStream(in, charsetName);
  }
  
  public boolean isStopped()
  {
    return serverReader.isStopped() || !connection.isConnected();
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
  
  public void startSession() throws IOException
  {
    connection.getCommandWriter().writeLong(connection.getSecureRandom().nextLong());
    connection.getCommandWriter().flush();
    connection.getResultReader().readLong();
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
    stopTasks();
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
    }
    tunnelsHandler.getConnection().close();
  }
  
  public void waitThreads()
  {
    try
    {
      serverReader.joinThread();
      clientWriter.joinThread();
      fileTransferClient.joinThread();
      graphicsClient.joinThread();
      clipboardTransferTask.joinThread();
      tunnelsHandler.joinThread();
      pingServiceClient.joinThread();
      pingServiceServer.joinThread();
    }
    catch (Throwable e)
    {
      // return;
    }
  }
  
  public void negotiateShell() throws IOException
  {
    connection.setQuiet(client.isDaemon());
    String clientShell = client.getClientConnector().getSessionShell();
    clientShell = clientShell.replace("\r\n", "").replace("\n", "");
//    boolean requestPTY = connection.getQuiet() && client.isAgent() && !connection.isManaged() && VTMainNativeUtils.checkTerminalAvailable();
    connection.getCommandWriter().writeUTF(clientShell);
    connection.getCommandWriter().writeBoolean(connection.getQuiet());
    connection.getCommandWriter().writeBoolean(client.isAgent());
//    connection.getCommandWriter().writeBoolean(requestPTY);
    connection.getCommandWriter().flush();
//    boolean hasPTY = connection.getResultReader().readBoolean();
//    if (requestPTY && hasPTY)
//    {
//      VTMainNativeUtils.disableTerminalSanity();
//    }
  }
}