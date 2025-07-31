package org.vash.vate.client.graphicslink.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener implements ActionListener
{
  private VTGraphicsLinkClientWriter writer;
  private int captureScale;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(VTGraphicsLinkClientWriter writer, int captureScale)
  {
    this.writer = writer;
    this.captureScale = captureScale;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.updateCaptureScale(captureScale);
  }
}