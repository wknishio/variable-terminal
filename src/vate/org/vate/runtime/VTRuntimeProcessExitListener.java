package org.vate.runtime;

public class VTRuntimeProcessExitListener implements Runnable
{
	private VTRuntimeProcess process;
	private VTRuntimeProcessOutputConsumer consumer;
	
	public VTRuntimeProcessExitListener(VTRuntimeProcess process, VTRuntimeProcessOutputConsumer consumer)
	{
		this.process = process;
		this.consumer = consumer;
	}
	
	public void finalize()
	{
		if (process != null && process.getProcess() != null)
		{
			process.getProcess().destroy();
		}
	}
	
	public void run()
	{
		Thread.currentThread().setName(getClass().getSimpleName());
		try
		{
			process.getProcess().waitFor();
		}
		catch (Throwable e)
		{
			
		}
		consumer.stop();
		
		if (process.isRestart())
		{
			try
			{
				Thread.sleep(1000);
			}
			catch (Throwable e)
			{
				
			}
			while (!process.restart())
			{
				try
				{
					Thread.sleep(1000);
				}
				catch (Throwable e)
				{
					
				}
			}
		}
	}
}