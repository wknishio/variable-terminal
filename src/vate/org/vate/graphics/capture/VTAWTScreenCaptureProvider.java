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
	public static final int VT_COLOR_QUALITY_64 = 0; // 64 rgb222
	public static final int VT_COLOR_QUALITY_216 = 1; // 216 6x6x6
	public static final int VT_COLOR_QUALITY_32768 = 2; // 32768 rgb555
	public static final int VT_COLOR_QUALITY_16777216 = 3; // 16777216 rgb888
	public static final int VT_COLOR_QUALITY_16 = 4; // 16 rgbi
	public static final int VT_COLOR_QUALITY_32 = 5; // 32 rgbii
	public static final int VT_COLOR_QUALITY_512 = 6; // 512 rgb333
	public static final int VT_COLOR_QUALITY_4096 = 7; // 4096 rgb444
	public static final int VT_COLOR_QUALITY_8 = 8; // 8 rgb111
	public static final int VT_COLOR_QUALITY_125 = 9; // 125 5x5x5
	public static final int VT_COLOR_QUALITY_27 = 10; // 27 3x3x3
	
	//c16777216 16777216
	//c32768 32768
	//c4096 4096
	//c512 512
	//c216 216
	//c125 125
	//c64 64
	//c32 32
	//c27 27
	//c16 16
	//c8 8
	
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
	
	private static final int RGB888_222_RED_MASK = 0x00c00000;
	private static final int RGB888_222_GREEN_MASK = 0x0000c000;
	private static final int RGB888_222_BLUE_MASK = 0x000000c0;
	
	private volatile int colorQuality;
	private volatile boolean c16QualityScreenCaptureInitialized;
	private volatile boolean c32QualityScreenCaptureInitialized;
	private volatile boolean c512QualityScreenCaptureInitialized;
	private volatile boolean c4096QualityScreenCaptureInitialized;
	private volatile boolean c8QualityScreenCaptureInitialized;
	private volatile boolean c64QualityScreenCaptureInitialized;
	private volatile boolean c216QualityScreenCaptureInitialized;
	private volatile boolean c32768QualityScreenCaptureInitialized;
	private volatile boolean c16777216QualityScreenCaptureInitialized;
	private volatile boolean c125QualityScreenCaptureInitialized;
	private volatile boolean c27QualityScreenCaptureInitialized;
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
		/* try { c16777216CursorSize =
		 * Toolkit.getDefaultToolkit().get16777216CursorSize(32, 32); } catch
		 * (Throwable e) {
		 * } */
		// System.out.println("c16777216CursorSize: x: " + c16777216CursorSize.width + ",
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
	
	private final boolean initialize16QualityScreenCapture(GraphicsDevice device)
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
				refresh16Settings();
			}
			c16QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c16QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize32QualityScreenCapture(GraphicsDevice device)
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
				refresh32Settings();
			}
			c32QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c32QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize8QualityScreenCapture(GraphicsDevice device)
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
				refresh8Settings();
			}
			c8QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c8QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize512QualityScreenCapture(GraphicsDevice device)
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
				refresh512Settings();
			}
			c512QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c512QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize4096QualityScreenCapture(GraphicsDevice device)
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
				refresh4096Settings();
			}
			c4096QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			//e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c4096QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize64QualityScreenCapture(GraphicsDevice device)
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
				refresh64Settings();
			}
			c64QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c64QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize216QualityScreenCapture(GraphicsDevice device)
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
				refresh216Settings();
			}
			c216QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c216QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize32768QualityScreenCapture(GraphicsDevice device)
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
				refresh32768Settings();
			}
			c32768QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c32768QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize16777216QualityScreenCapture(GraphicsDevice device)
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
				refresh16777216Settings();
			}
			c16777216QualityScreenCaptureInitialized = true;
			// dispose64QualityScreenCapturec16777216urces();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c16777216QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize125QualityScreenCapture(GraphicsDevice device)
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
				refresh125Settings();
			}
			c125QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c125QualityScreenCaptureInitialized = false;
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
	
	private final boolean initialize27QualityScreenCapture(GraphicsDevice device)
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
				refresh27Settings();
			}
			c27QualityScreenCaptureInitialized = true;
			// dispose16777216QualityScreenCaptureResources();
			return true;
		}
		catch (Throwable e)
		{
			// e.printStackTrace();
			// e.printStackTrace(VTTerminal.getSystemOut());
			c27QualityScreenCaptureInitialized = false;
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
			return initialize16777216QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return initialize32768QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return initialize216QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return initialize16QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return initialize32QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return initialize512QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return initialize4096QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return initialize8QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return initialize125QualityScreenCapture(graphicsDevice);
		}
		else if (colorQuality == VT_COLOR_QUALITY_27)
		{
			return initialize27QualityScreenCapture(graphicsDevice);
		}
		else
		{
			return initialize64QualityScreenCapture(graphicsDevice);
		}
	}
	
	public final synchronized boolean initializeScreenCapture(GraphicsDevice device)
	{
		if (colorQuality == VT_COLOR_QUALITY_16777216)
		{
			return initialize16777216QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return initialize32768QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return initialize216QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return initialize16QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return initialize32QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return initialize512QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return initialize4096QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return initialize8QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return initialize125QualityScreenCapture(device);
		}
		else if (colorQuality == VT_COLOR_QUALITY_27)
		{
			return initialize27QualityScreenCapture(device);
		}
		else
		{
			return initialize64QualityScreenCapture(device);
		}
	}
	
	private final boolean is27QualityScreenCaptureInitialized()
	{
		return c27QualityScreenCaptureInitialized;
	}
	
	private final boolean is32QualityScreenCaptureInitialized()
	{
		return c32QualityScreenCaptureInitialized;
	}
	
	private final boolean is512QualityScreenCaptureInitialized()
	{
		return c512QualityScreenCaptureInitialized;
	}
	
	private final boolean is4096QualityScreenCaptureInitialized()
	{
		return c4096QualityScreenCaptureInitialized;
	}
	
	private final boolean is8QualityScreenCaptureInitialized()
	{
		return c8QualityScreenCaptureInitialized;
	}
	
	private final boolean is16QualityScreenCaptureInitialized()
	{
		return c16QualityScreenCaptureInitialized;
	}
	
	private final boolean is64QualityScreenCaptureInitialized()
	{
		return c64QualityScreenCaptureInitialized;
	}
	
	private final boolean is125QualityScreenCaptureInitialized()
	{
		return c125QualityScreenCaptureInitialized;
	}
	
	private final boolean is216QualityScreenCaptureInitialized()
	{
		return c216QualityScreenCaptureInitialized;
	}
	
	private final boolean is32768QualityScreenCaptureInitialized()
	{
		return c32768QualityScreenCaptureInitialized;
	}
	
	private final boolean is16777216QualityScreenCaptureInitialized()
	{
		return c16777216QualityScreenCaptureInitialized;
	}
	
	public final synchronized boolean isScreenCaptureInitialized()
	{
		if (colorQuality == VT_COLOR_QUALITY_16777216)
		{
			return is16777216QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return is32768QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return is216QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return is16QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return is32QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return is512QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return is4096QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return is8QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return is125QualityScreenCaptureInitialized();
		}
		else if (colorQuality == VT_COLOR_QUALITY_27)
		{
			return is27QualityScreenCaptureInitialized();
		}
		else
		{
			return is64QualityScreenCaptureInitialized();
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
		c16QualityScreenCaptureInitialized = false;
		c32QualityScreenCaptureInitialized = false;
		c512QualityScreenCaptureInitialized = false;
		c4096QualityScreenCaptureInitialized = false;
		c8QualityScreenCaptureInitialized = false;
		c64QualityScreenCaptureInitialized = false;
		c216QualityScreenCaptureInitialized = false;
		c32768QualityScreenCaptureInitialized = false;
		c16777216QualityScreenCaptureInitialized = false;
		c125QualityScreenCaptureInitialized = false;
		c27QualityScreenCaptureInitialized = false;
	}
	
	public final void clearResources()
	{
		c16QualityScreenCaptureInitialized = false;
		c32QualityScreenCaptureInitialized = false;
		c512QualityScreenCaptureInitialized = false;
		c4096QualityScreenCaptureInitialized = false;
		c8QualityScreenCaptureInitialized = false;
		c64QualityScreenCaptureInitialized = false;
		c216QualityScreenCaptureInitialized = false;
		c32768QualityScreenCaptureInitialized = false;
		c16777216QualityScreenCaptureInitialized = false;
		c125QualityScreenCaptureInitialized = false;
		c27QualityScreenCaptureInitialized = false;
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
	
	private final void refresh27Settings()
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
		screenCurrentImage = VTImageIO.newImage(0, 0, screenCurrentWidth, screenCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 27, recyclableScreenDataBuffer);
		recyclableScreenDataBuffer = screenCurrentImage.getRaster().getDataBuffer();
		refreshScaled27Settings();
	}
	
	private final void refreshScaled27Settings()
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
			scaledCurrentImage = VTImageIO.newImage(0, 0, scaledCurrentWidth, scaledCurrentHeight, BufferedImage.TYPE_BYTE_INDEXED, 27, recyclableScaledDataBuffer);
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
	
	private final void refresh16Settings()
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
		refreshScaled16Settings();
	}
	
	private final void refreshScaled16Settings()
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
	
	private final void refresh32Settings()
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
		refreshScaled32Settings();
	}
	
	private final void refreshScaled32Settings()
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
	
	private final void refresh8Settings()
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
		refreshScaled8Settings();
	}
	
	private final void refreshScaled8Settings()
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
	
	
	
	private final void refresh64Settings()
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
		refreshScaled64Settings();
	}
	
	private final void refreshScaled64Settings()
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
	
	private final void refresh125Settings()
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
		refreshScaled125Settings();
	}
	
	private final void refreshScaled125Settings()
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
	
	private final void refresh216Settings()
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
		refreshScaled216Settings();
	}
	
	private final void refreshScaled216Settings()
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
	
	private final void refresh512Settings()
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
		refreshScaled512Settings();
	}
	
	private final void refreshScaled512Settings()
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
	
	private final void refresh4096Settings()
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
		refreshScale4096Settings();
	}
	
	private final void refreshScale4096Settings()
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
	
	private final void refresh32768Settings()
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
		refreshScaled32768Settings();
	}
	
	private final void refreshScaled32768Settings()
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
	
	
	
	private final void refresh16777216Settings()
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
		refreshScaled16777216Settings();
	}
	
	private final void refreshScaled16777216Settings()
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
	
	private final BufferedImage create32QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh32Settings();
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
	
	private final BufferedImage create32QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh32Settings();
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
	
	private final BufferedImage create8QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh8Settings();
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
	
	private final BufferedImage create8QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh8Settings();
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
	
	private final BufferedImage create16QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh16Settings();
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
	
	private final BufferedImage create16QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh16Settings();
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
	
	private final BufferedImage create216QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh216Settings();
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
			
			//red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 6) >> 8) * 36);
			//green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 6) >> 8) * 6);
			//blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 6) >> 8));
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 26) / 51) * 36);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 26) / 51) * 6);
			blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 26) / 51));
