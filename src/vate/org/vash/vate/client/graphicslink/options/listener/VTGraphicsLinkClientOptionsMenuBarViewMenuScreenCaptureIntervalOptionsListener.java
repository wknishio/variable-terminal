package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener implements ItemListener
{
  private VTGraphicsLinkClientWriter writer;
  private CheckboxMenuItem option;
  private int screenCaptureInterval;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureIntervalOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option, int screenCaptureInterval)
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