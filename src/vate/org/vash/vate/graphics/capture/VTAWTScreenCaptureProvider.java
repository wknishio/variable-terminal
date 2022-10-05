package org.vash.vate.graphics.capture;

import com.bric.image.VTARGBPixelGrabber;
//import com.sun.jna.Platform;

import java.awt.Dimension;
import java.awt.Graphics2D;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.MouseInfo;
import java.awt.Point;
import java.awt.PointerInfo;
import java.awt.Rectangle;
import java.awt.Robot;
//import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.awt.image.DataBuffer;
import java.awt.image.DataBufferByte;
import java.awt.image.DataBufferInt;
import java.awt.image.DataBufferUShort;
//import java.awt.peer.RobotPeer;
//import java.awt.image.WritableRaster;
//import javax.swing.JLabel;
//import javax.swing.UIManager;

import org.vash.vate.VT;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.graphics.image.VTIndexedColorModel;

public final class VTAWTScreenCaptureProvider
{
  public static final int VT_COLOR_QUALITY_64 = 0; // 64 rgb-222 or rgb-4x4x4
  public static final int VT_COLOR_QUALITY_216 = 1; // 216 rgb-6x6x6
  public static final int VT_COLOR_QUALITY_32768 = 2; // 32768 rgb-555
  public static final int VT_COLOR_QUALITY_16777216 = 3; // 16777216 rgb-888
  public static final int VT_COLOR_QUALITY_16 = 4; // 16 rgbi
  public static final int VT_COLOR_QUALITY_32 = 5; // 32 rgbii
  public static final int VT_COLOR_QUALITY_512 = 6; // 512 rgb-333
  public static final int VT_COLOR_QUALITY_4096 = 7; // 4096 rgb-444
  public static final int VT_COLOR_QUALITY_8 = 8; // 8 rgb-111
  public static final int VT_COLOR_QUALITY_125 = 9; // 125 rgb-5x5x5
  public static final int VT_COLOR_QUALITY_27 = 10; // 27 rgb-3x3x3

  // c16777216 16777216
  // c32768 32768
  // c4096 4096
  // c512 512
  // c216 216
  // c125 125
  // c64 64
  // c32 32
  // c27 27
  // c16 16
  // c8 8

  private static final int RGB888_RED_MASK = 0x00FF0000;
  private static final int RGB888_GREEN_MASK = 0x0000FF00;
  private static final int RGB888_BLUE_MASK = 0x000000FF;

  private static final int RGB555_RED_MASK = 0x00F80000;
  private static final int RGB555_GREEN_MASK = 0x0000F800;
  private static final int RGB555_BLUE_MASK = 0x000000F8;
  
  private static final int RGB444_RED_MASK = 0x00F00000;
  private static final int RGB444_GREEN_MASK = 0x0000F000;
  private static final int RGB444_BLUE_MASK = 0x000000F0;
  
  private static final int RGB333_RED_MASK = 0x00E00000;
  private static final int RGB333_GREEN_MASK = 0x0000E000;
  private static final int RGB333_BLUE_MASK = 0x000000E0;

  //private static final int RGB555_444_RED_MASK = 0x00f00000;
  //private static final int RGB555_444_GREEN_MASK = 0x0000f000;
  //private static final int RGB555_444_BLUE_MASK = 0x000000f0;

  //private static final int RGB555_444_RED_ADD = RGB555_RED_MASK - RGB555_444_RED_MASK;
  //private static final int RGB555_444_GREEN_ADD = RGB555_GREEN_MASK - RGB555_444_GREEN_MASK;
  //private static final int RGB555_444_BLUE_ADD = RGB555_BLUE_MASK - RGB555_444_BLUE_MASK;
  
  //private static final int RGB555_333_RED_MASK = 0x00e00000;
  //private static final int RGB555_333_GREEN_MASK = 0x0000e000;
  //private static final int RGB555_333_BLUE_MASK = 0x000000e0;

  //private static final int RGB555_333_RED_ADD = RGB555_RED_MASK - RGB555_333_RED_MASK;
  //private static final int RGB555_333_GREEN_ADD = RGB555_GREEN_MASK - RGB555_333_GREEN_MASK;
  //private static final int RGB555_333_BLUE_ADD = RGB555_BLUE_MASK - RGB555_333_BLUE_MASK;

  private static final int RGB222_RED_MASK = 0x00C00000;
  private static final int RGB222_GREEN_MASK = 0x0000C000;
  private static final int RGB222_BLUE_MASK = 0x000000C0;
  
  private static final int RGB111_RED_MASK = 0x00800000;
  private static final int RGB111_GREEN_MASK = 0x00008000;
  private static final int RGB111_BLUE_MASK = 0x00000080;

  private static final int RGB888_XOR_MASK = 0x00FFFFFF;
  //private static final short RGB555_333_XOR_MASK = 0x0000739C;
  //private static final short RGB555_444_XOR_MASK = 0x00007BDE;
  //private static final short RGB555_XOR_MASK = 0x00007FFF;
  //private static final short RGB444_XOR_MASK = 0x00000FFF;
  //private static final short RGB333_XOR_MASK = 0x000001FF;
  // private static final short RGB888_555_XOR_MASK = 0x00007FFF;

  private volatile int colorQuality;
  private volatile boolean initialized16ScreenCapture;
  private volatile boolean initialized32ScreenCapture;
  private volatile boolean initialized512ScreenCapture;
  private volatile boolean initialized4096ScreenCapture;
  private volatile boolean initialized8ScreenCapture;
  private volatile boolean initialized64ScreenCapture;
  private volatile boolean initialized216ScreenCapture;
  private volatile boolean initialized32768ScreenCapture;
  private volatile boolean initialized16777216ScreenCapture;
  private volatile boolean initialized125ScreenCapture;
  private volatile boolean initialized27ScreenCapture;
  private volatile int scaledWidth = 0;
  private volatile int scaledHeight = 0;
  private volatile double scaleFactorX = 1;
  private volatile double scaleFactorY = 1;
  private volatile boolean keepRatio = false;
  private volatile boolean forceScaleFactors = false;
  private int i;
  private int red, green, blue;
  private int screenCurrentWidth, screenCurrentHeight;
  private int screenCurrentX, screenCurrentY;
  private int scaledCurrentWidth, scaledCurrentHeight;
  // private int[] pixelBlock = new int[64 * 64];
  private int[] sectionPixelBufferInt;
  private int[] pixelBufferInt;
  private byte[] pixelBufferByte;
  private short[] pixelBufferShort;
  private volatile Rectangle currentDeviceBounds;
//	private volatile BufferedImage sectionImage;
  private volatile BufferedImage sectionCurrentImage;
  private volatile BufferedImage screenCurrentImage;
  private volatile BufferedImage scaledCurrentImage;
  private volatile Graphics2D scaledCurrentGraphics;
  // private volatile Graphics2D sectionGraphics;
  private volatile GraphicsDevice graphicsDevice;
  private volatile Robot standardCaptureRobot;
  private volatile VTDirectRobot directCaptureRobot;
  // private Toolkit toolkit;
  private VTARGBPixelGrabber pixelGrabber;
  private DataBuffer recyclableSectionDataBuffer;
  private DataBuffer recyclableScreenDataBuffer;
  private DataBuffer recyclableScaledDataBuffer;
//	private DataBuffer recyclableSectionDataBuffer;
  private int drawnCursorSize = 32;
  private int initialDrawnCursorSize;

  public int getColorCount()
  {
    switch (colorQuality)
    {
      case VT_COLOR_QUALITY_8:
        return 8;
      case VT_COLOR_QUALITY_16:
        return 16;
      case VT_COLOR_QUALITY_27:
        return 27;
      case VT_COLOR_QUALITY_32:
        return 32;
      case VT_COLOR_QUALITY_64:
        return 64;
      case VT_COLOR_QUALITY_125:
        return 125;
      case VT_COLOR_QUALITY_216:
        return 216;
      case VT_COLOR_QUALITY_512:
        return 512;
      case VT_COLOR_QUALITY_4096:
        return 4096;
      case VT_COLOR_QUALITY_32768:
        return 32768;
      case VT_COLOR_QUALITY_16777216:
        return 16777216;
      default:
        return -1;
    }
  }

  private final int roundUp(int numToRound, int multiple)
  {
    if (multiple == 0)
    {
      return numToRound;
    }
    int remainder = numToRound % multiple;
    if (remainder == 0)
    {
      return numToRound;
    }
    return numToRound + multiple - remainder;
  }

  public final void increaseDrawnCursorSize()
  {
    drawnCursorSize += 8;
  }

  public final void decreaseDrawnCursorSize()
  {
    drawnCursorSize -= 8;
    if (drawnCursorSize < 24)
    {
      drawnCursorSize = 24;
    }
  }

  public final void normalizeDrawnCursorSize()
  {
    drawnCursorSize = initialDrawnCursorSize;
  }

  public final void setScaledDimensions(int scaledWidth, int scaledHeight, boolean keepRatio)
  {
    if (scaledWidth <= 0 && scaledHeight <= 0)
    {
      this.forceScaleFactors = false;
      this.scaleFactorX = 1;
      this.scaleFactorY = 1;
      this.scaledWidth = 0;
      this.scaledHeight = 0;
    }
    else
    {
      this.forceScaleFactors = false;
      this.keepRatio = keepRatio;
      this.scaledWidth = scaledWidth;
      this.scaledHeight = scaledHeight;
    }
    calculateScaledDimensions();
  }

