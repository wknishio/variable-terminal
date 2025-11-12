package org.vash.vate.console.standard;

import java.io.BufferedWriter;
// import java.io.Console;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.awt.Frame;
import java.awt.Panel;
// import java.io.FileInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
// import java.nio.channels.ClosedByInterruptException;
import java.util.Locale;
import org.vash.vate.console.VTConsoleBooleanToggleNotify;
import org.vash.vate.VTSystem;
import org.vash.vate.console.VTConsole;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.stream.filter.VTDoubledOutputStream;

// import jline.ConsoleReader;
// import java.nio.channels.Channels;
// import java.nio.channels.ClosedByInterruptException;
// import java.nio.charset.Charset;

public class VTStandardConsole extends VTConsole
{
  private static VTStandardConsole instance;
  private static VTDoubledOutputStream output;
  private static VTDoubledOutputStream error;
  // private static InputStream inputStream;
  private static PrintStream printStream;
  private static PrintStream errorStream;
  private static VTStandardConsoleInterruptibleInputStream inputStream;
  
  private static BufferedReader standardTerminalReader;
  private static BufferedWriter standardTerminalWriter;
  private static StringBuilder colorCode;
  public static boolean isatty = false;
  public static boolean systemconsolesupport = false;
  private static BufferedWriter readLineLog = null;
  private static PrintStream logOutput = null;
  
  // private static Console systemConsole;
  // private static ConsoleReader jlineConsole;
  // private static Object console;
  // private static BufferedWriter errorWriter;
  
  private VTStandardConsole()
  {
    //VTNativeUtils.initialize();
    // standardTerminalReader = new BufferedReader(new InputStreamReader(new
    // VTStandardTerminalInterruptibleStream(System.in)));
    // standardTerminalReader = new BufferedReader(new
    // InputStreamReader(System.in));
    
    // bright = true;
    isatty = VTMainNativeUtils.isatty(1) != 0;
    // System.out.println(isatty);
//		if (!isatty)
//		{
//			isatty = VTNativeUtils.attach_console();
//			if (isatty)
//			{
//				System.out.println("using native things");
//				inputStream = new VTStandardConsoleInterruptibleInputStreamNative();
//				standardTerminalReader = new BufferedReader(new InputStreamReader(inputStream));
//				standardTerminalWriter = new BufferedWriter(new OutputStreamWriter(System.out));
//				System.setIn(inputStream);
//				System.setOut(new PrintStream(new FileOutputStream(FileDescriptor.out), true));
//				System.setErr(new PrintStream(new FileOutputStream(FileDescriptor.err), true));
//			}
//			else
//			{
//				inputStream = new VTStandardConsoleInterruptibleInputStream();
//				standardTerminalReader = new BufferedReader(new InputStreamReader(inputStream));
//				standardTerminalWriter = new BufferedWriter(new OutputStreamWriter(System.out));
//			}
//		}
//		else
//		{
//			inputStream = new VTStandardConsoleInterruptibleInputStream();
//			standardTerminalReader = new BufferedReader(new InputStreamReader(inputStream));
//			standardTerminalWriter = new BufferedWriter(new OutputStreamWriter(System.out));
//		}
    output = new VTDoubledOutputStream(new VTStandardConsoleOutputStream(FileDescriptor.out), null, true);
    error = new VTDoubledOutputStream(new VTStandardConsoleOutputStream(FileDescriptor.err), null, true);
    
    errorStream = new PrintStream(error, true);
    printStream = new PrintStream(output, true);
    
    inputStream = new VTStandardConsoleInterruptibleInputStream();
    
    standardTerminalReader = new BufferedReader(new InputStreamReader(inputStream, VTSystem.getCharsetDecoder(null)));
    standardTerminalWriter = new BufferedWriter(new OutputStreamWriter(output, VTSystem.getCharsetEncoder(null)));
    
    colorCode = new StringBuilder();
    // jlineConsoleSupport = false;
    // systemConsoleSupport = false;
    /*
     * try { jlineConsole = new ConsoleReader();
     * jlineConsole.setBellEnabled(false); jlineConsole.setUseHistory(true);
     * jlineConsole.setUsePagination(true); jlineConsoleSupport = true; } catch
     * (Throwable e) { }
     */
    /*
     * try { Class.forName("java.io.Console"); //console =
     * Class.forName("System").getMethod("console").invoke(null); systemConsole
     * = System.console(); if (systemConsole != null) { systemConsoleSupport =
     * true; } } catch (Throwable e) { }
     */
    // System.out.println("jlineConsoleSupport = " + jlineConsoleSupport);
    // System.out.println("systemConsoleSupport = " + systemConsoleSupport);
    /*
     * foregroundColor = VTTerminal.VT_CONSOLE_COLOR_GREEN; backgroundColor =
     * VTTerminal.VT_CONSOLE_COLOR_BLACK; bright = true;
     * trueSetColors(foregroundColor, backgroundColor); trueSetBright(true);
     */
    // errorWriter = new BufferedWriter(new OutputStreamWriter(System.err));
    // try
    // {
    // standardTerminalWriter.write("\u001B[6n\n");
    // standardTerminalWriter.flush();
    // }
    // catch (Throwable t)
    // {
    // //t.printStackTrace();
    // }
  }
  
