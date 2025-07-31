package org.vash.vate.client.graphicslink.remote.listener;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;
import org.vash.vate.graphics.control.VTAWTControlEvent;

public class VTGraphicsLinkClientRemoteInterfaceMouseWheelListener implements MouseWheelListener
{
  private VTGraphicsLinkClientWriter writer;
  private VTAWTControlEvent untyped;
  
  public VTGraphicsLinkClientRemoteInterfaceMouseWheelListener(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
    this.untyped = new VTAWTControlEvent();
  }
  
  public void mouseWheelMoved(MouseWheelEvent event)
  {
    // System.out.println(event.toString());
    untyped.id = event.getID();
    untyped.wheel = event.getWheelRotation();
    writer.writeEvent(untyped);
    event.consume();
  }
}