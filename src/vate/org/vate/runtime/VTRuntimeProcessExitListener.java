package org.vate.runtime;

public class VTRuntimeProcessExitListener implements Runnable
{
	private VTRuntimeProcess process;
	
	public VTRuntimeProcessExitListener(VTRuntimeProcess process)
	{
		this.process = process;
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
		
		if (process.isRestart())
		{
			process.stop();
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
		else
		{
			process.stop();
		}
	}
}