package org.vash.vate.graphics.capture;

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

import org.vash.vate.VTSystem;
import org.vash.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vash.vate.graphics.font.VTFontManager;
import org.vash.vate.graphics.image.VTImageIO;
import org.vash.vate.graphics.image.VTRectangle;
import org.vash.vate.reflection.VTReflectionUtils;

import com.bric.imagevt.VTARGBPixelGrabber;

public final class VTAWTScreenCaptureProvider
{
  public static final int VT_COLOR_QUALITY_64 = 0; // 64 rgb-222 or rgb-4x4x4 6bit
  public static final int VT_COLOR_QUALITY_216 = 1; // 216 rgb-6x6x6 8bit
  public static final int VT_COLOR_QUALITY_32768 = 2; // 32768 rgb-555 15bit
  public static final int VT_COLOR_QUALITY_16777216 = 3; // 16777216 rgb-888 24bit
  public static final int VT_COLOR_QUALITY_512 = 4; // 512 rgb-333 9bit
  public static final int VT_COLOR_QUALITY_4096 = 5; // 4096 rgb-444 12bit
  public static final int VT_COLOR_QUALITY_125 = 6; // 125 rgb-5x5x5 7bit
  public static final int VT_COLOR_QUALITY_27 = 7; // 27 rgb-3x3x3 5bit
  public static final int VT_COLOR_QUALITY_262144 = 8; // 262144 rgb-666 18bit
  public static final int VT_COLOR_QUALITY_2097152 = 9; // 2097152 rgb-777 21bit
  public static final int VT_COLOR_QUALITY_8 = 10; // 8 rgb-111 3bit or grayscale 3bit (r+g+b) * 6 >> 4
  public static final int VT_COLOR_QUALITY_16 = 11; // 16 grayscale 4bit (r+g+b) * 11 >> 5
  public static final int VT_COLOR_QUALITY_4 = 12; // 4 grayscale 2bit (r+g+b) * 3 >> 3
  //public static final int VT_COLOR_QUALITY_134217728 = 13; // 134217728 rgb-999 27bit
  //public static final int VT_COLOR_QUALITY_1073741824 = 14; // 1073741824 rgb-AAA 30bit
  //public static final int VT_COLOR_QUALITY_32 = 15; // 32 grayscale 5bit (r+g+b) * 11 >> 5
  
//  private static final int RGBAAA_RED_MASK = 0x00FF0000;
//  private static final int RGBAAA_GREEN_MASK = 0x0000FF00;
//  private static final int RGBAAA_BLUE_MASK = 0x000000FF;
//  
//  private static final int RGB999_RED_MASK = 0x00FF0000;
//  private static final int RGB999_GREEN_MASK = 0x0000FF00;
//  private static final int RGB999_BLUE_MASK = 0x000000FF;
  
  private static final int RGB888_RED_MASK = 0x00FF0000;
  private static final int RGB888_GREEN_MASK = 0x0000FF00;
  private static final int RGB888_BLUE_MASK = 0x000000FF;
  
  private static final int RGB777_RED_MASK = 0x00FE0000;
  private static final int RGB777_GREEN_MASK = 0x0000FE00;
  private static final int RGB777_BLUE_MASK = 0x000000FE;
  
  private static final int RGB666_RED_MASK = 0x00FC0000;
  private static final int RGB666_GREEN_MASK = 0x0000FC00;
  private static final int RGB666_BLUE_MASK = 0x000000FC;
  
  private static final int RGB555_RED_MASK = 0x00F80000;
  private static final int RGB555_GREEN_MASK = 0x0000F800;
  private static final int RGB555_BLUE_MASK = 0x000000F8;
  
  private static final int RGB444_RED_MASK = 0x00F00000;
  private static final int RGB444_GREEN_MASK = 0x0000F000;
  private static final int RGB444_BLUE_MASK = 0x000000F0;
  
  private static final int RGB333_RED_MASK = 0x00E00000;
  private static final int RGB333_GREEN_MASK = 0x0000E000;
  private static final int RGB333_BLUE_MASK = 0x000000E0;
  
  private static final int RGB222_RED_MASK = 0x00C00000;
  private static final int RGB222_GREEN_MASK = 0x0000C000;
  private static final int RGB222_BLUE_MASK = 0x000000C0;
  
  //private static final int RGB111_RED_MASK = 0x00800000;
  //private static final int RGB111_GREEN_MASK = 0x00008000;
  //private static final int RGB111_BLUE_MASK = 0x00000080;
  
  private static final int RGB888_XOR_MASK = 0x00FFFFFF;
  
  private int colorQuality;
  private boolean initialized4ScreenCapture;
  private boolean initialized8ScreenCapture;
  private boolean initialized16ScreenCapture;
  //private boolean initialized32ScreenCapture;
  private boolean initialized512ScreenCapture;
  private boolean initialized4096ScreenCapture;
  private boolean initialized64ScreenCapture;
  private boolean initialized216ScreenCapture;
  private boolean initialized32768ScreenCapture;
  private boolean initialized16777216ScreenCapture;
  private boolean initialized125ScreenCapture;
  private boolean initialized27ScreenCapture;
  private boolean initialized262144ScreenCapture;
  private boolean initialized2097152ScreenCapture;
  private int scaledWidth = 0;
  private int scaledHeight = 0;
  private double scaleFactorX = 1;
  private double scaleFactorY = 1;
  private boolean keepRatio = false;
  private boolean forceScaleFactors = false;
  private int padding = 0;
  private int i;
  private int red, green, blue;
  private int screenCurrentWidth, screenCurrentHeight;
  private int screenCurrentX, screenCurrentY;
  private int scaledCurrentWidth, scaledCurrentHeight;
  //private int sectionCurrentImageGrayscaleColorCount;
  //private int sectionCurrentImageConvertedColorCount;
  // private int[] pixelBlock = new int[64 * 64];
  private int[] sectionPixelBufferInt;
  //private short[] sectionPixelBufferShort;
  private byte[] sectionPixelBufferByte;
  private int[] pixelBufferInt;
  private short[] pixelBufferShort;
  private byte[] pixelBufferByte;
  private Rectangle currentDeviceBounds;
  private BufferedImage sectionCurrentImageGrayscale;
  private BufferedImage sectionCurrentImageTrue;
  //private BufferedImage sectionCurrentImageConverted;
  private BufferedImage screenCurrentImage;
  private BufferedImage scaledCurrentImage;
  private Graphics2D scaledCurrentImageGraphics;
  private Graphics2D sectionCurrentImageGrayscaleGraphics;
  //private Graphics2D sectionCurrentImageConvertedGraphics;
  private GraphicsDevice graphicsDevice;
  private Robot standardCaptureRobot;
  private VTDirectRobot directCaptureRobot;
  // private Toolkit toolkit;
  private final VTARGBPixelGrabber pixelGrabber;
  private DataBuffer recyclableSectionDataBufferTrue;
  private DataBuffer recyclableSectionDataBufferGrayscale;
  //private DataBuffer recyclableSectionDataBufferConverted;
  private DataBuffer recyclableScreenDataBuffer;
  private DataBuffer recyclableScaledDataBuffer;
//	private DataBuffer recyclableSectionDataBuffer;
  private int drawnCursorSize = 32;
  private int initialDrawnCursorSize;
  
