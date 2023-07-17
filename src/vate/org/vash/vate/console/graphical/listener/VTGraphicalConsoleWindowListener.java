package org.vash.vate.console.graphical.listener;

import java.awt.Frame;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.vash.vate.console.graphical.VTGraphicalConsole;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTGraphicalConsoleWindowListener implements WindowListener
{
  public void windowActivated(WindowEvent e)
  {
    // VTGraphicalConsole.setShowing(true);
    VTGraphicalConsole.deiconifyFrame();
  }
  
  public void windowClosed(WindowEvent e)
  {
    
  }
  
  public void windowClosing(WindowEvent e)
  {
    if (VTGraphicalConsole.ignoreClose)
    {
      VTGraphicalConsole.getStaticFrame().setExtendedState(Frame.ICONIFIED);
      return;
    }
    VTRuntimeExit.exit(0);
  }
  
  public void windowDeactivated(WindowEvent e)
  {
    VTGraphicalConsole.iconifyFrame();
    // VTGraphicalConsole.setShowing(false);
  }
  
  public void windowDeiconified(WindowEvent e)
  {
    // VTGraphicalConsole.deiconifyFrame();
  }
  
  public void windowIconified(WindowEvent e)
  {
    // VTGraphicalConsole.iconifyFrame();
  }
  
  public void windowOpened(WindowEvent e)
  {
    // VTGraphicalConsole.deiconifyFrame();
  }
}