package org.vate.graphics.font;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.MenuBar;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.UIManager;

import org.vate.compatibility.VTArrays;
import org.vate.console.VTConsole;

import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;
import com.sun.jna.Platform;

public class VTGlobalTextStyleManager
{
	private static final List<Window> windows = new LinkedList<Window>();
	private static final List<Component> monospaceds = new LinkedList<Component>();
	private static final List<Component> texts = new LinkedList<Component>();
	private static final List<Component> scaleds = new LinkedList<Component>();
	private static final List<List<Font>> lists = new LinkedList<List<Font>>();
	private static final List<List<Font>> defaultlists = new LinkedList<List<Font>>();
	
	public static double FONT_SCALING_FACTOR_WINDOW;
	public static double FONT_SCALING_FACTOR_MONOSPACED;
	
	private static boolean checked = false;
	
	public static void checkScaling()
	{
		if (checked)
		{
			return;
		}
		FONT_SCALING_FACTOR_WINDOW = 1;
		FONT_SCALING_FACTOR_MONOSPACED = 1;
		try
		{
			Class<?> runtime = Class.forName("java.lang.Runtime");
			Method version = runtime.getMethod("version");
			version.toString();
			//System.out.println("java > 9 detected!");
			//boolean java9 = version.invoke(Runtime.getRuntime(), null) instanceof Object;
			//if (java9)
			//{
				
			//}
			//in java 9 and beyond font size scaling for hidpi displays is done by jvm, but we override the scaling
		}
		catch (Throwable t)
		{
			//System.out.println("java < 9 detected!");
			//java 8 or lesser detected, try to detect font dpi settings and scale accordingly
			
		}
		
		try
		{
			if (Platform.isWindows())
			{
				FONT_SCALING_FACTOR_WINDOW = (Math.max(1.0, Toolkit.getDefaultToolkit().getScreenResolution() / 96.0));
				//if (FONT_SCALING_FACTOR_WINDOW > 1.0)
				//{
					//FONT_SCALING_FACTOR = FONT_SCALING_FACTOR * (7d / 6d);
					//FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_WINDOW * (7d / 6d);
					//FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_WINDOW;
					//FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_WINDOW;
					//FONT_SCALING_FACTOR_MONOSPACED = (Math.max(1.0, Toolkit.getDefaultToolkit().getScreenResolution() / 72.0));
				//}
				//else
				//{
					//FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_WINDOW;
				//}
				//if (FONT_SCALING_FACTOR > 1.0)
				//{
					//FONT_SCALING_FACTOR = FONT_SCALING_FACTOR * (1.0 + (1.0 / 4.0));
				//}
				FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_WINDOW * (7d / 6d);
			}
			else
			{
				try
				{
					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
				}
				catch (Throwable t1)
				{
					
				}
				FONT_SCALING_FACTOR_WINDOW = (Math.max(1.0, new JLabel().getFont().getSize2D() / 15.0));
				FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_WINDOW;
			}
		}
		catch (Throwable t1)
		{
			
		}
		System.setProperty("sun.java2d.dpiaware", "false");
		System.setProperty("sun.java2d.ddscale", "false");
		System.setProperty("sun.java2d.uiScale.enabled", "false");
		System.setProperty("sun.java2d.uiScale", "1.0");
		checked = true;
	}
	
	static
	{
		checkScaling();
		//AWTTerminalFontConfiguration.setFontScalingFactor(FONT_SCALING_FACTOR);
	}
	
	private static volatile Font windowFont = Font.decode("Dialog").deriveFont((float) ((((Font.decode("Dialog 12").getSize2D()) + (FONT_SCALING_FACTOR_WINDOW > 0 ? 0 : 0)) * FONT_SCALING_FACTOR_WINDOW) + (FONT_SCALING_FACTOR_WINDOW > 0 ? 0 : 0)));
	//private static volatile Font windowFont = Font.decode("Dialog").deriveFont((float) ((((Font.decode("Dialog 12").getSize2D())))));
	private static volatile Font monospacedFont = Font.decode("Monospaced").deriveFont((float) ((((Font.decode("Monospaced 12").getSize2D()) + (FONT_SCALING_FACTOR_MONOSPACED > 0 ? 0 : 0)) * FONT_SCALING_FACTOR_MONOSPACED) + (FONT_SCALING_FACTOR_MONOSPACED > 0 ? 0 : 0)));
	private static float defaultWindowFontSize = windowFont.getSize2D();
	//private static float defaultWindowFontSize = (float) (12.0 * FONT_SCALING_FACTOR);
	private static float defaultMonospacedFontSize = monospacedFont.getSize2D();
	private static volatile boolean fontStyleBold = false;
	//private static float windowFontSize = defaultWindowFontSize;
	//private static List<Font> monospacedFonts;
	//private static int currentMonospacedFontIndex = 0;
	
//	static
//	{
//		monospacedFonts = new LinkedList<Font>();
//		Font allFonts[] = GraphicsEnvironment.getLocalGraphicsEnvironment().getAllFonts();
//		FontRenderContext frc = new FontRenderContext(null, RenderingHints.VALUE_TEXT_ANTIALIAS_DEFAULT, RenderingHints.VALUE_FRACTIONALMETRICS_DEFAULT);
//		
//		for (Font font : allFonts)
//		{
//			Rectangle2D iBounds = font.getStringBounds("i", frc);
//			Rectangle2D mBounds = font.getStringBounds("m", frc);
//			if (iBounds.getWidth() == mBounds.getWidth())
//			{
//				monospacedFonts.add(font);
//				//System.out.println("MonoSpacedFont: [" + font.getFontName() + "]");
//			}
//		}
//	}
	
