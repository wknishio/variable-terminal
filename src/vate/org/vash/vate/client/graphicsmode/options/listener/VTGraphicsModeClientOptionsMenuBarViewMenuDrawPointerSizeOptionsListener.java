package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener implements ActionListener
{
  private VTGraphicsModeClientWriter writer;
  private int mode;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuDrawPointerSizeOptionsListener(VTGraphicsModeClientWriter writer, int mode)
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