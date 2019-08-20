package org.vate.server.console.local.standard.command;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

import org.vate.console.VTConsole;
import org.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTDISPLAYDEVICES extends VTServerStandardLocalConsoleCommandProcessor
{
	public VTDISPLAYDEVICES()
	{
		this.setFullName("*VTDISPLAYDEVICES");
		this.setAbbreviatedName("*VTDPDS");
		this.setFullSyntax("*VTDISPLAYDEVICES");
		this.setAbbreviatedSyntax("*VTDPDS");
	}

	public void execute(String command, String[] parsed) throws Exception
	{
		message.setLength(0);
		int count = 0;
		GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
		if (devices != null && devices.length > 0)
		{
			message.append("\rVT>List of graphical display devices on server:\nVT>");
			for (GraphicsDevice device : devices)
			{
				DisplayMode mode = device.getDisplayMode();
				message.append("\nVT>Number: [" + (count++) + "]");
				message.append("\nVT>ID: [" + device.getIDstring() + "]");
				message.append("\nVT>Mode: [" + mode.getWidth() + "x" + mode.getHeight() + "]");
				Rectangle bounds = device.getDefaultConfiguration().getBounds();
				message.append("\nVT>Origin: [X:" + bounds.x + " Y:" + bounds.y + "]");
				message.append("\nVT>");
			}
			message.append("\nVT>End of graphical display devices list\nVT>");
		}
		else
		{
			message.append("\rVT>No graphical display devices found on server!\nVT>");
		}
		VTConsole.print(message.toString());
	}

	public void close()
	{
		
	}
}
