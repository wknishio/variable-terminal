package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuDeltaCodingOptionsListener implements ItemListener
{
  //private CheckboxMenuItem option;
  //private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuDeltaCodingOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option)
  {
    //this.writer = writer;
    //this.option = option;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      //writer.toggleSeparatedCoding();
    }
    else
    {
      //option.setState(true);
    }
  }
}