//			
			pixelBufferByte[i] = (byte) (red + green + blue);
			
			//pixelBufferByte[i] = VTIndexedColorModel.get216Color6LevelRGBValue(sectionPixelBufferInt[i]);
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
	
	private final BufferedImage create216QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh216Settings();
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
			
			//red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 6) >> 8) * 36);
			//green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 6) >> 8) * 6);
			//blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 6) >> 8));
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 26) / 51) * 36);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 26) / 51) * 6);
			blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 26) / 51));
			
			pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
			
			//pixelBufferByte[startOffset + currentWidth + currentHeight] = VTIndexedColorModel.get216Color6LevelRGBValue(sectionPixelBufferInt[i]);
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
	
	private final BufferedImage create64QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh64Settings();
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
			red = ((sectionPixelBufferInt[i] & RGB888_222_RED_MASK) >> 18);
			green = ((sectionPixelBufferInt[i] & RGB888_222_GREEN_MASK) >> 12);
			blue = ((sectionPixelBufferInt[i] & RGB888_222_BLUE_MASK) >> 6);
			
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
	
	private final BufferedImage create64QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh64Settings();
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
			
			red = ((sectionPixelBufferInt[i] & RGB888_222_RED_MASK) >> 18);
			green = ((sectionPixelBufferInt[i] & RGB888_222_GREEN_MASK) >> 12);
			blue = ((sectionPixelBufferInt[i] & RGB888_222_BLUE_MASK) >> 6);
			
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
	
	private final BufferedImage create512QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh512Settings();
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
	
	private final BufferedImage create512QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh512Settings();
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
	
	private final BufferedImage create4096QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh4096Settings();
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
	
	private final BufferedImage create4096QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh4096Settings();
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
	
	private final BufferedImage create32768QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh32768Settings();
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
	
	private final BufferedImage create32768QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh32768Settings();
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
	
	private final BufferedImage create16777216QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh16777216Settings();
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
	
	private final BufferedImage create16777216QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh16777216Settings();
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
	
	private final BufferedImage create125QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh125Settings();
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
			
			//red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 5) >> 8) * 25);
			//green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 5) >> 8) * 5);
			//blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 5) >> 8));
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 32) >> 6) * 25);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 32) >> 6) * 5);
			blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 32) >> 6));
			
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
			//while (!scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null))
			//{
				//Thread.yield();
			//}
			scaledCurrentGraphics.drawImage(screenCurrentImage, 0, 0, scaledCurrentWidth, scaledCurrentHeight, null);
			return scaledCurrentImage;
		}
	}
	
	private final BufferedImage create125QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh125Settings();
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
			
			//red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 5) >> 8) * 25);
			//green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 5) >> 8) * 5);
			//blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 5) >> 8));
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 32) >> 6) * 25);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 32) >> 6) * 5);
			blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 32) >> 6));
			
			pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
			
			//pixelBufferByte[startOffset + currentWidth + currentHeight] = VTIndexedColorModel.get125Color5LevelRGBValue(sectionPixelBufferInt[i]);
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
	
	private final BufferedImage create27QualityScreenCapture(boolean drawPointer)
	{
		if (changedCurrentSettings())
		{
			refresh27Settings();
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
			
			//red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 3) >> 8));
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 64) >> 7) * 9);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 64) >> 7) * 3);
			blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 64) >> 7));
			
			pixelBufferByte[i] = (byte) (red + green + blue);
			
			//pixelBufferByte[i] = VTIndexedColorModel.get27Color3LevelRGBValue(sectionPixelBufferInt[i]);
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
	
	private final BufferedImage create27QualityScreenCapture(boolean drawPointer, Rectangle originalArea)
	{
		if (changedCurrentSettings())
		{
			refresh27Settings();
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
			
			//red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) * 3) >> 8) * 9);
			//green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) * 3) >> 8) * 3);
			//blue = (((((sectionPixelBufferInt[i]) & RGB888_BLUE_MASK) * 3) >> 8));
			
			red = (((((sectionPixelBufferInt[i] & RGB888_RED_MASK) >> 16) + 64) >> 7) * 9);
			green = (((((sectionPixelBufferInt[i] & RGB888_GREEN_MASK) >> 8) + 64) >> 7) * 3);
			blue = (((((sectionPixelBufferInt[i] & RGB888_BLUE_MASK)) + 64) >> 7));
			
			pixelBufferByte[startOffset + currentWidth + currentHeight] = (byte) (red + green + blue);
			
			//pixelBufferByte[startOffset + currentWidth + currentHeight] = VTIndexedColorModel.get27Color3LevelRGBValue(sectionPixelBufferInt[i]);
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
			return create16777216QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return create32768QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return create216QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return create16QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return create32QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return create512QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return create4096QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return create8QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return create125QualityScreenCapture(drawPointer);
		}
		else if (colorQuality == VT_COLOR_QUALITY_27)
		{
			return create27QualityScreenCapture(drawPointer);
		}
		else
		{
			return create64QualityScreenCapture(drawPointer);
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
			return create16777216QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32768)
		{
			return create32768QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_216)
		{
			return create216QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_16)
		{
			return create16QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_32)
		{
			return create32QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_512)
		{
			return create512QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_4096)
		{
			return create4096QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_8)
		{
			return create8QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_125)
		{
			return create125QualityScreenCapture(drawPointer, area);
		}
		else if (colorQuality == VT_COLOR_QUALITY_27)
		{
			return create27QualityScreenCapture(drawPointer, area);
		}
		else
		{
			return create64QualityScreenCapture(drawPointer, area);
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