package org.vate.stream.multiplex;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.concurrent.ExecutorService;

import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingInputStream.VTLinkableDynamicMultiplexedInputStream;
import org.vate.stream.multiplex.VTLinkableDynamicMultiplexingOutputStream.VTLinkableDynamicMultiplexedOutputStream;

public class VTMultiplexingConnection
{
	private VTLinkableDynamicMultiplexingInputStream dataInputStream;
	private VTLinkableDynamicMultiplexingOutputStream dataOutputStream;
	private BufferedReader controlReader;
	private BufferedWriter controlWriter;
	private final ExecutorService threads;
	private final VTMultiplexingControlThread controlThread;
	
	public VTMultiplexingConnection(ExecutorService threads)
	{
		this.threads = threads;
		this.controlThread = new VTMultiplexingControlThread(this, threads);
	}
	
	public synchronized void start()
	{
		threads.execute(controlThread);
	}
	
	public synchronized void stop()
	{
		//controlThread.stop();
	}
	
	public synchronized void close()
	{
		stop();
		try
		{
			controlReader.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
		try
		{
			controlWriter.close();
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
		}
	}
	
	public synchronized VTLinkableDynamicMultiplexedOutputStream getOutputStream(short type, Object link)
	{
		if (link instanceof Integer)
		{
			return dataOutputStream.getOutputStream(type, (Integer) link);
		}
		return dataOutputStream.linkOutputStream(type, link);
	}
	
	public synchronized VTLinkableDynamicMultiplexedOutputStream getOutputStream(short type, int number, Object link)
	{
		VTLinkableDynamicMultiplexedOutputStream stream = dataOutputStream.getOutputStream(type, number);
		stream.setLink(link);
		return stream;
	}
	
	public synchronized void releaseOutputStream(VTLinkableDynamicMultiplexedOutputStream stream)
	{
		if (stream != null)
		{
			dataOutputStream.releaseOutputStream(stream);
		}
	}
	
	public synchronized VTLinkableDynamicMultiplexedInputStream getInputStream(short type, int number)
	{
		VTLinkableDynamicMultiplexedInputStream stream = dataInputStream.getInputStream(type, number);
		return stream;
	}

	public BufferedReader getControlReader()
	{
		return controlReader;
	}
	
	public BufferedWriter getControlWriter()
	{
		return controlWriter;
	}
	
	public void setDataInputStream(VTLinkableDynamicMultiplexingInputStream in)
	{
		this.dataInputStream = in;
	}
	
	public void setControlInputStream(InputStream in)
	{
		controlReader = new BufferedReader(new InputStreamReader(in));
	}
	
	public void setDataOutputStream(VTLinkableDynamicMultiplexingOutputStream out)
	{
		this.dataOutputStream = out;
	}
	
	public void setControlOutputStream(OutputStream out)
	{
		controlWriter = new BufferedWriter(new OutputStreamWriter(out));
	}
}