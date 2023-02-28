package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener implements ActionListener
{
  private VTGraphicsModeClientWriter writer;
  private int keycode;
  
  public VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(VTGraphicsModeClientWriter writer, int keycode)
  {
    this.writer = writer;
    this.keycode = keycode;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.remoteKeyPress(keycode, 0, 0, ' ');
  }
}