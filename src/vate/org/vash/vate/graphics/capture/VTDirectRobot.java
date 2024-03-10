package org.vash.vate.graphics.capture;

import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.Rectangle;
import java.awt.Robot;
import java.awt.Toolkit;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

public final class VTDirectRobot
{
  public final GraphicsDevice device;
  private final Object robotPeer;
  
  private final int getRGBPixelsMethodType;
  private final Method getRGBPixelsMethod;
  private Object getRGBPixelsMethodParam;
  
  private final boolean methodAvailable;
  private boolean disposed = false;
  
  // private static boolean searchedMouseInfoPeer;
  // private static MouseInfoPeer mouseInfoPeer;
  
  public VTDirectRobot() throws Throwable
  {
    this(null);
  }
  
  public boolean getDirectRGBPixelsMethodAvailable()
  {
    return !disposed && methodAvailable;
  }
  
  @SuppressWarnings("all")
  public VTDirectRobot(GraphicsDevice device) throws Throwable
  {
    boolean available = false;
    if (device == null)
    {
      device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
    }
    
    this.device = device;
    Toolkit toolkit = Toolkit.getDefaultToolkit();
    int parameterCount = 2;
    
    Object cf = toolkit;
    
    Method createRobot = null;
    try
    {
      createRobot = Class.forName("sun.awt.ComponentFactory").getDeclaredMethod("createRobot", Robot.class, GraphicsDevice.class);
      parameterCount = 2;
    }
    catch (NoSuchMethodException e)
    {
      try
      {
        createRobot = Class.forName("sun.awt.ComponentFactory").getDeclaredMethod("createRobot", GraphicsDevice.class);
        parameterCount = 1;
      }
      catch (NoSuchMethodException e1)
      {
        // e1.printStackTrace();
      }
      
    }
    
    if (parameterCount == 1)
    {
      createRobot.setAccessible(true);
      robotPeer = createRobot.invoke(toolkit, device);
    }
    else
    {
      createRobot.setAccessible(true);
      robotPeer = createRobot.invoke(toolkit, null, device);
    }
    // peer = ((ComponentFactory) toolkit).createRobot(null, device);
    
    Class<?> peerClass = robotPeer.getClass();
    Method method = null;
    int methodType = -1;
    Object methodParam = null;
    try
    {
      method = peerClass.getDeclaredMethod("getRGBPixels", new Class<?>[]
      { Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, int[].class });
      methodType = 0;
    }
    catch (Throwable ex)
    {
    }
    if (methodType < 0)
    {
      try
      {
        method = peerClass.getDeclaredMethod("getScreenPixels", new Class<?>[]
        { Rectangle.class, int[].class });
        methodType = 1;
      }
      catch (Throwable ex)
      {
      }
    }
    
    if (methodType < 0)
    {
      try
      {
        method = peerClass.getDeclaredMethod("getScreenPixels", new Class<?>[]
        { Integer.TYPE, Rectangle.class, int[].class });
        methodType = 2;
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        int count = devices.length;
        for (int i = 0; i != count; ++i)
        {
          if (device.equals(devices[i]))
          {
            methodParam = Integer.valueOf(i);
            break;
          }
        }
      }
      catch (Throwable ex)
      {
      }
    }
    
    if (methodType < 0)
    {
      try
      {
        method = peerClass.getDeclaredMethod("getRGBPixelsImpl", new Class<?>[]
        { Class.forName("sun.awt.X11GraphicsConfig"), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, int[].class });
        methodType = 3;
        Field field = peerClass.getDeclaredField("xgc");
        try
        {
          field.setAccessible(true);
          methodParam = field.get(robotPeer);
        }
        finally
        {
          // field.setAccessible(false);
        }
      }
      catch (Throwable ex)
      {
      }
    }
    
    if (methodType >= 0 && method != null && (methodType <= 1 || methodParam != null))
    {
      getRGBPixelsMethod = method;
      getRGBPixelsMethodType = methodType;
      getRGBPixelsMethodParam = methodParam;
      
      try
      {
        getRGBPixelsMethod.setAccessible(true);
      }
      catch (Throwable t)
      {
        
      }
      
      // test screen capture
      int x = 0;
      int y = 0;
      int width = 1;
      int height = 1;
      int[] pixels = new int[1];
      
      try
      {
        if (getRGBPixelsMethodType == 0)
        {
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
        }
        else if (getRGBPixelsMethodType == 1)
        {
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { new Rectangle(x, y, width, height), pixels });
        }
        else if (getRGBPixelsMethodType == 2)
        {
          methodParam = Integer.valueOf(0);
          GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
          int count = devices.length;
          for (int i = 0; i != count; ++i)
          {
            if (device.equals(devices[i]))
            {
              methodParam = Integer.valueOf(i);
              break;
            }
          }
          getRGBPixelsMethodParam = methodParam;
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { getRGBPixelsMethodParam, new Rectangle(x, y, width, height), pixels });
        }
        else
        {
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { getRGBPixelsMethodParam, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
        }
        available = true;
      }
      catch (Throwable ex)
      {
        
      }
      methodAvailable = available;
    }
    else
    {
      getRGBPixelsMethodType = -1;
      getRGBPixelsMethod = null;
      methodAvailable = false;
    }
  }
  
  public final boolean getRGBPixels(final int x, final int y, final int width, final int height, final int[] pixels)
  {
    if (!getDirectRGBPixelsMethodAvailable())
    {
      return false;
    }
//		if (Platform.isWindows())
//		{
//			return VTWin32JNAScreenShot.getPixelData(x, y, width, height, pixels, null);
//		}
    if (getRGBPixelsMethod != null)
    {
      try
      {
        if (getRGBPixelsMethodType == 0)
        {
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
        }
        else if (getRGBPixelsMethodType == 1)
        {
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { new Rectangle(x, y, width, height), pixels });
        }
        else if (getRGBPixelsMethodType == 2)
        {
          Integer methodParam = Integer.valueOf(0);
          GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
          int count = devices.length;
          for (int i = 0; i != count; ++i)
          {
            if (device.equals(devices[i]))
            {
              methodParam = Integer.valueOf(i);
              break;
            }
          }
          getRGBPixelsMethodParam = methodParam;
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { getRGBPixelsMethodParam, new Rectangle(x, y, width, height), pixels });
        }
        else
        {
          getRGBPixelsMethod.invoke(robotPeer, new Object[]
          { getRGBPixelsMethodParam, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
        }
        return true;
      }
      catch (Throwable ex)
      {
        // ex.printStackTrace();
      }
    }
    // int[] tmp = getRGBPixels(new Rectangle(x, y, width, height));
    // System.arraycopy(tmp, 0, pixels, 0, width * height);
    return false;
  }
  
  public final void dispose()
  {
    getRGBPixelsMethodParam = null;
    Method method = getRGBPixelsMethod;
    if (method != null)
    {
      try
      {
        // method.setAccessible(false);
      }
      catch (Throwable ex)
      {
        
      }
    }
    // Using reflection now because of some peers not having ANY support at all
    // (1.5)
    try
    {
      robotPeer.getClass().getDeclaredMethod("dispose", new Class<?>[0]).invoke(robotPeer, (Object) null);
    }
    catch (Throwable t)
    {
      
    }
    disposed = true;
  }
  
  @SuppressWarnings("all")
  protected final void finalize() throws Throwable
  {
    try
    {
      dispose();
    }
    finally
    {
      super.finalize();
    }
  }
}