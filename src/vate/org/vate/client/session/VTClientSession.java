package org.vate.client.session;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;

import org.vate.VT;
import org.vate.client.VTClient;
import org.vate.client.connection.VTClientConnection;
import org.vate.client.console.VTClientRemoteConsoleReader;
import org.vate.client.console.VTClientRemoteConsoleWriter;
import org.vate.client.filesystem.VTClientZipFileOperation;
import org.vate.client.filetransfer.VTFileTransferClient;
import org.vate.client.graphicsmode.VTGraphicsModeClient;
import org.vate.graphics.clipboard.VTClipboardTransferTask;
import org.vate.ping.VTNanoPingListener;
import org.vate.ping.VTNanoPingService;
import org.vate.tunnel.connection.VTTunnelConnection;
import org.vate.tunnel.connection.VTTunnelConnectionHandler;

public class VTClientSession
{
	private volatile long sessionLocalNanoDelay;
	private volatile long sessionRemoteNanoDelay;
	private volatile boolean runningAudio;
	// private File workingDirectory;
	private VTClient client;
	private VTClientConnection connection;
	private ExecutorService threads;
	
	private VTClientRemoteConsoleReader serverReader;
	private VTClientRemoteConsoleWriter clientWriter;
	private VTFileTransferClient fileTransferClient;
	private VTGraphicsModeClient graphicsClient;
	private VTClipboardTransferTask clipboardTransferTask;
	private VTClientZipFileOperation zipFileOperation;
	private VTTunnelConnectionHandler tcpTunnelsHandler;
	private VTTunnelConnectionHandler socksTunnelsHandler;
	private VTNanoPingService pingService;
	
	public VTClientSession(VTClient client, VTClientConnection connection)
	{
		this.client = client;
		this.connection = connection;
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
		this.serverReader = new VTClientRemoteConsoleReader(this);
		this.clientWriter = new VTClientRemoteConsoleWriter(this);
		this.fileTransferClient = new VTFileTransferClient(this);
		this.graphicsClient = new VTGraphicsModeClient(this);
		this.clipboardTransferTask = new VTClipboardTransferTask();
		this.zipFileOperation = new VTClientZipFileOperation(this);
		
		this.tcpTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(VTTunnelConnection.TUNNEL_TYPE_TCP, threads), threads);
		this.socksTunnelsHandler = new VTTunnelConnectionHandler(new VTTunnelConnection(VTTunnelConnection.TUNNEL_TYPE_SOCKS, threads), threads);
		this.pingService = new VTNanoPingService(VT.VT_PING_SERVICE_INTERVAL, false);
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
	
	public boolean isRunningAudio()
	{
		return runningAudio;
	}
	
	public void setRunningAudio(boolean runningAudio)
	{
		this.runningAudio = runningAudio;
	}
	
	public long getLocalNanoDelay()
	{
		return sessionLocalNanoDelay;
	}
	
	public long getRemoteNanoDelay()
	{
		return sessionRemoteNanoDelay;
	}
	
	/* public File getWorkingDirectory() { return workingDirectory; }
	 * public void setWorkingDirectory(File workingDirectory) {
	 * this.workingDirectory = workingDirectory; } */
	
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
		return serverReader.isStopped() || clientWriter.isStopped() || !connection.isConnected();
		// return serverReader.isStopped() || clientWriter.isStopped() ||
		// !connection.isConnected();
	}
	
	public void setStopped(boolean stopped)
	{
		connection.closeSockets();
		client.getAudioSystem().stop();
		serverReader.setStopped(stopped);
		clientWriter.setStopped(stopped);
		fileTransferClient.getHandler().getSession().getTransaction().setStopped(stopped);
		graphicsClient.setStopped(stopped);
		pingService.setStopped(stopped);
		pingService.ping();
	}
	
	public void startSession()
	{
		runningAudio = false;
		serverReader.setStopped(false);
		clientWriter.setStopped(false);
		tcpTunnelsHandler.getConnection().setControlInputStream(connection.getTunnelControlInputStream());
		tcpTunnelsHandler.getConnection().setControlOutputStream(connection.getTunnelControlOutputStream());
		tcpTunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
		tcpTunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
		socksTunnelsHandler.getConnection().setControlInputStream(connection.getSocksControlInputStream());
		socksTunnelsHandler.getConnection().setControlOutputStream(connection.getSocksControlOutputStream());
		socksTunnelsHandler.getConnection().setDataInputStream(connection.getMultiplexedConnectionInputStream());
		socksTunnelsHandler.getConnection().setDataOutputStream(connection.getMultiplexedConnectionOutputStream());
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
		tcpTunnelsHandler.startThread();
		socksTunnelsHandler.startThread();
		if (client.getInputMenuBar() != null)
		{
			client.getInputMenuBar().setEnabled(true);
		}
	}
	
	public void waitSession()
	{
		/* while (!isStopped()) { try { Thread.sleep(1); } catch (Throwable e) {
		 * return; } } */
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
		setStopped(true);
		// if (writerThread != null && writerThread.isAlive())
		// {
		// System.out.println("interrupting writerThread...");
		// writerThread.interrupt();
		// }
		
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
		tcpTunnelsHandler.getConnection().close();
		socksTunnelsHandler.getConnection().close();
	}
	
	public void waitThreads()
	{
		/* while (readerThread.isAlive() || writerThread.isAlive() ||
		 * fileTransferThread.isAlive() || graphicsThread.isAlive()) { try {
		 * Thread.sleep(1); } catch (Throwable e) { return; } } */
		try
		{
			serverReader.joinThread();
			clientWriter.joinThread();
			fileTransferClient.joinThread();
			graphicsClient.joinThread();
			clipboardTransferTask.joinThread();
			zipFileOperation.joinThread();
			tcpTunnelsHandler.joinThread();
			socksTunnelsHandler.joinThread();
			pingService.joinThread();
		}
		catch (Throwable e)
		{
			// return;
		}
	}
}