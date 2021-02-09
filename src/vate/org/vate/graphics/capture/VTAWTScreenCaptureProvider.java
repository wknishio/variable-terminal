package org.vate.graphics.capture;

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

//import javax.swing.JLabel;
//import javax.swing.UIManager;

import org.vate.VT;
import org.vate.graphics.device.VTGraphicalDeviceResolver;
import org.vate.graphics.font.VTGlobalTextStyleManager;
import org.vate.graphics.image.VTImageIO;
import org.vate.graphics.image.VTIndexedColorModel;

public final class VTAWTScreenCaptureProvider
{
	public static final int VT_COLOR_QUALITY_64 = 0; // 64 rgb222, normal
	public static final int VT_COLOR_QUALITY_216 = 1; // 216 6x6x6, medium
	public static final int VT_COLOR_QUALITY_32768 = 2; // 32768 rgb555, high
	public static final int VT_COLOR_QUALITY_16777216 = 3; // 16777216 rgb888, best
	public static final int VT_COLOR_QUALITY_16 = 4; // 16 rgbi, low
	public static final int VT_COLOR_QUALITY_32 = 5; // 32 rgbii, simple
	public static final int VT_COLOR_QUALITY_512 = 6; // 512 rgb333, good
	public static final int VT_COLOR_QUALITY_4096 = 7; // 4096 rgb444, extra
	public static final int VT_COLOR_QUALITY_8 = 8; // 8 rgb111, worst
	public static final int VT_COLOR_QUALITY_125 = 9; // 125 5x5x5, below
	
	//best
	//high
	//extra
	//good
	//medium
	//normal
	//simple
	//low
	//worst
	
	private static final int RGB888_RED_MASK = 0x00ff0000;
	private static final int RGB888_GREEN_MASK = 0x0000ff00;
	private static final int RGB888_BLUE_MASK = 0x000000ff;
	
	private static final int RGB555_RED_MASK = 0x00f80000;
	private static final int RGB555_GREEN_MASK = 0x0000f800;
	private static final int RGB555_BLUE_MASK = 0x000000f8;
	
	private static final int RGB555_444_RED_MASK = 0x00f00000;
	private static final int RGB555_444_GREEN_MASK = 0x0000f000;
	private static final int RGB555_444_BLUE_MASK = 0x000000f0;
	
	private static final int RGB555_444_RED_ADD = RGB555_RED_MASK - RGB555_444_RED_MASK;
	private static final int RGB555_444_GREEN_ADD = RGB555_GREEN_MASK - RGB555_444_GREEN_MASK;
	private static final int RGB555_444_BLUE_ADD = RGB555_BLUE_MASK - RGB555_444_BLUE_MASK;
	
	private static final int RGB555_333_RED_MASK = 0x00e00000;
	private static final int RGB555_333_GREEN_MASK = 0x0000e000;
	private static final int RGB555_333_BLUE_MASK = 0x000000e0;
	
	private static final int RGB555_333_RED_ADD = RGB555_RED_MASK - RGB555_333_RED_MASK;
	private static final int RGB555_333_GREEN_ADD = RGB555_GREEN_MASK - RGB555_333_GREEN_MASK;
	private static final int RGB555_333_BLUE_ADD = RGB555_BLUE_MASK - RGB555_333_BLUE_MASK;
	
	private static final int RGB222_RED_MASK = 0x00c00000;
	private static final int RGB222_GREEN_MASK = 0x0000c000;
	private static final int RGB222_BLUE_MASK = 0x000000c0;
	
	private volatile int colorQuality;
	private volatile boolean lowQualityScreenCaptureInitialized;
	private volatile boolean simpleQualityScreenCaptureInitialized;
	private volatile boolean goodQualityScreenCaptureInitialized;
	private volatile boolean extraQualityScreenCaptureInitialized;
	private volatile boolean worstQualityScreenCaptureInitialized;
	private volatile boolean normalQualityScreenCaptureInitialized;
	private volatile boolean mediumQualityScreenCaptureInitialized;
	private volatile boolean highQualityScreenCaptureInitialized;
	private volatile boolean bestQualityScreenCaptureInitialized;
	private volatile boolean reducedQualityScreenCaptureInitialized;
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
	//private int[] pixelBlock = new int[64 * 64];
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
	//private volatile Graphics2D sectionGraphics;
	private volatile GraphicsDevice graphicsDevice;
	private volatile Robot standardCaptureRobot;
	private volatile VTDirectRobot directCaptureRobot;
	//private Toolkit toolkit;
	private VTARGBPixelGrabber grabber;
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
		this.grabber = new VTARGBPixelGrabber();
		//toolkit = Toolkit.getDefaultToolkit();
		try
		{
			this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		}
		catch (Throwable e)
		{
			// e.printStackTrace(VTTerminal.getSystemOut());
		}
		//int dpi = toolkit.getScreenResolution();
		VTGlobalTextStyleManager.checkScaling();
		int dpi = VTGlobalTextStyleManager.BASE_FONT_DPI;
		drawnCursorSize = Math.max(32, dpi / 3);
		initialDrawnCursorSize = roundUp(drawnCursorSize, 8);
		/* try { bestCursorSize =
		 * Toolkit.getDefaultToolkit().getBestCursorSize(32, 32); } catch
		 * (Throwable e) {
		 * } */
		// System.out.println("bestCursorSize: x: " + bestCursorSize.width + ",
		// y: " +
		// bestCursorSize.height);
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
			// e.printStackTrace(VTTerminal.getSystemOut());
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
	
