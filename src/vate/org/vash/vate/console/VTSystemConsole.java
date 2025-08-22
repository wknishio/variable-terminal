package org.vash.vate.console;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.Panel;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

import org.vash.vate.VT;
import org.vash.vate.console.graphical.VTGraphicalConsole;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;
import org.vash.vate.console.lanterna.separated.VTLanternaConsole;
import org.vash.vate.console.standard.VTStandardConsole;
import org.vash.vate.nativeutils.VTSystemNativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;

public final class VTSystemConsole
{
  // private static boolean initialized;
  private static boolean lanterna = true;
  private static boolean graphical = false;
  private static boolean ansi = false;
  private static boolean daemon = false;
  private static boolean remoteIcon = false;
  // private static boolean split;
  private static VTConsole console;
  
  private static Object synchronizationObject = new Object();
  
  static
  {
    VT.initialize();
    VTSystemNativeUtils.initialize();
  }
  
  public static Object getSynchronizationObject()
  {
    return synchronizationObject;
  }
  
  public synchronized static void initialize()
  {
    if (console == null)
    {
      if (!daemon)
      {
        if (graphical)
        {
          // VTGraphicalConsole.setSplit(split);
          VTSystemNativeUtils.hideConsole();
          if (lanterna)
          {
            console = new VTLanternaConsole(graphical, remoteIcon, null);
          }
          else
          {
            console = VTGraphicalConsole.getInstance(remoteIcon);
          }
          console.getFrame().setMenuBar(new VTGraphicalConsoleMenuBar(console));
          console.getFrame().pack();
          // console = VTGraphicalConsole.getInstance();
          // setBold(true);
          // VTGraphicalConsole.setTitle(title);
          // initialized = true;
        }
        else
        {
          if (lanterna && ansi && VTSystemNativeUtils.checkANSI())
          {
            console = new VTLanternaConsole(graphical, remoteIcon, null);
          }
          else
          {
            console = VTStandardConsole.getInstance();
            console.setRemoteIcon(remoteIcon);
            resetAttributes();
            setColors(VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN, VTConsole.VT_CONSOLE_COLOR_DARK_BLACK);
          }
        }
      }
      else
      {
        VTSystemNativeUtils.hideConsole();
      }
    }
  }
  
  public synchronized static boolean isGraphical()
  {
    return graphical;
  }
  
  public synchronized static boolean isANSI()
  {
    return ansi;
  }
  
  public synchronized static void setANSI(boolean ansi)
  {
    VTSystemConsole.ansi = ansi;
  }
  
  public synchronized static void setLanterna(boolean lanterna)
  {
    VTSystemConsole.lanterna = lanterna;
  }
  
  public synchronized static void setGraphical(boolean graphical)
  {
    if (console == null)
    {
      VTSystemConsole.graphical = graphical;
      if (VTReflectionUtils.isAWTHeadless())
      {
        VTSystemConsole.graphical = false;
      }
      else
      {
        if (!graphical)
        {
          try
          {
            if (!checkIOConsole())
            {
              // VTTerminal.graphical = true;
              // File in = new File(FileDescriptor.in);
              try
              {
                if (FileDescriptor.in.valid())
                {
                  FileDescriptor.in.sync();
                }
                else
                {
                  VTSystemConsole.graphical = true;
                }
              }
              catch (Throwable e)
              {
                VTSystemConsole.graphical = true;
              }
            }
          }
          catch (Throwable e)
          {
            try
            {
              if (FileDescriptor.in.valid())
              {
                FileDescriptor.in.sync();
              }
              else
              {
                VTSystemConsole.graphical = true;
              }
            }
            catch (Throwable e2)
            {
              VTSystemConsole.graphical = true;
            }
          }
        }
      }
    }
  }
  
  public synchronized static boolean isDaemon()
  {
    return daemon;
  }
  
  private static class HideGraphicalConsole implements Runnable
  {
    public void run()
    {
      try
      {
        VTSystemConsole.getFrame().setVisible(false);
      }
      catch (Throwable t)
      {
        // t.printStackTrace();
      }
    }
  }
  
