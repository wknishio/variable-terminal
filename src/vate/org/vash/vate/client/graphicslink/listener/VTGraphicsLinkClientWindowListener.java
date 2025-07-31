package org.vash.vate.client.graphicslink.listener;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

public class VTGraphicsLinkClientWindowListener implements WindowListener
{
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientWindowListener(VTGraphicsLinkClientWriter writer)
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