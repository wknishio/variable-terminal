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
import java.util.List;

import org.vash.vate.com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;
import org.vash.vate.com.googlecode.lanterna.terminal.swing.AWTTerminalPanel;
import org.vash.vate.compatibility.VTArrays;
import org.vash.vate.console.VTConsole;
import org.vash.vate.reflection.VTReflectionUtils;

public class VTFontManager
{
  private static final List<Window> windows = new ArrayList<Window>();
  private static final List<Component> monospaceds = new ArrayList<Component>();
  private static final List<Component> inputs = new ArrayList<Component>();
  //private static final List<Component> scaleds = new ArrayList<Component>();
  private static final List<List<Font>> lists = new ArrayList<List<Font>>();
  private static final List<List<Font>> defaultlists = new ArrayList<List<Font>>();
  private static final List<VTConsole> consoles = new ArrayList<VTConsole>();
  
  public static float FONT_SCALING_FACTOR_DIALOG;
  public static float FONT_SCALING_FACTOR_MONOSPACED;
  public static float BASE_FONT_SIZE_DIALOG = 12F;
  public static float BASE_FONT_SIZE_MONOSPACED = 12F;
  public static int BASE_FONT_DPI = 96;
  
  private static boolean checked = false;
  public static boolean java9plus = false;
  private static boolean loadedCustomMonospacedFont = false;
  public static Font CUSTOM_MONOSPACED_FONT_PLAIN;
  public static Font CUSTOM_MONOSPACED_FONT_BOLD;
  
  private static Font windowFont;
  //private static Font windowFontPlain;
  //private static Font windowFontBold;
  private static Font inputFont;
  //private static Font inputFontPlain;
  //private static Font inputFontBold;
  private static Font monospacedFont;
  //private static Font monospacedFontPlain;
  //private static Font monospacedFontBold;
  private static Font customMonospacedFont;
  private static Font customMonospacedFontPlain;
  private static Font customMonospacedFontBold;
  private static float defaultWindowFontSize;
  private static float defaultMonospacedFontSize;
  private static boolean fontStyleBold = false;
  
