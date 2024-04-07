package org.vash.vate.graphics.font;

import java.awt.Component;
import java.awt.Dialog;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.MenuBar;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.vash.vate.compatibility.VTArrays;
import org.vash.vate.console.VTConsole;
import org.vash.vate.reflection.VTReflectionUtils;

import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;

public class VTGlobalTextStyleManager
{
  private static final List<Window> windows = new ArrayList<Window>();
  private static final List<Component> monospaceds = new ArrayList<Component>();
  private static final List<Component> texts = new ArrayList<Component>();
  private static final List<Component> scaleds = new ArrayList<Component>();
  private static final List<List<Font>> lists = new ArrayList<List<Font>>();
  private static final List<List<Font>> defaultlists = new ArrayList<List<Font>>();
  
  public static float FONT_SCALING_FACTOR_DIALOG;
  public static float FONT_SCALING_FACTOR_MONOSPACED;
  public static float BASE_FONT_SIZE_DIALOG = 12F;
  public static float BASE_FONT_SIZE_MONOSPACED = 12F;
  public static int BASE_FONT_DPI = 96;
  
  private static boolean checked = false;
  public static boolean java9plus = false;
  
  public static void checkScaling()
  {
    if (checked)
    {
      return;
    }
    FONT_SCALING_FACTOR_DIALOG = 1;
    FONT_SCALING_FACTOR_MONOSPACED = 1;
    try
    {
      Class<?> runtime = Class.forName("java.lang.Runtime");
      Method version = runtime.getMethod("version");
      version.toString();
      java9plus = true;
      // System.out.println("java > 9 detected!");
      // boolean java9 = version.invoke(Runtime.getRuntime(), null) instanceof
      // Object;
      // if (java9)
      // {
      
      // }
      // in java 9 and beyond font size scaling for hidpi displays is done by
      // jvm, but
      // we override the scaling
    }
    catch (Throwable t)
    {
      // System.out.println("java < 9 detected!");
      // java 8 or lesser detected, try to detect font dpi settings and scale
      // accordingly
    }
    
    try
    {
      if (java9plus)
      {
        // System.setProperty("sun.java2d.dpiaware", "false");
        // return;
      }
      if (VTReflectionUtils.detectWindows())
      {
        // discover dpi with Toolkit.getScreenResolution();
        
        System.setProperty("sun.java2d.dpiaware", "true");
        System.setProperty("sun.java2d.ddscale", "false");
        System.setProperty("sun.java2d.uiScale.enabled", "false");
        System.setProperty("sun.java2d.uiScale", "1");
        System.setProperty("sun.java2d.win.uiScaleX", "1");
        System.setProperty("sun.java2d.win.uiScaleY", "1");
        FONT_SCALING_FACTOR_DIALOG = (Math.max(1.0F, Toolkit.getDefaultToolkit().getScreenResolution() / 96.0F));
        FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
        BASE_FONT_DPI = (int) (96 * FONT_SCALING_FACTOR_DIALOG);
        if (FONT_SCALING_FACTOR_DIALOG > 1.0)
        {
          BASE_FONT_SIZE_DIALOG = 13F;
          BASE_FONT_SIZE_MONOSPACED = 13F;
        }
      }
      else
      {
        float gtkScaleFactor = 0;
        
        try
        {
          gtkScaleFactor = (float) VTGtkUtilities.getScaleFactor();
        }
        catch (Throwable t)
        {
          
        }
        
        if (gtkScaleFactor > 0)
        {
          // use GtkUtilities if available
          
          FONT_SCALING_FACTOR_DIALOG = gtkScaleFactor;
          FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
          BASE_FONT_DPI = (int) (96 * FONT_SCALING_FACTOR_DIALOG);
          if (FONT_SCALING_FACTOR_DIALOG > 1.0)
          {
            BASE_FONT_SIZE_DIALOG = 13F;
            BASE_FONT_SIZE_MONOSPACED = 13F;
          }
          System.setProperty("sun.java2d.dpiaware", "true");
          System.setProperty("sun.java2d.ddscale", "false");
          System.setProperty("sun.java2d.uiScale.enabled", "false");
          System.setProperty("sun.java2d.uiScale", "1");
          System.setProperty("sun.java2d.win.uiScaleX", "1");
          System.setProperty("sun.java2d.win.uiScaleY", "1");
        }
        else
        {
          String gdkScaleEnv = null;
          String gdkDPIScaleEnv = null;
          float gdkScaleFactor = 0;
          float gdkDPIScaleFactor = 0;
          
          try
          {
            gdkScaleEnv = System.getenv("GDK_SCALE");
            if (gdkScaleEnv != null)
            {
              try
              {
                gdkScaleFactor = Float.parseFloat(gdkScaleEnv);
              }
              catch (Throwable t)
              {
                
              }
            }
            gdkDPIScaleEnv = System.getenv("GDK_DPI_SCALE");
            if (gdkDPIScaleEnv != null)
            {
              try
              {
                gdkDPIScaleFactor = Float.parseFloat(gdkDPIScaleEnv);
              }
              catch (Throwable t)
              {
                
              }
            }
            if (gdkScaleFactor > 0 && gdkDPIScaleFactor > 0)
            {
              gdkScaleFactor = gdkScaleFactor * gdkDPIScaleFactor;
            }
            if (gdkDPIScaleFactor > 0 && gdkScaleFactor == 0)
            {
              gdkScaleFactor = gdkDPIScaleFactor;
            }
          }
          catch (Throwable t)
          {
            
          }
          
          if (gdkScaleFactor > 0)
          {
            // use GTK scaling environment variables if available
            
            FONT_SCALING_FACTOR_DIALOG = gdkScaleFactor;
            FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
            BASE_FONT_DPI = (int) (96 * FONT_SCALING_FACTOR_DIALOG);
            if (FONT_SCALING_FACTOR_DIALOG > 1.0)
            {
              BASE_FONT_SIZE_DIALOG = 13F;
              BASE_FONT_SIZE_MONOSPACED = 13F;
            }
            System.setProperty("sun.java2d.dpiaware", "true");
            System.setProperty("sun.java2d.ddscale", "false");
            System.setProperty("sun.java2d.uiScale.enabled", "false");
            System.setProperty("sun.java2d.uiScale", "1");
            System.setProperty("sun.java2d.win.uiScaleX", "1");
            System.setProperty("sun.java2d.win.uiScaleY", "1");
          }
          else
          {
            String qtScaleEnv = null;
            String qtFontDPIEnv = null;
            float qtScaleFactor = 0;
            float qtFontDPI = 0;
            
            qtScaleEnv = System.getenv("QT_SCALE_FACTOR");
            if (qtScaleEnv != null)
            {
              try
              {
                qtScaleFactor = Float.parseFloat(qtScaleEnv);
              }
              catch (Throwable t)
              {
                
              }
            }
            qtFontDPIEnv = System.getenv("QT_FONT_DPI");
            if (qtFontDPIEnv != null)
            {
              try
              {
                qtFontDPI = Float.parseFloat(qtFontDPIEnv);
              }
              catch (Throwable t)
              {
                
              }
            }
            
            if (qtScaleFactor > 0 || qtFontDPI > 0)
            {
              // use QT scaling environment variables if available
              
              if (qtFontDPI == 0 && qtScaleFactor > 0)
              {
                FONT_SCALING_FACTOR_DIALOG = qtScaleFactor;
                FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
                BASE_FONT_DPI = (int) (96 * FONT_SCALING_FACTOR_DIALOG);
                if (FONT_SCALING_FACTOR_DIALOG > 1.0)
                {
                  BASE_FONT_SIZE_DIALOG = 13F;
                  BASE_FONT_SIZE_MONOSPACED = 13F;
                }
                System.setProperty("sun.java2d.dpiaware", "true");
                System.setProperty("sun.java2d.ddscale", "false");
                System.setProperty("sun.java2d.uiScale.enabled", "false");
                System.setProperty("sun.java2d.uiScale", "1");
                System.setProperty("sun.java2d.win.uiScaleX", "1");
                System.setProperty("sun.java2d.win.uiScaleY", "1");
              }
              if (qtFontDPI > 0)
              {
                FONT_SCALING_FACTOR_DIALOG = (Math.max(1.0F, qtFontDPI / 96.0F));
                FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
                BASE_FONT_DPI = (int) qtFontDPI;
                if (FONT_SCALING_FACTOR_DIALOG > 1.0)
                {
                  BASE_FONT_SIZE_DIALOG = 13F;
                  BASE_FONT_SIZE_MONOSPACED = 13F;
                }
                System.setProperty("sun.java2d.dpiaware", "true");
                System.setProperty("sun.java2d.ddscale", "false");
                System.setProperty("sun.java2d.uiScale.enabled", "false");
                System.setProperty("sun.java2d.uiScale", "1");
                System.setProperty("sun.java2d.win.uiScaleX", "1");
                System.setProperty("sun.java2d.win.uiScaleY", "1");
              }
            }
            else
            {
              if (java9plus)
              {
                // set everything unscaled and pixel based
                System.setProperty("sun.java2d.dpiaware", "true");
                System.setProperty("sun.java2d.ddscale", "false");
                System.setProperty("sun.java2d.uiScale.enabled", "false");
                System.setProperty("sun.java2d.uiScale", "1");
                System.setProperty("sun.java2d.win.uiScaleX", "1");
                System.setProperty("sun.java2d.win.uiScaleY", "1");
              }
              else
              {
                // set everything unscaled and pixel based
                System.setProperty("sun.java2d.dpiaware", "true");
                System.setProperty("sun.java2d.ddscale", "false");
                System.setProperty("sun.java2d.uiScale.enabled", "false");
                System.setProperty("sun.java2d.uiScale", "1");
                System.setProperty("sun.java2d.win.uiScaleX", "1");
                System.setProperty("sun.java2d.win.uiScaleY", "1");
              }
              try
              {
                FONT_SCALING_FACTOR_DIALOG = Math.max(1.0F, ((((float)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getNormalizingTransform().getScaleX()) * 72F) / 96F));
                FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
                BASE_FONT_DPI = (int) (96 * FONT_SCALING_FACTOR_DIALOG);
                if (FONT_SCALING_FACTOR_DIALOG > 1.0)
                {
                  BASE_FONT_SIZE_DIALOG = 13F;
                  BASE_FONT_SIZE_MONOSPACED = 13F;
                }
              }
              catch (Throwable t)
              {
                
              }
            }
          }
        }
      }
    }
    catch (Throwable t1)
    {
      
    }
    
    windowFont = Font.decode("Serif").deriveFont(round(BASE_FONT_SIZE_DIALOG * FONT_SCALING_FACTOR_DIALOG, 0.5F));
    monospacedFont = Font.decode("Monospaced").deriveFont(round(BASE_FONT_SIZE_MONOSPACED * FONT_SCALING_FACTOR_MONOSPACED, 0.5F));
    
    defaultWindowFontSize = windowFont.getSize2D();
    defaultMonospacedFontSize = monospacedFont.getSize2D();
    
    // System.out.println("monospacedFont:" + defaultMonospacedFontSize);
    // System.out.println("windowFont:" + defaultWindowFontSize);
    // System.out.println("FONT_SCALING_FACTOR_MONOSPACED:" +
    // FONT_SCALING_FACTOR_MONOSPACED);
    // System.out.println("FONT_SCALING_FACTOR_DIALOG:" +
    // FONT_SCALING_FACTOR_DIALOG);
    checked = true;
  }
  
