package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener implements ActionListener
{
  private VTGraphicsModeClientWriter writer;
  private int keycode;
  
  public VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(VTGraphicsModeClientWriter writer, int keycode)
  {
    this.writer = writer;
    this.keycode = keycode;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.remoteKeyDown(keycode, 0, 0, ' ');
  }
}