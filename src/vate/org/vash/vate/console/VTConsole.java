package org.vash.vate.console;

import java.awt.Frame;
import java.awt.Panel;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Locale;

import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;
import org.vash.vate.console.lanterna.separated.VTLanternaConsole;
import org.vash.vate.console.standard.VTStandardConsole;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;

public abstract class VTConsole
{
  public static final int VT_CONSOLE_COLOR_DARK_BLACK = 0;
  public static final int VT_CONSOLE_COLOR_DARK_RED = 1;
  public static final int VT_CONSOLE_COLOR_DARK_GREEN = 2;
  public static final int VT_CONSOLE_COLOR_DARK_YELLOW = 3;
  public static final int VT_CONSOLE_COLOR_DARK_BLUE = 4;
  public static final int VT_CONSOLE_COLOR_DARK_MAGENTA = 5;
  public static final int VT_CONSOLE_COLOR_DARK_CYAN = 6;
  public static final int VT_CONSOLE_COLOR_DARK_WHITE = 7;
  public static final int VT_CONSOLE_COLOR_LIGHT_BLACK = 10;
  public static final int VT_CONSOLE_COLOR_LIGHT_RED = 11;
  public static final int VT_CONSOLE_COLOR_LIGHT_GREEN = 12;
  public static final int VT_CONSOLE_COLOR_LIGHT_YELLOW = 13;
  public static final int VT_CONSOLE_COLOR_LIGHT_BLUE = 14;
  public static final int VT_CONSOLE_COLOR_LIGHT_MAGENTA = 15;
  public static final int VT_CONSOLE_COLOR_LIGHT_CYAN = 16;
  public static final int VT_CONSOLE_COLOR_LIGHT_WHITE = 17;
  public static final int VT_CONSOLE_COLOR_DEFAULT = 9;
  
  private static boolean checkIOConsole()
  {
    try
    {
      Class.forName("java.io.Console");
      Class<?> systemClass = Class.forName("java.lang.System");
      Method consoleMethod = systemClass.getDeclaredMethod("console");
      Object consoleObject = consoleMethod.invoke(null);
      if (consoleObject != null)
      {
        try
        {
          Method isTermninalMethod = consoleObject.getClass().getDeclaredMethod("isTerminal");
          Object isTerminal = isTermninalMethod.invoke(consoleObject);
          if (isTerminal instanceof Boolean)
          {
            return (Boolean) isTerminal;
          }
        }
        catch (Throwable e)
        {
          
        }
        return true;
      }
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public static boolean hasTerminal()
  {
    if (VTReflectionUtils.getJavaVersion() >= 6)
    {
      return checkIOConsole();
    }
    try
    {
      if (FileDescriptor.in.valid())
      {
        FileDescriptor.in.sync();
      }
      else
      {
        return false;
      }
    }
    catch (Throwable e)
    {
      return false;
    }
    return true;
  }
  
  public static boolean isHeadless()
  {
    return VTReflectionUtils.isAWTHeadless();
  }
  
  public static VTConsole createConsole(boolean graphical, boolean separated)
  {
    VTConsole console = null;
    boolean headless = isHeadless();
    boolean terminal = hasTerminal();
    if (!headless)
    {
      if (graphical)
      {
        console = new VTLanternaConsole(true, true, null);
        console.getFrame().setMenuBar(new VTGraphicalConsoleMenuBar(console));
        console.getFrame().pack();
      }
      else
      {
        if (!terminal)
        {
          console = new VTLanternaConsole(true, true, null);
          console.getFrame().setMenuBar(new VTGraphicalConsoleMenuBar(console));
          console.getFrame().pack();
        }
        else
        {
          if (separated && terminal && VTMainNativeUtils.checkANSI() && !VTReflectionUtils.detectWindows())
          {
            console = new VTLanternaConsole(false, true, null);
          }
          else
          {
            console = VTStandardConsole.getInstance();
            console.setRemoteIcon(true);
            console.resetAttributes();
            console.setColors(VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN, VTConsole.VT_CONSOLE_COLOR_DARK_BLACK);
          }
        }
      }
    }
    else
    {
      if (separated && terminal && VTMainNativeUtils.checkANSI() && !VTReflectionUtils.detectWindows())
      {
        console = new VTLanternaConsole(false, true, null);
      }
      else
      {
        console = VTStandardConsole.getInstance();
        console.setRemoteIcon(true);
        console.resetAttributes();
        console.setColors(VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN, VTConsole.VT_CONSOLE_COLOR_DARK_BLACK);
      }
    }
    return console;
  }
  
  public abstract String readLine(boolean echo) throws InterruptedException;
  
  // public abstract boolean isReadingLine();
  public abstract void interruptReadLine();
  
  public abstract void print(String str);
  
  public abstract void println(String str);
  
  public abstract void printf(String format, Object... args);
  
  public abstract void printfln(String format, Object... args);
  
  public abstract void printf(Locale l, String format, Object... args);
  
  public abstract void printfln(Locale l, String format, Object... args);
  
  public abstract void write(String str);
  
  public abstract void write(char[] buf, int off, int len);
  
  public abstract void flush();
  
  public abstract void clear();
  
  public abstract void bell();
  
  public abstract void setTitle(String title);
  
  public abstract void setColors(int foregroundColor, int backgroundColor);
  
  public abstract void setBold(boolean bold);
  
  public abstract void resetAttributes();
  
  public abstract void setSystemIn();
  
  public abstract void setSystemOut();
  
  public abstract void setSystemErr();
  
  public abstract InputStream getSystemIn();
  
  public abstract PrintStream getSystemOut();
  
  public abstract PrintStream getSystemErr();
  
  public abstract void toggleFlushMode();
  
  public abstract void toggleInputMode();
  
  public abstract void input(String text);
  
  public abstract void copyText();
  
  public abstract void pasteText();
  
  public abstract void clearInput();
  
  public abstract String getSelectedText();
  
  public abstract void refreshText();
  
  public abstract boolean isCommandEcho();
  
  public abstract void setCommandEcho(boolean commandEcho);
  
  public abstract String getAllText();
  
  public abstract void copyAllText();
  
  public abstract void selectAllText();
  
  public abstract void setIgnoreClose(boolean ignoreClose);
  
  public abstract void setRemoteIcon(boolean remoteIcon);
  
  public abstract void addToggleFlushModePauseNotify(VTConsoleBooleanToggleNotify notifyFlushInterrupted);
  
  public abstract void addToggleInputModeReplaceNotify(VTConsoleBooleanToggleNotify notifyReplaceInput);
  
  public abstract void requestFocus();
  
  public abstract void closeConsole();
  
  public abstract boolean setLogReadLine(String path);
  
  public abstract boolean setLogOutput(String path);
  
  public abstract String getLastOutputLine();
  
  public abstract boolean isFlushModePause();
  
  public abstract boolean isInputModeReplace();
  
  public abstract Frame getFrame();
  
  public abstract Panel getPanel();
}