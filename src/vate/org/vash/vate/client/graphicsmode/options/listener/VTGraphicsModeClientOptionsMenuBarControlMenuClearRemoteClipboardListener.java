package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarControlMenuClearRemoteClipboardListener implements ActionListener
{
  private VTGraphicsModeClientWriter writer;

  public VTGraphicsModeClientOptionsMenuBarControlMenuClearRemoteClipboardListener(VTGraphicsModeClientWriter writer)
  {
    this.writer = writer;
  }

  public void actionPerformed(ActionEvent e)
  {
    writer.clearRemoteClipboardContents();
  }
}