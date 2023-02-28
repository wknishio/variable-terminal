package org.vash.vate.console.graphical.menu;

import java.awt.MenuItem;

import org.vash.vate.console.graphical.menu.listener.VTGraphicalConsoleMenuItemListener;

public class VTGraphicalConsoleMenuItem extends MenuItem
{
  private static final long serialVersionUID = 1L;
  
  public VTGraphicalConsoleMenuItem(String label, String command)
  {
    this.setLabel(label.trim());
    this.setActionCommand(command);
    this.addActionListener(new VTGraphicalConsoleMenuItemListener());
  }
}