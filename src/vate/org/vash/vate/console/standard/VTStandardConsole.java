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

public class VTStandardConsole extends VTConsole
{
  private static VTStandardConsole instance;
  private static VTStandardConsoleOutputStream outputStandard;
  private static VTStandardConsoleOutputStream errorStandard;
  private static VTDoubledOutputStream outputDoubled;
  private static VTDoubledOutputStream errorDoubled;
  // private static InputStream inputStream;
  private static PrintStream printStream;
  private static PrintStream errorStream;
  private static VTStandardConsoleInterruptibleInputStream inputStream;
  
  private static BufferedReader inputReader;
  private static BufferedWriter outputWriter;
  private static StringBuilder colorCode;
  public static boolean isatty = false;
  public static boolean systemconsolesupport = false;
  private static BufferedWriter readLineLog = null;
  private static PrintStream logOutput = null;
  private volatile boolean lastOutputLineEmpty = false;
  
  private VTStandardConsole()
  {
    isatty = VTConsole.hasIOConsole();
    
    outputStandard = new VTStandardConsoleOutputStream(this, FileDescriptor.out);
    errorStandard = new VTStandardConsoleOutputStream(this, FileDescriptor.err);
    
    outputDoubled = new VTDoubledOutputStream(outputStandard, null, true);
    errorDoubled = new VTDoubledOutputStream(errorStandard, null, true);
    
    errorStream = new PrintStream(errorDoubled, true);
    printStream = new PrintStream(outputDoubled, true);
    
    inputStream = new VTStandardConsoleInterruptibleInputStream(this);
    
    inputReader = new BufferedReader(new InputStreamReader(inputStream, VTSystem.getCharsetDecoder(null)));
    outputWriter = new BufferedWriter(new OutputStreamWriter(outputDoubled, VTSystem.getCharsetEncoder(null)));
    
    colorCode = new StringBuilder();
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
      String data = inputReader.readLine();
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
      outputWriter.write(str);
    }
    catch (IOException e)
    {
      
    }
  }
  
  public void write(char[] buf, int off, int len)
  {
    try
    {
      outputWriter.write(buf, off, len);
    }
    catch (IOException e)
    {
      
    }
  }
  
  public void flush()
  {
    try
    {
      outputWriter.flush();
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
    if (VTReflectionUtils.detectWindows() && VTMainNativeUtils.isAvailable())
    {
      VTMainNativeUtils.system("cls");
    }
    else if (!VTReflectionUtils.detectWindows())
    {
      // VT100 here
      printStream.print("\u001B[2J");
      printStream.print("\u001B[H");
    }
  }
  
  public void bell()
  {
    printStream.print("\u0007");
  }
  
  public void setTitle(String title)
  {
    if (!isatty)
    {
      return;
    }
    if (VTReflectionUtils.detectWindows() && VTMainNativeUtils.isAvailable())
    {
      VTMainNativeUtils.system("title " + title);
    }
    else if (!VTReflectionUtils.detectWindows())
    {
      printStream.print("\u001B]0;" + title + "\u0007");
    }
  }
  
  public void setColors(int foregroundColor, int backgroundColor)
  {
    if (!isatty)
    {
      return;
    }
    if (VTReflectionUtils.detectWindows() && VTMainNativeUtils.isAvailable())
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
    }
    else if (!VTReflectionUtils.detectWindows())
    {
      // Unix
      // VT100 here
      printStream.print("\u001B[" + getUnixForegroundColor(foregroundColor) + "m");
      printStream.print("\u001B[" + getUnixBackgroundColor(backgroundColor) + "m");
    }
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
      if (VTReflectionUtils.detectWindows() && VTMainNativeUtils.isAvailable())
      {
        // printStream.print("\u001B[1m");
      }
      else if (!VTReflectionUtils.detectWindows())
      {
        printStream.print("\u001B[1m");
      }
    }
    else
    {
      if (VTReflectionUtils.detectWindows() && VTMainNativeUtils.isAvailable())
      {
        // printStream.print("\u001B[21m");
      }
      else if (!VTReflectionUtils.detectWindows())
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
    if (VTReflectionUtils.detectWindows() && VTMainNativeUtils.isAvailable())
    {
      VTMainNativeUtils.system("color");
    }
    else if (!VTReflectionUtils.detectWindows())
    {
      printStream.print("\u001B[0m");
    }
  }
  
  public void setSystemIn()
  {
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
        outputDoubled.setAnother(logOutput);
        errorDoubled.setAnother(logOutput);
        return true;
      }
      catch (Throwable t)
      {
        
      }
    }
    else
    {
      outputDoubled.setAnother(null);
      errorDoubled.setAnother(null);
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
    if (lastOutputLineEmpty)
    {
      return 0;
    }
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
  
  public void setlastOutputLineEmpty(boolean empty)
  {
    this.lastOutputLineEmpty = empty;
  }
}