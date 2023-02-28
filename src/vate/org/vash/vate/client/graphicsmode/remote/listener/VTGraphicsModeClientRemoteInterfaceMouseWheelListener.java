package org.vash.vate.client.graphicsmode.remote.listener;

import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vash.vate.graphics.control.VTAWTControlEvent;

public class VTGraphicsModeClientRemoteInterfaceMouseWheelListener implements MouseWheelListener
{
  private VTGraphicsModeClientWriter writer;
  private VTAWTControlEvent untyped;
  
  public VTGraphicsModeClientRemoteInterfaceMouseWheelListener(VTGraphicsModeClientWriter writer)
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