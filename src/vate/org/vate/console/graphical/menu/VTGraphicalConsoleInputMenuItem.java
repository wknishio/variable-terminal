package org.vate.console.graphical.menu;

import java.awt.MenuItem;

import org.vate.console.graphical.menu.listener.VTGraphicalConsoleInputMenuItemListener;

public class VTGraphicalConsoleInputMenuItem extends MenuItem
{
	private static final long serialVersionUID = 1L;
	
	public VTGraphicalConsoleInputMenuItem(String label, String command)
	{
		this.setLabel(label.trim());
		this.setActionCommand(command);
		this.addActionListener(new VTGraphicalConsoleInputMenuItemListener());
	}
}