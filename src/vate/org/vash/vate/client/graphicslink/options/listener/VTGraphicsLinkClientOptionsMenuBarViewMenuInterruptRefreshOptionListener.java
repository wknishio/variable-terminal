package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuInterruptRefreshOptionListener implements ItemListener
{
  private CheckboxMenuItem option;
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuInterruptRefreshOptionListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option)
  {
    this.writer = writer;
    this.option = option;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.interruptRefresh();
    }
    else
    {
      option.setState(true);
    }
  }
}