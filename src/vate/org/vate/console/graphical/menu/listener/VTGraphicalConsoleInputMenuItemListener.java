package org.vate.console.graphical.menu.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.console.graphical.VTGraphicalConsole;

public class VTGraphicalConsoleInputMenuItemListener implements ActionListener
{
	public void actionPerformed(ActionEvent e)
	{
		VTGraphicalConsole.clearInput();
		VTGraphicalConsole.input(e.getActionCommand());
	}
}