	public static boolean processKeyEvent(KeyEvent e)
	{
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
		{
			e.consume();
			VTGlobalTextStyleManager.decreaseFontSize();
			return true;
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_UP)
		{
			e.consume();
			VTGlobalTextStyleManager.increaseFontSize();
			return true;
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME)
		{
			e.consume();
			VTGlobalTextStyleManager.defaultFontSize();
			return true;
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE)
		{
			e.consume();
			VTGlobalTextStyleManager.defaultComponentSize();
			return true;
		}
		if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END)
		{
			e.consume();
			if (VTGlobalTextStyleManager.isFontStyleBold())
			{
				VTGlobalTextStyleManager.disableFontStyleBold();
			}
			else
			{
				VTGlobalTextStyleManager.enableFontStyleBold();
			}
//			updateComponents();
			return true;
		}
		return false;
	}
	
	public static void registerMonospacedComponent(Component component)
	{
		component.setFont(monospacedFont);
		monospaceds.add(component);
	}
	
	public static void unregisterMonospacedComponent(Component component)
	{
		monospaceds.remove(component);
	}
	
	public static void registerTextComponent(Component component)
	{
		component.setFont(windowFont);
		//component.setFont(component.getFont().deriveFont(windowFontSize));
		texts.add(component);
	}
	
	public static void unregisterTextComponent(Component component)
	{
		texts.remove(component);
	}
	
	public static void registerWindow(Window window)
	{
		window.setFont(windowFont);
		//window.setFont(window.getFont().deriveFont(windowFontSize));
		windows.add(window);
	}
	
	public static void unregisterWindow(Window window)
	{
		windows.remove(window);
	}
	
	public static void registerScaledComponent(Component component)
	{
		scaleds.add(component);
	}
	
	public static void unregisterScaledComponent(Component component)
	{
		scaleds.remove(component);
	}
	
	public static void registerFontList(List<Font> list)
	{
		lists.add(list);
		defaultlists.add(Arrays.asList(list.toArray(new Font[]{})));
	}
	
	public static void defaultComponentSize()
	{
		//System.out.println("FONT_SCALING_FACTOR:" + FONT_SCALING_FACTOR);
		//System.out.println("defaultMonospacedFontSize:" + defaultMonospacedFontSize);
		//System.out.println("defaultWindowFontSize:" + defaultWindowFontSize);
		updateComponents(true);
	}
	
	public static void defaultFontSize()
	{
		//System.out.println("FONT_SCALING_FACTOR:" + FONT_SCALING_FACTOR);
		//System.out.println("defaultMonospacedFontSize:" + defaultMonospacedFontSize);
		//System.out.println("defaultWindowFontSize:" + defaultWindowFontSize);
		fontStyleBold = false;
		monospacedFont = monospacedFont.deriveFont(defaultMonospacedFontSize).deriveFont(Font.PLAIN);
		//windowFontSize = defaultWindowFontSize;
		windowFont = windowFont.deriveFont(defaultWindowFontSize).deriveFont(Font.PLAIN);
		//plainScaleds();
		defaultLists();
		updateComponents(true);
	}
	
	public static boolean isFontStyleBold()
	{
		//return false;
		return fontStyleBold;
	}
	
	public static void enableFontStyleBold()
	{
		fontStyleBold = true;
		monospacedFont = monospacedFont.deriveFont(Font.BOLD);
		windowFont = windowFont.deriveFont(Font.BOLD);
		boldScaleds();
		boldLists();
		updateComponents(false);
	}
	
	public static void disableFontStyleBold()
	{
		fontStyleBold = false;
		monospacedFont = monospacedFont.deriveFont(Font.PLAIN);
		windowFont = windowFont.deriveFont(Font.PLAIN);
		plainScaleds();
		plainLists();
		updateComponents(false);
	}
	
	public static void increaseFontSize()
	{
		monospacedFont = monospacedFont.deriveFont((float) (monospacedFont.getSize2D() + 1));
		windowFont = windowFont.deriveFont((float) (windowFont.getSize2D() + 1));
		//windowFontSize = windowFontSize + 1;
		increaseScaleds();
		increaseLists();
		updateComponents(false);
	}
	
	public static void decreaseFontSize()
	{
		monospacedFont = monospacedFont.deriveFont((float) (monospacedFont.getSize2D() - 1));
		windowFont = windowFont.deriveFont((float) (windowFont.getSize2D() - 1));
		//windowFontSize = windowFontSize - 1;
		decreaseScaleds();
		decreaseLists();
		updateComponents(false);
	}
	
	private static void boldScaleds()
	{
		for (Component component : scaleds)
		{
			Font font = component.getFont();
			component.setFont(font.deriveFont(Font.BOLD));
		}
	}
	
	private static void plainScaleds()
	{
		for (Component component : scaleds)
		{
			Font font = component.getFont();
			component.setFont(font.deriveFont(Font.PLAIN));
		}
	}
	
	private static void increaseScaleds()
	{
		for (Component component : scaleds)
		{
			Font font = component.getFont();
			component.setFont(font.deriveFont((float) (font.getSize2D() + 1)));
		}
	}
	
	private static void decreaseScaleds()
	{
		for (Component component : scaleds)
		{
			Font font = component.getFont();
			component.setFont(font.deriveFont((float) (font.getSize2D() - 1)));
		}
	}
	
	private static void increaseLists()
	{
		for (List<Font> list : lists)
		{
			Font[] fonts = VTArrays.copyOf(list.toArray(new Font[]{}), list.size());
			list.clear();
			for (Font font : fonts)
			{
				list.add(font.deriveFont((float) (font.getSize2D() + 1)));
			}
		}
	}
	
	private static void decreaseLists()
	{
		for (List<Font> list : lists)
		{
			Font[] fonts = VTArrays.copyOf(list.toArray(new Font[]{}), list.size());
			list.clear();
			for (Font font : fonts)
			{
				list.add(font.deriveFont((float) (font.getSize2D() - 1)));
			}
		}
	}
	
	private static void defaultLists()
	{
		int i = 0;
		for (List<Font> list : lists)
		{
			list.clear();
			list.addAll(defaultlists.get(i++));
		}
	}
	
	private static void boldLists()
	{
		for (List<Font> list : lists)
		{
			Font[] fonts = VTArrays.copyOf(list.toArray(new Font[]{}), list.size());
			list.clear();
			for (Font font : fonts)
			{
				list.add(font.deriveFont(Font.BOLD, font.getSize2D()));
			}
		}
	}
	
	private static void plainLists()
	{
		for (List<Font> list : lists)
		{
			Font[] fonts = VTArrays.copyOf(list.toArray(new Font[]{}), list.size());
			list.clear();
			for (Font font : fonts)
			{
				list.add(font.deriveFont(Font.PLAIN, font.getSize2D()));
			}
		}
	}
	
	private static void updateComponents(boolean useDefaults)
	{
		for (Component component : monospaceds)
		{
			try
			{
				component.setFont(monospacedFont);
			}
			catch (Throwable t)
			{
				
			}
		}
		for (Component component : texts)
		{
			try
			{
				component.setFont(windowFont);
				//component.setFont(component.getFont().deriveFont(windowFontSize));
			}
			catch (Throwable t)
			{
				
			}
		}
		for (Window window : windows)
		{
			try
			{
				window.setFont(windowFont);
				//window.setFont(window.getFont().deriveFont(windowFontSize));
				if (window instanceof Frame)
				{
					Frame frame = (Frame) window;
					MenuBar menubar = frame.getMenuBar();
					//int frameState = frame.getExtendedState();
					//Point frameLocation = frame.getLocationOnScreen();
					if (menubar != null)
					{
						menubar.setFont(windowFont);
						//menubar.setFont(menubar.getFont().deriveFont(windowFontSize));
					}
					frame.setMenuBar(null);
					frame.setMenuBar(menubar);
					if (useDefaults && frame instanceof AWTTerminalFrame)
					{
						((AWTTerminalFrame)frame).resetTerminalSize();
					}
					//if ((frameState & Frame.MAXIMIZED_BOTH) != 0)
					//{
						//frame.setBounds(frame.getBounds());
						//frame.setExtendedState(Frame.NORMAL);
						//frame.setBounds(frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds());
						//frame.setLocation(frameLocation);
					//}
					frame.invalidate();
					frame.pack();
					//frame.validate();
				}
				if (window instanceof Dialog)
				{
					Component[] elements = window.getComponents();
					Component focused = window.getFocusOwner();
					window.removeAll();
					window.invalidate();
					window.pack();
					for (Component element : elements)
					{
						window.add(element);
					}
					window.invalidate();
					window.pack();
					focused.requestFocus();
				}
			}
			catch (Throwable t)
			{
				
			}
		}
		VTConsole.refreshText();
	}
}