  public final void setScaleFactors(double scaleFactorX, double scaleFactorY)
  {
    if (scaleFactorX == 1 && scaleFactorY == 1)
    {
      this.forceScaleFactors = false;
      this.scaleFactorX = 1;
      this.scaleFactorY = 1;
      this.scaledWidth = 0;
      this.scaledHeight = 0;
    }
    else
    {
      this.forceScaleFactors = true;
      this.keepRatio = false;
      this.scaleFactorX = scaleFactorX;
      this.scaleFactorY = scaleFactorY;
    }
    calculateScaledDimensions();
  }

  private final void calculateScaledDimensions()
  {
    if (isScaling())
    {
      Dimension screenSize = getCurrentScreenSize();
      if (keepRatio)
      {
        double adjustedScaleFactorX = (scaledWidth / screenSize.getWidth());
        double adjustedScaleFactorY = (scaledHeight / screenSize.getHeight());
        if (adjustedScaleFactorX < adjustedScaleFactorY)
        {
          adjustedScaleFactorY = adjustedScaleFactorX;
        }
        else
        {
          adjustedScaleFactorX = adjustedScaleFactorY;
        }
        scaleFactorX = adjustedScaleFactorX;
        scaleFactorY = adjustedScaleFactorY;
        scaledWidth = (int) Math.round(screenSize.getWidth() * adjustedScaleFactorX);
        scaledHeight = (int) Math.round(screenSize.getHeight() * adjustedScaleFactorY);
      }
      else
      {
        if (forceScaleFactors())
        {
          scaledWidth = (int) Math.round(screenSize.getWidth() * scaleFactorX);
          scaledHeight = (int) Math.round(screenSize.getHeight() * scaleFactorY);
        }
        else
        {
          scaleFactorX = (scaledWidth / screenSize.getWidth());
          scaleFactorY = (scaledHeight / screenSize.getHeight());
        }
      }
    }
  }

  public final boolean isScaling()
  {
    return (scaledWidth > 0 && scaledHeight > 0) || forceScaleFactors();
  }

  public final boolean forceScaleFactors()
  {
    return forceScaleFactors;
  }

  public final double getScaleFactorX()
  {
    return scaleFactorX;
  }

  public final double getScaleFactorY()
  {
    return scaleFactorY;
  }

  public VTAWTScreenCaptureProvider()
  {
    if (GraphicsEnvironment.isHeadless())
    {
      return;
    }
    this.pixelGrabber = new VTARGBPixelGrabber();
    // toolkit = Toolkit.getDefaultToolkit();
//    try
//    {
//      this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//    }
//    catch (Throwable e)
//    {
//      
//    }
    // int dpi = toolkit.getScreenResolution();
    VTGlobalTextStyleManager.checkScaling();
    int dpi = VTGlobalTextStyleManager.BASE_FONT_DPI;
    drawnCursorSize = Math.max(32, dpi / 3);
    initialDrawnCursorSize = roundUp(drawnCursorSize, 8);
    /*
     * try { c16777216CursorSize =
     * Toolkit.getDefaultToolkit().get16777216CursorSize(32, 32); } catch (Throwable
     * e) { }
     */
    // System.out.println("c16777216CursorSize: x: " + c16777216CursorSize.width +
    // ",
    // y: " +
    // c16777216CursorSize.height);
    // System.out.println("center: x: " + center.x + ", y: " + center.y);
    // System.out.println("limit: " + limit);
    // this.toolkit = Toolkit.getDefaultToolkit();
  }

  public final void resetGraphicsDevice()
  {
    if (GraphicsEnvironment.isHeadless())
    {
      return;
    }
    try
    {
      setGraphicsDevice(GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice());
    }
    catch (Throwable e)
    {
      
    }
  }

  public final void setGraphicsDevice(GraphicsDevice graphicsDevice)
  {
    if (this.graphicsDevice != null)
    {
      if (graphicsDevice != null)
      {
        if (!this.graphicsDevice.getIDstring().equals(graphicsDevice.getIDstring()))
        {
          this.graphicsDevice = graphicsDevice;
          this.standardCaptureRobot = null;
          this.directCaptureRobot = null;
          initializeScreenCapture();
        }
      }
      else
      {
        this.graphicsDevice = graphicsDevice;
        this.standardCaptureRobot = null;
        this.directCaptureRobot = null;
        initializeScreenCapture();
      }
    }
    else
    {
      if (this.graphicsDevice != graphicsDevice)
      {
        this.graphicsDevice = graphicsDevice;
        this.standardCaptureRobot = null;
        this.directCaptureRobot = null;
        initializeScreenCapture();
      }
    }
  }

  public final int getColorQuality()
  {
    return this.colorQuality;
  }

  public final synchronized void setColorQuality(int colorQuality)
  {
    this.colorQuality = colorQuality;
  }

  private final Dimension getCurrentScreenSize()
  {
    try
    {
      Rectangle deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
      return new Dimension(deviceBounds.width, deviceBounds.height);
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
      // e.printStackTrace();
    }
    return null;
    // return toolkit.getScreenSize();
  }

  public final Dimension getCurrentScaledSize()
  {
    if (isScaling())
    {
      calculateScaledDimensions();
      return new Dimension(scaledWidth, scaledHeight);
    }
    else
    {
      return getCurrentScreenSize();
    }
  }
  
  private final boolean initializeCaptureRobot(GraphicsDevice device)
  {
    if (standardCaptureRobot == null)
    {
      if (device != null)
      {
        try
        {
          standardCaptureRobot = new Robot(device);
        }
        catch (Throwable t)
        {
          
        }
        try
        {
          directCaptureRobot = new VTDirectRobot(device);
        }
        catch (Throwable t)
        {

        }
      }
      else
      {
        GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
        GraphicsDevice topleft = null;
        for (GraphicsDevice screen : devices)
        {
          if (topleft != null)
          {
            Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
            Rectangle topleftBounds = topleft.getDefaultConfiguration().getBounds();
            if (screenBounds.x < topleftBounds.x || screenBounds.y < topleftBounds.y)
            {
              topleft = screen;
            }
          }
          else
          {
            topleft = screen;
          }
        }
        if (topleft != null)
        {
          try
          {
            standardCaptureRobot = new Robot(topleft);
          }
          catch (Throwable t)
          {
            
          }
          try
          {
            directCaptureRobot = new VTDirectRobot(topleft);
          }
          catch (Throwable t)
          {

          }
        }
        else
        {
          try
          {
            standardCaptureRobot = new Robot();
          }
          catch (Throwable t)
          {

          }
          try
          {
            directCaptureRobot = new VTDirectRobot();
          }
          catch (Throwable t)
          {

          }
        }
      }
      try
      {
        standardCaptureRobot.setAutoDelay(0);
        standardCaptureRobot.setAutoWaitForIdle(false);
      }
      catch (Throwable t)
      {
        
      }
    }
    return directCaptureRobot != null
      || standardCaptureRobot != null;
  }

