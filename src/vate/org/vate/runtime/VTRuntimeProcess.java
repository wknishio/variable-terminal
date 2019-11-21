package org.vate.runtime;

import java.io.BufferedWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.ExecutorService;

public class VTRuntimeProcess
{
	private String command;
	private ProcessBuilder builder;
	private Process process;
	private InputStream in;
	private InputStream err;
	private OutputStream out;
	private VTRuntimeProcessOutputConsumer outputConsumer;
	private VTRuntimeProcessOutputConsumer errorConsumer;
	private VTRuntimeProcessExitListener exitListener;
	private ExecutorService threads;
	private BufferedWriter writer;
	private volatile boolean verbose;
	private volatile boolean restart;
	
	public VTRuntimeProcess(String command, ProcessBuilder builder, BufferedWriter writer, boolean verbose, ExecutorService threads, boolean restart)
	{
		this.command = command;
		this.builder = builder;
		this.writer = writer;
		this.verbose = verbose;
		this.threads = threads;
		this.restart = restart;
	}
	
	public void start() throws Throwable
	{
		this.process = builder.start();
		this.in = process.getInputStream();
		this.err = process.getErrorStream();
		this.out = process.getOutputStream();
		this.outputConsumer = new VTRuntimeProcessOutputConsumer(in, writer, verbose);
		this.errorConsumer = new VTRuntimeProcessOutputConsumer(err, writer, verbose);
		this.exitListener = new VTRuntimeProcessExitListener(this, outputConsumer);
		threads.execute(outputConsumer);
		threads.execute(errorConsumer);
		threads.execute(exitListener);
	}
	
	/* public boolean isRunning() { try { process.exitValue(); return false; }
	 * catch (IllegalThreadStateException e) { return true; } } */
	
	public Integer getExitValue()
	{
		try
		{
			return process.exitValue();
		}
		catch (Throwable e)
		{
			return null;
		}
	}
	
	public String getCommand()
	{
		return command;
	}
	
	public Process getProcess()
	{
		return process;
	}
	
	public InputStream getIn()
	{
		return in;
	}
	
	public InputStream getErr()
	{
		return err;
	}
	
	public OutputStream getOut()
	{
		return out;
	}
	
	public void setRestart(boolean restart)
	{
		this.restart = restart;
	}
	
	public boolean isRestart()
	{
		return this.restart;
	}
	
	public void setVerbose(boolean verbose)
	{
		this.verbose = verbose;
	}
	
	public boolean isVerbose()
	{
		return this.verbose;
	}
	
	public boolean restart()
	{
		try
		{
			stop();
			start();
			return true;
		}
		catch (Throwable t)
		{
			
		}
		return false;
	}
	
	public void stop()
	{
		if (process != null)
		{
			try
			{
				process.destroy();
			}
			catch (Throwable e)
			{
				
			}
		}
		if (in != null)
		{
			try
			{
				in.close();
			}
			catch (Throwable e)
			{
				
			}
		}
		if (out != null)
		{
			try
			{
				out.close();
			}
			catch (Throwable e)
			{
				
			}
		}
	}
	
	public void destroy()
	{
		this.restart = false;
		stop();
	}
	
	public void finalize()
	{
		destroy();
	}
}