package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuDeltaCodingOptionsListener implements ItemListener
{
  //private CheckboxMenuItem option;
  //private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuDeltaCodingOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option)
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