package org.vash.vate.client.graphicslink.remote.listener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientRemoteInterfaceFocusListener implements FocusListener
{
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientRemoteInterfaceFocusListener(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void focusGained(FocusEvent e)
  {
    synchronized (writer)
    {
      writer.notify();
    }
  }
  
  public void focusLost(FocusEvent e)
  {
    
  }
}