  public int getColorCount()
  {
    switch (colorQuality)
    {
      case VT_COLOR_QUALITY_4:
        return 4;
      case VT_COLOR_QUALITY_8:
        return 8;
      case VT_COLOR_QUALITY_16:
        return 16;
      case VT_COLOR_QUALITY_27:
        return 27;
      //case VT_COLOR_QUALITY_32:
        //return 32;
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
      case VT_COLOR_QUALITY_262144:
        return 262144;
      case VT_COLOR_QUALITY_2097152:
        return 2097152;
      case VT_COLOR_QUALITY_16777216:
        return 16777216;
      //case VT_COLOR_QUALITY_134217728:
        //return 134217728;
      //case VT_COLOR_QUALITY_1073741824:
        //return 1073741824;
      default:
        return -1;
    }
  }
  
  private final int roundUp(final int numToRound, final int multiple)
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
  
  public final void setScaledDimensions(final int scaledWidth, final int scaledHeight, boolean keepRatio)
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
  
  public final void setScaleFactors(final double scaleFactorX, final double scaleFactorY)
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
    else
    {
      //scaledCurrentWidth = 0;
      //scaledCurrentHeight = 0;
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
    pixelGrabber = new VTARGBPixelGrabber();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return;
    }
    
    VTFontManager.checkScaling();
    int dpi = VTFontManager.BASE_FONT_DPI;
    
    drawnCursorSize = Math.max(32, dpi / 3);
    initialDrawnCursorSize = roundUp(drawnCursorSize, 8);
  }
  
  public final void resetGraphicsDevice()
  {
    if (VTReflectionUtils.isAWTHeadless())
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
          initializeScreenCapture(padding);
        }
      }
      else
      {
        this.graphicsDevice = graphicsDevice;
        this.standardCaptureRobot = null;
        this.directCaptureRobot = null;
        initializeScreenCapture(padding);
      }
    }
    else
    {
      if (this.graphicsDevice != graphicsDevice)
      {
        this.graphicsDevice = graphicsDevice;
        this.standardCaptureRobot = null;
        this.directCaptureRobot = null;
        initializeScreenCapture(padding);
      }
    }
  }
  
  public final int getColorQuality()
  {
    return this.colorQuality;
  }
  
  public final void setColorQuality(final int colorQuality)
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
      
    }
    return null;
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
  
  private final boolean initializeCaptureRobot(final GraphicsDevice device)
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
          //t.printStackTrace();
        }
        try
        {
          directCaptureRobot = new VTDirectRobot(device);
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
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
            //t.printStackTrace();
          }
          try
          {
            directCaptureRobot = new VTDirectRobot(topleft);
          }
          catch (Throwable t)
          {
            //t.printStackTrace();
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
            //t.printStackTrace();
          }
          try
          {
            directCaptureRobot = new VTDirectRobot();
          }
          catch (Throwable t)
          {
            //t.printStackTrace();
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
        //t.printStackTrace();
      }
    }
    return directCaptureRobot != null || standardCaptureRobot != null;
  }
  
  private final boolean initialize16ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh16ScreenCapture();
      }
      initialized16ScreenCapture = true;
      return true;
    }
    return false;
  }
  