  public synchronized static VTStandardConsole getInstance()
  {
    if (instance == null)
    {
      instance = new VTStandardConsole();
    }
    return instance;
  }
  
  public String readLine(boolean echo) throws InterruptedException
  {
    inputStream.setEcho(echo);
    try
    {
      String data = standardTerminalReader.readLine();
      writeLogReadLine(data);
      return data;
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      return "";
    }
    // String line = null;
  }
  
  public void print(String str)
  {
    printStream.print(str);
    printStream.flush();
  }
  
  public void println(String str)
  {
    printStream.println(str);
  }
  
  public void printf(String format, Object... args)
  {
    printStream.printf(format, args);
  }
  
  public void printfln(String format, Object... args)
  {
    printStream.printf(format + "\n", args);
  }
  
  public void printf(Locale l, String format, Object... args)
  {
    printStream.printf(l, format, args);
  }
  
  public void printfln(Locale l, String format, Object... args)
  {
    printStream.printf(l, format + "\n", args);
  }
  
  public void write(String str)
  {
    try
    {
      standardTerminalWriter.write(str);
    }
    catch (IOException e)
    {
      
    }
  }
  
  public void write(char[] buf, int off, int len)
  {
    try
    {
      standardTerminalWriter.write(buf, off, len);
    }
    catch (IOException e)
    {
      
    }
  }
  
  /*
   * public void trueWrite(byte[] buf, int off, int len) { System.out.write(buf,
   * off, len); }
   */
  
  public void flush()
  {
    try
    {
      standardTerminalWriter.flush();
    }
    catch (IOException e)
    {
      
    }
  }
  
  public void clear()
  {
    if (!isatty)
    {
      return;
    }
    if (VTReflectionUtils.detectWindows())
    {
      // System.out.print("\u001B[2J");
      // System.out.print("\u001B[H");
      VTMainNativeUtils.system("cls");
    }
    else
    {
      // VT100 here
      // VTNativeUtils.system("tput clear");
      printStream.print("\u001B[2J");
      printStream.print("\u001B[H");
    }
  }
  
  public void bell()
  {
    // VTNativeUtils.printf("\u0007");
    printStream.print("\u0007");
  }
  
  public void setTitle(String title)
  {
    if (!isatty)
    {
      return;
    }
    if (VTReflectionUtils.detectWindows())
    {
      // System.out.print("\u001B]0;" + title + "\u0007");
      VTMainNativeUtils.system("title " + title);
    }
    else
    {
      // VTNativeUtils.system("echo -n \"\\033]0;" + title + "\\007\"");
      // VTNativeUtils.printf("\u001B]0;" + title + "\u0007");
      // VTNativeUtils.printf("\u001B]1;" + title + "\u0007");
      // VTNativeUtils.printf("\u001B]2;" + title + "\u0007");
      printStream.print("\u001B]0;" + title + "\u0007");
      // System.out.print("\u001B]1;" + title + "\u0007");
      // System.out.print("\u001B]2;" + title + "\u0007");
    }
  }
  
