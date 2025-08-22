package org.vash.vate.console.graphical.menu.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTConsole;

public class VTGraphicalConsoleMenuItemListener implements ActionListener
{
  private final VTConsole console;
  
  public VTGraphicalConsoleMenuItemListener(final VTConsole console)
  {
    this.console = console;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    console.requestFocus();
    console.clearInput();
    console.input(e.getActionCommand());
  }
}