//  private final boolean initialize32ScreenCapture(GraphicsDevice device)
//  {
//    reset();
//    if (VT.isAWTHeadless())
//    {
//      return false;
//    }
//    try
//    {
//      if (initializeCaptureRobot(device))
//      {
//        if (changedCurrentScreenCapture())
//        {
//          refresh32ScreenCapture();
//        }
//        initialized32ScreenCapture = true;
//        return true;
//      }
//    }
//    catch (Throwable e)
//    {
//      initialized32ScreenCapture = false;
//    }
//    return false;
//  }
  
  private final boolean initialize4ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh4ScreenCapture();
      }
      initialized4ScreenCapture = true;
      return true;
    }
    initialized4ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize8ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh8ScreenCapture();
      }
      initialized8ScreenCapture = true;
      return true;
    }
    initialized8ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize512ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh512ScreenCapture();
      }
      initialized512ScreenCapture = true;
      return true;
    }
    initialized512ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize4096ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh4096ScreenCapture();
      }
      initialized4096ScreenCapture = true;
      return true;
    }
    initialized4096ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize64ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh64ScreenCapture();
      }
      initialized64ScreenCapture = true;
      return true;
    }
    initialized64ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize216ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh216ScreenCapture();
      }
      initialized216ScreenCapture = true;
      return true;
    }
    initialized216ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize32768ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh32768ScreenCapture();
      }
      initialized32768ScreenCapture = true;
      return true;
    }
    initialized32768ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize16777216ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh16777216ScreenCapture();
      }
      initialized16777216ScreenCapture = true;
      return true;
    }
    initialized16777216ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize125ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh125ScreenCapture();
      }
      initialized125ScreenCapture = true;
      return true;
    }
    initialized125ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize27ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh27ScreenCapture();
      }
      initialized27ScreenCapture = true;
      return true;
    }
    initialized27ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize262144ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh262144ScreenCapture();
      }
      initialized262144ScreenCapture = true;
      return true;
    }
    initialized262144ScreenCapture = false;
    return false;
  }
  
  private final boolean initialize2097152ScreenCapture(final GraphicsDevice device) throws Throwable
  {
    reset();
    if (VTReflectionUtils.isAWTHeadless())
    {
      return false;
    }
    if (initializeCaptureRobot(device))
    {
      if (changedCurrentScreenCapture())
      {
        refresh2097152ScreenCapture();
      }
      initialized2097152ScreenCapture = true;
      return true;
    }
    initialized2097152ScreenCapture = false;
    return false;
  }
  
  public final boolean initializeScreenCapture(final int padding)
  {
    this.padding = padding;
    try
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
      //else if (colorQuality == VT_COLOR_QUALITY_32)
      //{
        //return initialize32ScreenCapture(graphicsDevice);
      //}
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
      else if (colorQuality == VT_COLOR_QUALITY_4)
      {
        return initialize4ScreenCapture(graphicsDevice);
      }
      else if (colorQuality == VT_COLOR_QUALITY_125)
      {
        return initialize125ScreenCapture(graphicsDevice);
      }
      else if (colorQuality == VT_COLOR_QUALITY_27)
      {
        return initialize27ScreenCapture(graphicsDevice);
      }
      else if (colorQuality == VT_COLOR_QUALITY_262144)
      {
        return initialize262144ScreenCapture(graphicsDevice);
      }
      else if (colorQuality == VT_COLOR_QUALITY_2097152)
      {
        return initialize2097152ScreenCapture(graphicsDevice);
      }
      else if (colorQuality == VT_COLOR_QUALITY_64)
      {
        return initialize64ScreenCapture(graphicsDevice);
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return false;
  }
  
  public final boolean initializeScreenCapture(final int padding, final GraphicsDevice device)
  {
    this.padding = padding;
    try
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
      //else if (colorQuality == VT_COLOR_QUALITY_32)
      //{
        //return initialize32ScreenCapture(device);
      //}
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
      else if (colorQuality == VT_COLOR_QUALITY_4)
      {
        return initialize4ScreenCapture(device);
      }
      else if (colorQuality == VT_COLOR_QUALITY_125)
      {
        return initialize125ScreenCapture(device);
      }
      else if (colorQuality == VT_COLOR_QUALITY_27)
      {
        return initialize27ScreenCapture(device);
      }
      else if (colorQuality == VT_COLOR_QUALITY_262144)
      {
        return initialize262144ScreenCapture(device);
      }
      else if (colorQuality == VT_COLOR_QUALITY_2097152)
      {
        return initialize2097152ScreenCapture(device);
      }
      else if (colorQuality == VT_COLOR_QUALITY_64)
      {
        return initialize64ScreenCapture(device);
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
    return false;
    
  }
  
  private final boolean is27ScreenCaptureInitialized()
  {
    return initialized27ScreenCapture;
  }
  
  //private final boolean is32ScreenCaptureInitialized()
  //{
    //return initialized32ScreenCapture;
  //}
  
  private final boolean is512ScreenCaptureInitialized()
  {
    return initialized512ScreenCapture;
  }
  
  private final boolean is4096ScreenCaptureInitialized()
  {
    return initialized4096ScreenCapture;
  }
  
  private final boolean is4ScreenCaptureInitialized()
  {
    return initialized4ScreenCapture;
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
  
  private final boolean is262144ScreenCaptureInitialized()
  {
    return initialized262144ScreenCapture;
  }
  
  private final boolean is2097152ScreenCaptureInitialized()
  {
    return initialized2097152ScreenCapture;
  }
  
  private final boolean is16777216ScreenCaptureInitialized()
  {
    return initialized16777216ScreenCapture;
  }
  
  public final synchronized boolean isScreenCaptureInitialized(int padding)
  {
    if (this.padding != padding)
    {
      return false;
    }
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
    //else if (colorQuality == VT_COLOR_QUALITY_32)
    //{
      //return is32ScreenCaptureInitialized();
    //}
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return is512ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return is4096ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_4)
    {
      return is4ScreenCaptureInitialized();
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
    else if (colorQuality == VT_COLOR_QUALITY_262144)
    {
      return is262144ScreenCaptureInitialized();
    }
    else if (colorQuality == VT_COLOR_QUALITY_2097152)
    {
      return is2097152ScreenCaptureInitialized();
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
//    System.runFinalization();
//    System.gc();
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
//    sectionPixelBufferShort = null;
    sectionPixelBufferByte = null;
    standardCaptureRobot = null;
    if (directCaptureRobot != null)
    {
      directCaptureRobot.dispose();
    }
    directCaptureRobot = null;
    recyclableScreenDataBuffer = null;
    recyclableScaledDataBuffer = null;
    recyclableSectionDataBufferTrue = null;
    recyclableSectionDataBufferGrayscale = null;
    //recyclableSectionDataBufferConverted = null;
    
//    System.runFinalization();
//    System.gc();
  }
  
  private final void disposeScreenCaptureResources()
  {
    if (screenCurrentImage != null)
    {
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    if (scaledCurrentImage != null)
    {
      try
      {
        scaledCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      scaledCurrentImage = null;
    }
    if (scaledCurrentImageGraphics != null)
    {
      try
      {
        scaledCurrentImageGraphics.dispose();
      }
      catch (Throwable t)
      {
        
      }
      scaledCurrentImageGraphics = null;
    }
    if (sectionCurrentImageTrue != null)
    {
      try
      {
        sectionCurrentImageTrue.flush();
      }
      catch (Throwable t)
      {
        
      }
      sectionCurrentImageTrue = null;
    }
    if (sectionCurrentImageGrayscale != null)
    {
      try
      {
        sectionCurrentImageGrayscale.flush();
      }
      catch (Throwable t)
      {
        
      }
      sectionCurrentImageGrayscale = null;
      //sectionCurrentImageGrayscaleColorCount = 0;
      //sectionCurrentImageConvertedColorCount = 0;
    }
    if (sectionCurrentImageGrayscaleGraphics != null)
    {
      try
      {
        sectionCurrentImageGrayscaleGraphics.dispose();
      }
      catch (Throwable t)
      {
        
      }
      sectionCurrentImageGrayscaleGraphics = null;
    }
    pixelBufferByte = null;
    pixelBufferShort = null;
    pixelBufferInt = null;
    screenCurrentWidth = 0;
    screenCurrentHeight = 0;
    scaledCurrentWidth = 0;
    scaledCurrentHeight = 0;
    initialized16ScreenCapture = false;
    //initialized32ScreenCapture = false;
    initialized512ScreenCapture = false;
    initialized4096ScreenCapture = false;
    initialized4ScreenCapture = false;
    initialized8ScreenCapture = false;
    initialized64ScreenCapture = false;
    initialized216ScreenCapture = false;
    initialized32768ScreenCapture = false;
    initialized16777216ScreenCapture = false;
    initialized125ScreenCapture = false;
    initialized27ScreenCapture = false;
    initialized262144ScreenCapture = false;
    initialized2097152ScreenCapture = false;
  }
  
  public final void clearResources()
  {
    initialized16ScreenCapture = false;
    //initialized32ScreenCapture = false;
    initialized512ScreenCapture = false;
    initialized4096ScreenCapture = false;
    initialized4ScreenCapture = false;
    initialized8ScreenCapture = false;
    initialized64ScreenCapture = false;
    initialized216ScreenCapture = false;
    initialized32768ScreenCapture = false;
    initialized16777216ScreenCapture = false;
    initialized125ScreenCapture = false;
    initialized27ScreenCapture = false;
    initialized262144ScreenCapture = false;
    initialized2097152ScreenCapture = false;
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 27, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 27, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
    }
  }
  
  private final void refresh4ScreenCapture()
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 4, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled4ScreenCapture();
  }
  
  private final void refreshScaled4ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 4, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
    }
  }
  
  private final void refresh16ScreenCapture()
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 16, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 16, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
    }
  }
  
//  private final void refresh32ScreenCapture()
//  {
//    currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
//    if (currentDeviceBounds == null)
//    {
//      return;
//    }
//    screenCurrentWidth = currentDeviceBounds.width;
//    screenCurrentHeight = currentDeviceBounds.height;
//    screenCurrentX = currentDeviceBounds.x;
//    screenCurrentY = currentDeviceBounds.y;
//    if (screenCurrentImage != null)
//    {
//      screenCurrentImage.flush();
//      screenCurrentImage = null;
//    }
//    int x = 0;
//    int y = 0;
//    if (padding > 0)
//    {
//      x = padding;
//      y = padding;
//    }
//    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 32, recyclableScreenDataBuffer);
//    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
//    refreshScaled32ScreenCapture();
//  }
  
