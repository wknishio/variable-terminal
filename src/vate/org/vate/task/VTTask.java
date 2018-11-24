package org.vate.task;

public class VTTask implements Runnable
{
	protected volatile boolean stopped;
	private Thread taskThread;
	
	public boolean isStopped()
	{
		return stopped;
	}
	
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}
	
	public void interruptThread()
	{
		if (taskThread != null)
		{
			taskThread.interrupt();
		}
	}
	
	@SuppressWarnings("deprecation")
	public void stopThread()
	{
		if (taskThread != null)
		{
			try
			{
				taskThread.stop();
			}
			catch (Throwable t)
			{
				
			}
		}
	}
	
	public void joinThread()
	{
		if (taskThread != null)
		{
			try
			{
				taskThread.join();
			}
			catch (InterruptedException e)
			{
				
			}
		}
	}
	
	public boolean aliveThread()
	{
		if (taskThread != null)
		{
			return taskThread.isAlive();
		}
		return false;
	}
	
	public void startThread()
	{
		// setStopped(false);
		stopped = false;
		taskThread = new Thread(null, this, this.getClass().getSimpleName());
		taskThread.setDaemon(true);
		taskThread.start();
	}
	
	public void run()
	{
		
	}
}