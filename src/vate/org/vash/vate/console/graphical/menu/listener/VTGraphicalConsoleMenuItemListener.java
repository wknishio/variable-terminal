package org.vash.vate.console.graphical.menu.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTConsole;

public class VTGraphicalConsoleMenuItemListener implements ActionListener
{
  public void actionPerformed(ActionEvent e)
  {
    VTConsole.requestFocus();
    VTConsole.clearInput();
    VTConsole.input(e.getActionCommand());
  }
}