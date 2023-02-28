package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener implements ActionListener
{
  private VTGraphicsModeClientWriter writer;
  private int captureScale;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuScreenCaptureScaleOptionsListener(VTGraphicsModeClientWriter writer, int captureScale)
  {
    this.writer = writer;
    this.captureScale = captureScale;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.updateCaptureScale(captureScale);
  }
}