package org.vash.vate.client.graphicslink.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuToggleScrollbarsOptionListener implements ActionListener
{
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuToggleScrollbarsOptionListener(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.toggleHideScrollBars();
  }
}