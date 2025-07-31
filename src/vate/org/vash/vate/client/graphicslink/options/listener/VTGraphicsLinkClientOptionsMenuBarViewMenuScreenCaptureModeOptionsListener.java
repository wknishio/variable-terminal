package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener implements ItemListener
{
  private int value;
  private CheckboxMenuItem option;
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option, int value)
  {
    this.writer = writer;
    this.option = option;
    this.value = value;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.setScreenCaptureMode(value);
    }
    else
    {
      option.setState(true);
    }
  }
}