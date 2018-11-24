package org.vate.console.standard;

import java.io.BufferedReader;
import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadFactory;

public class VTStandardConsoleInterruptibleReader implements Runnable
{
	// private volatile boolean stopped;
	private volatile boolean requested;
	private volatile boolean echo;
	// private volatile boolean discard;
	private BufferedReader standardTerminalReader;
	private volatile Console systemConsole;
	private volatile BlockingQueue<String> buffer;
	// private static final String ANSIDetectionPattern =
	// "(\\u001B)(\\[)([^R])*([R])";
	private Executor executor;
	
	public VTStandardConsoleInterruptibleReader()
	{
		// String testString = "\u001B[1;1eeR";
		// System.out.println("matches:" +
		// testString.matches(ANSIDetectionPattern));
		this.buffer = new LinkedBlockingQueue<String>();
		this.executor = Executors.newFixedThreadPool(1, new ThreadFactory()
		{
			public Thread newThread(Runnable r)
			{
				Thread created = new Thread(null, r, r.getClass().getSimpleName());
				created.setDaemon(true);
				return created;
			}
		});
		standardTerminalReader = new BufferedReader(new InputStreamReader(new FileInputStream(FileDescriptor.in)));
		try
		{
			Class.forName("java.io.Console");
			VTStandardConsole.systemconsoleclass = true;
			// console =
			// Class.forName("System").getMethod("console").invoke(null);
			systemConsole = System.console();
			if (systemConsole != null)
			{
				VTStandardConsole.systemconsolesupport = true;
			}
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public void setEcho(boolean echo)
	{
		this.echo = echo;
	}
	
	public synchronized String read() throws InterruptedException
	{
		if (buffer.size() == 0)
		{
			if (requested == false)
			{
				requested = true;
				executor.execute(this);
			}
		}
		String data = buffer.take();
		requested = false;
		if (data != null)
		{
			return data + "\n";
		}
		return null;
	}
	
	public void run()
	{
		try
		{
			String line = null;
			if (VTStandardConsole.systemconsoleclass && VTStandardConsole.systemconsolesupport)
			{
				if (!echo)
				{
					try
					{
						line = new String(systemConsole.readPassword());
					}
					catch (Throwable e)
					{
						// e.printStackTrace();
					}
				}
				else
				{
					try
					{
						line = systemConsole.readLine();
					}
					catch (Throwable e)
					{
						// e.printStackTrace();
					}
				}
			}
			if (line != null)
			{
				// int before = line.length();
				// line = line.replaceAll(ANSIDetectionPattern, "");
				// int after = line.length();
				// if (before > after)
				// {
				// VTStandardConsole.ansidetected = true;
				// }
				buffer.offer(line);
			}
			else
			{
				try
				{
					line = standardTerminalReader.readLine();
				}
				catch (IOException e)
				{
					// e.printStackTrace();
					line = "";
				}
				catch (Throwable e)
				{
					// e.printStackTrace();
					line = "";
				}
				if (line == null)
				{
					line = "";
				}
				// int before = line.length();
				// line = line.replaceAll(ANSIDetectionPattern, "");
				// int after = line.length();
				// if (before > after)
				// {
				// VTStandardConsole.ansidetected = true;
				// }
				buffer.offer(line);
			}
		}
		catch (Throwable t)
		{
			
		}
		requested = false;
	}
}