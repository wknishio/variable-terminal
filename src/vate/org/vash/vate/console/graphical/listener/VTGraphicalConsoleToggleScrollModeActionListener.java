package org.vash.vate.console.graphical.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTMainConsole;

public class VTGraphicalConsoleToggleScrollModeActionListener implements ActionListener
{
  public VTGraphicalConsoleToggleScrollModeActionListener()
  {
    
  }
  
  public void actionPerformed(ActionEvent e)
  {
    try
    {
      VTMainConsole.toggleFlushMode();
    }
    catch (Throwable ex)
    {
      
    }
  }
}
