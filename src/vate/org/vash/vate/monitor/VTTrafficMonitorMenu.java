package org.vash.vate.monitor;

import java.awt.Menu;

public class VTTrafficMonitorMenu extends VTTrafficMonitorPanel
{
  private final Menu menu;
  
  public VTTrafficMonitorMenu(Menu menu)
  {
    this.menu = menu;
  }
  
  public void setMessage(String message)
  {
    menu.setLabel(message);
  }
}