package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeySupressListener implements ItemListener
{
  private CheckboxMenuItem option;
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeySupressListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option)
  {
    this.writer = writer;
    this.option = option;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.toggleSuppressLocalKeyCombinations();
    }
    else
    {
      option.setState(true);
    }
  }
}