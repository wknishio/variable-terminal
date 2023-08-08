package org.vash.vate.graphics.device;

import java.awt.DisplayMode;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Point;
import java.awt.Rectangle;
import java.awt.Window;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.reflection.VTReflectionUtils;

public class VTGraphicalDeviceResolver
{
  public static GraphicsDevice[] getRasterDevices()
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return null;
    }
    try
    {
      List<GraphicsDevice> devices = new ArrayList<GraphicsDevice>();
      for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
      {
        if (device.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
        {
          devices.add(device);
        }
      }
      return devices.toArray(new GraphicsDevice[devices.size()]);
    }
    catch (Throwable t)
    {
      return null;
    }
  }
  
  public static GraphicsDevice getCurrentDevice(Window window)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return null;
    }
    GraphicsDevice current = null;
    for (GraphicsDevice device : GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices())
    {
      Rectangle deviceBounds = device.getDefaultConfiguration().getBounds();
      Point windowLocation = window.getLocation();
      if (windowLocation.getX() >= deviceBounds.getMinX() && windowLocation.getX() <= deviceBounds.getMaxX() && windowLocation.getY() >= deviceBounds.getMinY() && windowLocation.getY() <= deviceBounds.getMaxY())
      {
        current = device;
        break;
      }
    }
    return current;
  }
  
  public static Rectangle getDeviceBounds(GraphicsDevice graphicsDevice)
  {
    if (VTReflectionUtils.isAWTHeadless())
    {
      return null;
    }
    if (graphicsDevice == null)
    {
      Rectangle virtualBounds = new Rectangle();
      GraphicsEnvironment environment = GraphicsEnvironment.getLocalGraphicsEnvironment();
      GraphicsDevice[] devices = environment.getScreenDevices();
      for (int j = 0; j < devices.length; j++)
      {
        GraphicsDevice device = devices[j];
        if (device.getType() == GraphicsDevice.TYPE_RASTER_SCREEN)
        {
          Rectangle bounds = device.getDefaultConfiguration().getBounds();
          DisplayMode mode = device.getDisplayMode();
          virtualBounds = virtualBounds.union(new Rectangle(bounds.x, bounds.y, mode.getWidth(), mode.getHeight()));
        }
      }
      // VTConsole.println("virtualBounds:" + virtualBounds.x + " " +
      // virtualBounds.y + " " + virtualBounds.width + " " +
      // virtualBounds.height);
      // virtualBounds.x = 0;
      // virtualBounds.y = 0;
      return virtualBounds;
    }
    Rectangle configuration = graphicsDevice.getDefaultConfiguration().getBounds();
    DisplayMode mode = graphicsDevice.getDisplayMode();
    Rectangle deviceBounds = new Rectangle(configuration.x, configuration.y, mode.getWidth(), mode.getHeight());
    // bounds.x = 0;
    // bounds.y = 0;
    return deviceBounds;
  }
  
  /*
   * public static void main(String[] args) { GraphicsDevice[] devices =
   * VTUsableGraphicsDeviceResolver.getRasterDevices();
   * System.out.println("devices: " + devices.length); for (GraphicsDevice
   * device : devices) { System.out.println("device: " + device.getIDstring());
   * } }
   */
}