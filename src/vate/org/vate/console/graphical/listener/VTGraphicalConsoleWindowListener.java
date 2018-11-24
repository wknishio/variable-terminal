package org.vate.console.graphical.listener;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.vate.console.graphical.VTGraphicalConsole;

public class VTGraphicalConsoleWindowListener implements WindowListener
{
	public void windowActivated(WindowEvent e)
	{
		// VTGraphicalConsole.setShowing(true);
	}
	
	public void windowClosed(WindowEvent e)
	{
		
	}
	
	public void windowClosing(WindowEvent e)
	{
		if (VTGraphicalConsole.getIgnoreClose())
		{
			VTGraphicalConsole.getFrame().setExtendedState(Frame.ICONIFIED);
			return;
		}
		System.exit(0);
	}
	
	public void windowDeactivated(WindowEvent e)
	{
		// VTGraphicalConsole.setShowing(false);
	}
	
	public void windowDeiconified(WindowEvent e)
	{
		VTGraphicalConsole.deiconifyFrame();
	}
	
	public void windowIconified(WindowEvent e)
	{
		VTGraphicalConsole.iconifyFrame();
	}
	
	public void windowOpened(WindowEvent e)
	{
		
	}
}