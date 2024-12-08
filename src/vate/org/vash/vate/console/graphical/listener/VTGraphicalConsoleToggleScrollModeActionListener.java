package org.vash.vate.console.graphical.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTConsole;

public class VTGraphicalConsoleToggleScrollModeActionListener implements ActionListener
{
  public VTGraphicalConsoleToggleScrollModeActionListener()
  {
    
  }
  
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      VTConsole.toggleFlushMode();
    }
    catch (Throwable ex)
    {
      
    }
  }
}
