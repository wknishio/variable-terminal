package org.vash.vate.console.graphical.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTSystemConsole;

public class VTGraphicalConsoleToggleScrollModeActionListener implements ActionListener
{
  public VTGraphicalConsoleToggleScrollModeActionListener()
  {
    
  }
  
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      VTSystemConsole.toggleFlushMode();
    }
    catch (Throwable ex)
    {
      
    }
  }
}
