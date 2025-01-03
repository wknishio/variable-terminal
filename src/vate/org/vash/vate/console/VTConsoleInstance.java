package org.vash.vate.console;

import java.awt.Frame;
import java.awt.Panel;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;

public interface VTConsoleInstance
{
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