package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener implements ItemListener
{
  private VTGraphicsModeClientWriter writer;
  private CheckboxMenuItem option;
  private int screenCaptureInterval;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option, int screenCaptureInterval)
  {
    this.writer = writer;
    this.option = option;
    this.screenCaptureInterval = screenCaptureInterval;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.updateScreenCaptureInterval(screenCaptureInterval);
    }
    else
    {
      option.setState(true);
    }
  }
}