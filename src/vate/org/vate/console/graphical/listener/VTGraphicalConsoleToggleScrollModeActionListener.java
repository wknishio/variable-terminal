package org.vate.console.graphical.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.console.graphical.VTGraphicalConsole;

public class VTGraphicalConsoleToggleScrollModeActionListener implements ActionListener
{
	public VTGraphicalConsoleToggleScrollModeActionListener()
	{
		
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			VTGraphicalConsole.toggleScrollMode();
		}
		catch (Throwable ex)
		{
			
		}
	}
}
