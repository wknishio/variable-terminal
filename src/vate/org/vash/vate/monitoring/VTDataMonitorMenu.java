package org.vash.vate.monitoring;

import java.awt.Menu;

public class VTDataMonitorMenu extends VTDataMonitorPanel
{
  private final Menu menu;
  
  public VTDataMonitorMenu(Menu menu)
  {
    this.menu = menu;
  }
  
  public void setMessage(String message)
  {
    menu.setLabel(message);
  }
}