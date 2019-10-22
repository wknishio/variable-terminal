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
import java.util.LinkedList;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.UIManager;

import org.vate.console.graphical.VTGraphicalConsoleFrame;

import com.sun.jna.Platform;

public class VTGlobalTextStyleManager
{
	private static final List<Component> monospaceds = new LinkedList<Component>();
	private static final List<Component> texts = new LinkedList<Component>();
	private static final List<Window> windows = new LinkedList<Window>();
	
	private static double FONT_SCALING_FACTOR;
	
	static
	{
		FONT_SCALING_FACTOR = 1;
		try
		{
			Class<?> runtime = Class.forName("java.lang.Runtime");
			Method version = runtime.getMethod("version", (Class<?>)null);
			version.toString();
			//boolean java9 = version.invoke(Runtime.getRuntime(), null) instanceof Object;
			//if (java9)
			//{
				
			//}
			//in java 9 and beyond font size scaling for hidpi displays is done by jvm
		}
		catch (Throwable t)
		{
			//java 8 or lesser detected, try to detect font dpi settings and scale accordingly
			try
			{
				if (Platform.isWindows())
				{
					FONT_SCALING_FACTOR = (Math.max(1.0, Toolkit.getDefaultToolkit().getScreenResolution() / 96.0));
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
					FONT_SCALING_FACTOR = (Math.max(1.0, new JLabel().getFont().getSize2D() / 15.0));
				}
			}
			catch (Throwable t1)
			{
				
			}
		}
	}
	
	private static Font windowFont = Font.decode("Dialog").deriveFont((float) ((((Font.decode("Dialog").getSize2D()) + (FONT_SCALING_FACTOR > 0 ? 0 : 0)) * FONT_SCALING_FACTOR) + (FONT_SCALING_FACTOR > 0 ? 0 : 0)));
	private static Font monospacedFont = Font.decode("Monospaced").deriveFont((float) ((((Font.decode("Monospaced").getSize2D()) + (FONT_SCALING_FACTOR > 0 ? 0 : 0)) * FONT_SCALING_FACTOR) + (FONT_SCALING_FACTOR > 0 ? 0 : 0)));
	private static float defaultWindowFontSize = windowFont.getSize2D();
	private static float defaultMonospacedFontSize = monospacedFont.getSize2D();
	
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
		texts.add(component);
	}
	
	public static void unregisterTextComponent(Component component)
	{
		texts.remove(component);
	}
	
	public static void registerWindow(Window window)
	{
		window.setFont(windowFont);
		windows.add(window);
	}
	
	public static void unregisterWindow(Window window)
	{
		windows.remove(window);
	}
	
	public static void defaultFontSize()
	{
		//System.out.println("FONT_SCALING_FACTOR:" + FONT_SCALING_FACTOR);
		//System.out.println("defaultMonospacedFontSize:" + defaultMonospacedFontSize);
		//System.out.println("defaultWindowFontSize:" + defaultWindowFontSize);
		monospacedFont = monospacedFont.deriveFont(defaultMonospacedFontSize);
		windowFont = windowFont.deriveFont(defaultWindowFontSize);
		updateComponents();
	}
	
	public static boolean isFontStyleBold()
	{
		//return false;
		return monospacedFont.isBold();
	}
	
	public static void enableFontStyleBold()
	{
//		Font nextFont = monospacedFonts.get(currentMonospacedFontIndex);
//		currentMonospacedFontIndex++;
//		if (currentMonospacedFontIndex >= monospacedFonts.size())
//		{
//			currentMonospacedFontIndex = 0;
//		}
//		nextFont = nextFont.deriveFont(windowFont.getSize2D());
		
		//System.out.println("newFont: [" + nextFont.getFontName() + "]");
		
		//monospacedFont = nextFont;
		//windowFont = nextFont;
		monospacedFont = monospacedFont.deriveFont(Font.BOLD);
		windowFont = windowFont.deriveFont(Font.BOLD);
		updateComponents();
	}
	
	public static void disableFontStyleBold()
	{
		monospacedFont = monospacedFont.deriveFont(Font.PLAIN);
		windowFont = windowFont.deriveFont(Font.PLAIN);
		updateComponents();
	}
	
	public static void increaseFontSize()
	{
		monospacedFont = monospacedFont.deriveFont((float) (monospacedFont.getSize2D() + 1));
		windowFont = windowFont.deriveFont((float) (windowFont.getSize2D() + 1));
		updateComponents();
	}
	
	public static void decreaseFontSize()
	{
		monospacedFont = monospacedFont.deriveFont((float) (monospacedFont.getSize2D() - 1));
		windowFont = windowFont.deriveFont((float) (windowFont.getSize2D() - 1));
		updateComponents();
	}
	
	public static void packComponents()
	{
		updateComponents();
	}
	
	private static void updateComponents()
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
				if (window instanceof Frame)
				{
					Frame frame = (Frame) window;
					MenuBar menubar = frame.getMenuBar();
					if (menubar != null)
					{
						menubar.setFont(windowFont);
					}
					frame.setMenuBar(null);
					frame.setMenuBar(menubar);
					if (window instanceof VTGraphicalConsoleFrame)
					{
						window.invalidate();
						window.pack();
					}
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
	}
}