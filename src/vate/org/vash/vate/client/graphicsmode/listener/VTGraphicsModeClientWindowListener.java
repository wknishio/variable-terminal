package org.vash.vate.client.graphicsmode.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

public class VTGraphicsModeClientWindowListener implements WindowListener
{
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientWindowListener(VTGraphicsModeClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void windowActivated(WindowEvent e)
  {
    // System.out.println("writer.windowActivated()");
    writer.requestFocus();
  }
  
  public void windowClosed(WindowEvent e)
  {
    // writer.setStopped(true);
  }
  
  public void windowClosing(WindowEvent e)
  {
    writer.setStopped(true);
    VTGlobalTextStyleManager.unregisterWindow(e.getWindow());
    // writer.dispose();
  }
  
  public void windowDeactivated(WindowEvent e)
  {
    writer.releaseAllPressedKeys();
  }
  
  public void windowDeiconified(WindowEvent e)
  {
    
  }
  
  public void windowIconified(WindowEvent e)
  {
    
  }
  
  public void windowOpened(WindowEvent e)
  {
    writer.requestFocus();
  }
}