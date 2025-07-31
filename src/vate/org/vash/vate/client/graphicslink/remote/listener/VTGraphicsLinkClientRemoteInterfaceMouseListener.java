package org.vash.vate.client.graphicslink.remote.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;
import org.vash.vate.graphics.control.VTAWTControlEvent;

public class VTGraphicsLinkClientRemoteInterfaceMouseListener implements MouseListener
{
  private VTGraphicsLinkClientWriter writer;
  private VTAWTControlEvent untyped;
  
  public VTGraphicsLinkClientRemoteInterfaceMouseListener(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
    this.untyped = new VTAWTControlEvent();
  }
  
  public void mouseClicked(MouseEvent event)
  {
    
  }
  
  public void mouseEntered(MouseEvent event)
  {
    
  }
  
  public void mouseExited(MouseEvent event)
  {
    
  }
  
  public void mousePressed(MouseEvent event)
  {
    // System.out.println(event.toString());
    untyped.id = event.getID();
    untyped.button = event.getModifiersEx();
    writer.writeEvent(untyped);
    event.consume();
  }
  
  public void mouseReleased(MouseEvent event)
  {
    // System.out.println(event.toString());
    untyped.id = event.getID();
    untyped.button = event.getModifiersEx();
    writer.writeEvent(untyped);
    event.consume();
  }
}