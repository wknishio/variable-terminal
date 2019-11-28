package org.vate.client.console.remote;

import org.vate.client.connection.VTClientConnection;
import org.vate.client.session.VTClientSession;
import org.vate.console.VTConsole;
import org.vate.task.VTTask;

public class VTClientRemoteConsoleReader extends VTTask
{
	private static final int resultBufferSize = 1024 * 64;
	//private int available;
	private int readChars;
	private final char[] resultBuffer = new char[resultBufferSize];
	// private VTClient client;
	private VTClientSession session;
	private VTClientConnection connection;
	
	public VTClientRemoteConsoleReader(VTClientSession session)
	{
		// this.client = session.getClient();
		this.session = session;
		this.connection = session.getConnection();
	}
	
	public void run()
	{
		while (!stopped)
		{
			try
			{
//				available = connection.getShellInputStream().available();
//				if (available > 0)
//				{
//					while ((available = connection.getShellInputStream().available()) > 0)
//					{
//						readChars = connection.getResultReader().read(resultBuffer, 0, resultBufferSize);
//						VTConsole.write(resultBuffer, 0, readChars);
//						VTConsole.flush();
//					}
////					if (!stopped)
////					{
////						VTConsole.flush();
////					}
//				}
//				else if (available < 0)
//				{
//					stopped = true;
//					break;
//				}
//				else
//				{
//					Thread.sleep(1);
//				}
				readChars = connection.getResultReader().read(resultBuffer, 0, resultBufferSize);
				VTConsole.write(resultBuffer, 0, readChars);
				VTConsole.flush();
			}
			catch (Throwable e)
			{
				// e.printStackTrace();
				stopped = true;
				break;
			}
		}
		stopped = true;
		synchronized (session)
		{
			session.notify();
		}
	}
}