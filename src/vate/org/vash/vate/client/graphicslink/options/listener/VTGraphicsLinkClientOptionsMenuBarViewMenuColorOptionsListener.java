package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener implements ItemListener
{
  private int colorQuality;
  private CheckboxMenuItem option;
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuColorOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option, int colorQuality)
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