	private final boolean initializeLowQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshWorstSettings();
			}
			lowQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			lowQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeSimpleQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshSmallSettings();
			}
			simpleQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			simpleQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeWorstQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshSimpleSettings();
			}
			worstQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			worstQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeGoodQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshGoodSettings();
			}
			goodQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			goodQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeExtraQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshExtraSettings();
			}
			extraQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			extraQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeNormalQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshLowSettings();
			}
			normalQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			normalQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeMediumQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshWebSettings();
			}
			mediumQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			mediumQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeHighQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshMediumSettings();
			}
			highQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			highQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeBestQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshHighSettings();
			}
			bestQualityScreenCaptureInitialized = true;
			// disposeNormalQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			bestQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	private final boolean initializeReducedQualityScreenCapture(GraphicsDevice device)
	{
		reset();
		if (GraphicsEnvironment.isHeadless())
		{
			return false;
		}
		try
		{
			if (standardCaptureRobot == null)
			{
				if (device != null)
				{
					standardCaptureRobot = new Robot(device);
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
							if (screen.getDefaultConfiguration().getBounds().x < topleft.getDefaultConfiguration().getBounds().x || screen.getDefaultConfiguration().getBounds().y < topleft.getDefaultConfiguration().getBounds().y)
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
						standardCaptureRobot = new Robot(topleft);
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
						standardCaptureRobot = new Robot();
						try
						{
							directCaptureRobot = new VTDirectRobot();
						}
						catch (Throwable t)
						{
							
						}
					}
				}
				standardCaptureRobot.setAutoDelay(0);
				standardCaptureRobot.setAutoWaitForIdle(false);
			}
			if (changedCurrentSettings())
			{
				refreshReducedSettings();
			}
			reducedQualityScreenCaptureInitialized = true;
			// disposeBestQualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			reducedQualityScreenCaptureInitialized = false;
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable ex)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
			return false;
		}
	}
	
	public final synchronized boolean initializeScreenCapture()
	{
		if (colorQuality == VT_COLOR_QUALITY_16777216)
		{
			return initializeBestQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return initializeHighQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return initializeMediumQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return initializeLowQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return initializeSimpleQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return initializeGoodQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return initializeExtraQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return initializeWorstQualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return initializeReducedQualityScreenCapture(graphicsDevice);
		}
		else
		{
			return initializeNormalQualityScreenCapture(graphicsDevice);
		}
	}
	
	public final synchronized boolean initializeScreenCapture(GraphicsDevice device)
	{
		if (colorQuality == VT_COLOR_QUALITY_16777216)
		{
			return initializeBestQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return initializeHighQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return initializeMediumQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return initializeLowQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return initializeSimpleQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return initializeGoodQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return initializeExtraQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return initializeWorstQualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return initializeReducedQualityScreenCapture(device);
		}
		else
		{
			return initializeNormalQualityScreenCapture(device);
		}
	}
	
	private final boolean isSimpleQualityScreenCaptureInitialized()
	{
		return simpleQualityScreenCaptureInitialized;
	}
	
	private final boolean isGoodQualityScreenCaptureInitialized()
	{
		return goodQualityScreenCaptureInitialized;
	}
	
	private final boolean isExtraQualityScreenCaptureInitialized()
	{
		return extraQualityScreenCaptureInitialized;
	}
	
	private final boolean isWorstQualityScreenCaptureInitialized()
	{
		return worstQualityScreenCaptureInitialized;
	}
	
	private final boolean isLowQualityScreenCaptureInitialized()
	{
		return lowQualityScreenCaptureInitialized;
	}
	
	private final boolean isNormalQualityScreenCaptureInitialized()
	{
		return normalQualityScreenCaptureInitialized;
	}
	
	private final boolean isReducedQualityScreenCaptureInitialized()
	{
		return reducedQualityScreenCaptureInitialized;
	}
	
	private final boolean isMediumQualityScreenCaptureInitialized()
	{
		return mediumQualityScreenCaptureInitialized;
	}
	
	private final boolean isHighQualityScreenCaptureInitialized()
	{
		return highQualityScreenCaptureInitialized;
	}
	
	private final boolean isBestQualityScreenCaptureInitialized()
	{
		return bestQualityScreenCaptureInitialized;
	}
	
	public final synchronized boolean isScreenCaptureInitialized()
	{
		if (colorQuality == VT_COLOR_QUALITY_16777216)
		{
			return isBestQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return isHighQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return isMediumQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return isLowQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return isSimpleQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return isGoodQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return isExtraQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return isWorstQualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return isReducedQualityScreenCaptureInitialized();
		}
		else
		{
			return isNormalQualityScreenCaptureInitialized();
		}
	}
	
	public final synchronized void reset()
	{
		disposeScreenCaptureResources();
		/* try { this.graphicsDevice =
		 * GraphicsEnvironment.getLocalGraphicsEnvironment().
		 * getDefaultScreenDevice(); } catch (Throwable e) {
		 * //e.printStackTrace(VTTerminal.getSystemOut()); } */
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
		if (!GraphicsEnvironment.isHeadless())
		{
			try
			{
				this.graphicsDevice = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
			}
			catch (Throwable e)
			{
				// e.printStackTrace(VTTerminal.getSystemOut());
			}
		}
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
		// normalQualityColorModel = null;
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
		lowQualityScreenCaptureInitialized = false;
		simpleQualityScreenCaptureInitialized = false;
		goodQualityScreenCaptureInitialized = false;
		extraQualityScreenCaptureInitialized = false;
		worstQualityScreenCaptureInitialized = false;
		normalQualityScreenCaptureInitialized = false;
		mediumQualityScreenCaptureInitialized = false;
		highQualityScreenCaptureInitialized = false;
		bestQualityScreenCaptureInitialized = false;
		reducedQualityScreenCaptureInitialized = false;
	}
	
	public final void clearResources()
	{
		lowQualityScreenCaptureInitialized = false;
		simpleQualityScreenCaptureInitialized = false;
		goodQualityScreenCaptureInitialized = false;
		extraQualityScreenCaptureInitialized = false;
		worstQualityScreenCaptureInitialized = false;
		normalQualityScreenCaptureInitialized = false;
		mediumQualityScreenCaptureInitialized = false;
		highQualityScreenCaptureInitialized = false;
		bestQualityScreenCaptureInitialized = false;
		reducedQualityScreenCaptureInitialized = false;
	}
	
	private final boolean changedCurrentSettings()
	{
		currentDeviceBounds = VTGraphicalDeviceResolver.getDeviceBounds(graphicsDevice);
		if (currentDeviceBounds == null)
		{
			return true;
		}
		return (currentDeviceBounds.width != screenCurrentWidth || currentDeviceBounds.height != screenCurrentHeight || currentDeviceBounds.x != screenCurrentX || currentDeviceBounds.y != screenCurrentY) || changedScaledCurrentSettings();
	}
	
	private final boolean changedScaledCurrentSettings()
	{
		calculateScaledDimensions();
		return scaledWidth != scaledCurrentWidth || scaledHeight != scaledCurrentHeight;
	}
	
	private final void refreshWorstSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 16, recyclableScreenDataBuffer);
		//screenCurrentImage = VTImageIO.newImage(screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledWorstSettings();
	}
	
	private final void refreshSmallSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 32, recyclableScreenDataBuffer);
		//screenCurrentImage = VTImageIO.newImage(screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledSmallSettings();
	}
	
	private final void refreshScaledSmallSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 32, recyclableScaledDataBuffer);
			//scaledCurrentImage = VTImageIO.newImage(scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16, recyclableScaledDataBuffer);
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
	
	private final void refreshSimpleSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 8, recyclableScreenDataBuffer);
		//screenCurrentImage = VTImageIO.newImage(screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledSimpleSettings();
	}
	
	private final void refreshScaledSimpleSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 8, recyclableScaledDataBuffer);
			//scaledCurrentImage = VTImageIO.newImage(scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16, recyclableScaledDataBuffer);
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
	
	private final void refreshScaledWorstSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 16, recyclableScaledDataBuffer);
			//scaledCurrentImage = VTImageIO.newImage(scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_BINARY, 16, recyclableScaledDataBuffer);
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
	
	private final void refreshLowSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 64, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledLowSettings();
	}
	
	private final void refreshScaledLowSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 64, recyclableScaledDataBuffer);
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
	
	private final void refreshReducedSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 125, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledReducedSettings();
	}
	
	private final void refreshScaledReducedSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 125, recyclableScaledDataBuffer);
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
	
	private final void refreshWebSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 216, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledWebSettings();
	}
	
	private final void refreshScaledWebSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 216, recyclableScaledDataBuffer);
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
	
	private final void refreshGoodSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 512, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledGoodSettings();
	}
	
	private final void refreshScaledGoodSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 512, recyclableScaledDataBuffer);
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
	
	private final void refreshExtraSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 4096, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaleExtraSettings();
	}
	
	private final void refreshScaleExtraSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 4096, recyclableScaledDataBuffer);
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
	
	private final void refreshMediumSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 0, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledMediumSettings();
	}
	
	private final void refreshScaledMediumSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_USHORT_555_RGB, 0, recyclableScaledDataBuffer);
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
	
	
	
	private final void refreshHighSettings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_INT_RGB, 0, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaledHighSettings();
	}
	
	private final void refreshScaledHighSettings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_INT_RGB, 0, recyclableScaledDataBuffer);
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
	
	private final BufferedImage createSimpleQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshSmallSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		// System.runFinalization();
		// System.gc();
		byte rgbiValue = 0;
		pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			//red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
			//pixelBufferByte[i] = (byte) (red + green + blue);
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
	
	private final BufferedImage createSimpleQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshSmallSettings();
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
		//BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			
			//red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
			//pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
			Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
			scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
			scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
			scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
			scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
			//while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
			//{
				//Thread.yield();
			//}
			scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
			return scaledCurrentImage;
		}
	}
	
	private final BufferedImage createWorstQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshSimpleSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		// System.runFinalization();
		// System.gc();
		byte rgbiValue = 0;
		pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			//red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
			//pixelBufferByte[i] = (byte) (red + green + blue);
			rgbiValue = (byte) VTIndexedColorModel.get8ColorRGBValue(sectionPixelBufferInt[i]);
			
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
	
	private final BufferedImage createWorstQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshSimpleSettings();
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
		//BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			
			//red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
			//pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
			rgbiValue = (byte) VTIndexedColorModel.get8ColorRGBValue(sectionPixelBufferInt[i]);
			
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
			Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
			scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
			scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
			scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
			scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
			//while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
			//{
				//Thread.yield();
			//}
			scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
			return scaledCurrentImage;
		}
	}
	
	private final BufferedImage createLowQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshWorstSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		// System.runFinalization();
		// System.gc();
		byte rgbiValue = 0;
		pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			//red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
			//pixelBufferByte[i] = (byte) (red + green + blue);
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
	
	private final BufferedImage createLowQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshWorstSettings();
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
		//BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			
			//red = (((((sectionPixelBufferInt[i] >> 16) & 0xFF) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] >> 8) & 0xFF) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & 0xFF) * 3) >> 8));
			//pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
			Rectangle scaledArea = new Rectangle(0, 0, 0, 0);
			scaledArea.x = (int) Math.round(captureArea.x * getScaleFactorX());
			scaledArea.y = (int) Math.round(captureArea.y * getScaleFactorY());
			scaledArea.width = (int) Math.round(captureArea.width * getScaleFactorX());
			scaledArea.height = (int) Math.round(captureArea.height * getScaleFactorY());
			//while (!scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null))
			//{
				//Thread.yield();
			//}
			scaledCurrentGraphics.drawImage(screenCurrentImage, scaledArea.x, scaledArea.y, scaledArea.x + scaledArea.width, scaledArea.y + scaledArea.height, captureArea.x, captureArea.y, captureArea.x + captureArea.width, captureArea.y + captureArea.height, null);
			return scaledCurrentImage;
		}
	}
	
	private final BufferedImage createMediumQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshWebSettings();
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
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		
		pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 6) >> 8) * 36);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 6) >> 8) * 6);
			blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 6) >> 8));
			
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
			//while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
			//{
				//Thread.yield();
			//}
			scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
			return scaledCurrentImage;
		}
	}
	
	private final BufferedImage createMediumQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshWebSettings();
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
		//BufferedImage screenCapture = createDirectScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
	
	private final BufferedImage createNormalQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshLowSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
	
	private final BufferedImage createNormalQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshLowSettings();
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
		//BufferedImage screenCapture = standardCaptureRobot.createScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
	
	private final BufferedImage createGoodQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshGoodSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		
		pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			//red = ((sectionPixelBufferInt[i] & RGB555_333_RED_MASK) >> 9);
			//green = ((sectionPixelBufferInt[i] & RGB555_333_GREEN_MASK) >> 6);
			//blue = ((sectionPixelBufferInt[i] & RGB555_333_BLUE_MASK) >> 3);
			
			red = (((sectionPixelBufferInt[i] & RGB555_333_RED_MASK) | RGB555_333_RED_ADD) >> 9);
			green = (((sectionPixelBufferInt[i] & RGB555_333_GREEN_MASK) | RGB555_333_GREEN_ADD) >> 6);
			blue = (((sectionPixelBufferInt[i] & RGB555_333_BLUE_MASK) | RGB555_333_BLUE_ADD) >> 3);

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
	
	private final BufferedImage createGoodQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshGoodSettings();
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
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			//red = ((sectionPixelBufferInt[i] & RGB555_333_RED_MASK) >> 9);
			//green = ((sectionPixelBufferInt[i] & RGB555_333_GREEN_MASK) >> 6);
			//blue = ((sectionPixelBufferInt[i] & RGB555_333_BLUE_MASK) >> 3);
			
			red = (((sectionPixelBufferInt[i] & RGB555_333_RED_MASK) | RGB555_333_RED_ADD) >> 9);
			green = (((sectionPixelBufferInt[i] & RGB555_333_GREEN_MASK) | RGB555_333_GREEN_ADD) >> 6);
			blue = (((sectionPixelBufferInt[i] & RGB555_333_BLUE_MASK) | RGB555_333_BLUE_ADD) >> 3);
			
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
	
	private final BufferedImage createExtraQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshExtraSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		
		pixelBufferShort = ((DataBufferUShort) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			red = (((sectionPixelBufferInt[i] & RGB555_444_RED_MASK) | RGB555_444_RED_ADD) >> 9);
			green = (((sectionPixelBufferInt[i] & RGB555_444_GREEN_MASK) | RGB555_444_GREEN_ADD) >> 6);
			blue = (((sectionPixelBufferInt[i] & RGB555_444_BLUE_MASK) | RGB555_444_BLUE_ADD) >> 3);
			
			//red = ((sectionPixelBufferInt[i] & RGB555_444_RED_MASK) >> 9);
			//green = ((sectionPixelBufferInt[i] & RGB555_444_GREEN_MASK) >> 6);
			//blue = ((sectionPixelBufferInt[i] & RGB555_444_BLUE_MASK) >> 3);

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
	
	private final BufferedImage createExtraQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshExtraSettings();
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
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			red = (((sectionPixelBufferInt[i] & RGB555_444_RED_MASK) | RGB555_444_RED_ADD) >> 9);
			green = (((sectionPixelBufferInt[i] & RGB555_444_GREEN_MASK) | RGB555_444_GREEN_ADD) >> 6);
			blue = (((sectionPixelBufferInt[i] & RGB555_444_BLUE_MASK) | RGB555_444_BLUE_ADD) >> 3);
			
			//red = ((sectionPixelBufferInt[i] & RGB555_444_RED_MASK) >> 9);
			//green = ((sectionPixelBufferInt[i] & RGB555_444_GREEN_MASK) >> 6);
			//blue = ((sectionPixelBufferInt[i] & RGB555_444_BLUE_MASK) >> 3);
			
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
	
	private final BufferedImage createHighQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshMediumSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
	
	private final BufferedImage createHighQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshMediumSettings();
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
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
	
	private final BufferedImage createBestQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshHighSettings();
		}
		Rectangle captureArea = new Rectangle(0, 0, screenCurrentWidth, screenCurrentHeight);
		if (captureArea.width <= 0 || captureArea.height <= 0)
		{
			return null;
		}
		BufferedImage screenCapture = createRobotCapture(captureArea);
		//BufferedImage screenCapture = standardCaptureRobot.createScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		
		// clear alpha channel
		int length = screenCapture.getWidth() * screenCapture.getHeight();
		for (int i = 0; i < length; i++)
		{
			sectionPixelBufferInt[i] &= 0x00FFFFFF;
		}
		
		pixelBufferInt = ((DataBufferInt) screenCurrentImage.getRaster().getDataBuffer()).getData();
		System.arraycopy(sectionPixelBufferInt, 0, pixelBufferInt, 0, pixelDataLength);
		//VTImageDataUtils.copyArea(sectionPixelBufferInt, pixelBufferInt, 0, captureArea.width, captureArea.height, captureArea);
		//screenCurrentImage.getRaster().setDataElements(captureArea.x, captureArea.y, captureArea.width, captureArea.height, sectionPixelBufferInt);
		
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
	
	private final BufferedImage createBestQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshHighSettings();
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
		//BufferedImage screenCapture = createMultiplePassScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		
		// clear alpha channel
		int length = screenCapture.getWidth() * screenCapture.getHeight();
		for (int i = 0; i < length; i++)
		{
			sectionPixelBufferInt[i] &= 0x00FFFFFF;
		}
		
		pixelBufferInt = ((DataBufferInt) screenCurrentImage.getRaster().getDataBuffer()).getData();
		//VTImageDataUtils.copyArea(sectionPixelBufferInt, pixelBufferInt, 0, screenCurrentImage.getWidth(), screenCurrentImage.getHeight(), captureArea);
		//screenCurrentImage.getRaster().setDataElements(captureArea.x, captureArea.y, captureArea.width, captureArea.height, sectionPixelBufferInt);
		int destinationOffset = captureArea.x + screenCurrentImage.getWidth() * captureArea.y;
		int destinationIndex = destinationOffset;
		int sourceIndex = 0;
		;
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
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
	
	private final BufferedImage createReducedQualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refreshReducedSettings();
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
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
		}
		
		pixelBufferByte = ((DataBufferByte) screenCurrentImage.getRaster().getDataBuffer()).getData();
		for (i = 0; i < pixelDataLength; i++)
		{
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 5) >> 8) * 25);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 5) >> 8) * 5);
			blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 5) >> 8));
			