  private final boolean initialize16ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh16ScreenCapture();
        }
        initialized16ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized16ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize32ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh32ScreenCapture();
        }
        initialized32ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized32ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize8ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh8ScreenCapture();
        }
        initialized8ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized8ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize512ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh512ScreenCapture();
        }
        initialized512ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized512ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize4096ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh4096ScreenCapture();
        }
        initialized4096ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized4096ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize64ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh64ScreenCapture();
        }
        initialized64ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized64ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize216ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh216ScreenCapture();
        }
        initialized216ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized216ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize32768ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh32768ScreenCapture();
        }
        initialized32768ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized32768ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize16777216ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh16777216ScreenCapture();
        }
        initialized16777216ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized16777216ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize125ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh125ScreenCapture();
        }
        initialized125ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized125ScreenCapture = false;
    }
    return false;
  }

  private final boolean initialize27ScreenCapture(GraphicsDevice device)
  {
    reset();
    if (GraphicsEnvironment.isHeadless())
    {
      return false;
    }
    try
    {
      if (initializeCaptureRobot(device))
      {
        if (changedCurrentScreenCapture())
        {
          refresh27ScreenCapture();
        }
        initialized27ScreenCapture = true;
        return true;
      }
    }
    catch (Throwable e)
    {
      initialized27ScreenCapture = false;
    }
    return false;
  }

  public final synchronized boolean initializeScreenCapture()
  {
    if (colorQuality == VT_COLOR_QUALITY_16777216)
    {
      return initialize16777216ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32768)
    {
      return initialize32768ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_216)
    {
      return initialize216ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_16)
    {
      return initialize16ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32)
    {
      return initialize32ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return initialize512ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return initialize4096ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_8)
    {
      return initialize8ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_125)
    {
      return initialize125ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_27)
    {
      return initialize27ScreenCapture(graphicsDevice);
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return initialize64ScreenCapture(graphicsDevice);
    }
    return false;
  }

  public final synchronized boolean initializeScreenCapture(GraphicsDevice device)
  {
    if (colorQuality == VT_COLOR_QUALITY_16777216)
    {
      return initialize16777216ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32768)
    {
      return initialize32768ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_216)
    {
      return initialize216ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_16)
    {
      return initialize16ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32)
    {
      return initialize32ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return initialize512ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return initialize4096ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_8)
    {
      return initialize8ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_125)
    {
      return initialize125ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_27)
    {
      return initialize27ScreenCapture(device);
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return initialize64ScreenCapture(device);
    }
    return false;
  }

  private final boolean is27ScreenCaptureInitialized()
  {
    return initialized27ScreenCapture;
  }

  private final boolean is32ScreenCaptureInitialized()
  {
    return initialized32ScreenCapture;
  }

  private final boolean is512ScreenCaptureInitialized()
  {
    return initialized512ScreenCapture;
  }

  private final boolean is4096ScreenCaptureInitialized()
  {
    return initialized4096ScreenCapture;
  }

  private final boolean is8ScreenCaptureInitialized()
  {
    return initialized8ScreenCapture;
  }

  private final boolean is16ScreenCaptureInitialized()
  {
    return initialized16ScreenCapture;
  }

  private final boolean is64ScreenCaptureInitialized()
  {
    return initialized64ScreenCapture;
  }

  private final boolean is125ScreenCaptureInitialized()
  {
    return initialized125ScreenCapture;
  }

  private final boolean is216ScreenCaptureInitialized()
  {
    return initialized216ScreenCapture;
  }

  private final boolean is32768ScreenCaptureInitialized()
  {
    return initialized32768ScreenCapture;
  }

  private final boolean is16777216ScreenCaptureInitialized()
  {
    return initialized16777216ScreenCapture;
  }

  public final synchronized boolean isScreenCaptureInitialized()
  {
    if (colorQuality == VT_COLOR_QUALITY_16777216)
    {
      return is16777216ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_32768)
    {
      return is32768ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_216)
    {
      return is216ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_16)
    {
      return is16ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_32)
    {
      return is32ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return is512ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return is4096ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_8)
    {
      return is8ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_125)
    {
      return is125ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_27)
    {
      return is27ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return is64ScreenCaptureInitialized();
    }
    return false;
  }

  public final synchronized void reset()
  {
    disposeScreenCaptureResources();
    /*
     * try { this.graphicsDevice =
     * GraphicsEnvironment.getLocalGraphicsEnvironment(). getDefaultScreenDevice();
     * } catch (Throwable e) { //e.printStackTrace(VTTerminal.getSystemOut()); }
     */
    // this.standardCaptureRobot = null;
    System.runFinalization();
    System.gc();
  }

  public final synchronized void dispose()
  {
    keepRatio = false;
    forceScaleFactors = false;
    scaledWidth = 0;
    scaledHeight = 0;
    scaleFactorX = 1;
    scaleFactorY = 1;
    disposeScreenCaptureResources();
    sectionPixelBufferInt = null;
    standardCaptureRobot = null;
    if (directCaptureRobot != null)
    {
      directCaptureRobot.dispose();
    }
    directCaptureRobot = null;
    recyclableScreenDataBuffer = null;
    recyclableScaledDataBuffer = null;
    recyclableSectionDataBuffer = null;
    // recyclableSectionDataBuffer = null;
    // recyclableDataBufferInt = null;
    // recyclableScaledDataBufferByte = null;
    // recyclableScaledDataBufferUShort = null;
    // recyclableScaledDataBufferInt = null;
//    if (!GraphicsEnvironment.isHeadless())
//    {
//      try
//      {
//        this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
//      }
//      catch (Throwable e)
//      {
//        
//      }
//    }
    System.runFinalization();
    System.gc();
    // toolkit = null;
  }

  private final void disposeScreenCaptureResources()
  {
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    if (scaledCurrentImage != null)
    {
      scaledCurrentImage.flush();
      scaledCurrentImage = null;
    }
    if (scaledCurrentGraphics != null)
    {
      scaledCurrentGraphics.dispose();
      scaledCurrentGraphics = null;
    }
    if (sectionCurrentImage != null)
    {
      sectionCurrentImage.flush();
      sectionCurrentImage = null;
    }
//		if (sectionImage != null)
//		{
//			sectionImage.flush();
//			sectionImage = null;
//		}
//		if (sectionGraphics != null)
//		{
//			sectionGraphics.dispose();
//			sectionGraphics = null;
//		}
    // c64QualityColorModel = null;
    // colorConversionPixelBufferInt = null;
    // sectionPixelBufferInt = null;
    // sectionPixelBufferByte = null;
    pixelBufferByte = null;
    pixelBufferShort = null;
    pixelBufferInt = null;
    screenCurrentWidth = 0;
    screenCurrentHeight = 0;
    scaledCurrentWidth = 0;
    scaledCurrentHeight = 0;
    initialized16ScreenCapture = false;
    initialized32ScreenCapture = false;
    initialized512ScreenCapture = false;
    initialized4096ScreenCapture = false;
    initialized8ScreenCapture = false;
    initialized64ScreenCapture = false;
    initialized216ScreenCapture = false;
    initialized32768ScreenCapture = false;
    initialized16777216ScreenCapture = false;
    initialized125ScreenCapture = false;
    initialized27ScreenCapture = false;
  }

  public final void clearResources()
  {
    initialized16ScreenCapture = false;
    initialized32ScreenCapture = false;
    initialized512ScreenCapture = false;
    initialized4096ScreenCapture = false;
    initialized8ScreenCapture = false;
    initialized64ScreenCapture = false;
    initialized216ScreenCapture = false;
    initialized32768ScreenCapture = false;
    initialized16777216ScreenCapture = false;
    initialized125ScreenCapture = false;
    initialized27ScreenCapture = false;
  }

  private final boolean changedCurrentScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return true;
    }
    return (currentDeviceBounds.width != screenCurrentWidth || currentDeviceBounds.height != screenCurrentHeight || currentDeviceBounds.x != screenCurrentX || currentDeviceBounds.y != screenCurrentY) || changedScaledCurrentScreenCapture();
  }

  private final boolean changedScaledCurrentScreenCapture()
  {
    calculateScaledDimensions();
    return scaledWidth != scaledCurrentWidth || scaledHeight != scaledCurrentHeight;
  }

  private final void refresh27ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 27, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled27ScreenCapture();
  }

  private final void refreshScaled27ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 27, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh16ScreenCapture()
  {
    // currentScreenSize = toolkit.getScreenSize();
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 16, recyclableScreenDataBuffer);
    // screenCurrentImage = VTImageIO.newImage(screenCurrentWidth,
    // screenCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16,
    // recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled16ScreenCapture();
  }

  private final void refreshScaled16ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 16, recyclableScaledDataBuffer);
      // scaledCurrentImage = VTImageIO.newImage(scaledCurrentWidth,
      // scaledCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16,
      // recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh32ScreenCapture()
  {
    // currentScreenSize = toolkit.getScreenSize();
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 32, recyclableScreenDataBuffer);
    // screenCurrentImage = VTImageIO.newImage(screenCurrentWidth,
    // screenCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16,
    // recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled32ScreenCapture();
  }

  private final void refreshScaled32ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 32, recyclableScaledDataBuffer);
      // scaledCurrentImage = VTImageIO.newImage(scaledCurrentWidth,
      // scaledCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16,
      // recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh8ScreenCapture()
  {
    // currentScreenSize = toolkit.getScreenSize();
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 8, recyclableScreenDataBuffer);
    // screenCurrentImage = VTImageIO.newImage(screenCurrentWidth,
    // screenCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16,
    // recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled8ScreenCapture();
  }

  private final void refreshScaled8ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 8, recyclableScaledDataBuffer);
      // scaledCurrentImage = VTImageIO.newImage(scaledCurrentWidth,
      // scaledCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16,
      // recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh64ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 64, recyclableScreenDataBuffer);
    //screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 64, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled64ScreenCapture();
  }

  private final void refreshScaled64ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 64, recyclableScaledDataBuffer);
      //scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 64, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh125ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 125, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled125ScreenCapture();
  }

  private final void refreshScaled125ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 125, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh216ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 216, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled216ScreenCapture();
  }

  private final void refreshScaled216ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 216, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh512ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 512, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled512ScreenCapture();
  }

  private final void refreshScaled512ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 512, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh4096ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 4096, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScale4096ScreenCapture();
  }

  private final void refreshScale4096ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 4096, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh32768ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 32768, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled32768ScreenCapture();
  }

  private final void refreshScaled32768ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 32768, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final void refresh16777216ScreenCapture()
  {
    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
    if (currentDeviceBounds == null)
    {
      return;
    }
    screenCurrentWidth = currentDeviceBounds.width;
    screenCurrentHeight = currentDeviceBounds.height;
    screenCurrentX = currentDeviceBounds.x;
    screenCurrentY = currentDeviceBounds.y;
    if (screenCurrentImage != null)
    {
      screenCurrentImage.flush();
      screenCurrentImage = null;
    }
    screenCurrentImage = VTImageIO.createImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_INT_RGB, 16777216, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled16777216ScreenCapture();
  }

  private final void refreshScaled16777216ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        scaledCurrentImage.flush();
        scaledCurrentImage = null;
      }
      if (scaledCurrentGraphics != null)
      {
        scaledCurrentGraphics.dispose();
        scaledCurrentGraphics = null;
      }
      scaledCurrentImage = VTImageIO.createImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_INT_RGB, 16777216, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
