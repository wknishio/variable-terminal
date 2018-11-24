package org.vate.graphics.message;

import java.awt.Frame;
import java.awt.SystemTray;
import java.awt.TrayIcon;
import java.awt.TrayIcon.MessageType;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

public class VTTrayIconInterface
{
	private TrayIcon trayIcon;
	private SystemTray systemTray;
	
	public VTTrayIconInterface()
	{
		
	}
	
	public void install(final Frame frame, String tooltip)
	{
		try
		{
			if (!SystemTray.isSupported())
			{
				return;
			}
			systemTray = SystemTray.getSystemTray();
			trayIcon = new TrayIcon(frame.getIconImage(), tooltip);
			trayIcon.addMouseListener(new MouseListener()
			{
				public void mouseClicked(MouseEvent e)
				{
					if (!frame.isVisible())
					{
						frame.setVisible(true);
					}
					if ((frame.getExtendedState() & Frame.ICONIFIED) != 0)
					{
						frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
					}
					frame.toFront();
				}
				
				public void mousePressed(MouseEvent e)
				{
					
				}
				
				public void mouseReleased(MouseEvent e)
				{
					
				}
				
				public void mouseEntered(MouseEvent e)
				{
					
				}
				
				public void mouseExited(MouseEvent e)
				{
					
				}
			});
			trayIcon.addActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent e)
				{
					if (!frame.isVisible())
					{
						frame.setVisible(true);
					}
					if ((frame.getExtendedState() & Frame.ICONIFIED) != 0)
					{
						frame.setExtendedState(frame.getExtendedState() ^ Frame.ICONIFIED);
					}
					frame.toFront();
				}
			});
			trayIcon.setImageAutoSize(true);
			systemTray.add(trayIcon);
			// Thread shutdownHook = new Thread(null, new Runnable()
			// {
			// public void run()
			// {
			// removeTrayIcon();
			// }
			// });
			// Runtime.getRuntime().addShutdownHook(shutdownHook);
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public void displayMessage(String caption, String text)
	{
		try
		{
			if (trayIcon != null)
			{
				trayIcon.displayMessage(caption, text, MessageType.NONE);
			}
		}
		catch (Throwable t)
		{
			
		}
	}
	
	// public void removeTrayIcon()
	// {
	// try
	// {
	// if (trayIcon != null)
	// {
	// systemTray.remove(trayIcon);
	// }
	// }
	// catch (Throwable t)
	// {
	// //t.printStackTrace();
	// }
	// }
}