  private static class ShowGraphicalConsole implements Runnable
  {
    public void run()
    {
      try
      {
        VTSystemConsole.getFrame().setVisible(true);
        Object waiter = VTSystemConsole.getSynchronizationObject();
        synchronized (waiter)
        {
          waiter.notifyAll();
        }
      }
      catch (Throwable t)
      {
        // t.printStackTrace();
      }
    }
  }
  
  private static HideGraphicalConsole daemonizeThread = new HideGraphicalConsole();
  private static ShowGraphicalConsole undaemonizeThread = new ShowGraphicalConsole();
  
  public synchronized static void setDaemon(boolean daemon)
  {
    if (VTSystemConsole.daemon == daemon)
    {
      return;
    }
    if (console == null)
    {
      VTSystemConsole.daemon = daemon;
    }
    else
    {
      if (isGraphical())
      {
        if (daemon == true)
        {
          VTSystemConsole.daemon = daemon;
          EventQueue.invokeLater(daemonizeThread);
        }
        else
        {
          VTSystemConsole.daemon = daemon;
          EventQueue.invokeLater(undaemonizeThread);
        }
      }
      else
      {
        if (daemon == true)
        {
          VTSystemConsole.daemon = daemon;
          VTSystemNativeUtils.hideConsole();
        }
        else
        {
          VTSystemConsole.daemon = daemon;
          VTSystemNativeUtils.attachConsole();
          Object waiter = VTSystemConsole.getSynchronizationObject();
          synchronized (waiter)
          {
            waiter.notifyAll();
          }
          // if using a tty console, it may be unable to reattach
        }
      }
    }
  }
  
  /* public synchronized static boolean isSplit() { return split; } */
  
  /*
   * public synchronized static void setSplit(boolean split) { if (terminal ==
   * null) { VTTerminal.split = split; } }
   */
  
  public static void setTitle(String title)
  {
    if (checkConsole())
    {
      console.setTitle(title);
      // console.clear();
    }
  }
  
  public static String readLine() throws InterruptedException
  {
    String line = readLine(true);
    return line;
  }
  
  public static String readLine(boolean echo) throws InterruptedException
  {
    if (checkConsole())
    {
      if (!daemon)
      {
        try
        {
          String line = console.readLine(echo);
          return line;
        }
        catch (Throwable t)
        {
          // t.printStackTrace();
          return "";
        }
      }
      else
      {
        return "";
      }
    }
    return null;
  }
  
  public static void interruptReadLine()
  {
    if (checkConsole())
    {
      if (!daemon)
      {
        console.interruptReadLine();
//        StringBuilder message = new StringBuilder();
//        message.append("\nVT>interruptReadLine()!");
//        message.append("\nVT>StackTrace: ");
//        StackTraceElement[] stackStrace = Thread.currentThread().getStackTrace();
//        for (int i = stackStrace.length - 1; i >= 0; i--)
//        {
//          message.append(stackStrace[i].toString() + "\n");
//        }
//        System.err.println(message.toString());
      }
      else
      {
        
      }
    }
  }
  
  // public static boolean isReadingLine()
  // {
  // if (!daemon)
  // {
  // if (console == null)
  // {
  // initialize();
  // }
  // return console.isReadingLine();
  // }
  // else
  // {
  // return false;
  // }
  // }
  
  public static void print(String str)
  {
    if (checkConsole())
    {
      console.print(str);
    }
  }
  
  public static void println(String str)
  {
    if (checkConsole())
    {
      console.println(str);
    }
  }
  
  public static void printf(String format, Object... args)
  {
    if (checkConsole())
    {
      console.printf(format, args);
    }
  }
  
  public static void printfln(String format, Object... args)
  {
    if (checkConsole())
    {
      console.printfln(format, args);
    }
  }
  
  public static void printf(Locale l, String format, Object... args)
  {
    if (checkConsole())
    {
      console.printf(l, format, args);
    }
  }
  
  public static void printfln(Locale l, String format, Object... args)
  {
    if (checkConsole())
    {
      console.printfln(l, format, args);
    }
  }
  
  public static void write(String str)
  {
    if (checkConsole())
    {
      console.write(str);
    }
  }
  
  public static void write(char[] cbuf, int off, int len)
  {
    if (checkConsole())
    {
      console.write(cbuf, off, len);
    }
  }
  
