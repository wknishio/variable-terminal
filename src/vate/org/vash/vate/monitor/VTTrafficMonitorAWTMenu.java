package org.vash.vate.monitor;

import java.awt.Menu;

public class VTTrafficMonitorAWTMenu extends VTTrafficMonitorPanel
{
  private final Menu menu;
  
  public VTTrafficMonitorAWTMenu(Menu menu)
  {
    this.menu = menu;
  }
  
  public void setMessage(String message)
  {
    menu.setLabel(message);
  }
}