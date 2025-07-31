package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener implements ItemListener
{
  private int imageCoding;
  private CheckboxMenuItem option;
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuImageCodingOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option, int imageCoding)
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