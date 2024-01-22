package org.vash.vate.server.console.local.standard.command;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.Rectangle;

import org.vash.vate.console.VTConsole;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.server.console.local.standard.VTServerStandardLocalConsoleCommandProcessor;

public class VTDISPLAY extends VTServerStandardLocalConsoleCommandProcessor
{
  public VTDISPLAY()
  {
    this.setFullName("*VTDISPLAY");
    this.setAbbreviatedName("*VTDP");
    this.setFullSyntax("*VTDISPLAY");
    this.setAbbreviatedSyntax("*VTDP");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    message.setLength(0);
    int count = 0;
    GraphicsDevice[] devices = VTGraphicalDeviceResolver.getRasterDevices();
    if (devices != null && devices.length > 0)
    {
      message.append("\rVT>List of server graphical display devices:\nVT>");
      for (GraphicsDevice device : devices)
      {
        DisplayMode mode = device.getDisplayMode();
        int refreshRate = mode.getRefreshRate();
        Rectangle bounds = device.getDefaultConfiguration().getBounds();
        message.append("\nVT>Number: [" + (count++) + "]");
        message.append("\nVT>ID: [" + device.getIDstring() + "]");
        message.append("\nVT>Mode: [" + mode.getWidth() + "x" + mode.getHeight() + (refreshRate > 0 ? "@" + mode.getRefreshRate() + "Hz" : "") + "]");
        message.append("\nVT>Origin: [X:" + bounds.x + " Y:" + bounds.y + "]");
        message.append("\nVT>");
      }
      message.append("\nVT>End of server graphical display devices list\nVT>");
    }
    else
    {
      message.append("\rVT>No graphical display devices found on server!\nVT>");
    }
    VTConsole.print(message.toString());
  }
  
  public void close()
  {
    
  }
}