//			if (red + green + blue == 62)
//			{
//				red = red + 25;
//				green = green + 5;
//				blue = blue + 1;
//			}
			
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
			drawPointerFilterGray(screenCurrentImage);
		}
		
		if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
		{
			return screenCurrentImage;
		}
		else
		{
			//while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
			//{
				//Thread.yield();
			//}
			scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
			return scaledCurrentImage;
		}
	}
	
	private final BufferedImage createReducedQualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refreshReducedSettings();
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
		//BufferedImage screenCapture = createDirectScreenCapture(captureArea);
		int pixelDataLength = (screenCapture.getWidth() * screenCapture.getHeight());
		if (screenCapture.getType() == BufferedImage.TYPE_INT_RGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB
		|| screenCapture.getType() == BufferedImage.TYPE_INT_ARGB_PRE)
		{
			sectionPixelBufferInt = ((DataBufferInt)screenCapture.getRaster().getDataBuffer()).getData();
		}
		else
		{
			grabber.setImage(screenCapture);
			if (sectionPixelBufferInt != null && sectionPixelBufferInt.length >= pixelDataLength)
			{
				sectionPixelBufferInt = grabber.getPixels(sectionPixelBufferInt);
			}
			else
			{
				sectionPixelBufferInt = grabber.getPixels();
			}
			grabber.dispose();
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
			
//			if (red + green + blue == 62)
//			{
//				red = red + 25;
//				green = green + 5;
//				blue = blue + 1;
//			}
			
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
			drawPointerFilterGray(screenCurrentImage, captureArea);
		}
		
		if (scaledCurrentWidth <= 0 || scaledCurrentHeight <= 0)
		{
			return screenCurrentImage;
		}
		else
		{
			//Rectangle scaledArea = new Rectangle(Math.min(originalArea.x, scaledCurrentWidth), Math.min(originalArea.y, scaledCurrentHeight), Math.min(originalArea.width, scaledCurrentWidth - originalArea.x), Math.min(originalArea.height, scaledCurrentHeight - originalArea.y));
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
				sectionCurrentImage = VTImageIO.newImage(0, 0, captureArea.width, captureArea.height, BufferedImage.TYPE_INT_RGB, 0, recyclableSectionDataBuffer);
				recyclableSectionDataBuffer = sectionCurrentImage.getRaster().getDataBuffer();
			}
			if (directCaptureRobot.getRGBPixels(captureArea.x, captureArea.y, captureArea.width, captureArea.height, ((DataBufferInt)recyclableSectionDataBuffer).getData()))
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
		//int dpi = toolkit.getScreenResolution();
		//int dpiCursorSize = Math.max(32, dpi / 3);
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
				image.setRGB(x + 2, y, (image.getRGB(x + 2, y) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 3, y))
			{
				image.setRGB(x + 3, y, (image.getRGB(x + 3, y) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 4, y))
			{
				image.setRGB(x + 4, y, (image.getRGB(x + 4, y) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 2, y))
			{
				image.setRGB(x - 2, y, (image.getRGB(x - 2, y) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 3, y))
			{
				image.setRGB(x - 3, y, (image.getRGB(x - 3, y) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 4, y))
			{
				image.setRGB(x - 4, y, (image.getRGB(x - 4, y) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y + 2))
			{
				image.setRGB(x, y + 2, (image.getRGB(x, y + 2) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y + 3))
			{
				image.setRGB(x, y + 3, (image.getRGB(x, y + 3) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y + 4))
			{
				image.setRGB(x, y + 4, (image.getRGB(x, y + 4) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y - 2))
			{
				image.setRGB(x, y - 2, (image.getRGB(x, y - 2) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y - 3))
			{
				image.setRGB(x, y - 3, (image.getRGB(x, y - 3) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y - 4))
			{
				image.setRGB(x, y - 4, (image.getRGB(x, y - 4) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 2, y + 1))
			{
				image.setRGB(x + 2, y + 1, (image.getRGB(x + 2, y + 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 3, y + 1))
			{
				image.setRGB(x + 3, y + 1, (image.getRGB(x + 3, y + 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 4, y + 1))
			{
				image.setRGB(x + 4, y + 1, (image.getRGB(x + 4, y + 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 2, y - 1))
			{
				image.setRGB(x + 2, y - 1, (image.getRGB(x + 2, y - 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 3, y - 1))
			{
				image.setRGB(x + 3, y - 1, (image.getRGB(x + 3, y - 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 4, y - 1))
			{
				image.setRGB(x + 4, y - 1, (image.getRGB(x + 4, y - 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 2, y + 1))
			{
				image.setRGB(x - 2, y + 1, (image.getRGB(x - 2, y + 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 3, y + 1))
			{
				image.setRGB(x - 3, y + 1, (image.getRGB(x - 3, y + 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 4, y + 1))
			{
				image.setRGB(x - 4, y + 1, (image.getRGB(x - 4, y + 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 2, y - 1))
			{
				image.setRGB(x - 2, y - 1, (image.getRGB(x - 2, y - 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 3, y - 1))
			{
				image.setRGB(x - 3, y - 1, (image.getRGB(x - 3, y - 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 4, y - 1))
			{
				image.setRGB(x - 4, y - 1, (image.getRGB(x - 4, y - 1) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y + 2))
			{
				image.setRGB(x + 1, y + 2, (image.getRGB(x + 1, y + 2) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y + 3))
			{
				image.setRGB(x + 1, y + 3, (image.getRGB(x + 1, y + 3) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y + 4))
			{
				image.setRGB(x + 1, y + 4, (image.getRGB(x + 1, y + 4) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y - 2))
			{
				image.setRGB(x + 1, y - 2, (image.getRGB(x + 1, y - 2) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y - 3))
			{
				image.setRGB(x + 1, y - 3, (image.getRGB(x + 1, y - 3) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y - 4))
			{
				image.setRGB(x + 1, y - 4, (image.getRGB(x + 1, y - 4) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y + 2))
			{
				image.setRGB(x - 1, y + 2, (image.getRGB(x - 1, y + 2) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y + 3))
			{
				image.setRGB(x - 1, y + 3, (image.getRGB(x - 1, y + 3) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y + 4))
			{
				image.setRGB(x - 1, y + 4, (image.getRGB(x - 1, y + 4) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y - 2))
			{
				image.setRGB(x - 1, y - 2, (image.getRGB(x - 1, y - 2) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y - 3))
			{
				image.setRGB(x - 1, y - 3, (image.getRGB(x - 1, y - 3) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y - 4))
			{
				image.setRGB(x - 1, y - 4, (image.getRGB(x - 1, y - 4) ^ 0x00FFFFFF));
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
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
			}
			// Second quadrant
			x = pointerLocation.x + 2;
			y = pointerLocation.y + 2;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
			}
			// Third quadrant
			x = pointerLocation.x + 2;
			y = pointerLocation.y - 2;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
			}
			// Fourth quadrant
			x = pointerLocation.x - 2;
			y = pointerLocation.y - 2;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
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
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
			}
			// Second quadrant
			x = pointerLocation.x + 3;
			y = pointerLocation.y + 3;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
			}
			// Third quadrant
			x = pointerLocation.x + 3;
			y = pointerLocation.y - 3;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
			}
			// Fourth quadrant
			x = pointerLocation.x - 3;
			y = pointerLocation.y - 3;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
			}
			
			//n = n / 2;
			n = n - 1;
			// First quadrant
			x = pointerLocation.x - 4;
			y = pointerLocation.y + 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x - (4 + n - 1);
			y = pointerLocation.y + (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x + m, y - m))
				{
					image.setRGB(x + m, y - m, (image.getRGB(x + m, y - m) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y -	m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
				if (area.contains(x + 1, y - m))
				{
					image.setRGB(x + 1, y -	m, (image.getRGB(x + 1, y - m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y - 1))
				{
					image.setRGB(x + m, y - 1, (image.getRGB(x + m, y - 1) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x + 2, y - m))
				{
					image.setRGB(x + 2, y -	m, (image.getRGB(x + 2, y - m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y - 2))
				{
					image.setRGB(x + m, y - 2, (image.getRGB(x + m, y - 2) ^ 0x00FFFFFF));
				}
			}
			
//			x = pointerLocation.x - (4 + (n / 2));
//			y = pointerLocation.y + (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x - m, y + m))
//				{
//					image.setRGB(x - m, y + m, (image.getRGB(x - m, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x - 1 - m, y + m))
//				{
//					image.setRGB(x - 1 - m, y +	m, (image.getRGB(x - 1 - m, y + m) ^ 0x00FFFFFF));
//				}
//				if	(area.contains(x - m, y + 1 + m))
//				{
//					image.setRGB(x - m, y + 1 +	m, (image.getRGB(x - m, y + 1 + m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x - 2 - m, y + m))
//				{
//					image.setRGB(x - 2 - m, y + m, (image.getRGB(x - 2 - m, y + m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x - m, y + 2 + m))
//				{
//					image.setRGB(x - m, y + 2 + m, (image.getRGB(x - m, y + 2 + m) ^ 0x00FFFFFF));
//				}
//			}
			
			// Second quadrant
			x = pointerLocation.x + 4;
			y = pointerLocation.y + 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x + (4 + n - 1);
			y = pointerLocation.y + (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x - m, y - m))
				{
					image.setRGB(x - m, y - m, (image.getRGB(x - m, y - m) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y -	m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
				if (area.contains(x - 1, y - m))
				{
					image.setRGB(x - 1, y -	m, (image.getRGB(x - 1, y - m) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y - 1))
				{
					image.setRGB(x - m, y - 1, (image.getRGB(x - m, y - 1) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x - 2, y - m))
				{
					image.setRGB(x - 2, y -	m, (image.getRGB(x - 2, y - m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y - 2))
				{
					image.setRGB(x - m, y - 2, (image.getRGB(x - m, y - 2) ^ 0x00FFFFFF));
				}
			}
			
//			x = pointerLocation.x + (4 + (n / 2));
//			y = pointerLocation.y + (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x + m, y + m))
//				{
//					image.setRGB(x + m, y + m, (image.getRGB(x + m, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x + 1 + m, y + m))
//				{
//					image.setRGB(x + 1 + m, y +	m, (image.getRGB(x + 1 + m, y + m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y + 1 + m))
//				{
//					image.setRGB(x + m, y + 1 +	m, (image.getRGB(x + m, y + 1 + m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x + 2 + m, y + m))
//				{
//					image.setRGB(x + 2 + m, y + m, (image.getRGB(x + 2 + m, y + m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y + 2 + m))
//				{
//					image.setRGB(x + m, y + 2 + m, (image.getRGB(x + m, y + 2 + m) ^ 0x00FFFFFF));
//				}
//			}
			
			// Third quadrant
			x = pointerLocation.x + 4;
			y = pointerLocation.y - 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x + (4 + n - 1);
			y = pointerLocation.y - (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x - m, y + m))
				{
					image.setRGB(x - m, y + m, (image.getRGB(x - m, y + m) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y +	m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
				if (area.contains(x - 1, y + m))
				{
					image.setRGB(x - 1, y +	m, (image.getRGB(x - 1, y + m) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y + 1))
				{
					image.setRGB(x - m, y + 1, (image.getRGB(x - m, y + 1) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x - 2, y + m))
				{
					image.setRGB(x - 2, y +	m, (image.getRGB(x - 2, y + m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y + 2))
				{
					image.setRGB(x - m, y + 2, (image.getRGB(x - m, y + 2) ^ 0x00FFFFFF));
				}
			}
						
//			x = pointerLocation.x + (4 + (n / 2));
//			y = pointerLocation.y - (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x + m, y - m))
//				{
//					image.setRGB(x + m, y - m, (image.getRGB(x + m, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x + 1 + m, y - m))
//				{
//					image.setRGB(x + 1 + m, y -	m, (image.getRGB(x + 1 + m, y - m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y - 1 - m))
//				{
//					image.setRGB(x + m, y - 1 -	m, (image.getRGB(x + m, y - 1 - m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x + 2 + m, y - m))
//				{
//					image.setRGB(x + 2 + m, y - m, (image.getRGB(x + 2 + m, y - m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y - 2 - m))
//				{
//					image.setRGB(x + m, y - 2 - m, (image.getRGB(x + m, y - 2 - m) ^ 0x00FFFFFF));
//				}
//			}
			
			// Fourth quadrant
			x = pointerLocation.x - 4;
			y = pointerLocation.y - 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x - (4 + n - 1);
			y = pointerLocation.y - (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x + m, y + m))
				{
					image.setRGB(x + m, y + m, (image.getRGB(x + m, y + m) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y +	m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
				}
				if (area.contains(x + 1, y + m))
				{
					image.setRGB(x + 1, y +	m, (image.getRGB(x + 1, y + m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y + 1))
				{
					image.setRGB(x + m, y + 1, (image.getRGB(x + m, y + 1) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x + 2, y + m))
				{
					image.setRGB(x + 2, y +	m, (image.getRGB(x + 2, y + m) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y + 2))
				{
					image.setRGB(x + m, y + 2, (image.getRGB(x + m, y + 2) ^ 0x00FFFFFF));
				}
			}
						
//			x = pointerLocation.x - (4 + (n / 2));
//			y = pointerLocation.y - (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x - m, y - m))
//				{
//					image.setRGB(x - m, y - m, (image.getRGB(x - m, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x - 1 - m, y - m))
//				{
//					image.setRGB(x - 1 - m, y -	m, (image.getRGB(x - 1 - m, y - m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x - m, y - 1 - m))
//				{
//					image.setRGB(x - m, y - 1 - m, (image.getRGB(x - m, y - 1 - m) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x - 2 - m, y - m))
//				{
//					image.setRGB(x - 2 - m, y - m, (image.getRGB(x - 2 - m, y - m) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x - m, y - 2 - m))
//				{
//					image.setRGB(x - m, y - 2 - m, (image.getRGB(x - m, y - 2 - m) ^ 0x00FFFFFF));
//				}
//			}
			
//			n = n - 1;
//			//First quadrant
//			x = pointerLocation.x - 5;
//			y =	pointerLocation.y + 5;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x+ 5;
//			y = pointerLocation.y + 5;
//			//Second quadrant
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			//Third quadrant
//			x = pointerLocation.x + 5;
//			y =	pointerLocation.y - 5;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y,(image.getRGB(x + m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			//Fourth quadrant
//			x =	pointerLocation.x - 5;
//			y = pointerLocation.y - 5;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			
//			n = n / 2;
//			//First quadrant
//			x = pointerLocation.x - 6;
//			y =	pointerLocation.y + 6;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			//Second quadrant
//			x =	pointerLocation.x + 6;
//			y = pointerLocation.y + 6;
//			if	(area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			//Third quadrant
//			x = pointerLocation.x + 6;
//			y = pointerLocation.y - 6;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			//Fourth quadrant
//			x =	pointerLocation.x - 6;
//			y = pointerLocation.y - 6;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			
//			n = n - 1;
//			x = pointerLocation.x - 7;
//			y = pointerLocation.y + 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x + 7;
//			y =	pointerLocation.y + 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x + 7;
//			y = pointerLocation.y - 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x - 7;
//			y = pointerLocation.y - 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m) ^ 0x00FFFFFF));
//				}
//			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace(VTTerminal.getSystemOut());
			// e.printStackTrace();
		}
	}
	
	/* public final BufferedImage createScreenCapture() { return
	 * createScreenCapture(false); } */
	
	/* public final BufferedImage createScreenCapture(Rectangle area) { return
	 * createScreenCapture(false, area, 1.0); } */
	
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
			return createBestQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return createHighQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return createMediumQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return createLowQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return createSimpleQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return createGoodQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return createExtraQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return createWorstQualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return createReducedQualityScreenCapture(drawPointer);
		}
		else
		{
			return createNormalQualityScreenCapture(drawPointer);
		}
	}
	
	public final synchronized BufferedImage createScreenCapture(boolean drawPointer, Rectangle area)
	{
		if (!isScreenCaptureInitialized())
		{
			if (!initializeScreenCapture())
			{
				return null;
			} ;
		}
		if (colorQuality == VT_COLOR_QUALITY_16777216)
		{
			return createBestQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return createHighQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return createMediumQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return createLowQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return createSimpleQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return createGoodQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return createExtraQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return createWorstQualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return createReducedQualityScreenCapture(drawPointer, area);
		}
		else
		{
			return createNormalQualityScreenCapture(drawPointer, area);
		}
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
		//int dpi = toolkit.getScreenResolution();
		//int dpiCursorSize = Math.max(32, dpi / 3);
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
				image.setRGB(x + 2, y, (filterGray(image.getRGB(x + 2, y)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 3, y))
			{
				image.setRGB(x + 3, y, (filterGray(image.getRGB(x + 3, y)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 4, y))
			{
				image.setRGB(x + 4, y, (filterGray(image.getRGB(x + 4, y)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 2, y))
			{
				image.setRGB(x - 2, y, (filterGray(image.getRGB(x - 2, y)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 3, y))
			{
				image.setRGB(x - 3, y, (filterGray(image.getRGB(x - 3, y)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 4, y))
			{
				image.setRGB(x - 4, y, (filterGray(image.getRGB(x - 4, y)) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y + 2))
			{
				image.setRGB(x, y + 2, (filterGray(image.getRGB(x, y + 2)) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y + 3))
			{
				image.setRGB(x, y + 3, (filterGray(image.getRGB(x, y + 3)) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y + 4))
			{
				image.setRGB(x, y + 4, (filterGray(image.getRGB(x, y + 4)) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y - 2))
			{
				image.setRGB(x, y - 2, (filterGray(image.getRGB(x, y - 2)) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y - 3))
			{
				image.setRGB(x, y - 3, (filterGray(image.getRGB(x, y - 3)) ^ 0x00FFFFFF));
			}
			if (area.contains(x, y - 4))
			{
				image.setRGB(x, y - 4, (filterGray(image.getRGB(x, y - 4)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 2, y + 1))
			{
				image.setRGB(x + 2, y + 1, (filterGray(image.getRGB(x + 2, y + 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 3, y + 1))
			{
				image.setRGB(x + 3, y + 1, (filterGray(image.getRGB(x + 3, y + 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 4, y + 1))
			{
				image.setRGB(x + 4, y + 1, (filterGray(image.getRGB(x + 4, y + 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 2, y - 1))
			{
				image.setRGB(x + 2, y - 1, (filterGray(image.getRGB(x + 2, y - 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 3, y - 1))
			{
				image.setRGB(x + 3, y - 1, (filterGray(image.getRGB(x + 3, y - 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 4, y - 1))
			{
				image.setRGB(x + 4, y - 1, (filterGray(image.getRGB(x + 4, y - 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 2, y + 1))
			{
				image.setRGB(x - 2, y + 1, (filterGray(image.getRGB(x - 2, y + 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 3, y + 1))
			{
				image.setRGB(x - 3, y + 1, (filterGray(image.getRGB(x - 3, y + 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 4, y + 1))
			{
				image.setRGB(x - 4, y + 1, (filterGray(image.getRGB(x - 4, y + 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 2, y - 1))
			{
				image.setRGB(x - 2, y - 1, (filterGray(image.getRGB(x - 2, y - 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 3, y - 1))
			{
				image.setRGB(x - 3, y - 1, (filterGray(image.getRGB(x - 3, y - 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 4, y - 1))
			{
				image.setRGB(x - 4, y - 1, (filterGray(image.getRGB(x - 4, y - 1)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y + 2))
			{
				image.setRGB(x + 1, y + 2, (filterGray(image.getRGB(x + 1, y + 2)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y + 3))
			{
				image.setRGB(x + 1, y + 3, (filterGray(image.getRGB(x + 1, y + 3)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y + 4))
			{
				image.setRGB(x + 1, y + 4, (filterGray(image.getRGB(x + 1, y + 4)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y - 2))
			{
				image.setRGB(x + 1, y - 2, (filterGray(image.getRGB(x + 1, y - 2)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y - 3))
			{
				image.setRGB(x + 1, y - 3, (filterGray(image.getRGB(x + 1, y - 3)) ^ 0x00FFFFFF));
			}
			if (area.contains(x + 1, y - 4))
			{
				image.setRGB(x + 1, y - 4, (filterGray(image.getRGB(x + 1, y - 4)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y + 2))
			{
				image.setRGB(x - 1, y + 2, (filterGray(image.getRGB(x - 1, y + 2)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y + 3))
			{
				image.setRGB(x - 1, y + 3, (filterGray(image.getRGB(x - 1, y + 3)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y + 4))
			{
				image.setRGB(x - 1, y + 4, (filterGray(image.getRGB(x - 1, y + 4)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y - 2))
			{
				image.setRGB(x - 1, y - 2, (filterGray(image.getRGB(x - 1, y - 2)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y - 3))
			{
				image.setRGB(x - 1, y - 3, (filterGray(image.getRGB(x - 1, y - 3)) ^ 0x00FFFFFF));
			}
			if (area.contains(x - 1, y - 4))
			{
				image.setRGB(x - 1, y - 4, (filterGray(image.getRGB(x - 1, y - 4)) ^ 0x00FFFFFF));
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
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
			}
			// Second quadrant
			x = pointerLocation.x + 2;
			y = pointerLocation.y + 2;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
			}
			// Third quadrant
			x = pointerLocation.x + 2;
			y = pointerLocation.y - 2;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
			}
			// Fourth quadrant
			x = pointerLocation.x - 2;
			y = pointerLocation.y - 2;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
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
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
			}
			// Second quadrant
			x = pointerLocation.x + 3;
			y = pointerLocation.y + 3;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
			}
			// Third quadrant
			x = pointerLocation.x + 3;
			y = pointerLocation.y - 3;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
			}
			// Fourth quadrant
			x = pointerLocation.x - 3;
			y = pointerLocation.y - 3;
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
			}
			
			//n = n / 2;
			n = n - 1;
			// First quadrant
			x = pointerLocation.x - 4;
			y = pointerLocation.y + 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x - (4 + n - 1);
			y = pointerLocation.y + (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x + m, y - m))
				{
					image.setRGB(x + m, y - m, (filterGray(image.getRGB(x + m, y - m)) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y -	m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + 1, y - m))
				{
					image.setRGB(x + 1, y -	m, (filterGray(image.getRGB(x + 1, y - m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y - 1))
				{
					image.setRGB(x + m, y - 1, (filterGray(image.getRGB(x + m, y - 1)) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x + 2, y - m))
				{
					image.setRGB(x + 2, y -	m, (filterGray(image.getRGB(x + 2, y - m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y - 2))
				{
					image.setRGB(x + m, y - 2, (filterGray(image.getRGB(x + m, y - 2)) ^ 0x00FFFFFF));
				}
			}
			
//			x = pointerLocation.x - (4 + (n / 2));
//			y = pointerLocation.y + (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x - m, y + m))
//				{
//					image.setRGB(x - m, y + m, (image.getRGB(x - m, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x - 1 - m, y + m))
//				{
//					image.setRGB(x - 1 - m, y +	m, (image.getRGB(x - 1 - m, y + m)) ^ 0x00FFFFFF));
//				}
//				if	(area.contains(x - m, y + 1 + m))
//				{
//					image.setRGB(x - m, y + 1 +	m, (image.getRGB(x - m, y + 1 + m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x - 2 - m, y + m))
//				{
//					image.setRGB(x - 2 - m, y + m, (image.getRGB(x - 2 - m, y + m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x - m, y + 2 + m))
//				{
//					image.setRGB(x - m, y + 2 + m, (image.getRGB(x - m, y + 2 + m)) ^ 0x00FFFFFF));
//				}
//			}
			
			// Second quadrant
			x = pointerLocation.x + 4;
			y = pointerLocation.y + 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y + m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x + (4 + n - 1);
			y = pointerLocation.y + (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x - m, y - m))
				{
					image.setRGB(x - m, y - m, (filterGray(image.getRGB(x - m, y - m)) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y -	m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
				if (area.contains(x - 1, y - m))
				{
					image.setRGB(x - 1, y -	m, (filterGray(image.getRGB(x - 1, y - m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y - 1))
				{
					image.setRGB(x - m, y - 1, (filterGray(image.getRGB(x - m, y - 1)) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x - 2, y - m))
				{
					image.setRGB(x - 2, y -	m, (filterGray(image.getRGB(x - 2, y - m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y - 2))
				{
					image.setRGB(x - m, y - 2, (filterGray(image.getRGB(x - m, y - 2)) ^ 0x00FFFFFF));
				}
			}
			
//			x = pointerLocation.x + (4 + (n / 2));
//			y = pointerLocation.y + (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x + m, y + m))
//				{
//					image.setRGB(x + m, y + m, (image.getRGB(x + m, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x + 1 + m, y + m))
//				{
//					image.setRGB(x + 1 + m, y +	m, (image.getRGB(x + 1 + m, y + m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y + 1 + m))
//				{
//					image.setRGB(x + m, y + 1 +	m, (image.getRGB(x + m, y + 1 + m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x + 2 + m, y + m))
//				{
//					image.setRGB(x + 2 + m, y + m, (image.getRGB(x + 2 + m, y + m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y + 2 + m))
//				{
//					image.setRGB(x + m, y + 2 + m, (image.getRGB(x + m, y + 2 + m)) ^ 0x00FFFFFF));
//				}
//			}
			
			// Third quadrant
			x = pointerLocation.x + 4;
			y = pointerLocation.y - 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x + (4 + n - 1);
			y = pointerLocation.y - (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x - m, y + m))
				{
					image.setRGB(x - m, y + m, (filterGray(image.getRGB(x - m, y + m)) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y +	m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
				if (area.contains(x - 1, y + m))
				{
					image.setRGB(x - 1, y +	m, (filterGray(image.getRGB(x - 1, y + m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x - m, y + 1))
				{
					image.setRGB(x - m, y + 1, (filterGray(image.getRGB(x - m, y + 1)) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x - 2, y + m))
				{
					image.setRGB(x - 2, y +	m, (filterGray(image.getRGB(x - 2, y + m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y + 2))
				{
					image.setRGB(x - m, y + 2, (filterGray(image.getRGB(x - m, y + 2)) ^ 0x00FFFFFF));
				}
			}
						
//			x = pointerLocation.x + (4 + (n / 2));
//			y = pointerLocation.y - (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x + m, y - m))
//				{
//					image.setRGB(x + m, y - m, (image.getRGB(x + m, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x + 1 + m, y - m))
//				{
//					image.setRGB(x + 1 + m, y -	m, (image.getRGB(x + 1 + m, y - m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y - 1 - m))
//				{
//					image.setRGB(x + m, y - 1 -	m, (image.getRGB(x + m, y - 1 - m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x + 2 + m, y - m))
//				{
//					image.setRGB(x + 2 + m, y - m, (image.getRGB(x + 2 + m, y - m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x + m, y - 2 - m))
//				{
//					image.setRGB(x + m, y - 2 - m, (image.getRGB(x + m, y - 2 - m)) ^ 0x00FFFFFF));
//				}
//			}
			
			// Fourth quadrant
			x = pointerLocation.x - 4;
			y = pointerLocation.y - 4;
			
			if (area.contains(x, y))
			{
				image.setRGB(x, y, (filterGray(image.getRGB(x, y)) ^ 0x00FFFFFF));
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x - m, y))
				{
					image.setRGB(x - m, y, (filterGray(image.getRGB(x - m, y)) ^ 0x00FFFFFF));
				}
			}
			for (m = 1; (m < n); m++)
			{
				if (area.contains(x, y - m))
				{
					image.setRGB(x, y - m, (filterGray(image.getRGB(x, y - m)) ^ 0x00FFFFFF));
				}
			}
			
			x = pointerLocation.x - (4 + n - 1);
			y = pointerLocation.y - (4 + n - 1);
			
			for (m = 0;(m < 3);m++)
			{
				if (area.contains(x + m, y + m))
				{
					image.setRGB(x + m, y + m, (filterGray(image.getRGB(x + m, y + m)) ^ 0x00FFFFFF));
				}
			}
						
			for (m = 1;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x, y + m))
				{
					image.setRGB(x, y +	m, (filterGray(image.getRGB(x, y + m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y))
				{
					image.setRGB(x + m, y, (filterGray(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + 1, y + m))
				{
					image.setRGB(x + 1, y +	m, (filterGray(image.getRGB(x + 1, y + m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y + 1))
				{
					image.setRGB(x + m, y + 1, (filterGray(image.getRGB(x + m, y + 1)) ^ 0x00FFFFFF));
				}
			}
			
			for (m = 3;(m < (n / 2) + 1);m++)
			{
				if (area.contains(x + 2, y + m))
				{
					image.setRGB(x + 2, y +	m, (filterGray(image.getRGB(x + 2, y + m)) ^ 0x00FFFFFF));
				}
				if (area.contains(x + m, y + 2))
				{
					image.setRGB(x + m, y + 2, (filterGray(image.getRGB(x + m, y + 2)) ^ 0x00FFFFFF));
				}
			}
						
//			x = pointerLocation.x - (4 + (n / 2));
//			y = pointerLocation.y - (4 + (n / 2));
//			
//			for (m = 1;(m < (n / 2));m++)
//			{
//				if (area.contains(x - m, y - m))
//				{
//					image.setRGB(x - m, y - m, (image.getRGB(x - m, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 1);m++)
//			{
//				if (area.contains(x - 1 - m, y - m))
//				{
//					image.setRGB(x - 1 - m, y -	m, (image.getRGB(x - 1 - m, y - m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x - m, y - 1 - m))
//				{
//					image.setRGB(x - m, y - 1 - m, (image.getRGB(x - m, y - 1 - m)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < (n / 2) - 2);m++)
//			{
//				if (area.contains(x - 2 - m, y - m))
//				{
//					image.setRGB(x - 2 - m, y - m, (image.getRGB(x - 2 - m, y - m)) ^ 0x00FFFFFF));
//				}
//				if (area.contains(x - m, y - 2 - m))
//				{
//					image.setRGB(x - m, y - 2 - m, (image.getRGB(x - m, y - 2 - m)) ^ 0x00FFFFFF));
//				}
//			}
			
//			n = n - 1;
//			//First quadrant
//			x = pointerLocation.x - 5;
//			y =	pointerLocation.y + 5;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x+ 5;
//			y = pointerLocation.y + 5;
//			//Second quadrant
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			//Third quadrant
//			x = pointerLocation.x + 5;
//			y =	pointerLocation.y - 5;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y,(image.getRGB(x + m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			//Fourth quadrant
//			x =	pointerLocation.x - 5;
//			y = pointerLocation.y - 5;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			
//			n = n / 2;
//			//First quadrant
//			x = pointerLocation.x - 6;
//			y =	pointerLocation.y + 6;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			//Second quadrant
//			x =	pointerLocation.x + 6;
//			y = pointerLocation.y + 6;
//			if	(area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			//Third quadrant
//			x = pointerLocation.x + 6;
//			y = pointerLocation.y - 6;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x,	y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			//Fourth quadrant
//			x =	pointerLocation.x - 6;
//			y = pointerLocation.y - 6;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			
//			n = n - 1;
//			x = pointerLocation.x - 7;
//			y = pointerLocation.y + 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x + 7;
//			y =	pointerLocation.y + 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m <	n);m++)
//			{
//				if (area.contains(x, y + m))
//				{
//					image.setRGB(x, y + m, (image.getRGB(x, y + m)) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x + 7;
//			y = pointerLocation.y - 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x + m, y))
//				{
//					image.setRGB(x + m, y, (image.getRGB(x + m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m)) ^ 0x00FFFFFF));
//				}
//			}
//			x = pointerLocation.x - 7;
//			y = pointerLocation.y - 7;
//			if (area.contains(x, y))
//			{
//				image.setRGB(x, y, (image.getRGB(x, y)) ^ 0x00FFFFFF));
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x - m, y))
//				{
//					image.setRGB(x - m, y, (image.getRGB(x - m, y)) ^ 0x00FFFFFF));
//				}
//			}
//			for (m = 1;(m < n);m++)
//			{
//				if (area.contains(x, y - m))
//				{
//					image.setRGB(x, y - m, (image.getRGB(x, y - m)) ^ 0x00FFFFFF));
//				}
//			}
		}
		catch (Throwable e)
		{
			// e.printStackTrace(VTTerminal.getSystemOut());
			// e.printStackTrace();
		}
	}
	
	private int filterGray(int rgb)
	{
		if (((rgb & 0x00FFFFFF) == 0x00808080) || ((rgb & 0x00FFFFFF) == 0x007F7F7F))
		{
			return rgb & 0xFF000000;
		}
		return rgb;
	}
}