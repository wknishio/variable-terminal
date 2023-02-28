package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener implements ItemListener
{
  private int imageCoding;
  private CheckboxMenuItem option;
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuImageCodingOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option, int imageCoding)
  {
    this.writer = writer;
    this.option = option;
    this.imageCoding = imageCoding;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.updateImageCoding(imageCoding);
    }
    else
    {
      option.setState(true);
    }
  }
}