//			if (scaledCurrentImage != null)
//			{
//				scaledCurrentImage.flush();
//				scaledCurrentImage = null;
//			}
//			if (scaledCurrentGraphics != null)
//			{
//				scaledCurrentGraphics.dispose();
//				scaledCurrentGraphics = null;
//			}
//			recyclableScaledDataBuffer = null;
    }
  }

  private final BufferedImage create32ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh32ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);

    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }
    // System.runFinalization();
    // System.gc();
    byte rgbiValue = 0;
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      // red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
      // green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
      // blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
      // pixelBufferByte[i] = (byte) (red + green + blue);
      rgbiValue = (byte) VTIndexedColorModel.get32ColorRGBIIValue(sectionPixelBufferInt[i]);

      pixelBufferByte[i] = rgbiValue;
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create32ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh32ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    byte rgbiValue = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      // red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
      // green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
      // blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
      // pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red +
      // green + blue);
      rgbiValue = (byte) VTIndexedColorModel.get32ColorRGBIIValue(sectionPixelBufferInt[i]);

      pixelBufferByte[startOffset + currentWidth + currentHeight] = rgbiValue;
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
      // while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x,
      // scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y +
      // scaledArea.height, captureArea.x, captureArea.y, captureArea.x +
      // captureArea.width, captureArea.y + captureArea.height, null))
      // {
      // Thread.yield();
      // }
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create8ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh8ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);

    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }
    // System.runFinalization();
    // System.gc();
    //byte rgbiValue = 0;
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      //rgbiValue = (byte) VTIndexedColorModel.get8ColorRGBValue(sectionPixelBufferInt[i]);

      //pixelBufferByte[i] = rgbiValue;
      
      red = ((sectionPixelBufferInt[i] & RGB111_RED_MASK) >> 21);
      green = ((sectionPixelBufferInt[i] & RGB111_GREEN_MASK) >> 14);
      blue = ((sectionPixelBufferInt[i] & RGB111_BLUE_MASK) >> 7);
      
      pixelBufferByte[i] = (byte) (red + green + blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create8ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh8ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    //byte rgbiValue = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }
      //rgbiValue = (byte) VTIndexedColorModel.get8ColorRGBValue(sectionPixelBufferInt[i]);
      
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = rgbiValue;
      
      red = ((sectionPixelBufferInt[i] & RGB111_RED_MASK) >> 21);
      green = ((sectionPixelBufferInt[i] & RGB111_GREEN_MASK) >> 14);
      blue = ((sectionPixelBufferInt[i] & RGB111_BLUE_MASK) >> 7);
      
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
      // while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x,
      // scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y +
      // scaledArea.height, captureArea.x, captureArea.y, captureArea.x +
      // captureArea.width, captureArea.y + captureArea.height, null))
      // {
      // Thread.yield();
      // }
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create16ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh16ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);

    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }
    // System.runFinalization();
    // System.gc();
    byte rgbiValue = 0;
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      // red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
      // green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
      // blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
      // pixelBufferByte[i] = (byte) (red + green + blue);
      rgbiValue = (byte) VTIndexedColorModel.get16ColorRGBIValue(sectionPixelBufferInt[i]);

      pixelBufferByte[i] = rgbiValue;
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create16ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh16ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    byte rgbiValue = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      // red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
      // green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
      // blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
      // pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red +
      // green + blue);
      rgbiValue = (byte) VTIndexedColorModel.get16ColorRGBIValue(sectionPixelBufferInt[i]);

      pixelBufferByte[startOffset + currentWidth + currentHeight] = rgbiValue;
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
      // while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x,
      // scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y +
      // scaledArea.height, captureArea.x, captureArea.y, captureArea.x +
      // captureArea.width, captureArea.y + captureArea.height, null))
      // {
      // Thread.yield();
      // }
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create216ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh216ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // colorConversionPixelBufferInt = new
    // VTARGBPixelGrabber(screenCapture).getPixels(colorConversionPixelBufferInt);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {

      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 6) >> 8) * 36);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 6) >> 8) * 6);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 6) >> 8));

      // red = (int)((Math.round((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16)
      // + 25) / 51d)) * 36);
      // green = (int)((Math.round((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >>
      // 8) + 25) / 51d)) * 6);
      // blue = (int)((Math.round((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) +
      // 25) / 51d)));
//			
      pixelBufferByte[i] = (byte) (red + green + blue);

      // pixelBufferByte[i] =
      // VTIndexedColorModel.get216Color6LevelRGBValue(sectionPixelBufferInt[i]);
    }
    // colorConversionPixelBufferInt = null;
    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0,
      // scaledCurrentWidth, scaledCurrentHeight, null))
      // {
      // Thread.yield();
      // }
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create216ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh216ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createDirectScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 6) >> 8) * 36);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 6) >> 8) * 6);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 6) >> 8));

      // red = (int)((Math.round((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16)
      // + 25) / 51d)) * 36);
      // green = (int)((Math.round((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >>
      // 8) + 25) / 51d)) * 6);
      // blue = (int)((Math.round((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) +
      // 25) / 51d)));

      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);

      // pixelBufferByte[startOffset + currentWidth + currentHeight] =
      // VTIndexedColorModel.get216Color6LevelRGBValue(sectionPixelBufferInt[i]);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create64ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh64ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      red = ((sectionPixelBufferInt[i] & RGB222_RED_MASK) >> 18);
      green = ((sectionPixelBufferInt[i] & RGB222_GREEN_MASK) >> 12);
      blue = ((sectionPixelBufferInt[i] & RGB222_BLUE_MASK) >> 6);

      pixelBufferByte[i] = (byte) (red + green + blue);
    }
    // colorConversionPixelBufferInt = null;
    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create64ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh64ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture =
    // standardCaptureRobot.createScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }
    // System.runFinalization();
    // System.gc();
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      red = ((sectionPixelBufferInt[i] & RGB222_RED_MASK) >> 18);
      green = ((sectionPixelBufferInt[i] & RGB222_GREEN_MASK) >> 12);
      blue = ((sectionPixelBufferInt[i] & RGB222_BLUE_MASK) >> 6);

      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create512ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh512ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      //red = (((sectionPixelBufferInt[i] & RGB555_333_RED_MASK) | RGB555_333_RED_ADD) >> 9);
      //green = (((sectionPixelBufferInt[i] & RGB555_333_GREEN_MASK) | RGB555_333_GREEN_ADD) >> 6);
      //blue = (((sectionPixelBufferInt[i] & RGB555_333_BLUE_MASK) | RGB555_333_BLUE_ADD) >> 3);
      
      red = ((sectionPixelBufferInt[i] & RGB333_RED_MASK) >> 15);
      green = ((sectionPixelBufferInt[i] & RGB333_GREEN_MASK) >> 10);
      blue = ((sectionPixelBufferInt[i] & RGB333_BLUE_MASK) >> 5);

      pixelBufferShort[i] = (short) (red | green | blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create512ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh512ScreenCapture();
    }
    // Rectangle trueArea = new Rectangle(0 + Math.min(area.x,
    // screenCurrentWidth),
    // 0 + Math.min(area.y, screenCurrentHeight), Math.min(area.width,
    // screenCurrentWidth - area.x), Math.min(area.height,
    // screenCurrentHeight -
    // area.y));
    // BufferedImage screenCapture =
    // standardCaptureRobot.createScreenCapture(trueArea);
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      //red = (((sectionPixelBufferInt[i] & RGB555_333_RED_MASK) | RGB555_333_RED_ADD) >> 9);
      //green = (((sectionPixelBufferInt[i] & RGB555_333_GREEN_MASK) | RGB555_333_GREEN_ADD) >> 6);
      //blue = (((sectionPixelBufferInt[i] & RGB555_333_BLUE_MASK) | RGB555_333_BLUE_ADD) >> 3);
      
      red = ((sectionPixelBufferInt[i] & RGB333_RED_MASK) >> 15);
      green = ((sectionPixelBufferInt[i] & RGB333_GREEN_MASK) >> 10);
      blue = ((sectionPixelBufferInt[i] & RGB333_BLUE_MASK) >> 5);

      pixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (red | green | blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create4096ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh4096ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      //red = (((sectionPixelBufferInt[i] & RGB555_444_RED_MASK) | RGB555_444_RED_ADD) >> 9);
      //green = (((sectionPixelBufferInt[i] & RGB555_444_GREEN_MASK) | RGB555_444_GREEN_ADD) >> 6);
      //blue = (((sectionPixelBufferInt[i] & RGB555_444_BLUE_MASK) | RGB555_444_BLUE_ADD) >> 3);

      red = ((sectionPixelBufferInt[i] & RGB444_RED_MASK) >> 12);
      green = ((sectionPixelBufferInt[i] & RGB444_GREEN_MASK) >> 8);
      blue = ((sectionPixelBufferInt[i] & RGB444_BLUE_MASK) >> 4);

      pixelBufferShort[i] = (short) (red | green | blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create4096ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh4096ScreenCapture();
    }
    // Rectangle trueArea = new Rectangle(0 + Math.min(area.x,
    // screenCurrentWidth),
    // 0 + Math.min(area.y, screenCurrentHeight), Math.min(area.width,
    // screenCurrentWidth - area.x), Math.min(area.height,
    // screenCurrentHeight -
    // area.y));
    // BufferedImage screenCapture =
    // standardCaptureRobot.createScreenCapture(trueArea);
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }
      //red = (((sectionPixelBufferInt[i] & RGB555_444_RED_MASK) | RGB555_444_RED_ADD) >> 9);
      //green = (((sectionPixelBufferInt[i] & RGB555_444_GREEN_MASK) | RGB555_444_GREEN_ADD) >> 6);
      //blue = (((sectionPixelBufferInt[i] & RGB555_444_BLUE_MASK) | RGB555_444_BLUE_ADD) >> 3);

      red = ((sectionPixelBufferInt[i] & RGB444_RED_MASK) >> 12);
      green = ((sectionPixelBufferInt[i] & RGB444_GREEN_MASK) >> 8);
      blue = ((sectionPixelBufferInt[i] & RGB444_BLUE_MASK) >> 4);

      pixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (red | green | blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create32768ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh32768ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {
      red = ((sectionPixelBufferInt[i] & RGB555_RED_MASK) >> 9);
      green = ((sectionPixelBufferInt[i] & RGB555_GREEN_MASK) >> 6);
      blue = ((sectionPixelBufferInt[i] & RGB555_BLUE_MASK) >> 3);

      pixelBufferShort[i] = (short) (red | green | blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create32768ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh32768ScreenCapture();
    }
    // Rectangle trueArea = new Rectangle(0 + Math.min(area.x,
    // screenCurrentWidth),
    // 0 + Math.min(area.y, screenCurrentHeight), Math.min(area.width,
    // screenCurrentWidth - area.x), Math.min(area.height,
    // screenCurrentHeight -
    // area.y));
    // BufferedImage screenCapture =
    // standardCaptureRobot.createScreenCapture(trueArea);
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }
      red = ((sectionPixelBufferInt[i] & RGB555_RED_MASK) >> 9);
      green = ((sectionPixelBufferInt[i] & RGB555_GREEN_MASK) >> 6);
      blue = ((sectionPixelBufferInt[i] & RGB555_BLUE_MASK) >> 3);

      pixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (red | green | blue);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create16777216ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh16777216ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture =
    // standardCaptureRobot.createScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    // clear alpha channel
    int length = screenCapture.getWidth() * screenCapture.getHeight();
    for (int i = 0; i < length; i++)
    {
      sectionPixelBufferInt[i] &= RGB888_XOR_MASK;
    }

    pixelBufferInt = ((DataBufferInt) screenCurrentImage.getRaster().getDataBuffer()).getData();
    System.arraycopy(sectionPixelBufferInt, 0, pixelBufferInt, 0, pixelDataLength);
    // VTImageDataUtils.copyArea(sectionPixelBufferInt, pixelBufferInt, 0,
    // captureArea.width, captureArea.height, captureArea);
    // screenCurrentImage.getRaster().setDataElements(captureArea.x, captureArea.y,
    // captureArea.width, captureArea.height, sectionPixelBufferInt);

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferInt = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create16777216ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh16777216ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    // clear alpha channel
    int length = screenCapture.getWidth() * screenCapture.getHeight();
    for (int i = 0; i < length; i++)
    {
      sectionPixelBufferInt[i] &= RGB888_XOR_MASK;
    }

    pixelBufferInt = ((DataBufferInt) screenCurrentImage.getRaster().getDataBuffer()).getData();
    // VTImageDataUtils.copyArea(sectionPixelBufferInt, pixelBufferInt, 0,
    // screenCurrentImage.getWidth(), screenCurrentImage.getHeight(), captureArea);
    // screenCurrentImage.getRaster().setDataElements(captureArea.x, captureArea.y,
    // captureArea.width, captureArea.height, sectionPixelBufferInt);
    int destinationOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int destinationIndex = destinationOffset;
    int sourceIndex = 0;
    
    for (int currentHeight = 0; currentHeight < captureArea.height; currentHeight++)
    {
      System.arraycopy(sectionPixelBufferInt, sourceIndex, pixelBufferInt, destinationIndex, captureArea.width);
      sourceIndex += captureArea.width;
      destinationIndex += screenCurrentImage.getWidth();
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferInt = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create125ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh125ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // colorConversionPixelBufferInt = new
    // VTARGBPixelGrabber(screenCapture).getPixels(colorConversionPixelBufferInt);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {

      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 5) >> 8) * 25);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 5) >> 8) * 5);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 5) >> 8));

      // red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 32) >> 6) *
      // 25);
      // green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 32) >> 6)
      // * 5);
      // blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 32) >> 6));

      pixelBufferByte[i] = (byte) (red + green + blue);

