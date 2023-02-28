package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener implements ItemListener
{
  private int value;
  private CheckboxMenuItem option;
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureModeOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option, int value)
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