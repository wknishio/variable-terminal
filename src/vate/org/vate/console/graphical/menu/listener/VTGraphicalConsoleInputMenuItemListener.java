package org.vate.console.graphical.menu.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vate.console.VTConsole;

public class VTGraphicalConsoleInputMenuItemListener implements ActionListener
{
	public void actionPerformed(ActionEvent e)
	{
		VTConsole.clearInput();
		VTConsole.input(e.getActionCommand());
	}
}