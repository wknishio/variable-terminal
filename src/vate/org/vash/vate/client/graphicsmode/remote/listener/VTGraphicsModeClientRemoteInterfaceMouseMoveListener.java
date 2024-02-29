package org.vash.vate.client.graphicsmode.remote.listener;

import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vash.vate.graphics.control.VTAWTControlEvent;

// import org.eclipse.swt.SWT;
// import org.eclipse.swt.events.MouseEvent;
// import org.eclipse.swt.events.MouseMoveListener;
// import org.eclipse.swt.widgets.Event;

public class VTGraphicsModeClientRemoteInterfaceMouseMoveListener implements MouseMotionListener
{
  private VTGraphicsModeClientWriter writer;
  private VTAWTControlEvent untyped;
  
  public VTGraphicsModeClientRemoteInterfaceMouseMoveListener(VTGraphicsModeClientWriter writer)
  {
    //this.ignoreNextEvent = false;
    this.writer = writer;
    this.untyped = new VTAWTControlEvent();
    // Thread refreshThread = new Thread(new MouseRefreshThread());
    // refreshThread.start();
  }
  
//  public void setIgnoreNextEvent()
//  {
//    this.ignoreNextEvent = true;
//  }
//  
//  public boolean isIgnoreNextEvent()
//  {
//    return this.ignoreNextEvent;
//  }
  
  /*
   * private class MouseRefreshThread implements Runnable { public void run() {
   * while (!writer.isStopped()) { if (untyped.refreshed) {
   * writer.writeEvent(untyped); untyped.refreshed = false; } else { try {
   * Thread.sleep(100); } catch (InterruptedException e) { } } } } }
   */
  
  public void mouseDragged(MouseEvent event)
  {
    //System.out.println(event.toString());
    untyped.id = event.getID();
    untyped.x = event.getX();
    untyped.y = event.getY();
    // untyped.refreshed = true;
    writer.writeEvent(untyped);
    event.consume();
  }
  
  public void mouseMoved(MouseEvent event)
  {
    //System.out.println(event.toString());
    untyped.id = event.getID();
    untyped.x = event.getX();
    untyped.y = event.getY();
    // untyped.refreshed = true;
    writer.writeEvent(untyped);
    event.consume();
  }
}