  public void setColors(int foregroundColor, int backgroundColor)
  {
    if (!isatty)
    {
      return;
    }
    if (VTReflectionUtils.detectWindows())
    {
      // Windows 2000 and beyond only
      colorCode.setLength(0);
      switch (backgroundColor)
      {
        case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
        {
          colorCode.append("0");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
        {
          colorCode.append("4");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
        {
          colorCode.append("2");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
        {
          colorCode.append("6");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
        {
          colorCode.append("1");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
        {
          colorCode.append("5");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
        {
          colorCode.append("3");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
        {
          colorCode.append("7");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
        {
          colorCode.append("C");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
        {
          colorCode.append("A");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
        {
          colorCode.append("E");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
        {
          colorCode.append("9");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
        {
          colorCode.append("D");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
        {
          colorCode.append("B");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
        {
          colorCode.append("F");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
        {
          colorCode.append("0");
          break;
        }
      }
      switch (foregroundColor)
      {
        case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
        {
          colorCode.append("0");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
        {
          colorCode.append("4");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
        {
          colorCode.append("2");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
        {
          colorCode.append("6");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
        {
          colorCode.append("1");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
        {
          colorCode.append("5");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
        {
          colorCode.append("3");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
        {
          colorCode.append("7");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
        {
          colorCode.append("C");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
        {
          colorCode.append("A");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
        {
          colorCode.append("E");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
        {
          colorCode.append("9");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
        {
          colorCode.append("D");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
        {
          colorCode.append("B");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
        {
          colorCode.append("F");
          break;
        }
        case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
        {
          colorCode.append("A");
          break;
        }
      }
      VTMainNativeUtils.system("color " + colorCode.toString());
      // System.out.print("\u001B[" + getUnixForegroundColor(foregroundColor) +
      // "m");
      // System.out.print("\u001B[" + getUnixBackgroundColor(backgroundColor) +
      // "m");
    }
    else
    {
      // Unix
      // VT100 here
      /*
       * VTNativeUtils.system("tput sgr0"); VTNativeUtils.system("tput bold");
       * VTNativeUtils.system("tput setaf " + foregroundColor);
       * VTNativeUtils.system("tput setf " + foregroundColor);
       * VTNativeUtils.system("tput setab " + backgroundColor);
       * VTNativeUtils.system("tput setb " + backgroundColor);
       */
      // System.out.print("\u001B[0m");
      // System.out.print("\u001B[1;3" + foregroundColor + ";4" +
      // backgroundColor +
      // "m");
      printStream.print("\u001B[" + getUnixForegroundColor(foregroundColor) + "m");
      printStream.print("\u001B[" + getUnixBackgroundColor(backgroundColor) + "m");
    }
    // VTStandardTerminal.foregroundColor = foregroundColor;
    // VTStandardTerminal.backgroundColor = backgroundColor;
  }
  
  private static String getUnixForegroundColor(int foregroundColor)
  {
    switch (foregroundColor)
    {
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
      {
        return "30";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
      {
        return "31";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
      {
        return "32";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
      {
        return "33";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
      {
        return "34";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
      {
        return "35";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
      {
        return "36";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
      {
        return "37";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLACK:
      {
        // return 101;
        return "30;90";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
      {
        // return 91;
        return "31;91";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
      {
        // return 92;
        return "32;92";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
      {
        // return 93;
        return "33;93";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
      {
        // return 94;
        return "34;94";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
      {
        // return 95;
        return "35;95";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
      {
        // return 96;
        return "36;96";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
      {
        // return 97;
        return "37;97";
      }
      case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
      {
        return "39";
      }
      default:
      {
        return "32;92";
      }
    }
  }
  
  private static String getUnixBackgroundColor(int backgroundColor)
  {
    switch (backgroundColor)
    {
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
      {
        return "40";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
      {
        return "41";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
      {
        return "42";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
      {
        return "43";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
      {
        return "44";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
      {
        return "45";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
      {
        return "46";
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
      {
        return "47";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLACK:
      {
        // return 101;
        return "40;100";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
      {
        // return 101;
        return "41;101";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
      {
        // return 102;
        return "42;102";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
      {
        // return 103;
        return "43;103";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
      {
        // return 104;
        return "44;104";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
      {
        // return 105;
        return "45;105";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
      {
        // return 106;
        return "46;106";
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
      {
        // return 107;
        return "47;107";
      }
      case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
      {
        return "49";
      }
      default:
      {
        return "32";
      }
    }
  }
  
  public void setBold(boolean bold)
  {
    if (!isatty)
    {
      return;
    }
    if (bold)
    {
      if (VTReflectionUtils.detectWindows())
      {
        // printStream.print("\u001B[1m");
      }
      else
      {
        printStream.print("\u001B[1m");
      }
    }
    else
    {
      if (VTReflectionUtils.detectWindows())
      {
        // printStream.print("\u001B[21m");
      }
      else
      {
        printStream.print("\u001B[21m");
      }
    }
  }
  
  public void resetAttributes()
  {
    if (!isatty)
    {
      return;
    }
    if (VTReflectionUtils.detectWindows())
    {
      // setColors(VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN,
      // VTConsole.VT_CONSOLE_COLOR_NORMAL_BLACK);
      // setBold(true);
      VTMainNativeUtils.system("color");
      // System.out.print("\u001B[0m");
    }
    else
    {
      printStream.print("\u001B[0m");
    }
  }
  
  public void setSystemIn()
  {
    /*
     * if (inputStream == null) { inputStream = new
     * FileInputStream(FileDescriptor.in); }
     */
    System.setIn(inputStream);
  }
  
  public void setSystemOut()
  {
    System.setOut(printStream);
  }
  
  public void setSystemErr()
  {
    System.setErr(errorStream);
  }
  
  public InputStream getSystemIn()
  {
    /*
     * if (inputStream == null) { inputStream = new
     * FileInputStream(FileDescriptor.in); }
     */
    return inputStream;
  }
  
  public PrintStream getSystemOut()
  {
    return printStream;
  }
  
  public PrintStream getSystemErr()
  {
    return errorStream;
  }
  
  public void interruptReadLine()
  {
    inputStream.interruptRead();
  }
  
  public void toggleFlushMode()
  {
    
  }
  
  public void toggleInputMode()
  {
    
  }
  
  public void input(String text)
  {
    if (text != null)
    {
      if (inputStream.getEcho())
      {
        print(text);
      }
      inputStream.input(text);
    }
  }
  
  public void copyText()
  {
    
  }
  
  public void pasteText()
  {
    
  }
  
  public void clearInput()
  {
    
  }
  
  public String getSelectedText()
  {
    return null;
  }
  
  public void refreshText()
  {
    
  }
  
  public boolean isCommandEcho()
  {
    return true;
  }
  
  public void setCommandEcho(boolean commandEcho)
  {
    
  }
  
  public String getAllText()
  {
    return null;
  }
  
  public void copyAllText()
  {
    
  }
  
  public void selectAllText()
  {
    
  }
  
  public void setIgnoreClose(boolean ignoreClose)
  {
    
  }
  
  public void setRemoteIcon(boolean remote)
  {
    
  }
  
  public void addToggleFlushModePauseNotify(VTConsoleBooleanToggleNotify notify)
  {
    
  }
  
  public void addToggleInputModeReplaceNotify(VTConsoleBooleanToggleNotify notify)
  {
    
  }
  
  public void requestFocus()
  {
    
  }
  
  public void closeConsole()
  {
    
  }
  
  public boolean setLogOutput(String path)
  {
    if (path != null)
    {
      try
      {
        logOutput = new PrintStream(new FileOutputStream(path, true), true, "UTF-8");
        output.setAnother(logOutput);
        error.setAnother(logOutput);
        return true;
      }
      catch (Throwable t)
      {
        
      }
    }
    else
    {
      output.setAnother(null);
      error.setAnother(null);
      return true;
    }
    return false;
  }
  
  public boolean setLogReadLine(String path)
  {
    if (path != null)
    {
      try
      {
        readLineLog = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), VTSystem.getCharsetEncoder("UTF-8")));
        return true;
      }
      catch (Throwable t)
      {
        closeReadLineLog();
      }
    }
    else
    {
      closeReadLineLog();
    }
    return false;
  }
  
  private void writeLogReadLine(String line)
  {
    if (line == null || line.length() < 0)
    {
      return;
    }
    if (readLineLog != null)
    {
      try
      {
        readLineLog.write(line + "\r\n");
        readLineLog.flush();
      }
      catch (Throwable t)
      {
        closeReadLineLog();
      }
    }
  }
  
  private void closeReadLineLog()
  {
    if (readLineLog != null)
    {
      try
      {
        readLineLog.close();
      }
      catch (Throwable e)
      {
        
      }
      readLineLog = null;
    }
  }
  
  public String getLastOutputLine()
  {
    return "";
  }
  
  public int getLastOutputLineLength()
  {
    return -1;
  }
  
  public boolean isFlushModePause()
  {
    return false;
  }
  
  public boolean isInputModeReplace()
  {
    return false;
  }
  
  public Frame getFrame()
  {
    return null;
  }
  
  public Panel getPanel()
  {
    return null;
  }
}