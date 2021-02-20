package org.vate.client.graphicsmode.remote.listener;

import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;

import org.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientRemoteInterfaceFocusListener implements FocusListener
{
  private VTGraphicsModeClientWriter writer;

  public VTGraphicsModeClientRemoteInterfaceFocusListener(VTGraphicsModeClientWriter writer)
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