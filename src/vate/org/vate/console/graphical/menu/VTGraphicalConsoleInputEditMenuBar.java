package org.vate.console.graphical.menu;

import java.awt.Menu;
import java.awt.MenuItem;

import org.vate.console.graphical.listener.VTGraphicalConsoleCopyActionListener;
import org.vate.console.graphical.listener.VTGraphicalConsolePasteActionListener;
import org.vate.console.graphical.listener.VTGraphicalConsoleToggleScrollModeActionListener;

public class VTGraphicalConsoleInputEditMenuBar extends Menu
{
	private static final long serialVersionUID = 1L;
	private MenuItem copyMenuItem;
	private MenuItem pasteMenuItem;
	private MenuItem toggleScrollModeItem;
	private VTGraphicalConsoleCopyActionListener copyActionListener;
	private VTGraphicalConsolePasteActionListener pasteActionListener;
	private VTGraphicalConsoleToggleScrollModeActionListener toggleScrollModeActionListener;
	
	public VTGraphicalConsoleInputEditMenuBar()
	{
		copyActionListener = new VTGraphicalConsoleCopyActionListener();
		pasteActionListener = new VTGraphicalConsolePasteActionListener();
		toggleScrollModeActionListener = new VTGraphicalConsoleToggleScrollModeActionListener();
		this.setLabel("Console Controls ");
		this.copyMenuItem = new MenuItem("Copy Text");
		this.pasteMenuItem = new MenuItem("Paste Text");
		this.toggleScrollModeItem = new MenuItem("Toggle Scroll Mode");
		this.copyMenuItem.addActionListener(copyActionListener);
		this.pasteMenuItem.addActionListener(pasteActionListener);
		this.toggleScrollModeItem.addActionListener(toggleScrollModeActionListener);
		this.add(copyMenuItem);
		this.add(pasteMenuItem);
		this.add(toggleScrollModeItem);
		this.setEnabled(false);
	}
	
	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		for (int i = 0; i < this.getItemCount(); i++)
		{
			this.getItem(i).setEnabled(enabled);
		}
	}
}