//			pixelBufferByte[i] = VTIndexedColorModel.get125Color5LevelRGBValue(sectionPixelBufferInt[i]);
    }
    // colorConversionPixelBufferInt = null;
    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointerFilterGray(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0,
      // scaledCurrentWidth, scaledCurrentHeight, null))
      // {
      // Thread.yield();
      // }
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create125ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh125ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createDirectScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 5) >> 8) * 25);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 5) >> 8) * 5);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 5) >> 8));

      // red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 32) >> 6) *
      // 25);
      // green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 32) >> 6)
      // * 5);
      // blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 32) >> 6));

      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);

      // pixelBufferByte[startOffset + currentWidth + currentHeight] =
      // VTIndexedColorModel.get125Color5LevelRGBValue(sectionPixelBufferInt[i]);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointerFilterGray(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create27ScreenCapture(boolean drawPointer)
  {
    if (changedCurrentScreenCapture())
    {
      refresh27ScreenCapture();
    }
    Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // colorConversionPixelBufferInt = new
    // VTARGBPixelGrabber(screenCapture).getPixels(colorConversionPixelBufferInt);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    for (i = 0; i < pixelDataLength; i++)
    {

      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 3) >> 8) * 9);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 3) >> 8) * 3);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 3) >> 8));

      // red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 64) >> 7) *
      // 9);
      // green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 64) >> 7)
      // * 3);
      // blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 64) >> 7));

      pixelBufferByte[i] = (byte) (red + green + blue);

      // pixelBufferByte[i] =
      // VTIndexedColorModel.get27Color3LevelRGBValue(sectionPixelBufferInt[i]);
    }
    // colorConversionPixelBufferInt = null;
    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointerFilterGray(screenCurrentImage);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0,
      // scaledCurrentWidth, scaledCurrentHeight, null))
      // {
      // Thread.yield();
      // }
      scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage create27ScreenCapture(boolean drawPointer, Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh27ScreenCapture();
    }
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.round(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.round(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.round(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.round(originalArea.height / getScaleFactorY());
    if (screenArea.width > screenCurrentWidth)
    {
      screenArea.width = screenCurrentWidth;
    }
    if (screenArea.height > screenCurrentHeight)
    {
      screenArea.height = screenCurrentHeight;
    }
    if (screenArea.x > screenCurrentWidth - screenArea.width)
    {
      screenArea.x = screenCurrentWidth - screenArea.width;
    }
    if (screenArea.y > screenCurrentHeight - screenArea.height)
    {
      screenArea.y = screenCurrentHeight - screenArea.height;
    }
    Rectangle captureArea = new Rectangle(Math.min(screenArea.x, screenCurrentWidth), Math.min(screenArea.y, screenCurrentHeight), Math.min(screenArea.width, screenCurrentWidth - screenArea.x), Math.min(screenArea.height, screenCurrentHeight - screenArea.y));
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createRobotCapture(captureArea);
    // BufferedImage screenCapture = createDirectScreenCapture(captureArea);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB || screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
    {
      sectionPixelBufferInt = ((DataBufferInt) screenCapture.getRaster().getDataBuffer()).getData();
    }
    else
    {
      pixelGrabber.setImage(screenCapture);
      if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
      {
        sectionPixelBufferInt = pixelGrabber.getPixels(sectionPixelBufferInt);
      }
      else
      {
        sectionPixelBufferInt = pixelGrabber.getPixels();
      }
      pixelGrabber.dispose();
    }

    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
    int startOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth();
      }

      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 3) >> 8) * 9);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 3) >> 8) * 3);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 3) >> 8));

      // red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 64) >> 7) *
      // 9);
      // green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 64) >> 7)
      // * 3);
      // blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 64) >> 7));

      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);

      // pixelBufferByte[startOffset + currentWidth + currentHeight] =
      // VTIndexedColorModel.get27Color3LevelRGBValue(sectionPixelBufferInt[i]);
    }

    if (sectionCurrentImage != screenCapture)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;

    if (drawPointer)
    {
      drawPointerFilterGray(screenCurrentImage, captureArea);
    }

    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      // Rectangle scaledArea = new Rectangle(Math.min(originalArea.x,
      // scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight),
      // Math.min(originalArea.width, scaledCurrentWidth - originalArea.x),
      // Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
      Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
      scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
      scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
      scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
      scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
