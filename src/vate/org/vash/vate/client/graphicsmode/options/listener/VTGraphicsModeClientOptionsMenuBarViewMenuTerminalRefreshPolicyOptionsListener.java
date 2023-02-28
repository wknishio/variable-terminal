package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener implements ItemListener
{
  private CheckboxMenuItem option;
  private VTGraphicsModeClientWriter writer;
  private int state;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuTerminalRefreshPolicyOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option, int state)
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