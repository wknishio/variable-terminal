package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuColorCodingOptionsListener implements ItemListener
{
  //private CheckboxMenuItem option;
  //private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuColorCodingOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option)
  {
    //this.writer = writer;
    //this.option = option;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      //writer.toggleDynamicCoding();
    }
    else
    {
      //option.setState(true);
    }
  }
}