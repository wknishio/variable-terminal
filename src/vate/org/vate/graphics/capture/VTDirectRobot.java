package org.vate.graphics.capture;

import java.awt.*;
import java.awt.peer.*;
import sun.awt.*;
import java.lang.reflect.*;

public final class VTDirectRobot
{
	public VTDirectRobot() throws AWTException
	{
		this(null);
	}

	public VTDirectRobot(GraphicsDevice device) throws AWTException
	{
		if (device == null)
		{
			device = GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
		}

		this.device = device;
		Toolkit toolkit = Toolkit.getDefaultToolkit();
		peer = ((ComponentFactory) toolkit).createRobot(null, device);
		Class<?> peerClass = peer.getClass();
		Method method = null;
		int methodType = -1;
		Object methodParam = null;
		try
		{
			method = peerClass.getDeclaredMethod("getRGBPixels", new Class<?>[] { Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, int[].class });
			methodType = 0;
		}
		catch (Throwable ex) { }
		if (methodType < 0)
		{
			try
			{
				method = peerClass.getDeclaredMethod("getScreenPixels", new Class<?>[] { Rectangle.class, int[].class });
				methodType = 1;
			}
			catch (Throwable ex) { }
		}

		if (methodType < 0)
		{
			try
			{
				method = peerClass.getDeclaredMethod("getScreenPixels", new Class<?>[] { Integer.TYPE, Rectangle.class, int[].class });
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
			catch (Throwable ex) { }
		}

		if (methodType < 0)
		{
			try
			{
				method = peerClass.getDeclaredMethod("getRGBPixelsImpl", new Class<?>[] { Class.forName("sun.awt.X11GraphicsConfig"), Integer.TYPE, Integer.TYPE, Integer.TYPE, Integer.TYPE, int[].class });
				methodType = 3;
				Field field = peerClass.getDeclaredField("xgc");
				try
				{
					field.setAccessible(true);
					methodParam = field.get(peer);
				}
				finally
				{
					field.setAccessible(false);
				}
			}
			catch (Throwable ex) { }
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
			
			//test screen capture
			int x = 0;
			int y = 0;
			int width = 1;
			int height = 1;
			int[] pixels = new int[1];
			
			try
			{
				if (getRGBPixelsMethodType == 0)
				{
					getRGBPixelsMethod.invoke(peer, new Object[] { Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
				}
				else if (getRGBPixelsMethodType == 1)
				{
					getRGBPixelsMethod.invoke(peer, new Object[] { new Rectangle(x, y, width, height), pixels });
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
					getRGBPixelsMethod.invoke(peer, new Object[] { getRGBPixelsMethodParam, new Rectangle(x, y, width, height), pixels });
				}
				else
				{
					getRGBPixelsMethod.invoke(peer, new Object[] { getRGBPixelsMethodParam, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
				}
			}
			catch (Throwable ex)
			{
				getRGBPixelsMethod = null;
				getRGBPixelsMethodType = -1;
				getRGBPixelsMethodParam = null;
			}
		}
		else
		{
			//System.out.println("WARNING: Failed to acquire direct method for grabbing pixels, please post this on the main thread!");
			//System.out.println();
			//System.out.println(peer.getClass().getName());
			//System.out.println();
			try
			{
				//Method[] methods = peer.getClass().getDeclaredMethods();
				//for (Method method1 : methods)
				//{
					//System.out.println(method1);
				//}
			}
			catch (Throwable ex) { }
		}
	}

	public static GraphicsDevice getMouseInfo(Point point)
	{
		if (!hasMouseInfoPeer)
		{
			hasMouseInfoPeer = true;
			try
			{
				Toolkit toolkit = Toolkit.getDefaultToolkit();
				Method method = toolkit.getClass().getDeclaredMethod("getMouseInfoPeer", new Class<?>[0]);
				try
				{
					method.setAccessible(true);
					mouseInfoPeer = (MouseInfoPeer) method.invoke(toolkit, new Object[0]);
				}
				finally
				{
					method.setAccessible(false);
				}
			}
			catch (Throwable ex) { }
		}
		if (mouseInfoPeer != null)
		{
			int device = mouseInfoPeer.fillPointWithCoords(point != null ? point : new Point());
			GraphicsDevice[] devices = GraphicsEnvironment.getLocalGraphicsEnvironment().getScreenDevices();
			return devices[device];
		}
		PointerInfo info = MouseInfo.getPointerInfo();
		if (point != null)
		{
			Point location = info.getLocation();
			point.x = location.x;
			point.y = location.y;
		}
		return info.getDevice();
	}

	public static int getNumberOfMouseButtons()
	{
		return MouseInfo.getNumberOfButtons();
	}

	public static GraphicsDevice getDefaultScreenDevice()
	{
		return GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice();
	}

	public static GraphicsDevice getScreenDevice()
	{
		return getMouseInfo(null);
	}

	public void mouseMove(int x, int y)
	{
		peer.mouseMove(x, y);
	}

	public void mousePress(int buttons)
	{
		peer.mousePress(buttons);
	}

	public void mouseRelease(int buttons)
	{
		peer.mouseRelease(buttons);
	}

	public void mouseWheel(int wheelAmt)
	{
		peer.mouseWheel(wheelAmt);
	}

	public void keyPress(int keycode)
	{
		peer.keyPress(keycode);
	}

	public void keyRelease(int keycode)
	{
		peer.keyRelease(keycode);
	}

	public int getRGBPixel(int x, int y)
	{
		return peer.getRGBPixel(x, y);
	}

	public int[] getRGBPixels(Rectangle bounds)
	{
		return peer.getRGBPixels(bounds);
	}

	public boolean getRGBPixels(int x, int y, int width, int height, int[] pixels)
	{
		if (getRGBPixelsMethod != null)
		{
			try
			{
				if (getRGBPixelsMethodType == 0)
				{
					getRGBPixelsMethod.invoke(peer, new Object[] { Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
				}
				else if (getRGBPixelsMethodType == 1)
				{
					getRGBPixelsMethod.invoke(peer, new Object[] { new Rectangle(x, y, width, height), pixels });
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
					getRGBPixelsMethod.invoke(peer, new Object[] { getRGBPixelsMethodParam, new Rectangle(x, y, width, height), pixels });
				}
				else
				{
					getRGBPixelsMethod.invoke(peer, new Object[] { getRGBPixelsMethodParam, Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), pixels });
				}
				return true;
			}
			catch (Throwable ex) {
				//ex.printStackTrace();
			}
		}
		//int[] tmp = getRGBPixels(new Rectangle(x, y, width, height));
		//System.arraycopy(tmp, 0, pixels, 0, width * height);
		return false;
	}

	public void dispose()
	{
		getRGBPixelsMethodParam = null;
		Method method = getRGBPixelsMethod;
		if (method != null)
		{
			getRGBPixelsMethod = null;
			try
			{
				method.setAccessible(false);
			}
			catch (Throwable ex) { }
		}
		//Using reflection now because of some peers not having ANY support at all (1.5)
		try
		{
			peer.getClass().getDeclaredMethod("dispose", new Class<?>[0]).invoke(peer, (Object)null);
		}
		catch (Throwable t) { }
	}

	protected void finalize() throws Throwable
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
	
	public final GraphicsDevice device;
	private final RobotPeer peer;

	private Object getRGBPixelsMethodParam;
	private int getRGBPixelsMethodType;
	private Method getRGBPixelsMethod;
	private static boolean hasMouseInfoPeer;
	private static MouseInfoPeer mouseInfoPeer;
	
	public boolean getDirectRGBPixelsMethodAvailable()
	{
		return getRGBPixelsMethod != null;
	}
}