package org.vate.server.graphicsdevices;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

import org.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vate.server.session.VTServerSession;
import org.vate.task.VTTask;

public class VTServerGraphicsDeviceResolver extends VTTask
{
	private volatile boolean finished;
	private VTServerSession session;
	
	public VTServerGraphicsDeviceResolver(VTServerSession session)
	{
		this.session = session;
		this.finished = true;
	}
	
	public boolean isFinished()
	{
		return finished;
	}
	
	public void setFinished(boolean finished)
	{
		this.finished = finished;
	}
	
	public void run()
	{
		try
		{
			StringBuilder message = new StringBuilder();
			GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
			if (devices != null && devices.length > 0)
			{
				int count = 0;
				message.append("\nVT>List of graphical display devices on server:\nVT>");
				for (GraphicsDevice device : devices)
				{
					DisplayMode mode = device.getDisplayMode();
					Rectangle bounds = device.getDefaultConfiguration().getBounds();
					message.append("\nVT>Number: [" + (count++) + "]");
					message.append("\nVT>ID: [" + device.getIDstring() + "]");
					message.append("\nVT>Mode: [" + mode.getWidth() + "x" + mode.getHeight() + "]");
					message.append("\nVT>Origin: [X:" + bounds.x + " Y:" + bounds.y + "]");
					message.append("\nVT>");
				}
				message.append("\nVT>End of graphical display devices list\nVT>");
				synchronized (this)
				{
					session.getConnection().getResultWriter().write(message.toString());
					session.getConnection().getResultWriter().flush();
					finished = true;
				}
			}
			else
			{
				synchronized (this)
				{
					session.getConnection().getResultWriter().write("\nVT>No graphical display devices found on server!\nVT>");
					session.getConnection().getResultWriter().flush();
					finished = true;
				}
			}
		}
		catch (Throwable e)
		{
			
		}
		finished = true;
	}
}