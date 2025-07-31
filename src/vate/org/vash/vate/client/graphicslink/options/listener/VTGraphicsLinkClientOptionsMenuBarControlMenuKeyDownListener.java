package org.vash.vate.client.graphicslink.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener implements ActionListener
{
  private VTGraphicsLinkClientWriter writer;
  private int keycode;
  
  public VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(VTGraphicsLinkClientWriter writer, int keycode)
  {
    this.writer = writer;
    this.keycode = keycode;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.remoteKeyDown(keycode, 0, 0, ' ');
  }
}