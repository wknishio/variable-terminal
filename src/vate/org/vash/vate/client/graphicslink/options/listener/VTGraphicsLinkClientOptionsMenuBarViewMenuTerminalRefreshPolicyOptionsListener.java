package org.vash.vate.client.graphicslink.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;

public class VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener implements ItemListener
{
  private CheckboxMenuItem option;
  private VTGraphicsLinkClientWriter writer;
  private int state;
  
  public VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(VTGraphicsLinkClientWriter writer, CheckboxMenuItem option, int state)
  {
    this.writer = writer;
    this.option = option;
    this.state = state;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.setTerminalRefreshPolicy(state);
    }
    else
    {
      option.setState(true);
    }
  }
}