//  private final void refreshScaled32ScreenCapture()
//  {
//    scaledCurrentWidth = scaledWidth;
//    scaledCurrentHeight = scaledHeight;
//    if (scaledWidth > 0 && scaledHeight > 0)
//    {
//      if (scaledCurrentImage != null)
//      {
//        scaledCurrentImage.flush();
//        scaledCurrentImage = null;
//      }
//      if (scaledCurrentGraphics != null)
//      {
//        scaledCurrentGraphics.dispose();
//        scaledCurrentGraphics = null;
//      }
//      int x = 0;
//      int y = 0;
//      if (padding > 0)
//      {
//        x = padding;
//        y = padding;
//      }
//      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 32, recyclableScaledDataBuffer);
//      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
//      scaledCurrentGraphics = scaledCurrentImage.createGraphics();
//      scaledCurrentGraphics.setRenderingHints(VT.VT_GRAPHICS_RENDERING_HINTS);
//    }
//    else
//    {
//      
//    }
//  }
  
  private final void refresh8ScreenCapture()
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 8, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 8, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 64, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 64, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 125, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 125, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 216, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 216, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 512, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 512, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 4096, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled4096ScreenCapture();
  }
  
  private final void refreshScaled4096ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 4096, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 32768, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 32768, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
    }
  }
  
  private final void refresh262144ScreenCapture()
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 262144, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled262144ScreenCapture();
  }
  
  private final void refreshScaled262144ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 262144, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
    }
  }
  
  private final void refresh2097152ScreenCapture()
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_CUSTOM, 2097152, recyclableScreenDataBuffer);
    recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
    refreshScaled2097152ScreenCapture();
  }
  
  private final void refreshScaled2097152ScreenCapture()
  {
    scaledCurrentWidth = scaledWidth;
    scaledCurrentHeight = scaledHeight;
    if (scaledWidth > 0 && scaledHeight > 0)
    {
      if (scaledCurrentImage != null)
      {
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_CUSTOM, 2097152, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
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
      try
      {
        screenCurrentImage.flush();
      }
      catch (Throwable t)
      {
        
      }
      screenCurrentImage = null;
    }
    int x = 0;
    int y = 0;
    if (padding > 0)
    {
      x = padding;
      y = padding;
    }
    screenCurrentImage = scaledCurrentImage = VTImageIO.createImage(x, y, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_INT_RGB, 16777216, recyclableScreenDataBuffer);
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
        try
        {
          scaledCurrentImage.flush();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImage = null;
      }
      if (scaledCurrentImageGraphics != null)
      {
        try
        {
          scaledCurrentImageGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        scaledCurrentImageGraphics = null;
      }
      int x = 0;
      int y = 0;
      if (padding > 0)
      {
        x = padding;
        y = padding;
      }
      scaledCurrentImage = VTImageIO.createImage(x, y, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_INT_RGB, 16777216, recyclableScaledDataBuffer);
      recyclableScaledDataBuffer = scaledCurrentImage.getRaster().getDataBuffer();
      scaledCurrentImageGraphics = scaledCurrentImage.createGraphics();
      scaledCurrentImageGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
    }
    else
    {
      
    }
  }
  
  private final BufferedImage create4ScreenCapture(final boolean drawPointer)
  {
    return create4ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create4ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh4ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, true);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    sectionPixelBufferByte = ((DataBufferByte) screenCapture.getRaster().getDataBuffer()).getData();
    //sectionPixelBufferShort = ((DataBufferUShort) screenCapture.getRaster().getDataBuffer()).getData();
    
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
//    int destinationOffset = captureArea.x + minX + (screenCurrentImage.getWidth() + minX) * (captureArea.y + minY);
//    int destinationIndex = destinationOffset;
//    int sourceIndex = 0;
//    
//    for (int currentHeight = 0; currentHeight < captureArea.height; currentHeight++)
//    {
//      System.arraycopy(sectionPixelBufferByte, sourceIndex, pixelBufferByte, destinationIndex, captureArea.width);
//      sourceIndex += captureArea.width;
//      destinationIndex += screenCurrentImage.getWidth() + minX;
//    }
    
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferByte[i]));
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferByte[i] & 0xC0) >> 6);
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferShort[i] & 0xC000) >> 14);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferByte = null;
    //sectionPixelBufferShort = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create8ScreenCapture(final boolean drawPointer)
  {
    return create8ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create8ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh8ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, true);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    sectionPixelBufferByte = ((DataBufferByte) screenCapture.getRaster().getDataBuffer()).getData();
    //sectionPixelBufferShort = ((DataBufferUShort) screenCapture.getRaster().getDataBuffer()).getData();
    
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
//    int destinationOffset = captureArea.x + minX + (screenCurrentImage.getWidth() + minX) * (captureArea.y + minY);
//    int destinationIndex = destinationOffset;
//    int sourceIndex = 0;
//    
//    for (int currentHeight = 0; currentHeight < captureArea.height; currentHeight++)
//    {
//      System.arraycopy(sectionPixelBufferByte, sourceIndex, pixelBufferByte, destinationIndex, captureArea.width);
//      sourceIndex += captureArea.width;
//      destinationIndex += screenCurrentImage.getWidth() + minX;
//    }
    
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferByte[i]));
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferByte[i] & 0xE0) >> 5);
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferShort[i] & 0xE000) >> 13);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferByte = null;
    //sectionPixelBufferShort = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create16ScreenCapture(final boolean drawPointer)
  {
    return create16ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create16ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh16ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, true);
    int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
    sectionPixelBufferByte = ((DataBufferByte) screenCapture.getRaster().getDataBuffer()).getData();
    //sectionPixelBufferShort = ((DataBufferUShort) screenCapture.getRaster().getDataBuffer()).getData();
    
    pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