  /*
   * public static void write(byte[] buf, int off, int len) {
   * terminal.trueWrite(buf, off, len); }
   */
  
  public static void flush()
  {
    if (checkConsole())
    {
      console.flush();
    }
  }
  
  public static void clear()
  {
    if (checkConsole())
    {
      console.clear();
    }
  }
  
  public static void bell()
  {
    try
    {
      if (!daemon)
      {
        if (checkConsole())
        {
          console.bell();
        }
      }
      else
      {
        if (graphical)
        {
          VTGraphicalConsole.staticBell();
        }
        else
        {
          VTSystemNativeUtils.printf("\u0007");
          // System.out.print("\u0007");
        }
      }
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public static void setColors(int foregroundColor, int backgroundColor)
  {
    if (checkConsole())
    {
      console.setColors(foregroundColor, backgroundColor);
      console.refreshText();
    }
  }
  
  public static void setBold(boolean bold)
  {
    if (checkConsole())
    {
      console.setBold(bold);
    }
  }
  
  public static void resetAttributes()
  {
    if (checkConsole())
    {
      console.resetAttributes();
      console.refreshText();
    }
  }
  
  public static void setSystemIn()
  {
    if (checkConsole())
    {
      console.setSystemIn();
    }
  }
  
  public static void setSystemOut()
  {
    if (checkConsole())
    {
      console.setSystemOut();
    }
  }
  
  public static void setSystemErr()
  {
    if (checkConsole())
    {
      console.setSystemErr();
    }
  }
  
  public static InputStream getSystemIn()
  {
    if (checkConsole())
    {
      return console.getSystemIn();
    }
    return null;
  }
  
  public static PrintStream getSystemOut()
  {
    if (checkConsole())
    {
      return console.getSystemOut();
    }
    return null;
  }
  
  public static PrintStream getSystemErr()
  {
    if (checkConsole())
    {
      return console.getSystemErr();
    }
    return null;
  }
  
  public static void createInterruptibleReadline(final boolean echo, final ExecutorService executorService, final Runnable interrupt)
  {
    if (!checkConsole())
    {
      return;
    }
    Thread thread = new Thread("VTConsole")
    {
      public void run()
      {
        try
        {
          String line = readLine(echo);
          if (line != null)
          {
            interrupt.run();
            return;
          }
        }
        catch (Throwable t)
        {
          // t.printStackTrace();
        }
      }
    };
    //thread.start();
    executorService.execute(thread);
  }
  
  private static boolean checkConsole()
  {
    if (!daemon)
    {
      if (console == null)
      {
        initialize();
      }
    }
    else
    {
      return false;
    }
    return console != null;
  }
  
  public static void toggleFlushMode()
  {
    if (checkConsole())
    {
      console.toggleFlushMode();
    }
  }
  
  public static void toggleInputMode()
  {
    if (checkConsole())
    {
      console.toggleInputMode();
    }
  }
  
  public static void input(String text)
  {
    if (checkConsole())
    {
      console.input(text);
    }
  }
  
  public static void copyText()
  {
    if (checkConsole())
    {
      console.copyText();
    }
  }
  
  public static void pasteText()
  {
    if (checkConsole())
    {
      console.pasteText();
    }
  }
  
  public static void clearInput()
  {
    if (checkConsole())
    {
      console.clearInput();
    }
  }
  
  public static String getSelectedText()
  {
    if (checkConsole())
    {
      return console.getSelectedText();
    }
    return "";
  }
  
  public static String getAllText()
  {
    if (checkConsole())
    {
      return console.getAllText();
    }
    return "";
  }
  
  public static void refreshText()
  {
    if (checkConsole())
    {
      console.refreshText();
    }
  }
  
  public static boolean isCommandEcho()
  {
    if (checkConsole())
    {
      return console.isCommandEcho();
    }
    return true;
  }
  
  public static void setCommandEcho(boolean commandEcho)
  {
    if (checkConsole())
    {
      console.setCommandEcho(commandEcho);
    }
  }
  
  public static void copyAllText()
  {
    if (checkConsole())
    {
      console.copyAllText();
    }
  }
  
  public static void selectAllText()
  {
    if (checkConsole())
    {
      console.selectAllText();
    }
  }
  
  public static void setIgnoreClose(boolean ignoreClose)
  {
    if (checkConsole())
    {
      console.setIgnoreClose(ignoreClose);
    }
  }
  
  public static void setRemoteIcon(boolean remoteIcon)
  {
    VTSystemConsole.remoteIcon = remoteIcon;
  }
  
  public static void addToggleFlushModePauseNotify(VTConsoleBooleanToggleNotify notifyFlushModePause)
  {
    if (checkConsole())
    {
      console.addToggleFlushModePauseNotify(notifyFlushModePause);
    }
  }
  
  public static void addToggleInputModeReplaceNotify(VTConsoleBooleanToggleNotify notifyInputModeReplace)
  {
    if (checkConsole())
    {
      console.addToggleInputModeReplaceNotify(notifyInputModeReplace);
    }
  }
  
  public static boolean checkIOConsole()
  {
    try
    {
      Class.forName("java.io.Console");
      Class<?> systemClass = Class.forName("java.lang.System");
      Method consoleMethod = systemClass.getDeclaredMethod("console");
      // consoleMethod.setAccessible(true);
      Object consoleResult = consoleMethod.invoke(null);
      if (consoleResult != null)
      {
        return true;
      }
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public static Object getIOConsole()
  {
    try
    {
      Class.forName("java.io.Console");
      Class<?> systemClass = Class.forName("java.lang.System");
      Method consoleMethod = systemClass.getDeclaredMethod("console");
      // consoleMethod.setAccessible(true);
      Object consoleResult = consoleMethod.invoke(null);
      if (consoleResult != null)
      {
        return consoleResult;
      }
    }
    catch (Throwable e)
    {
      
    }
    return false;
  }
  
  public static boolean checkConsoleIsatty()
  {
    try
    {
      Class.forName("java.io.Console");
      Class<?> systemClass = Class.forName("java.lang.System");
      Method consoleMethod = systemClass.getDeclaredMethod("console");
      // consoleMethod.setAccessible(true);
      Object consoleResult = consoleMethod.invoke(null);
      if (consoleResult != null)
      {
        return VTSystemNativeUtils.isatty(0) != 0 && VTSystemNativeUtils.isatty(1) != 0;
      }
      try
      {
        if (FileDescriptor.in.valid())
        {
          FileDescriptor.in.sync();
          return VTSystemNativeUtils.isatty(0) != 0 && VTSystemNativeUtils.isatty(1) != 0;
        }
        else
        {
          
        }
      }
      catch (Throwable e)
      {
        
      }
    }
    catch (Throwable e)
    {
      try
      {
        if (FileDescriptor.in.valid())
        {
          FileDescriptor.in.sync();
          return VTSystemNativeUtils.isatty(0) != 0 && VTSystemNativeUtils.isatty(1) != 0;
        }
        else
        {
          
        }
      }
      catch (Throwable ex)
      {
        
      }
    }
    return false;
  }
  
  public static void requestFocus()
  {
    if (checkConsole())
    {
      console.requestFocus();
    }
  }
  
  public static void closeConsole()
  {
    if (checkConsole())
    {
      console.interruptReadLine();
      console.closeConsole();
    }
  }
  
  public static boolean setLogReadLine(String path)
  {
    if (checkConsole())
    {
      return console.setLogReadLine(path);
    }
    return false;
  }
  
  public static boolean setLogOutput(String path)
  {
    if (checkConsole())
    {
      return console.setLogOutput(path);
    }
    return false;
  }
  
  public static String getLastOutputLine()
  {
    if (checkConsole())
    {
      return console.getLastOutputLine();
    }
    return null;
  }
  
  public static boolean isFlushModePause()
  {
    if (checkConsole())
    {
      return console.isFlushModePause();
    }
    return false;
  }
  
  public static boolean isInputModeReplace()
  {
    if (checkConsole())
    {
      return console.isInputModeReplace();
    }
    return false;
  }
  
  public static VTConsole getConsoleInstance()
  {
    if (checkConsole())
    {
      return console;
    }
    return null;
  }
  
  public static Frame getFrame()
  {
    if (console != null)
    {
      return console.getFrame();
    }
    return null;
  }
  
  public static Panel getPanel()
  {
    if (console != null)
    {
      return console.getPanel();
    }
    return null;
  }
}