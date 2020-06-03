package org.vate.server.console.shell;

import org.vate.VT;
import org.vate.server.connection.VTServerConnection;
import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTServerShellOutputWriter extends VTTask
{
	private static final int resultBufferSize = VT.VT_DATA_BUFFER_SIZE;
	private int readChars;
	private final char[] resultBuffer = new char[resultBufferSize];
	private VTServerConnection connection;
	private VTServerSession session;
	
	public VTServerShellOutputWriter(VTServerSession session)
	{
		this.session = session;
		this.connection = session.getConnection();
		this.stopped = false;
	}
	
	public boolean isStopped()
	{
		return stopped;
	}
	
	public void setStopped(boolean stopped)
	{
		this.stopped = stopped;
	}
	
	public void run()
	{
		while (!stopped)
		{
			try
			{
//				if (session.getShellOutputReader().ready())
//				{
//					readChars = session.getShellOutputReader().read(resultBuffer, 0, resultBufferSize);
//					if (readChars > 0 && !stopped)
//					{
//						connection.getResultWriter().write(resultBuffer, 0, readChars);
//						connection.getResultWriter().flush();
//					}
//					else
//					{
//						stopped = true;
//						break;
//					}
//				}
//				else
//				{
//					Thread.sleep(5);
//				}
				readChars = session.getShellOutputReader().read(resultBuffer, 0, resultBufferSize);
				if (readChars > 0 && !stopped)
				{
					connection.getResultWriter().write(resultBuffer, 0, readChars);
					connection.getResultWriter().flush();
				}
				else
				{
					stopped = true;
					break;
				}
			}
			catch (Throwable e)
			{
				stopped = true;
				break;
			}
		}
		try
		{
			synchronized (session.getShell())
			{
				session.getShell().notify();
			}
		}
		catch (Throwable e)
		{
			
		}
	}
}