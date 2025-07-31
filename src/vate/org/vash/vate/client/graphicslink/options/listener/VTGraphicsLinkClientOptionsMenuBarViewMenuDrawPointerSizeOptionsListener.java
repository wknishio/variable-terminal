package org.vash.vate.client.graphicslink.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener implements ActionListener
{
  private VTGraphicsLinkClientWriter writer;
  private int mode;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(VTGraphicsLinkClientWriter writer, int mode)
  {
    this.writer = writer;
    this.mode = mode;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    if (mode == 0)
    {
      writer.normalizeDrawPointerSize();
    }
    else if (mode > 0)
    {
      writer.increaseDrawPointerSize();
    }
    else
    {
      writer.decreaseDrawPointerSize();
    }
  }
}