package org.vash.vate.graphics;

import java.awt.RenderingHints;
import java.awt.Toolkit;
import java.awt.image.BufferedImage;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.imageio.ImageIO;

import org.vash.vate.VTSystem;
import org.vash.vate.graphics.font.VTFontManager;

public class VTGraphicsSystem
{
  public static final Map<RenderingHints.Key, Object> VT_GRAPHICS_RENDERING_HINTS;
  
  public static BufferedImage remoteIcon;
  public static BufferedImage terminalIcon;
  public static BufferedImage desktopIcon;
  
  private static boolean initialized = false;
  
  static
  {
    if (!initialized)
    {
      initialize();
    }
    
    VT_GRAPHICS_RENDERING_HINTS = new LinkedHashMap<RenderingHints.Key, Object>();
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_SPEED);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_OFF);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_COLOR_RENDERING, RenderingHints.VALUE_COLOR_RENDER_SPEED);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_DITHERING, RenderingHints.VALUE_DITHER_DISABLE);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_ALPHA_INTERPOLATION, RenderingHints.VALUE_ALPHA_INTERPOLATION_SPEED);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
    VT_GRAPHICS_RENDERING_HINTS.put(RenderingHints.KEY_FRACTIONALMETRICS, RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
    
    try
    {
      remoteIcon = ImageIO.read(VTSystem.class.getResourceAsStream("/org/vash/vate/console/graphical/resource/remote.png"));
    }
    catch (Throwable e)
    {
      //remoteIcon = null;
    }
    
    try
    {
      terminalIcon = ImageIO.read(VTSystem.class.getResourceAsStream("/org/vash/vate/console/graphical/resource/terminal.png"));
    }
    catch (Throwable e)
    {
      //terminalIcon = null;
    }
    
    try
    {
      desktopIcon = ImageIO.read(VTSystem.class.getResourceAsStream("/org/vash/vate/console/graphical/resource/desktop.png"));
    }
    catch (Throwable e)
    {
      //desktopIcon = null;
    }
  }
  
  public static final void initialize()
  {
    VTFontManager.checkScaling();
    
    try
    {
      Toolkit.getDefaultToolkit().setDynamicLayout(false);
    }
    catch (Throwable t)
    {
      
    }
    
    ImageIO.setUseCache(false);
    
    initialized = true;
  }
}