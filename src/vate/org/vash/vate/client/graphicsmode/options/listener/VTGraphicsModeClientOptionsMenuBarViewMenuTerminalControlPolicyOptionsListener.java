package org.vash.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener implements ItemListener
{
  private CheckboxMenuItem option;
  private VTGraphicsModeClientWriter writer;
  private int state;
  
  public VTGraphicsModeClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option, int state)
  {
    this.writer = writer;
    this.option = option;
    this.state = state;
  }
  
  public void itemStateChanged(ItemEvent e)
  {
    if (e.getStateChange() == ItemEvent.SELECTED)
    {
      writer.setTerminalControlPolicy(state);
    }
    else
    {
      option.setState(true);
    }
  }
}