  static
  {
    //checkScaling();
    // AWTTerminalFontConfiguration.setFontScalingFactor(FONT_SCALING_FACTOR);
  }
  
  private static Font windowFont;
  // Font.decode("Dialog").deriveFont((float) ((((Font.decode("Dialog
  // 12").getSize2D())))));
  // Font.decode("Monospaced").deriveFont(((float)
  // Math.ceil((((BASE_FONT_SIZE_MONOSPACED)) *
  // FONT_SCALING_FACTOR_MONOSPACED))));
  private static Font monospacedFont;
  private static float defaultWindowFontSize;
  // private static float defaultWindowFontSize = (float) (12.0 *
  // FONT_SCALING_FACTOR);
  private static float defaultMonospacedFontSize;
  private static boolean fontStyleBold = false;
  // private static float windowFontSize = defaultWindowFontSize;
  // private static List<Font> monospacedFonts;
  // private static int currentMonospacedFontIndex = 0;
  
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
      VTGlobalTextStyleManager.defaultComponentSize();
      return true;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE)
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
    // component.setFont(component.getFont().deriveFont(windowFontSize));
    texts.add(component);
  }
  
  public static void unregisterTextComponent(Component component)
  {
    texts.remove(component);
  }
  
  public static void registerWindow(Window window)
  {
    window.setFont(windowFont);
    // window.setFont(window.getFont().deriveFont(windowFontSize));
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
    defaultlists.add(Arrays.asList(list.toArray(new Font[] {})));
  }
  
  public static void defaultComponentSize()
  {
    // System.out.println("FONT_SCALING_FACTOR:" + FONT_SCALING_FACTOR);
    // System.out.println("defaultMonospacedFontSize:" +
    // defaultMonospacedFontSize);
    // System.out.println("defaultWindowFontSize:" + defaultWindowFontSize);
    updateComponents(true);
  }
  
  public static void packComponentSize()
  {
    // System.out.println("FONT_SCALING_FACTOR:" + FONT_SCALING_FACTOR);
    // System.out.println("defaultMonospacedFontSize:" +
    // defaultMonospacedFontSize);
    // System.out.println("defaultWindowFontSize:" + defaultWindowFontSize);
    updateComponents(false);
  }
  
  public static void defaultFontSize()
  {
    // System.out.println("FONT_SCALING_FACTOR:" + FONT_SCALING_FACTOR);
    fontStyleBold = false;
    monospacedFont = monospacedFont.deriveFont(Font.PLAIN, defaultMonospacedFontSize);
    // windowFontSize = defaultWindowFontSize;
    windowFont = windowFont.deriveFont(Font.PLAIN, defaultWindowFontSize);
    // System.out.println("monospacedFont:" + monospacedFont.getSize2D());
    // System.out.println("windowFont:" + windowFont.getSize2D());
    // plainScaleds();
    defaultLists();
    updateComponents(true);
  }
  
  public static boolean isFontStyleBold()
  {
    // return false;
    return fontStyleBold;
  }
  
  public static void enableFontStyleBold()
  {
    fontStyleBold = true;
    monospacedFont = monospacedFont.deriveFont(Font.BOLD, monospacedFont.getSize2D());
    windowFont = windowFont.deriveFont(Font.BOLD, windowFont.getSize2D());
    // System.out.println("monospacedFont:" + monospacedFont.getSize2D());
    // System.out.println("windowFont:" + windowFont.getSize2D());
    boldScaleds();
    boldLists();
    updateComponents(false);
  }
  
  public static void disableFontStyleBold()
  {
    fontStyleBold = false;
    monospacedFont = monospacedFont.deriveFont(Font.PLAIN, monospacedFont.getSize2D());
    windowFont = windowFont.deriveFont(Font.PLAIN, windowFont.getSize2D());
    // System.out.println("monospacedFont:" + monospacedFont.getSize2D());
    // System.out.println("windowFont:" + windowFont.getSize2D());
    plainScaleds();
    plainLists();
    updateComponents(false);
  }
  
  public static void increaseFontSize()
  {
    monospacedFont = monospacedFont.deriveFont((monospacedFont.getSize2D() + 0.5f));
    windowFont = windowFont.deriveFont((windowFont.getSize2D() + 0.5f));
    // windowFontSize = windowFontSize + 0.5f;
    // System.out.println("monospacedFont:" + monospacedFont.getSize2D());
    // System.out.println("windowFont:" + windowFont.getSize2D());
    increaseScaleds();
    increaseLists();
    updateComponents(false);
  }
  
  public static void decreaseFontSize()
  {
    monospacedFont = monospacedFont.deriveFont((monospacedFont.getSize2D() - 0.5f));
    windowFont = windowFont.deriveFont((windowFont.getSize2D() - 0.5f));
    // windowFontSize = windowFontSize - 0.5f;
    // System.out.println("monospacedFont:" + monospacedFont.getSize2D());
    // System.out.println("windowFont:" + windowFont.getSize2D());
    decreaseScaleds();
    decreaseLists();
    updateComponents(false);
  }
  
  private static void boldScaleds()
  {
    for (Component component : scaleds)
    {
      Font font = component.getFont();
      component.setFont(font.deriveFont(Font.BOLD, font.getSize2D()));
    }
  }
  
  private static void plainScaleds()
  {
    for (Component component : scaleds)
    {
      Font font = component.getFont();
      component.setFont(font.deriveFont(Font.PLAIN, font.getSize2D()));
    }
  }
  
  private static void increaseScaleds()
  {
    for (Component component : scaleds)
    {
      Font font = component.getFont();
      component.setFont(font.deriveFont((font.getSize2D() + 0.5f)));
    }
  }
  
  private static void decreaseScaleds()
  {
    for (Component component : scaleds)
    {
      Font font = component.getFont();
      component.setFont(font.deriveFont((font.getSize2D() - 0.5f)));
    }
  }
  
  private static void increaseLists()
  {
    for (List<Font> list : lists)
    {
      Font[] fonts = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
      list.clear();
      for (Font font : fonts)
      {
        list.add(font.deriveFont((font.getSize2D() + 0.5f)));
      }
    }
  }
  
  private static void decreaseLists()
  {
    for (List<Font> list : lists)
    {
      Font[] fonts = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
      list.clear();
      for (Font font : fonts)
      {
        list.add(font.deriveFont((font.getSize2D() - 0.5f)));
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
      Font[] fonts = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
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
      Font[] fonts = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
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
        // component.setFont(component.getFont().deriveFont(windowFontSize));
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
        // window.setFont(window.getFont().deriveFont(windowFontSize));
        if (window instanceof Frame)
        {
          Frame frame = (Frame) window;
          MenuBar menubar = frame.getMenuBar();
          // int frameState = frame.getExtendedState();
          // Point frameLocation = frame.getLocationOnScreen();
          if (menubar != null)
          {
            menubar.setFont(windowFont);
            // menubar.setFont(menubar.getFont().deriveFont(windowFontSize));
          }
          frame.setMenuBar(null);
          frame.setMenuBar(menubar);
          if (useDefaults && frame instanceof AWTTerminalFrame)
          {
            ((AWTTerminalFrame) frame).resetTerminalSize();
          }
          // if ((frameState & Frame.MAXIMIZED_BOTH) != 0)
          // {
          // frame.setBounds(frame.getBounds());
          // frame.setExtendedState(Frame.NORMAL);
          // frame.setBounds(frame.getGraphicsConfiguration().getDevice().getDefaultConfiguration().getBounds());
          // frame.setLocation(frameLocation);
          // }
          frame.invalidate();
          frame.pack();
          // frame.validate();
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
          if (focused != null)
          {
            focused.requestFocusInWindow();
          }
        }
      }
      catch (Throwable t)
      {
        
      }
    }
    VTConsole.refreshText();
  }
  
  private static float round(float num, float step)
  {
    float inv = 1 / step;
    return (float) Math.round(num * inv) / inv;
  }
}