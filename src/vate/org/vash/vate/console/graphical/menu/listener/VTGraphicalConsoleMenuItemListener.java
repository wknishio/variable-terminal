package org.vash.vate.console.graphical.menu.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTConsoleImplementation;

public class VTGraphicalConsoleMenuItemListener implements ActionListener
{
  private final VTConsoleImplementation console;
  
  public VTGraphicalConsoleMenuItemListener(final VTConsoleImplementation console)
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