  private static void loadCustomMonospacedFont()
  {
    try
    {
      CUSTOM_MONOSPACED_FONT_PLAIN = Font.createFont(Font.TRUETYPE_FONT, VTFontManager.class.getResourceAsStream("/org/vash/vate/graphics/font/DejaVuSansMono.ttf")).deriveFont(round(BASE_FONT_SIZE_MONOSPACED * FONT_SCALING_FACTOR_MONOSPACED, 1F, FONT_SCALING_FACTOR_MONOSPACED));
      CUSTOM_MONOSPACED_FONT_BOLD = Font.createFont(Font.TRUETYPE_FONT, VTFontManager.class.getResourceAsStream("/org/vash/vate/graphics/font/DejaVuSansMono-Bold.ttf")).deriveFont(round(BASE_FONT_SIZE_MONOSPACED * FONT_SCALING_FACTOR_MONOSPACED, 1F, FONT_SCALING_FACTOR_MONOSPACED));
      loadedCustomMonospacedFont = true;
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
  }
  
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
      Class<?> runtimeClass = Class.forName("java.lang.Runtime");
      Method versionMethod = runtimeClass.getMethod("version");
      versionMethod.toString();
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
          BASE_FONT_SIZE_DIALOG = 12F;
          BASE_FONT_SIZE_MONOSPACED = 12F;
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
            BASE_FONT_SIZE_DIALOG = 12F;
            BASE_FONT_SIZE_MONOSPACED = 12F;
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
          if (java9plus)
          {
            
          }
          else
          {
            
          }
          // set everything unscaled and pixel based
          System.setProperty("sun.java2d.dpiaware", "true");
          System.setProperty("sun.java2d.ddscale", "false");
          System.setProperty("sun.java2d.uiScale.enabled", "false");
          System.setProperty("sun.java2d.uiScale", "1");
          System.setProperty("sun.java2d.win.uiScaleX", "1");
          System.setProperty("sun.java2d.win.uiScaleY", "1");
          try
          {
            FONT_SCALING_FACTOR_DIALOG = Math.max(1.0F, ((((float)GraphicsEnvironment.getLocalGraphicsEnvironment().getDefaultScreenDevice().getDefaultConfiguration().getNormalizingTransform().getScaleX()) * 72F) / 96F));
            FONT_SCALING_FACTOR_MONOSPACED = FONT_SCALING_FACTOR_DIALOG;
            BASE_FONT_DPI = (int) (96 * FONT_SCALING_FACTOR_DIALOG);
            if (FONT_SCALING_FACTOR_DIALOG > 1.0)
            {
              BASE_FONT_SIZE_DIALOG = 12F;
              BASE_FONT_SIZE_MONOSPACED = 12F;
            }
          }
          catch (Throwable t)
          {
            
          }
        }
      }
    }
    catch (Throwable t1)
    {
      
    }
    
    monospacedFont = Font.decode("Monospaced").deriveFont(round(BASE_FONT_SIZE_MONOSPACED * FONT_SCALING_FACTOR_MONOSPACED, 1F, FONT_SCALING_FACTOR_MONOSPACED));
    //monospacedFontPlain = monospacedFont;
    //monospacedFontBold = monospacedFont.deriveFont(Font.BOLD, monospacedFontPlain.getSize2D());
    
    windowFont = Font.decode("Dialog").deriveFont(round(BASE_FONT_SIZE_DIALOG * FONT_SCALING_FACTOR_DIALOG, 1F, FONT_SCALING_FACTOR_DIALOG));
    //windowFontPlain = windowFont;
    //windowFontBold = windowFont.deriveFont(Font.BOLD, windowFontPlain.getSize2D());
    
    inputFont = Font.decode("DialogInput").deriveFont(round(BASE_FONT_SIZE_DIALOG * FONT_SCALING_FACTOR_DIALOG, 1F, FONT_SCALING_FACTOR_DIALOG));
    //inputFontPlain = inputFont;
    //inputFontBold = inputFont.deriveFont(Font.BOLD, inputFontPlain.getSize2D());
    
    if (!loadedCustomMonospacedFont)
    {
      loadCustomMonospacedFont();
      if (!loadedCustomMonospacedFont)
      {
        CUSTOM_MONOSPACED_FONT_PLAIN = monospacedFont;
        CUSTOM_MONOSPACED_FONT_BOLD = monospacedFont.deriveFont(Font.BOLD);
      }
      customMonospacedFontPlain = CUSTOM_MONOSPACED_FONT_PLAIN;
      customMonospacedFontBold = CUSTOM_MONOSPACED_FONT_BOLD;
      customMonospacedFont = customMonospacedFontPlain;
    }
    
    defaultWindowFontSize = windowFont.getSize2D();
    defaultMonospacedFontSize = monospacedFont.getSize2D();
    
    checked = true;
  }
  
  static
  {
    //checkScaling();
    //AWTTerminalFontConfiguration.setFontScalingFactor(FONT_SCALING_FACTOR);
  }
  
  public static boolean processKeyEvent(KeyEvent e)
  {
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_DOWN)
    {
      e.consume();
      VTFontManager.decreaseFontSize();
      return true;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_PAGE_UP)
    {
      e.consume();
      VTFontManager.increaseFontSize();
      return true;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME)
    {
      e.consume();
      VTFontManager.packComponentSize();
      return true;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE)
    {
      e.consume();
      VTFontManager.defaultFontSize();
      return true;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END)
    {
      e.consume();
      if (VTFontManager.isFontStyleBold())
      {
        VTFontManager.disableFontStyleBold();
      }
      else
      {
        VTFontManager.enableFontStyleBold();
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
  
  public static void registerInputComponent(Component component)
  {
    component.setFont(inputFont);
    inputs.add(component);
  }
  
  public static void unregisterInputComponent(Component component)
  {
    inputs.remove(component);
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
  
//  public static void registerScaledComponent(Component component)
//  {
//    scaleds.add(component);
//  }
//  
//  public static void unregisterScaledComponent(Component component)
//  {
//    scaleds.remove(component);
//  }
  
  public static void registerFontList(List<Font> list)
  {
    lists.add(list);
    List<Font> defaultList = new ArrayList<Font>();
    for (Font font : list)
    {
      defaultList.add(font.deriveFont(defaultMonospacedFontSize));
    }
    defaultlists.add(defaultList);
  }
  
  public static void registerConsole(VTConsole console)
  {
    consoles.add(console);
  }
  
  public static void unregisterConsole(VTConsole console)
  {
    consoles.remove(console);
  }
  
//  public static void defaultComponentSize()
//  {
//    updateComponents(false);
//  }
  
  public static void packComponentSize()
  {
    updateComponents(true);
  }
  
  public static void defaultFontSize()
  {
    fontStyleBold = false;
    customMonospacedFont = customMonospacedFontPlain.deriveFont(defaultMonospacedFontSize);
    monospacedFont = monospacedFont.deriveFont(Font.PLAIN, defaultMonospacedFontSize);
    windowFont = windowFont.deriveFont(Font.PLAIN, defaultWindowFontSize);
    inputFont = inputFont.deriveFont(Font.PLAIN, defaultWindowFontSize);
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
    customMonospacedFont = customMonospacedFontBold.deriveFont(customMonospacedFont.getSize2D());
    monospacedFont = monospacedFont.deriveFont(Font.BOLD, monospacedFont.getSize2D());
    windowFont = windowFont.deriveFont(Font.BOLD, windowFont.getSize2D());
    inputFont = inputFont.deriveFont(Font.BOLD, inputFont.getSize2D());
    //boldScaleds();
    boldLists();
    updateComponents(false);
  }
  
  public static void disableFontStyleBold()
  {
    fontStyleBold = false;
    customMonospacedFont = customMonospacedFontPlain.deriveFont(customMonospacedFont.getSize2D());
    monospacedFont = monospacedFont.deriveFont(Font.PLAIN, monospacedFont.getSize2D());
    windowFont = windowFont.deriveFont(Font.PLAIN, windowFont.getSize2D());
    inputFont = inputFont.deriveFont(Font.PLAIN, inputFont.getSize2D());
    //plainScaleds();
    plainLists();
    updateComponents(false);
  }
  
  public static void increaseFontSize()
  {
    customMonospacedFont = customMonospacedFont.deriveFont((customMonospacedFont.getSize2D() + 1.0F));
    monospacedFont = monospacedFont.deriveFont((monospacedFont.getSize2D() + 1.0F));
    windowFont = windowFont.deriveFont((windowFont.getSize2D() + 1.0F));
    inputFont = inputFont.deriveFont((inputFont.getSize2D() + 1.0F));
    //increaseScaleds();
    increaseLists();
    updateComponents(false);
  }
  
  public static void decreaseFontSize()
  {
    customMonospacedFont = customMonospacedFont.deriveFont((customMonospacedFont.getSize2D() - 1.0F));
    monospacedFont = monospacedFont.deriveFont((monospacedFont.getSize2D() - 1.0F));
    windowFont = windowFont.deriveFont((windowFont.getSize2D() - 1.0F));
    inputFont = inputFont.deriveFont((inputFont.getSize2D() - 1.0F));
    //decreaseScaleds();
    decreaseLists();
    updateComponents(false);
  }
  
//  private static void boldScaleds()
//  {
//    for (Component component : scaleds)
//    {
//      Font font = component.getFont();
//      component.setFont(font.deriveFont(Font.BOLD, font.getSize2D()));
//    }
//  }
//  
//  private static void plainScaleds()
//  {
//    for (Component component : scaleds)
//    {
//      Font font = component.getFont();
//      component.setFont(font.deriveFont(Font.PLAIN, font.getSize2D()));
//    }
//  }
//  
//  private static void increaseScaleds()
//  {
//    for (Component component : scaleds)
//    {
//      Font font = component.getFont();
//      component.setFont(font.deriveFont((font.getSize2D() + 1.0F)));
//    }
//  }
//  
//  private static void decreaseScaleds()
//  {
//    for (Component component : scaleds)
//    {
//      Font font = component.getFont();
//      component.setFont(font.deriveFont((font.getSize2D() - 1.0F)));
//    }
//  }
  
  private static void increaseLists()
  {
    for (List<Font> list : lists)
    {
      Font[] fonts = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
      list.clear();
      for (Font font : fonts)
      {
        list.add(font.deriveFont((font.getSize2D() + 1.0F)));
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
        list.add(font.deriveFont((font.getSize2D() - 1.0F)));
      }
    }
  }
  
  private static void defaultLists()
  {
    int i = 0;
    for (List<Font> list : lists)
    {
      list.clear();
      List<Font> defaultList = defaultlists.get(i++);
      for (Font font : defaultList)
      {
        list.add(font);
      }
    }
  }
  
  private static void boldLists()
  {
    for (List<Font> list : lists)
    {
      float size2d = list.get(0).getSize2D();
      list.remove(0);
      Font[] copy = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
      list.clear();
      list.add(CUSTOM_MONOSPACED_FONT_BOLD.deriveFont(size2d));
      for (Font font : copy)
      {
        list.add(font.deriveFont(Font.BOLD, font.getSize2D()));
      }
    }
  }
  
  private static void plainLists()
  {
    for (List<Font> list : lists)
    {
      float size2d = list.get(0).getSize2D();
      list.remove(0);
      Font[] copy = VTArrays.copyOf(list.toArray(new Font[] {}), list.size());
      list.clear();
      list.add(CUSTOM_MONOSPACED_FONT_PLAIN.deriveFont(size2d));
      for (Font font : copy)
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
        if (useDefaults && component instanceof AWTTerminalPanel)
        {
          ((AWTTerminalPanel) component).resetTerminalSize();
        }
        else if (component instanceof AWTTerminalPanel)
        {
          ((AWTTerminalPanel) component).resetPaddingSize();
        }
      }
      catch (Throwable t)
      {
        
      }
    }
    for (Component component : inputs)
    {
      try
      {
        component.setFont(inputFont);
        if (useDefaults && component instanceof AWTTerminalPanel)
        {
          ((AWTTerminalPanel) component).resetTerminalSize();
        }
        else if (component instanceof AWTTerminalPanel)
        {
          ((AWTTerminalPanel) component).resetPaddingSize();
        }
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
          int frameState = frame.getExtendedState();
          MenuBar menubar = frame.getMenuBar();
          //frame.setMenuBar(null);
          //frame.invalidate();
          //frame.pack();
          if (menubar != null)
          {
            menubar.setFont(windowFont);
          }
          //frame.setMenuBar(menubar);
          //frame.invalidate();
          //frame.pack();
          if ((frameState & Frame.MAXIMIZED_HORIZ) != 0 || (frameState & Frame.MAXIMIZED_VERT) != 0)
          {
            //frame.setExtendedState(Frame.NORMAL);
            //frame.setExtendedState(frameState);
            if (frame instanceof AWTTerminalFrame)
            {
              ((AWTTerminalFrame) frame).resizePaddingSize();
            }
            frame.invalidate();
            frame.validate();
          }
          else
          {
            if (useDefaults && frame instanceof AWTTerminalFrame)
            {
              ((AWTTerminalFrame) frame).resetTerminalSize();
            }
            else if (frame instanceof AWTTerminalFrame)
            {
              ((AWTTerminalFrame) frame).resetPaddingSize();
            }
            frame.invalidate();
            frame.pack();
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
    //VTSystemConsole.refreshText();
    for (VTConsole console : consoles.toArray(new VTConsole[] {}))
    {
      console.refreshText();
    }
  }
  
  private static float round(float num, float step, float factor)
  {
    float inv = 1 / step;
    return (float) Math.round(num * inv) / inv;
  }
}