package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuClearInterfaceOptionListener implements ActionListener
{
  private VTGraphicsModeClientWriter writer;

  public VTGraphicsModeClientOptionsMenuBarViewMenuClearInterfaceOptionListener(VTGraphicsModeClientWriter writer)
  {
    this.writer = writer;
  }

  public void actionPerformed(ActionEvent e)
  {
    writer.clearRemoteGraphics();
  }
}