//    int destinationOffset = captureArea.x + minX + (screenCurrentImage.getWidth() + minX) * (captureArea.y + minY);
//    int destinationIndex = destinationOffset;
//    int sourceIndex = 0;
//    
//    for (int currentHeight = 0; currentHeight < captureArea.height; currentHeight++)
//    {
//      System.arraycopy(sectionPixelBufferByte, sourceIndex, pixelBufferByte, destinationIndex, captureArea.width);
//      sourceIndex += captureArea.width;
//      destinationIndex += screenCurrentImage.getWidth() + minX;
//    }
    
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferByte[i]));
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferByte[i] & 0xF0) >> 4);
      //pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) ((sectionPixelBufferShort[i] & 0xF000) >> 12);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferByte = null;
    //sectionPixelBufferShort = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create216ScreenCapture(final boolean drawPointer)
  {
    return create216ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create216ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh216ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
    //sectionPixelBufferByte = ((DataBufferByte) screenCapture.getRaster().getDataBuffer()).getData();
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
//    int destinationOffset = captureArea.x + minX + (screenCurrentImage.getWidth() + minX) * (captureArea.y + minY);
//    int destinationIndex = destinationOffset;
//    int sourceIndex = 0;
//    
//    for (int currentHeight = 0; currentHeight < captureArea.height; currentHeight++)
//    {
//      System.arraycopy(sectionPixelBufferByte, sourceIndex, pixelBufferByte, destinationIndex, captureArea.width);
//      sourceIndex += captureArea.width;
//      destinationIndex += screenCurrentImage.getWidth() + minX;
//    }
    
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 6) >> 8) * 36);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 6) >> 8) * 6);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 6) >> 8));
      
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    //screenCapture = null;
    pixelBufferByte = null;
    //sectionPixelBufferByte = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create64ScreenCapture(final boolean drawPointer)
  {
    return create64ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create64ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh64ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = ((sectionPixelBufferInt[i] & RGB222_RED_MASK) >> 18);
      green = ((sectionPixelBufferInt[i] & RGB222_GREEN_MASK) >> 12);
      blue = ((sectionPixelBufferInt[i] & RGB222_BLUE_MASK) >> 6);
      
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create512ScreenCapture(final boolean drawPointer)
  {
    return create512ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create512ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh512ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = ((sectionPixelBufferInt[i] & RGB333_RED_MASK) >> 15);
      green = ((sectionPixelBufferInt[i] & RGB333_GREEN_MASK) >> 10);
      blue = ((sectionPixelBufferInt[i] & RGB333_BLUE_MASK) >> 5);
      
      pixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (red | green | blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create4096ScreenCapture(final boolean drawPointer)
  {
    return create4096ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create4096ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh4096ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = ((sectionPixelBufferInt[i] & RGB444_RED_MASK) >> 12);
      green = ((sectionPixelBufferInt[i] & RGB444_GREEN_MASK) >> 8);
      blue = ((sectionPixelBufferInt[i] & RGB444_BLUE_MASK) >> 4);
      
      pixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (red | green | blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create32768ScreenCapture(final boolean drawPointer)
  {
    return create32768ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create32768ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh32768ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = ((sectionPixelBufferInt[i] & RGB555_RED_MASK) >> 9);
      green = ((sectionPixelBufferInt[i] & RGB555_GREEN_MASK) >> 6);
      blue = ((sectionPixelBufferInt[i] & RGB555_BLUE_MASK) >> 3);
      
      pixelBufferShort[startOffset + currentWidth + currentHeight] = (short) (red | green | blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferShort = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create262144ScreenCapture(final boolean drawPointer)
  {
    return create262144ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create262144ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh262144ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = ((sectionPixelBufferInt[i] & RGB666_RED_MASK) >> 6);
      green = ((sectionPixelBufferInt[i] & RGB666_GREEN_MASK) >> 4);
      blue = ((sectionPixelBufferInt[i] & RGB666_BLUE_MASK) >> 2);
      
      pixelBufferInt[startOffset + currentWidth + currentHeight] = (red | green | blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferInt = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create2097152ScreenCapture(final boolean drawPointer)
  {
    return create2097152ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create2097152ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh2097152ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = ((sectionPixelBufferInt[i] & RGB777_RED_MASK) >> 3);
      green = ((sectionPixelBufferInt[i] & RGB777_GREEN_MASK) >> 2);
      blue = ((sectionPixelBufferInt[i] & RGB777_BLUE_MASK) >> 1);
      
      pixelBufferInt[startOffset + currentWidth + currentHeight] = (red | green | blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferInt = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create16777216ScreenCapture(final boolean drawPointer)
  {
    return create16777216ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create16777216ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh16777216ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int destinationOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int destinationIndex = destinationOffset;
    int sourceIndex = 0;
    
    for (int currentHeight = 0; currentHeight < captureArea.height; currentHeight++)
    {
      System.arraycopy(sectionPixelBufferInt, sourceIndex, pixelBufferInt, destinationIndex, captureArea.width);
      sourceIndex += captureArea.width;
      destinationIndex += screenCurrentImage.getWidth() + padding;
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferInt = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointer(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create125ScreenCapture(final boolean drawPointer)
  {
    return create125ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create125ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh125ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 5) >> 8) * 25);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 5) >> 8) * 5);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 5) >> 8));
      
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointerFilterGray(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage create27ScreenCapture(final boolean drawPointer)
  {
    return create27ScreenCapture(drawPointer, calculateFullArea());
  }
  
  private final BufferedImage create27ScreenCapture(final boolean drawPointer, final Rectangle originalArea)
  {
    if (changedCurrentScreenCapture())
    {
      refresh27ScreenCapture();
    }
    Rectangle captureArea = calculateCaptureArea(originalArea);
    if (captureArea.width <= 0 || captureArea.height <= 0)
    {
      return null;
    }
    BufferedImage screenCapture = createScreenCapture(captureArea, false);
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
//    int minX = 0;
//    int minY = 0;
//    if (padding > 0)
//    {
//      minX = padding;
//      minY = padding;
//    }
    int startOffset = captureArea.x + padding + (screenCurrentImage.getWidth() + padding) * (captureArea.y + padding);
    int currentWidth = 0;
    int currentHeight = 0;
    for (i = 0; i < pixelDataLength; i++, currentWidth++)
    {
      if (currentWidth == captureArea.getWidth())
      {
        currentWidth = 0;
        currentHeight += screenCurrentImage.getWidth() + padding;
      }
      
      red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 3) >> 8) * 9);
      green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 3) >> 8) * 3);
      blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 3) >> 8));
      
      pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
    }
    
    if (sectionCurrentImageTrue != screenCapture && screenCapture != sectionCurrentImageGrayscale)
    {
      screenCapture.flush();
    }
    screenCapture = null;
    pixelBufferByte = null;
    sectionPixelBufferInt = null;
    
    if (drawPointer)
    {
      drawPointerFilterGray(screenCurrentImage, captureArea, graphicsDevice, drawnCursorSize);
    }
    
    if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
    {
      return screenCurrentImage;
    }
    else
    {
      scaledCurrentImageGraphics.drawImage(screenCurrentImage, originalArea.x, originalArea.y, originalArea.x + originalArea.width, originalArea.y + originalArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
      return scaledCurrentImage;
    }
  }
  
  private final BufferedImage createScreenCapture(final Rectangle captureArea, final boolean grayscale)
  {
    BufferedImage screenCapture;
    if (directCaptureRobot != null && directCaptureRobot.getDirectRGBPixelsMethodAvailable())
    {
      if (sectionCurrentImageTrue == null || sectionCurrentImageTrue.getWidth() != captureArea.width || sectionCurrentImageTrue.getHeight() != captureArea.height)
      {
        if (sectionCurrentImageTrue != null)
        {
          try
          {
            sectionCurrentImageTrue.flush();
          }
          catch (Throwable t)
          {
            
          }
        }
        sectionCurrentImageTrue = VTImageIO.createImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_INT_RGB, 0, recyclableSectionDataBufferTrue);
        recyclableSectionDataBufferTrue = sectionCurrentImageTrue.getRaster().getDataBuffer();
      }
      if (directCaptureRobot.getRGBPixels(captureArea.x, captureArea.y, captureArea.width, captureArea.height, ((DataBufferInt) recyclableSectionDataBufferTrue).getData()))
      {
        screenCapture = sectionCurrentImageTrue;
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
    if (grayscale)
    {
      if (sectionCurrentImageGrayscale == null
      || sectionCurrentImageGrayscale.getWidth() != captureArea.width
      || sectionCurrentImageGrayscale.getHeight() != captureArea.height)
      //|| sectionCurrentImageGrayscaleColorCount != getColorCount())
      {
        if (sectionCurrentImageGrayscale != null)
        {
          try
          {
            sectionCurrentImageGrayscale.flush();
          }
          catch (Throwable t)
          {
            
          }
        }
        //sectionCurrentImageGrayscaleColorCount = getColorCount();
        //sectionCurrentImageGrayscale = VTImageIO.createImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_CUSTOM, sectionCurrentImageGrayscaleColorCount, recyclableSectionDataBufferGrayscale);
        //sectionCurrentImageGrayscale = VTImageIO.createImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_BYTE_INDEXED, sectionCurrentImageGrayscaleColorCount, recyclableSectionDataBufferGrayscale);
        sectionCurrentImageGrayscale = VTImageIO.createImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_BYTE_GRAY, 256, recyclableSectionDataBufferGrayscale);
        //sectionCurrentImageGrayscale = VTImageIO.createImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_USHORT_GRAY, 65536, recyclableSectionDataBufferGrayscale);
        recyclableSectionDataBufferGrayscale = sectionCurrentImageGrayscale.getRaster().getDataBuffer();
        sectionCurrentImageGrayscaleGraphics = sectionCurrentImageGrayscale.createGraphics();
        sectionCurrentImageGrayscaleGraphics.setRenderingHints(VTSystem.VT_GRAPHICS_RENDERING_HINTS);
      }
      if (screenCapture == sectionCurrentImageTrue)
      {
        sectionCurrentImageGrayscaleGraphics.drawImage(sectionCurrentImageTrue, 0, 0, null);
        screenCapture = sectionCurrentImageGrayscale;
      }
      else
      {
        sectionCurrentImageGrayscaleGraphics.drawImage(screenCapture, 0, 0, null);
        screenCapture.flush();
        screenCapture = sectionCurrentImageGrayscale;
      }
    }
    else
    {
      if (sectionCurrentImageGrayscale != null)
      {
        try
        {
          sectionCurrentImageGrayscale.flush();
        }
        catch (Throwable t)
        {
          
        }
        sectionCurrentImageGrayscale = null;
      }
      if (sectionCurrentImageGrayscaleGraphics != null)
      {
        try
        {
          sectionCurrentImageGrayscaleGraphics.dispose();
        }
        catch (Throwable t)
        {
          
        }
        sectionCurrentImageGrayscaleGraphics = null;
      }
    }
    return screenCapture;
  }
  
  @SuppressWarnings("unused")
  private static final BufferedImage createSectionCurrentImageConverted(final int colors, final int width, final int height, final DataBuffer recyclableBuffer)
  {
    switch (colors)
    {
    case 4:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    case 8:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    case 16:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    case 27:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    //case VT_COLOR_QUALITY_32:
      //return 32;
    case 64:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    case 125:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    case 216:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_BYTE_INDEXED, colors, recyclableBuffer);
    case 512:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_CUSTOM, colors, recyclableBuffer);
    case 4096:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_CUSTOM, colors, recyclableBuffer);
    case 32768:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_USHORT_555_RGB, colors, recyclableBuffer);
    case 262144:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_CUSTOM, colors, recyclableBuffer);
    case 2097152:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_CUSTOM, colors, recyclableBuffer);
    case 16777216:
      return VTImageIO.createImage(0, 0, width, height, BufferedImage.TYPE_INT_RGB, colors, recyclableBuffer);
    //case VT_COLOR_QUALITY_134217728:
      //return 134217728;
    //case VT_COLOR_QUALITY_1073741824:
      //return 1073741824;
    default:
      return null;
    }
  }
  
  public final BufferedImage createScreenCapture(final int padding, final boolean drawPointer)
  {
    if (!isScreenCaptureInitialized(padding))
    {
      if (!initializeScreenCapture(padding))
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
    //else if (colorQuality == VT_COLOR_QUALITY_32)
    //{
      //return create32ScreenCapture(drawPointer);
    //}
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return create512ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return create4096ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4)
    {
      return create4ScreenCapture(drawPointer);
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
    else if (colorQuality == VT_COLOR_QUALITY_262144)
    {
      return create262144ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_2097152)
    {
      return create2097152ScreenCapture(drawPointer);
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return create64ScreenCapture(drawPointer);
    }
    return null;
  }
  
  public final BufferedImage createScreenCapture(final int padding, final boolean drawPointer, final Rectangle originalArea)
  {
    if (!isScreenCaptureInitialized(padding))
    {
      if (!initializeScreenCapture(padding))
      {
        return null;
      }
    }
    if (colorQuality == VT_COLOR_QUALITY_16777216)
    {
      return create16777216ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_32768)
    {
      return create32768ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_216)
    {
      return create216ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_16)
    {
      return create16ScreenCapture(drawPointer, originalArea);
    }
    //else if (colorQuality == VT_COLOR_QUALITY_32)
    //{
      //return create32ScreenCapture(drawPointer, area);
    //}
    else if (colorQuality == VT_COLOR_QUALITY_512)
    {
      return create512ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4096)
    {
      return create4096ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_4)
    {
      return create4ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_8)
    {
      return create8ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_125)
    {
      return create125ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_27)
    {
      return create27ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_262144)
    {
      return create262144ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_2097152)
    {
      return create2097152ScreenCapture(drawPointer, originalArea);
    }
    else if (colorQuality == VT_COLOR_QUALITY_64)
    {
      return create64ScreenCapture(drawPointer, originalArea);
    }
    return null;
  }
  
  public final BufferedImage createScreenCapture(final int padding, final boolean drawPointer, final VTRectangle originalArea)
  {
    return createScreenCapture(padding, drawPointer, new Rectangle(originalArea.x, originalArea.y, originalArea.width, originalArea.height));
  }
  
  private static final void drawPointer(final BufferedImage image, final Rectangle area, final GraphicsDevice graphicsDevice, final int drawnCursorSize)
  {
    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
    if (pointerInfo == null)
    {
      return;
    }
    GraphicsDevice pointerDevice = pointerInfo.getDevice();
    // DisplayMode displayMode = infoDevice.getDisplayMode();
    Point pointerLocation = pointerInfo.getLocation();
    Rectangle deviceBounds = new Rectangle();
    if (pointerDevice == null)
    {
      // VTTerminal.println("infoDevice = null");
      return;
    }
    else
    {
      if (pointerLocation == null)
      {
        return;
      }
      try
      {
        if (graphicsDevice != null)
        {
          if (!pointerDevice.getIDstring().equals(graphicsDevice.getIDstring()))
          {
            // out of current screen
            return;
          }
          deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(pointerDevice);
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
      invertPixel(image, area, x + 2, y);
      invertPixel(image, area, x + 3, y);
      invertPixel(image, area, x + 4, y);
      invertPixel(image, area, x - 2, y);
      invertPixel(image, area, x - 3, y);
      invertPixel(image, area, x - 4, y);
      invertPixel(image, area, x, y + 2);
      invertPixel(image, area, x, y + 3);
      invertPixel(image, area, x, y + 4);
      invertPixel(image, area, x, y - 2);
      invertPixel(image, area, x, y - 3);
      invertPixel(image, area, x, y - 4);
      
      invertPixel(image, area, x + 2, y + 1);
      invertPixel(image, area, x + 3, y + 1);
      invertPixel(image, area, x + 4, y + 1);
      invertPixel(image, area, x + 2, y - 1);
      invertPixel(image, area, x + 3, y - 1);
      invertPixel(image, area, x + 4, y - 1);
      invertPixel(image, area, x - 2, y + 1);
      invertPixel(image, area, x - 3, y + 1);
      invertPixel(image, area, x - 4, y + 1);
      invertPixel(image, area, x - 2, y - 1);
      invertPixel(image, area, x - 3, y - 1);
      invertPixel(image, area, x - 4, y - 1);
      invertPixel(image, area, x + 1, y + 2);
      invertPixel(image, area, x + 1, y + 3);
      invertPixel(image, area, x + 1, y + 4);
      invertPixel(image, area, x + 1, y - 2);
      invertPixel(image, area, x + 1, y - 3);
      invertPixel(image, area, x + 1, y - 4);
      invertPixel(image, area, x - 1, y + 2);
      invertPixel(image, area, x - 1, y + 3);
      invertPixel(image, area, x - 1, y + 4);
      invertPixel(image, area, x - 1, y - 2);
      invertPixel(image, area, x - 1, y - 3);
      invertPixel(image, area, x - 1, y - 4);
      
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
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y + m);
      }
      // Second quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y + 2;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y + m);
      }
      // Third quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y - 2;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y - m);
      }
      // Fourth quadrant
      x = pointerLocation.x - 2;
      y = pointerLocation.y - 2;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y - m);
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
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y + m);
      }
      // Second quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y + 3;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y + m);
      }
      // Third quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y - 3;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y - m);
      }
      // Fourth quadrant
      x = pointerLocation.x - 3;
      y = pointerLocation.y - 3;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y - m);
      }
      
      // n = n / 2;
      n = n - 1;
      // First quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y + 4;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y + m);
      }
      // Second quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y + 4;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y + m);
      }
      // Third quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y - 4;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y - m);
      }
      // Fourth quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y - 4;
      invertPixel(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixel(image, area, x, y - m);
      }
      
      // First quadrant
      x = pointerLocation.x - (3 + n);
      y = pointerLocation.y + (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixel(image, area, x + m, y - m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x, y - m);
        invertPixel(image, area, x + m, y);
        invertPixel(image, area, x + 1, y - m);
        invertPixel(image, area, x + m, y - 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x + 2, y - m);
        invertPixel(image, area, x + m, y - 2);
      }
      // Second quadrant
      x = pointerLocation.x + (3 + n);
      y = pointerLocation.y + (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixel(image, area, x - m, y - m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x, y - m);
        invertPixel(image, area, x - m, y);
        invertPixel(image, area, x - 1, y - m);
        invertPixel(image, area, x - m, y - 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x - 2, y - m);
        invertPixel(image, area, x - m, y - 2);
      }
      // Third quadrant
      x = pointerLocation.x + (3 + n);
      y = pointerLocation.y - (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixel(image, area, x - m, y + m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x, y + m);
        invertPixel(image, area, x - m, y);
        invertPixel(image, area, x - 1, y + m);
        invertPixel(image, area, x - m, y + 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x - 2, y + m);
        invertPixel(image, area, x - m, y + 2);
      }
      // Fourth quadrant
      x = pointerLocation.x - (3 + n);
      y = pointerLocation.y - (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixel(image, area, x + m, y + m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x, y + m);
        invertPixel(image, area, x + m, y);
        invertPixel(image, area, x + 1, y + m);
        invertPixel(image, area, x + m, y + 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixel(image, area, x + 2, y + m);
        invertPixel(image, area, x + m, y + 2);
      }
      
      int t = 1;
      int l = (n - 10) / 8;
      // System.out.println("l:" + l);
      for (t = 1; t < l; t++)
      {
        n = n - 1;
        // center
        x = pointerLocation.x;
        y = pointerLocation.y;
        
        invertPixel(image, area, x - 4 - t, y);
        invertPixel(image, area, x - 4 - t, y + 1);
        invertPixel(image, area, x - 4 - t, y - 1);
        invertPixel(image, area, x + 4 + t, y);
        invertPixel(image, area, x + 4 + t, y + 1);
        invertPixel(image, area, x + 4 + t, y - 1);
        invertPixel(image, area, x, y - 4 - t);
        invertPixel(image, area, x + 1, y - 4 - t);
        invertPixel(image, area, x - 1, y - 4 - t);
        invertPixel(image, area, x, y + 4 + t);
        invertPixel(image, area, x + 1, y + 4 + t);
        invertPixel(image, area, x - 1, y + 4 + t);
        
        // First quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y + 4 + t;
        invertPixel(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x - m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x, y + m);
        }
        // Second quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y + 4 + t;
        invertPixel(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x + m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x, y + m);
        }
        // Third quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y - 4 - t;
        invertPixel(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x + m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x, y - m);
        }
        // Fourth quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y - 4 - t;
        invertPixel(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x - m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixel(image, area, x, y - m);
        }
        // First quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y + (1 + n);
        invertPixel(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixel(image, area, x, y - m);
          invertPixel(image, area, x + m, y);
        }
        // Second quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y + (1 + n);
        invertPixel(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixel(image, area, x, y - m);
          invertPixel(image, area, x - m, y);
        }
        // Third quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y - (1 + n);
        invertPixel(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixel(image, area, x, y + m);
          invertPixel(image, area, x - m, y);
        }
        // Fourth quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y - (1 + n);
        invertPixel(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixel(image, area, x, y + m);
          invertPixel(image, area, x + m, y);
        }
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  private static final void drawPointerFilterGray(final BufferedImage image, final Rectangle area, final GraphicsDevice graphicsDevice, final int drawnCursorSize)
  {
    PointerInfo pointerInfo = MouseInfo.getPointerInfo();
    if (pointerInfo == null)
    {
      return;
    }
    GraphicsDevice pointerDevice = pointerInfo.getDevice();
    // DisplayMode displayMode = infoDevice.getDisplayMode();
    Point pointerLocation = pointerInfo.getLocation();
    Rectangle deviceBounds = new Rectangle();
    if (pointerDevice == null)
    {
      // VTTerminal.println("infoDevice = null");
      return;
    }
    else
    {
      if (pointerLocation == null)
      {
        return;
      }
      try
      {
        if (graphicsDevice != null)
        {
          if (!pointerDevice.getIDstring().equals(graphicsDevice.getIDstring()))
          {
            // out of current screen
            return;
          }
          deviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(pointerDevice);
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
      invertPixelFilterGray(image, area, x + 2, y);
      invertPixelFilterGray(image, area, x + 3, y);
      invertPixelFilterGray(image, area, x + 4, y);
      invertPixelFilterGray(image, area, x - 2, y);
      invertPixelFilterGray(image, area, x - 3, y);
      invertPixelFilterGray(image, area, x - 4, y);
      invertPixelFilterGray(image, area, x, y + 2);
      invertPixelFilterGray(image, area, x, y + 3);
      invertPixelFilterGray(image, area, x, y + 4);
      invertPixelFilterGray(image, area, x, y - 2);
      invertPixelFilterGray(image, area, x, y - 3);
      invertPixelFilterGray(image, area, x, y - 4);
      
      invertPixelFilterGray(image, area, x + 2, y + 1);
      invertPixelFilterGray(image, area, x + 3, y + 1);
      invertPixelFilterGray(image, area, x + 4, y + 1);
      invertPixelFilterGray(image, area, x + 2, y - 1);
      invertPixelFilterGray(image, area, x + 3, y - 1);
      invertPixelFilterGray(image, area, x + 4, y - 1);
      invertPixelFilterGray(image, area, x - 2, y + 1);
      invertPixelFilterGray(image, area, x - 3, y + 1);
      invertPixelFilterGray(image, area, x - 4, y + 1);
      invertPixelFilterGray(image, area, x - 2, y - 1);
      invertPixelFilterGray(image, area, x - 3, y - 1);
      invertPixelFilterGray(image, area, x - 4, y - 1);
      invertPixelFilterGray(image, area, x + 1, y + 2);
      invertPixelFilterGray(image, area, x + 1, y + 3);
      invertPixelFilterGray(image, area, x + 1, y + 4);
      invertPixelFilterGray(image, area, x + 1, y - 2);
      invertPixelFilterGray(image, area, x + 1, y - 3);
      invertPixelFilterGray(image, area, x + 1, y - 4);
      invertPixelFilterGray(image, area, x - 1, y + 2);
      invertPixelFilterGray(image, area, x - 1, y + 3);
      invertPixelFilterGray(image, area, x - 1, y + 4);
      invertPixelFilterGray(image, area, x - 1, y - 2);
      invertPixelFilterGray(image, area, x - 1, y - 3);
      invertPixelFilterGray(image, area, x - 1, y - 4);
      
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
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
      }
      // Second quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y + 2;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
      }
      // Third quadrant
      x = pointerLocation.x + 2;
      y = pointerLocation.y - 2;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
      }
      // Fourth quadrant
      x = pointerLocation.x - 2;
      y = pointerLocation.y - 2;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
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
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
      }
      // Second quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y + 3;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
      }
      // Third quadrant
      x = pointerLocation.x + 3;
      y = pointerLocation.y - 3;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
      }
      // Fourth quadrant
      x = pointerLocation.x - 3;
      y = pointerLocation.y - 3;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
      }
      
      // n = n / 2;
      n = n - 1;
      // First quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y + 4;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
      }
      // Second quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y + 4;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
      }
      // Third quadrant
      x = pointerLocation.x + 4;
      y = pointerLocation.y - 4;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x + m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
      }
      // Fourth quadrant
      x = pointerLocation.x - 4;
      y = pointerLocation.y - 4;
      invertPixelFilterGray(image, area, x, y);
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x - m, y);
      }
      for (m = 1; (m < n); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
      }
      
      // First quadrant
      x = pointerLocation.x - (3 + n);
      y = pointerLocation.y + (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixelFilterGray(image, area, x + m, y - m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
        invertPixelFilterGray(image, area, x + m, y);
        invertPixelFilterGray(image, area, x + 1, y - m);
        invertPixelFilterGray(image, area, x + m, y - 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x + 2, y - m);
        invertPixelFilterGray(image, area, x + m, y - 2);
      }
      // Second quadrant
      x = pointerLocation.x + (3 + n);
      y = pointerLocation.y + (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixelFilterGray(image, area, x - m, y - m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x, y - m);
        invertPixelFilterGray(image, area, x - m, y);
        invertPixelFilterGray(image, area, x - 1, y - m);
        invertPixelFilterGray(image, area, x - m, y - 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x - 2, y - m);
        invertPixelFilterGray(image, area, x - m, y - 2);
      }
      // Third quadrant
      x = pointerLocation.x + (3 + n);
      y = pointerLocation.y - (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixelFilterGray(image, area, x - m, y + m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
        invertPixelFilterGray(image, area, x - m, y);
        invertPixelFilterGray(image, area, x - 1, y + m);
        invertPixelFilterGray(image, area, x - m, y + 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x - 2, y + m);
        invertPixelFilterGray(image, area, x - m, y + 2);
      }
      // Fourth quadrant
      x = pointerLocation.x - (3 + n);
      y = pointerLocation.y - (3 + n);
      for (m = 0; (m < 3); m++)
      {
        invertPixelFilterGray(image, area, x + m, y + m);
      }
      for (m = 1; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x, y + m);
        invertPixelFilterGray(image, area, x + m, y);
        invertPixelFilterGray(image, area, x + 1, y + m);
        invertPixelFilterGray(image, area, x + m, y + 1);
      }
      for (m = 3; (m < (n / 2) + 1); m++)
      {
        invertPixelFilterGray(image, area, x + 2, y + m);
        invertPixelFilterGray(image, area, x + m, y + 2);
      }
      
      int t = 1;
      int l = (n - 10) / 8;
      // System.out.println("l:" + l);
      for (t = 1; t < l; t++)
      {
        n = n - 1;
        // center
        x = pointerLocation.x;
        y = pointerLocation.y;
        
        invertPixelFilterGray(image, area, x - 4 - t, y);
        invertPixelFilterGray(image, area, x - 4 - t, y + 1);
        invertPixelFilterGray(image, area, x - 4 - t, y - 1);
        invertPixelFilterGray(image, area, x + 4 + t, y);
        invertPixelFilterGray(image, area, x + 4 + t, y + 1);
        invertPixelFilterGray(image, area, x + 4 + t, y - 1);
        invertPixelFilterGray(image, area, x, y - 4 - t);
        invertPixelFilterGray(image, area, x + 1, y - 4 - t);
        invertPixelFilterGray(image, area, x - 1, y - 4 - t);
        invertPixelFilterGray(image, area, x, y + 4 + t);
        invertPixelFilterGray(image, area, x + 1, y + 4 + t);
        invertPixelFilterGray(image, area, x - 1, y + 4 + t);
        
        // First quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y + 4 + t;
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x - m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x, y + m);
        }
        // Second quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y + 4 + t;
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x + m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x, y + m);
        }
        // Third quadrant
        x = pointerLocation.x + 4 + t;
        y = pointerLocation.y - 4 - t;
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x + m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x, y - m);
        }
        // Fourth quadrant
        x = pointerLocation.x - 4 - t;
        y = pointerLocation.y - 4 - t;
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x - m, y);
        }
        for (m = 1; (m < n); m++)
        {
          invertPixelFilterGray(image, area, x, y - m);
        }
        // First quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y + (1 + n);
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixelFilterGray(image, area, x, y - m);
          invertPixelFilterGray(image, area, x + m, y);
        }
        // Second quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y + (1 + n);
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixelFilterGray(image, area, x, y - m);
          invertPixelFilterGray(image, area, x - m, y);
        }
        // Third quadrant
        x = pointerLocation.x + (1 + n);
        y = pointerLocation.y - (1 + n);
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixelFilterGray(image, area, x, y + m);
          invertPixelFilterGray(image, area, x - m, y);
        }
        // Fourth quadrant
        x = pointerLocation.x - (1 + n);
        y = pointerLocation.y - (1 + n);
        invertPixelFilterGray(image, area, x, y);
        for (m = 1; m < (((n - 1 - t) / 2)); m++)
        {
          invertPixelFilterGray(image, area, x, y + m);
          invertPixelFilterGray(image, area, x + m, y);
        }
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  private static final void invertPixel(final BufferedImage image, final Rectangle area, final int x, final int y)
  {
    if (area.contains(x, y))
    {
      image.setRGB(x, y, (image.getRGB(x, y) ^ RGB888_XOR_MASK));
    }
  }
  
  private static final void invertPixelFilterGray(final BufferedImage image, final Rectangle area, final int x, final int y)
  {
    if (area.contains(x, y))
    {
      image.setRGB(x, y, filterGray(image.getRGB(x, y) ^ RGB888_XOR_MASK));
    }
  }
  
  private static final int filterGray(final int rgb)
  {
    if (((rgb & RGB888_XOR_MASK) == 0x00808080) || ((rgb & RGB888_XOR_MASK) == 0x007F7F7F))
    {
      //return rgb & 0xFF000000;
      return 0x00FFFFFF;
      // return 0;
    }
    return rgb;
  }
  
  private final Rectangle calculateFullArea()
  {
    int maxWidth = screenCurrentWidth;
    int maxHeight = screenCurrentHeight;
    if (scaledCurrentWidth > 0 || scaledCurrentHeight > 0)
    {
      maxWidth = scaledCurrentWidth;
      maxHeight = scaledCurrentHeight;
    }
    return new Rectangle(0, 0, maxWidth, maxHeight);
  }
  
  private final Rectangle calculateCaptureArea(final Rectangle originalArea)
  {
    int maxWidth = screenCurrentWidth;
    int maxHeight = screenCurrentHeight;
    
    if (scaledCurrentWidth > 0 || scaledCurrentHeight > 0)
    {
      maxWidth = scaledCurrentWidth;
      maxHeight = scaledCurrentHeight;
    }
    if (originalArea.width > maxWidth)
    {
      originalArea.width = maxWidth;
    }
    if (originalArea.height > maxHeight)
    {
      originalArea.height = maxHeight;
    }
    if (originalArea.x > maxWidth - originalArea.width)
    {
      originalArea.x = maxWidth - originalArea.width;
    }
    if (originalArea.y > maxHeight - originalArea.height)
    {
      originalArea.y = maxHeight - originalArea.height;
    }
    
    Rectangle screenArea = new Rectangle();
    screenArea.x = (int) Math.floor(originalArea.x / getScaleFactorX());
    screenArea.y = (int) Math.floor(originalArea.y / getScaleFactorY());
    screenArea.width = (int) Math.ceil(originalArea.width / getScaleFactorX());
    screenArea.height = (int) Math.ceil(originalArea.height / getScaleFactorY());
    
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
    return captureArea;
  }
  
//  private boolean mustFilterGray(int rgb)
//  {
//    int red = (rgb & RGB888_RED_MASK >> 16);
//    int green = (rgb & RGB888_GREEN_MASK >> 8);
//    int blue = (rgb & RGB888_BLUE_MASK);
//    if (((red & green & blue) == (red | green | blue)))
//    {
//      return Math.abs(red - (red ^ 0xFF)) < 51;
//      return Math.abs(red - (red ^ 0xFF)) < 38;
//      //return Math.abs(red - (red ^ 0xFF)) < 19;
//    }
//    return false;
//  }
  
}