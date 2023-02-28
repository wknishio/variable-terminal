package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener implements ItemListener
{
  private int colorQuality;
  private CheckboxMenuItem option;
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuColorOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option, int colorQuality)
  {
    this.writer = writer;
    this.option = option;
    this.colorQuality = colorQuality;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.updateColorQuality(colorQuality);
    }
    else
    {
      option.setState(true);
    }
  }
}