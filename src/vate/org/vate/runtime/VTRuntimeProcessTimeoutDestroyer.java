package org.vate.runtime;

public class VTRuntimeProcessTimeoutDestroyer implements Runnable
{
	private volatile boolean running;
	private volatile long last = 0;
	private volatile long current = 0;
	private volatile long elapsed = 0;
	private volatile long timeout;
	private Process process;
	
	public VTRuntimeProcessTimeoutDestroyer(Process process, long timeout)
	{
		this.running = true;
		this.process = process;
		this.timeout = timeout;
	}
	
	public void close()
	{
		stop();
	}
	
	public void stop()
	{
		running = false;
		finalize();
	}
	
	public void finalize()
	{
		if (process != null)
		{
			try
			{
				process.destroy();
			}
			catch (Throwable t)
			{
				
			}
		}
	}
	
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		current = System.currentTimeMillis();
		last = current;
		while (running)
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (Throwable t)
			{
				
			}
			current = System.currentTimeMillis();
			if (current > last)
			{
				elapsed += current - last;
			}
			last = current;
			if (elapsed >= timeout)
			{
				stop();
			}
		}
	}
}