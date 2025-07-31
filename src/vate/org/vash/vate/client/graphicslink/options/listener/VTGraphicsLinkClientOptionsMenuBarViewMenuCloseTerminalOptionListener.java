package org.vash.vate.client.graphicslink.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuCloseTerminalOptionListener implements ActionListener
{
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuCloseTerminalOptionListener(VTGraphicsLinkClientWriter writer)
  {
    this.writer = writer;
  }
  
  public void actionPerformed(ActionEvent e)
  {
    writer.setStopped(true);
    // writer.dispose();
  }
}