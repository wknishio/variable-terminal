package org.vash.vate.graphics.font;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.Pointer;

public class GtkUtilities
{
  /**
   * Initializes Gtk2/3 and returns the desktop scaling factor, usually 1.0 or
   * 2.0
   */
  public static double getScaleFactor()
  {
    GTK gtkHandle = getGtkInstance();
    if (gtkHandle != null && gtkHandle.gtk_init_check(0, null))
    {
      // System.out.println("Initialized Gtk");
      
      if (gtkHandle instanceof GTK2)
      {
        return getGtk2ScaleFactor((GTK2) gtkHandle);
      }
      else if (gtkHandle instanceof GTK3)
      {
        return getGtk3ScaleFactor((GTK3) gtkHandle);
      }
    }
    else
    {
      // System.err.println("An error occurred initializing the Gtk library");
    }
    return 0;
  }
  
  private static GTK getGtkInstance()
  {
    // System.out.println("Finding preferred Gtk version...");//
    // if (GTK3_INSTANCE != null)
    // {
    // return GTK3_INSTANCE;
    // }
    if (GTK2_INSTANCE != null)
    {
      return GTK2_INSTANCE;
    }
    return null;
//        switch(getGtkMajorVersion()) {
//            case 2:
//                return GTK2.INSTANCE;
//            case 3:
//                return GTK3.INSTANCE;
//            default:
//                //System.err.println("Not a compatible Gtk version");
//        }
//        return null;
  }
  
  /**
   * Get the major version of Gtk (e.g. 2, 3) UNIXToolkit is unavailable on
   * Windows or Mac; reflection is required.
   * 
   * @return Major version if found, zero if not.
   */
//    private static int getGtkMajorVersion() {
//        try {
//            Class toolkitClass = Class.forName("sun.awt.UNIXToolkit");
//            Method versionMethod = toolkitClass.getDeclaredMethod("getGtkVersion");
//            Enum versionInfo = (Enum)versionMethod.invoke(toolkitClass);
//            Method numberMethod = versionInfo.getClass().getDeclaredMethod("getNumber");
//            int version = ((Integer)numberMethod.invoke(versionInfo)).intValue();
//            //System.out.println("Found Gtk " + version);
//            return version;
//        } catch(Throwable t) {
//            //System.err.println("Could not obtain GtkVersion information from UNIXToolkit: {}", t.getMessage());
//        }
//        return 0;
//    }
  
  private static double getGtk2ScaleFactor(GTK2 gtk2)
  {
    Pointer display = gtk2.gdk_display_get_default();
    // System.out.println("Gtk 2.10+ detected, calling
    // \"gdk_screen_get_resolution\"");
    Pointer screen = gtk2.gdk_display_get_default_screen(display);
    return gtk2.gdk_screen_get_resolution(screen) / 96.0d;
  }
  
  private static double getGtk3ScaleFactor(GTK3 gtk3)
  {
    Pointer display = gtk3.gdk_display_get_default();
    int gtkMinorVersion = gtk3.gtk_get_minor_version();
    if (gtkMinorVersion < 10)
    {
      // Pointer display = gtk3.gdk_display_get_default();
      Pointer screen = gtk3.gdk_display_get_default_screen(display);
      return gtk3.gdk_screen_get_resolution(screen) / 96.0d;
    }
    else if (gtkMinorVersion >= 22)
    {
      // System.out.println("Gtk 3.22+ detected, calling
      // \"gdk_monitor_get_scale_factor\"");
      Pointer monitor = gtk3.gdk_display_get_primary_monitor(display);
      return gtk3.gdk_monitor_get_scale_factor(monitor);
    }
    else if (gtkMinorVersion >= 10)
    {
      // System.out.println("Gtk 3.10+ detected, calling
      // \"gdk_screen_get_monitor_scale_factor\"");
      Pointer screen = gtk3.gdk_display_get_default_screen(display);
      return gtk3.gdk_screen_get_monitor_scale_factor(screen, 0);
    }
    return 0;
  }
  
  /**
   * Gtk2/Gtk3 wrapper
   */
  private interface GTK extends Library
  {
    // Gtk2.0+
    boolean gtk_init_check(int argc, String[] argv);
    
    Pointer gdk_display_get_default();
    
    Pointer gdk_display_get_default_screen(Pointer display);
  }
  
  private interface GTK3 extends GTK
  {
    
    double gdk_screen_get_resolution(Pointer screen);
    
    // Gtk 3.0+
    int gtk_get_minor_version();
    
    // Gtk 3.10-3.21
    int gdk_screen_get_monitor_scale_factor(Pointer screen, int monitor_num);
    
    // Gtk 3.22+
    Pointer gdk_display_get_primary_monitor(Pointer display);
    
    int gdk_monitor_get_scale_factor(Pointer monitor);
  }
  
  private interface GTK2 extends GTK
  {
    
    // Gtk 2.1-3.0
    double gdk_screen_get_resolution(Pointer screen);
  }
  
  // private static GTK3 GTK3_INSTANCE;
  
  private static GTK2 GTK2_INSTANCE;
  
  static
  {
    try
    {
      // GTK3_INSTANCE = Native.loadLibrary("gtk-3", GTK3.class);
    }
    catch (Throwable t)
    {
      
    }
    
    // if (GTK3_INSTANCE == null)
    {
      try
      {
        GTK2_INSTANCE = Native.load("gtk-x11-2.0", GTK2.class);
      }
      catch (Throwable t)
      {
        
      }
    }
  }
}