//			while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
//			{
//				Thread.yield();
//			}
      scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }

  private final BufferedImage createRobotCapture(Rectangle captureArea)
  {
    BufferedImage screenCapture;
    if (directCaptureRobot != null && directCaptureRobot.getDirectRGBPixelsMethodAvailable())
    {
      if (sectionCurrentImage == null || sectionCurrentImage.getWidth() != captureArea.width || sectionCurrentImage.getHeight() != captureArea.height)
      {
        if (sectionCurrentImage != null)
        {
          sectionCurrentImage.flush();
        }
        sectionCurrentImage = VTImageIO.createImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_INT_RGB, 0, recyclableSectionDataBuffer);
        recyclableSectionDataBuffer = sectionCurrentImage.getRaster().getDataBuffer();
      }
      if (directCaptureRobot.getRGBPixels(captureArea.x, captureArea.y, captureArea.width, captureArea.height, ((DataBufferInt) recyclableSectionDataBuffer).getData()))
      {
        screenCapture = sectionCurrentImage;
      }
      else
      {
        screenCapture = standardCaptureRobot.createScreenCapture(captureArea);
      }
    }
    else
    {
      screenCapture = standardCaptureRobot.createScreenCapture(captureArea);
    }
    return screenCapture;
  }

  private final void drawPointer(BufferedImage image)
  {
    drawPointer(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
  }

  private final void drawPointer(BufferedImage image, Rectangle area)
  {
    PointerInfo info = MouseInfo.getPointerInfo();
    if (info == null)
    {
      // VTTerminal.println("info = null");
      return;
    }
    GraphicsDevice infoDevice = info.getDevice();
    // DisplayMode displayMode = infoDevice.getDisplayMode();
    Point pointerLocation = info.getLocation();
    Rectangle deviceBounds = new Rectangle();
    if (infoDevice == null)
    {
      // VTTerminal.println("infoDevice = null");
      return;
    }
    else
    {
      try
      {
        if (graphicsDevice != null)
        {
          if (!infoDevice.getIDstring().equals(graphicsDevice.getIDstring()))
          {
            // out of current screen
            return;
          }
          deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(infoDevice);
          pointerLocation.translate(deviceBounds.x * -1, deviceBounds.y * -1);
        }
        else
        {
          deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(null);
          pointerLocation.translate(deviceBounds.x * -1, deviceBounds.y * -1);
        }
      }
      catch (Throwable e)
      {
        // return;
      }
    }
    int x, y, m, n, o;
    // int dpi = toolkit.getScreenResolution();
    // int dpiCursorSize = Math.max(32, dpi / 3);
    // int displayDimensionCursorSize = Math.min(displayMode.getWidth(),
    // displayMode.getHeight()) / 25;
    o = drawnCursorSize;
    // o = Math.min(displayMode.getWidth(), displayMode.getHeight()) / 25;
    // o = Math.min(26, displayMode.getHeight()) / 32;
    // o = 45;
    try
    {
      // Center area
      x = pointerLocation.x;
      y = pointerLocation.y;
      
      if (area.contains(x + 2, y))
      {
        image.setRGB(x + 2, y, (image.getRGB(x + 2, y) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 3, y))
      {
        image.setRGB(x + 3, y, (image.getRGB(x + 3, y) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 4, y))
      {
        image.setRGB(x + 4, y, (image.getRGB(x + 4, y) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 2, y))
      {
        image.setRGB(x - 2, y, (image.getRGB(x - 2, y) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 3, y))
      {
        image.setRGB(x - 3, y, (image.getRGB(x - 3, y) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 4, y))
      {
        image.setRGB(x - 4, y, (image.getRGB(x - 4, y) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y + 2))
      {
        image.setRGB(x, y + 2, (image.getRGB(x, y + 2) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y + 3))
      {
        image.setRGB(x, y + 3, (image.getRGB(x, y + 3) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y + 4))
      {
        image.setRGB(x, y + 4, (image.getRGB(x, y + 4) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y - 2))
      {
        image.setRGB(x, y - 2, (image.getRGB(x, y - 2) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y - 3))
      {
        image.setRGB(x, y - 3, (image.getRGB(x, y - 3) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y - 4))
      {
        image.setRGB(x, y - 4, (image.getRGB(x, y - 4) ^ RGB888_XOR_MASK));
      }
      
      if (area.contains(x + 2, y + 1))
      {
        image.setRGB(x + 2, y + 1, (image.getRGB(x + 2, y + 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 3, y + 1))
      {
        image.setRGB(x + 3, y + 1, (image.getRGB(x + 3, y + 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 4, y + 1))
      {
        image.setRGB(x + 4, y + 1, (image.getRGB(x + 4, y + 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 2, y - 1))
      {
        image.setRGB(x + 2, y - 1, (image.getRGB(x + 2, y - 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 3, y - 1))
      {
        image.setRGB(x + 3, y - 1, (image.getRGB(x + 3, y - 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 4, y - 1))
      {
        image.setRGB(x + 4, y - 1, (image.getRGB(x + 4, y - 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 2, y + 1))
      {
        image.setRGB(x - 2, y + 1, (image.getRGB(x - 2, y + 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 3, y + 1))
      {
        image.setRGB(x - 3, y + 1, (image.getRGB(x - 3, y + 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 4, y + 1))
      {
        image.setRGB(x - 4, y + 1, (image.getRGB(x - 4, y + 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 2, y - 1))
      {
        image.setRGB(x - 2, y - 1, (image.getRGB(x - 2, y - 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 3, y - 1))
      {
        image.setRGB(x - 3, y - 1, (image.getRGB(x - 3, y - 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 4, y - 1))
      {
        image.setRGB(x - 4, y - 1, (image.getRGB(x - 4, y - 1) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y + 2))
      {
        image.setRGB(x + 1, y + 2, (image.getRGB(x + 1, y + 2) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y + 3))
      {
        image.setRGB(x + 1, y + 3, (image.getRGB(x + 1, y + 3) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y + 4))
      {
        image.setRGB(x + 1, y + 4, (image.getRGB(x + 1, y + 4) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y - 2))
      {
        image.setRGB(x + 1, y - 2, (image.getRGB(x + 1, y - 2) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y - 3))
      {
        image.setRGB(x + 1, y - 3, (image.getRGB(x + 1, y - 3) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y - 4))
      {
        image.setRGB(x + 1, y - 4, (image.getRGB(x + 1, y - 4) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y + 2))
      {
        image.setRGB(x - 1, y + 2, (image.getRGB(x - 1, y + 2) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y + 3))
      {
        image.setRGB(x - 1, y + 3, (image.getRGB(x - 1, y + 3) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y + 4))
      {
        image.setRGB(x - 1, y + 4, (image.getRGB(x - 1, y + 4) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y - 2))
      {
        image.setRGB(x - 1, y - 2, (image.getRGB(x - 1, y - 2) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y - 3))
      {
        image.setRGB(x - 1, y - 3, (image.getRGB(x - 1, y - 3) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y - 4))
      {
        image.setRGB(x - 1, y - 4, (image.getRGB(x - 1, y - 4) ^ RGB888_XOR_MASK));
      }

      if (o > 24)
      {
        n = (o / 2);
      }
      else
      {
        n = 12;
      }

      // First quadrant
      x = pointerLocation.x - 2;
      y = pointerLocation.y + 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
      }
      // Second quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y + 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
      }
      // Third quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y - 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
      }
      // Fourth quadrant
      x = pointerLocation.x - 2;
      y = pointerLocation.y - 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
      }

      if (o > 24)
      {
        n = (o / 2) - 1;
      }
      else
      {
        n = 11;
        // n = 10;
        // n = 13;
      }

      // First quadrant
      x = pointerLocation.x - 3;
      y = pointerLocation.y + 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
      }
      // Second quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y + 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
      }
      // Third quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y - 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
      }
      // Fourth quadrant
      x = pointerLocation.x - 3;
      y = pointerLocation.y - 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
      }

      // n = n / 2;
      n = n - 1;
      // First quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y + 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
      }
      
      // Second quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y + 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
      }
      
      // Third quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y - 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
      }
      
      // Fourth quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y - 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
      }
      
      // First quadrant
      x = pointerLocation.x - (3 + n);
      y = pointerLocation.y + (3 + n);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x + m, y - m))
        {
          image.setRGB(x + m, y - m, (image.getRGB(x + m, y - m) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + 1, y - m))
        {
          image.setRGB(x + 1, y - m, (image.getRGB(x + 1, y - m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y - 1))
        {
          image.setRGB(x + m, y - 1, (image.getRGB(x + m, y - 1) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x + 2, y - m))
        {
          image.setRGB(x + 2, y - m, (image.getRGB(x + 2, y - m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y - 2))
        {
          image.setRGB(x + m, y - 2, (image.getRGB(x + m, y - 2) ^ RGB888_XOR_MASK));
        }
      }

      // Second quadrant
      x = pointerLocation.x + (3 + n);
      y = pointerLocation.y + (3 + n);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x - m, y - m))
        {
          image.setRGB(x - m, y - m, (image.getRGB(x - m, y - m) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - 1, y - m))
        {
          image.setRGB(x - 1, y - m, (image.getRGB(x - 1, y - m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y - 1))
        {
          image.setRGB(x - m, y - 1, (image.getRGB(x - m, y - 1) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x - 2, y - m))
        {
          image.setRGB(x - 2, y - m, (image.getRGB(x - 2, y - m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y - 2))
        {
          image.setRGB(x - m, y - 2, (image.getRGB(x - m, y - 2) ^ RGB888_XOR_MASK));
        }
      }

      // Third quadrant
      x = pointerLocation.x + (3 + n);
      y = pointerLocation.y - (3 + n);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x - m, y + m))
        {
          image.setRGB(x - m, y + m, (image.getRGB(x - m, y + m) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - 1, y + m))
        {
          image.setRGB(x - 1, y + m, (image.getRGB(x - 1, y + m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y + 1))
        {
          image.setRGB(x - m, y + 1, (image.getRGB(x - m, y + 1) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x - 2, y + m))
        {
          image.setRGB(x - 2, y + m, (image.getRGB(x - 2, y + m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y + 2))
        {
          image.setRGB(x - m, y + 2, (image.getRGB(x - m, y + 2) ^ RGB888_XOR_MASK));
        }
      }

      // Fourth quadrant
      x = pointerLocation.x - (3 + n);
      y = pointerLocation.y - (3 + n);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x + m, y + m))
        {
          image.setRGB(x + m, y + m, (image.getRGB(x + m, y + m) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + 1, y + m))
        {
          image.setRGB(x + 1, y + m, (image.getRGB(x + 1, y + m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y + 1))
        {
          image.setRGB(x + m, y + 1, (image.getRGB(x + m, y + 1) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x + 2, y + m))
        {
          image.setRGB(x + 2, y + m, (image.getRGB(x + 2, y + m) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y + 2))
        {
          image.setRGB(x + m, y + 2, (image.getRGB(x + m, y + 2) ^ RGB888_XOR_MASK));
        }
      }
      
      int t = 1;
      int l = (n - 10) / 8;
      //System.out.println("l:" + l);
      for (t = 1; t < l; t++)
      {
        n = n - 1;
        //center
        x = pointerLocation.x;
        y = pointerLocation.y;
        
        if (area.contains(x - 4 - t, y))
        {
          image.setRGB(x - 4 - t, y, (image.getRGB(x - 4 - t, y) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 4 - t, y + 1))
        {
          image.setRGB(x - 4 - t, y + 1, (image.getRGB(x - 4 - t, y + 1) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 4 - t, y - 1))
        {
          image.setRGB(x - 4 - t, y - 1, (image.getRGB(x - 4 - t, y - 1) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 4 + t, y))
        {
          image.setRGB(x + 4 + t, y, (image.getRGB(x + 4 + t, y) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 4 + t, y + 1))
        {
          image.setRGB(x + 4 + t, y + 1, (image.getRGB(x + 4 + t, y + 1) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 4 + t, y - 1))
        {
          image.setRGB(x + 4 + t, y - 1, (image.getRGB(x + 4 + t, y - 1) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x, y - 4 - t))
        {
          image.setRGB(x, y - 4 - t, (image.getRGB(x, y - 4 - t) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 1, y - 4 - t))
        {
          image.setRGB(x + 1, y - 4 - t, (image.getRGB(x + 1, y - 4 - t) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 1,y - 4 - t))
        {
          image.setRGB(x - 1, y - 4 - t, (image.getRGB(x - 1, y - 4 - t) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x, y + 4 + t))
        {
          image.setRGB(x, y + 4 + t, (image.getRGB(x, y + 4 + t) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 1, y + 4 + t))
        {
          image.setRGB(x + 1, y + 4 + t, (image.getRGB(x + 1, y + 4 + t) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 1, y + 4 + t))
        {
          image.setRGB(x - 1, y + 4 + t, (image.getRGB(x - 1, y + 4 + t) ^ RGB888_XOR_MASK));
        }
        
        // First quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y + 4 + t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
          }
        }
        
        // Second quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y + 4 + t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
          }
        }
        
        // Third quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y - 4 - t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
          }
        }
        
        // Fourth quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y - 4 - t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
          }
        }
        
        // First quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y + (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
          }
        }
        
        // Second quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y + (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
          }
        }
        
        // Third quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y - (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ RGB888_XOR_MASK));
          }
        }
        
        // Fourth quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y - (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ RGB888_XOR_MASK));
          }
        }
        
        
      }
      
      
      
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
      // e.printStackTrace();
    }
  }

  /*
   * public final BufferedImage createScreenCapture() { return
   * createScreenCapture(false); }
   */

  /*
   * public final BufferedImage createScreenCapture(Rectangle area) { return
   * createScreenCapture(false, area, 1.0); }
   */

  public final synchronized BufferedImage createScreenCapture(boolean drawPointer)
  {
    if (!isScreenCaptureInitialized())
    {
      if (!initializeScreenCapture())
      {
        return null;
      }
    }
    if (colorQuality == VT_COLOR_QUALITY_16777216)
    {
      return create16777216ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32768)
    {
      return create32768ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_216)
    {
      return create216ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_16)
    {
      return create16ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32)
    {
      return create32ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return create512ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return create4096ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_8)
    {
      return create8ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_125)
    {
      return create125ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_27)
    {
      return create27ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return create64ScreenCapture(drawPointer);
    }
    return null;
  }

  public final synchronized BufferedImage createScreenCapture(boolean drawPointer, Rectangle area)
  {
    if (!isScreenCaptureInitialized())
    {
      if (!initializeScreenCapture())
      {
        return null;
      }
    }
    if (colorQuality == VT_COLOR_QUALITY_16777216)
    {
      return create16777216ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32768)
    {
      return create32768ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_216)
    {
      return create216ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_16)
    {
      return create16ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32)
    {
      return create32ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return create512ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return create4096ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_8)
    {
      return create8ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_125)
    {
      return create125ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_27)
    {
      return create27ScreenCapture(drawPointer, area);
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return create64ScreenCapture(drawPointer, area);
    }
    return null;
  }

  private final void drawPointerFilterGray(BufferedImage image)
  {
    drawPointerFilterGray(image, new Rectangle(0, 0, image.getWidth(), image.getHeight()));
  }

  private final void drawPointerFilterGray(BufferedImage image, Rectangle area)
  {
    PointerInfo info = MouseInfo.getPointerInfo();
    if (info == null)
    {
      // VTTerminal.println("info = null");
      return;
    }
    GraphicsDevice infoDevice = info.getDevice();
    // DisplayMode displayMode = infoDevice.getDisplayMode();
    Point pointerLocation = info.getLocation();
    Rectangle deviceBounds = new Rectangle();
    if (infoDevice == null)
    {
      // VTTerminal.println("infoDevice = null");
      return;
    }
    else
    {
      try
      {
        if (graphicsDevice != null)
        {
          if (!infoDevice.getIDstring().equals(graphicsDevice.getIDstring()))
          {
            // out of current screen
            return;
          }
          deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(infoDevice);
          pointerLocation.translate(deviceBounds.x * -1, deviceBounds.y * -1);
        }
        else
        {
          deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(null);
          pointerLocation.translate(deviceBounds.x * -1, deviceBounds.y * -1);
        }
      }
      catch (Throwable e)
      {
        // return;
      }
    }
    int x, y, m, n, o;
    // int dpi = toolkit.getScreenResolution();
    // int dpiCursorSize = Math.max(32, dpi / 3);
    // int displayDimensionCursorSize = Math.min(displayMode.getWidth(),
    // displayMode.getHeight()) / 25;
    o = drawnCursorSize;
    // o = Math.min(displayMode.getWidth(), displayMode.getHeight()) / 25;
    // o = Math.min(26, displayMode.getHeight()) / 32;
    // o = 45;
    try
    {
      // Center area
      x = pointerLocation.x;
      y = pointerLocation.y;
      if (area.contains(x + 2, y))
      {
        image.setRGB(x + 2, y, (filterGray(image.getRGB(x + 2, y)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 3, y))
      {
        image.setRGB(x + 3, y, (filterGray(image.getRGB(x + 3, y)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 4, y))
      {
        image.setRGB(x + 4, y, (filterGray(image.getRGB(x + 4, y)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 2, y))
      {
        image.setRGB(x - 2, y, (filterGray(image.getRGB(x - 2, y)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 3, y))
      {
        image.setRGB(x - 3, y, (filterGray(image.getRGB(x - 3, y)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 4, y))
      {
        image.setRGB(x - 4, y, (filterGray(image.getRGB(x - 4, y)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y + 2))
      {
        image.setRGB(x, y + 2, (filterGray(image.getRGB(x, y + 2)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y + 3))
      {
        image.setRGB(x, y + 3, (filterGray(image.getRGB(x, y + 3)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y + 4))
      {
        image.setRGB(x, y + 4, (filterGray(image.getRGB(x, y + 4)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y - 2))
      {
        image.setRGB(x, y - 2, (filterGray(image.getRGB(x, y - 2)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y - 3))
      {
        image.setRGB(x, y - 3, (filterGray(image.getRGB(x, y - 3)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x, y - 4))
      {
        image.setRGB(x, y - 4, (filterGray(image.getRGB(x, y - 4)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 2, y + 1))
      {
        image.setRGB(x + 2, y + 1, (filterGray(image.getRGB(x + 2, y + 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 3, y + 1))
      {
        image.setRGB(x + 3, y + 1, (filterGray(image.getRGB(x + 3, y + 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 4, y + 1))
      {
        image.setRGB(x + 4, y + 1, (filterGray(image.getRGB(x + 4, y + 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 2, y - 1))
      {
        image.setRGB(x + 2, y - 1, (filterGray(image.getRGB(x + 2, y - 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 3, y - 1))
      {
        image.setRGB(x + 3, y - 1, (filterGray(image.getRGB(x + 3, y - 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 4, y - 1))
      {
        image.setRGB(x + 4, y - 1, (filterGray(image.getRGB(x + 4, y - 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 2, y + 1))
      {
        image.setRGB(x - 2, y + 1, (filterGray(image.getRGB(x - 2, y + 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 3, y + 1))
      {
        image.setRGB(x - 3, y + 1, (filterGray(image.getRGB(x - 3, y + 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 4, y + 1))
      {
        image.setRGB(x - 4, y + 1, (filterGray(image.getRGB(x - 4, y + 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 2, y - 1))
      {
        image.setRGB(x - 2, y - 1, (filterGray(image.getRGB(x - 2, y - 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 3, y - 1))
      {
        image.setRGB(x - 3, y - 1, (filterGray(image.getRGB(x - 3, y - 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 4, y - 1))
      {
        image.setRGB(x - 4, y - 1, (filterGray(image.getRGB(x - 4, y - 1)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y + 2))
      {
        image.setRGB(x + 1, y + 2, (filterGray(image.getRGB(x + 1, y + 2)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y + 3))
      {
        image.setRGB(x + 1, y + 3, (filterGray(image.getRGB(x + 1, y + 3)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y + 4))
      {
        image.setRGB(x + 1, y + 4, (filterGray(image.getRGB(x + 1, y + 4)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y - 2))
      {
        image.setRGB(x + 1, y - 2, (filterGray(image.getRGB(x + 1, y - 2)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y - 3))
      {
        image.setRGB(x + 1, y - 3, (filterGray(image.getRGB(x + 1, y - 3)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x + 1, y - 4))
      {
        image.setRGB(x + 1, y - 4, (filterGray(image.getRGB(x + 1, y - 4)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y + 2))
      {
        image.setRGB(x - 1, y + 2, (filterGray(image.getRGB(x - 1, y + 2)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y + 3))
      {
        image.setRGB(x - 1, y + 3, (filterGray(image.getRGB(x - 1, y + 3)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y + 4))
      {
        image.setRGB(x - 1, y + 4, (filterGray(image.getRGB(x - 1, y + 4)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y - 2))
      {
        image.setRGB(x - 1, y - 2, (filterGray(image.getRGB(x - 1, y - 2)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y - 3))
      {
        image.setRGB(x - 1, y - 3, (filterGray(image.getRGB(x - 1, y - 3)) ^ RGB888_XOR_MASK));
      }
      if (area.contains(x - 1, y - 4))
      {
        image.setRGB(x - 1, y - 4, (filterGray(image.getRGB(x - 1, y - 4)) ^ RGB888_XOR_MASK));
      }

      if (o > 24)
      {
        n = (o / 2);
      }
      else
      {
        n = 12;
      }

      // First quadrant
      x = pointerLocation.x - 2;
      y = pointerLocation.y + 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
      }
      // Second quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y + 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
      }
      // Third quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y - 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
      }
      // Fourth quadrant
      x = pointerLocation.x - 2;
      y = pointerLocation.y - 2;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
      }

      if (o > 24)
      {
        n = (o / 2) - 1;
      }
      else
      {
        n = 11;
        // n = 10;
        // n = 13;
      }

      // First quadrant
      x = pointerLocation.x - 3;
      y = pointerLocation.y + 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
      }
      // Second quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y + 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
      }
      // Third quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y - 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
      }
      // Fourth quadrant
      x = pointerLocation.x - 3;
      y = pointerLocation.y - 3;
      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
      }

      // n = n / 2;
      n = n - 1;
      // First quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y + 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
      }
      
      // Second quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y + 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
      }
      
      // Third quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y - 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
      }
      
      // Fourth quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y - 4;

      if (area.contains(x, y))
      {
        image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
      }
      for (m = 1; (m < n); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
      }

      x = pointerLocation.x - (4 + n - 1);
      y = pointerLocation.y + (4 + n - 1);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x + m, y - m))
        {
          image.setRGB(x + m, y - m, (filterGray(image.getRGB(x + m, y - m)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + 1, y - m))
        {
          image.setRGB(x + 1, y - m, (filterGray(image.getRGB(x + 1, y - m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y - 1))
        {
          image.setRGB(x + m, y - 1, (filterGray(image.getRGB(x + m, y - 1)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x + 2, y - m))
        {
          image.setRGB(x + 2, y - m, (filterGray(image.getRGB(x + 2, y - m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y - 2))
        {
          image.setRGB(x + m, y - 2, (filterGray(image.getRGB(x + m, y - 2)) ^ RGB888_XOR_MASK));
        }
      }

      x = pointerLocation.x + (4 + n - 1);
      y = pointerLocation.y + (4 + n - 1);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x - m, y - m))
        {
          image.setRGB(x - m, y - m, (filterGray(image.getRGB(x - m, y - m)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y - m))
        {
          image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - 1, y - m))
        {
          image.setRGB(x - 1, y - m, (filterGray(image.getRGB(x - 1, y - m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y - 1))
        {
          image.setRGB(x - m, y - 1, (filterGray(image.getRGB(x - m, y - 1)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x - 2, y - m))
        {
          image.setRGB(x - 2, y - m, (filterGray(image.getRGB(x - 2, y - m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y - 2))
        {
          image.setRGB(x - m, y - 2, (filterGray(image.getRGB(x - m, y - 2)) ^ RGB888_XOR_MASK));
        }
      }

      x = pointerLocation.x + (4 + n - 1);
      y = pointerLocation.y - (4 + n - 1);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x - m, y + m))
        {
          image.setRGB(x - m, y + m, (filterGray(image.getRGB(x - m, y + m)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y))
        {
          image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - 1, y + m))
        {
          image.setRGB(x - 1, y + m, (filterGray(image.getRGB(x - 1, y + m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x - m, y + 1))
        {
          image.setRGB(x - m, y + 1, (filterGray(image.getRGB(x - m, y + 1)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x - 2, y + m))
        {
          image.setRGB(x - 2, y + m, (filterGray(image.getRGB(x - 2, y + m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y + 2))
        {
          image.setRGB(x - m, y + 2, (filterGray(image.getRGB(x - m, y + 2)) ^ RGB888_XOR_MASK));
        }
      }

      x = pointerLocation.x - (4 + n - 1);
      y = pointerLocation.y - (4 + n - 1);

      for (m = 0; (m < 3); m++)
      {
        if (area.contains(x + m, y + m))
        {
          image.setRGB(x + m, y + m, (filterGray(image.getRGB(x + m, y + m)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 1; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x, y + m))
        {
          image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y))
        {
          image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + 1, y + m))
        {
          image.setRGB(x + 1, y + m, (filterGray(image.getRGB(x + 1, y + m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y + 1))
        {
          image.setRGB(x + m, y + 1, (filterGray(image.getRGB(x + m, y + 1)) ^ RGB888_XOR_MASK));
        }
      }

      for (m = 3; (m < (n / 2) + 1); m++)
      {
        if (area.contains(x + 2, y + m))
        {
          image.setRGB(x + 2, y + m, (filterGray(image.getRGB(x + 2, y + m)) ^ RGB888_XOR_MASK));
        }
        if (area.contains(x + m, y + 2))
        {
          image.setRGB(x + m, y + 2, (filterGray(image.getRGB(x + m, y + 2)) ^ RGB888_XOR_MASK));
        }
      }
      
      int t = 1;
      int l = (n - 10) / 8;
      //System.out.println("l:" + l);
      for (t = 1; t < l; t++)
      {
        n = n - 1;
        //center
        x = pointerLocation.x;
        y = pointerLocation.y;
        
        if (area.contains(x - 4 - t, y))
        {
          image.setRGB(x - 4 - t, y, (filterGray(image.getRGB(x - 4 - t, y)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 4 - t, y + 1))
        {
          image.setRGB(x - 4 - t, y + 1, (filterGray(image.getRGB(x - 4 - t, y + 1)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 4 - t, y - 1))
        {
          image.setRGB(x - 4 - t, y - 1, (filterGray(image.getRGB(x - 4 - t, y - 1)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 4 + t, y))
        {
          image.setRGB(x + 4 + t, y, (filterGray(image.getRGB(x + 4 + t, y)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 4 + t, y + 1))
        {
          image.setRGB(x + 4 + t, y + 1, (filterGray(image.getRGB(x + 4 + t, y + 1)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 4 + t, y - 1))
        {
          image.setRGB(x + 4 + t, y - 1, (filterGray(image.getRGB(x + 4 + t, y - 1)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x, y - 4 - t))
        {
          image.setRGB(x, y - 4 - t, (filterGray(image.getRGB(x, y - 4 - t)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 1, y - 4 - t))
        {
          image.setRGB(x + 1, y - 4 - t, (filterGray(image.getRGB(x + 1, y - 4 - t)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 1,y - 4 - t))
        {
          image.setRGB(x - 1, y - 4 - t, (filterGray(image.getRGB(x - 1, y - 4 - t)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x, y + 4 + t))
        {
          image.setRGB(x, y + 4 + t, (filterGray(image.getRGB(x, y + 4 + t)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x + 1, y + 4 + t))
        {
          image.setRGB(x + 1, y + 4 + t, (filterGray(image.getRGB(x + 1, y + 4 + t)) ^ RGB888_XOR_MASK));
        }
        
        if (area.contains(x - 1, y + 4 + t))
        {
          image.setRGB(x - 1, y + 4 + t, (filterGray(image.getRGB(x - 1, y + 4 + t)) ^ RGB888_XOR_MASK));
        }
        
        // First quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y + 4 + t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
          }
        }
        
        // Second quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y + 4 + t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
          }
        }
        
        // Third quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y - 4 - t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
          }
        }
        
        // Fourth quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y - 4 - t;

        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
          }
        }
        for (m = 1; (m < n); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
          }
        }
        
        // First quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y + (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
          }
        }
        
        // Second quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y + (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y - m))
          {
            image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
          }
        }
        
        // Third quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y - (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x - m, y))
          {
            image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ RGB888_XOR_MASK));
          }
        }
        
        // Fourth quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y - (1 + n);
        
        if (area.contains(x, y))
        {
          image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ RGB888_XOR_MASK));
        }
        
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          if (area.contains(x, y + m))
          {
            image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ RGB888_XOR_MASK));
          }
          if (area.contains(x + m, y))
          {
            image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ RGB888_XOR_MASK));
          }
        }
        
        
      }
      
    }
    catch (Throwable e)
    {
      // e.printStackTrace(VTTerminal.getSystemOut());
      // e.printStackTrace();
    }
  }

  private int filterGray(int rgb)
  {
    if (((rgb & RGB888_XOR_MASK) == 0x00808080) || ((rgb & RGB888_XOR_MASK) == 0x007F7F7F))
    //if (mustFilterGray(rgb))
    {
      return rgb & 0xFF000000;
      //return 0;
    }
    return rgb;
  }
  
//  private boolean mustFilterGray(int rgb)
//  {
//    int red = (rgb & RGB888_RED_MASK >> 16);
//    int green = (rgb & RGB888_GREEN_MASK >> 8);
//    int blue = (rgb & RGB888_BLUE_MASK);
//    if (((red & green & blue) == (red | green | blue)))
//    {
//      return Math.abs(red - (red ^ 0xFF)) < 38;
//      //return Math.abs(red - (red ^ 0xFF)) < 19;
//    }
//    return false;
//  }

}