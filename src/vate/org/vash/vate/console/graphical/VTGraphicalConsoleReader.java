package org.vash.vate.console.graphical;

import java.util.ArrayList;
import java.util.List;

import org.vash.vate.console.VTConsole;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTGraphicalConsoleReader
{
  // maybe: advanced command line editing capabilities
  private static boolean echo = false;
  private static boolean readingLine = false;
  private static final int commandHistoryMaxSize = 100;
  private static int commandHistoryPosition;
  private static int key;
  private static char character;
  private static StringBuilder currentLineBuffer = new StringBuilder("");
  private static List<String> commandHistory = new ArrayList<String>();
  private static List<String> pendingLineBuffer = new ArrayList<String>();
  private static Thread currentThread;
  /* static { commandHistory.add(""); } */
  
  public static boolean readingLine()
  {
    return readingLine;
  }
  
  public VTGraphicalConsoleReader()
  {
    
    // currentLineBuffer = new StringBuffer("");
    // commandHistory = new LinkedList<String>();
  }
  
  public static void interruptRead()
  {
    if (currentThread != null)
    {
      try
      {
        currentThread.interrupt();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  /* public static Thread getCurrentThread() { return currenthread; } */
  
  public static void close()
  {
    VTGraphicalConsole.close();
  }
  
  public static boolean ready()
  {
    return VTGraphicalConsole.available() > 0;
  }
  
  public static int read() throws InterruptedException
  {
    return VTGraphicalConsole.read();
  }
  
  public static int read(char[] cbuf, int off, int len) throws InterruptedException
  {
    int readChars = 0;
    for (int i = 0; i < len; i++)
    {
      key = read();
      if (key != -1)
      {
        character = (char) key;
        cbuf[off + i] = character;
        readChars++;
      }
      else
      {
        return readChars;
      }
    }
    return readChars;
  }
  
  public static boolean isReadingLine()
  {
    return readingLine;
  }
  
  public static void clearInput()
  {
    synchronized (pendingLineBuffer)
    {
      pendingLineBuffer.clear();
    }
    resetCurrentLine(echo);
  }
  
  public synchronized static String readLine(boolean echo) throws InterruptedException
  {
    currentThread = Thread.currentThread();
    readingLine = true;
    VTGraphicalConsoleReader.echo = echo;
    // currentLineBuffer.setLength(0);
    if (getPendingLineSize() > 0 && !getFromPendingLine().contains("\n"))
    {
      currentLineBuffer.append(removeFromPendingLine());
      /*
       * if (currentLineBuffer.length() >
       * VTGraphicalConsole.maxTerminalCharactersCount) { currentLineBuffer =
       * currentLineBuffer.delete(0, currentLineBuffer.length() -
       * VTGraphicalConsole.maxTerminalCharactersCount); }
       */
    }
    if ((currentLineBuffer.length() > 0) || (currentLineBuffer.length() == 0 && getPendingLineSize() == 0))
    {
      if (echo && !VTGraphicalConsole.isFlushInterrupted())
      {
        // return new BufferedReader(new
        // InputStreamReader(System.in)).readLine();
        while (true)
        {
          key = VTGraphicalConsole.read();
          character = (char) key;
          if (character == '\n')
          {
            readingLine = false;
            if (!VTGraphicalConsole.isFlushInterrupted())
            {
              VTGraphicalConsole.setCaretRecoilCount(0);
              if (checkCurrentLineForReplace())
              {
                // VTGraphicalConsole.backspace(currentLineBuffer.length(),
                // true);
              }
              else
              {
                if (VTGraphicalConsole.getDoubledOutput() != null)
                {
                  VTGraphicalConsole.getDoubledOutput().print(currentLineBuffer.toString());
                }
                else
                {
                  VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
                }
                
              }
            }
            
            if (VTGraphicalConsole.getDoubledOutput() != null)
            {
              VTGraphicalConsole.getDoubledOutput().println();
            }
            else
            {
              VTGraphicalConsole.write('\n');
              VTGraphicalConsole.flushBuffered(true);
            }
            // VTGraphicalConsole.updateCaretPosition();
            break;
          }
          else if (character == '\b')
          {
            if (currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              // VTGraphicalConsole.backspace(currentLineBuffer.length());
              boolean checkReplace = checkCurrentLineForReplace();
              int recoil = VTGraphicalConsole.getCaretRecoilCount();
              currentLineBuffer.deleteCharAt(currentLineBuffer.length() - 1 - VTGraphicalConsole.getCaretRecoilCount());
              // VTGraphicalConsole.setCaretRecoilCount(VTGraphicalConsole.getCaretRecoilCount()
              // - 1);
              if (!VTGraphicalConsole.isFlushInterrupted())
              {
                VTGraphicalConsole.setCaretRecoilCount(0);
                if (checkReplace)
                {
                  VTGraphicalConsole.backspace(recoil + 1, true);
                  VTGraphicalConsoleWriter.write(currentLineBuffer.substring(currentLineBuffer.length() - recoil, currentLineBuffer.length()));
                }
                else
                {
                  VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
                  // VTGraphicalConsole.setCaretRecoilCount(0);
                  // VTGraphicalConsole.write(currentLineBuffer.toString());
                }
                // VTGraphicalConsole.backspace(1);
                // VTGraphicalConsole.write(currentLineBuffer.toString());
                // VTGraphicalConsole.updateCaretPosition();
                VTGraphicalConsole.flushBuffered(false);
                VTGraphicalConsole.setCaretRecoilCount(recoil);
                VTGraphicalConsole.updateCaretPosition();
              }
              else
              {
                // VTGraphicalConsole.write('\b');
                // VTGraphicalConsole.flushBuffered(true);
                // VTGraphicalConsole.write('\b');
                // VTGraphicalConsole.write(currentLineBuffer.toString());
              }
              // VTGraphicalConsole.updateCaretPosition();
              // VTGraphicalConsole.increaseCaretRecoilCount();
            }
          }
          else if (character == VTGraphicalConsole.VT_VK_LEFT)
          {
            /*
             * if (currentLineBuffer.length() > 0) {
             * currentLineBuffer.deleteCharAt(currentLineBuffer. length() - 1 -
             * VTGraphicalConsole.getCaretRecoilCount());
             * VTGraphicalConsole.write('\b');
             * VTGraphicalConsole.updateCaretPosition();
             * //VTGraphicalConsole.flushBuffered(); }
             */
            if (!checkCurrentLineForCaretPositionChange())
            {
              VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
              VTGraphicalConsole.flushBuffered(false);
            }
            if (VTGraphicalConsole.getCaretRecoilCount() < currentLineBuffer.length())
            {
              VTGraphicalConsole.increaseCaretRecoilCount();
              VTGraphicalConsole.updateCaretPosition();
            }
          }
          else if (character == VTGraphicalConsole.VT_VK_UP)
          {
            VTGraphicalConsole.setCaretRecoilCount(0);
            scrollCommandHistoryUp(echo);
          }
          else if (character == VTGraphicalConsole.VT_VK_DOWN)
          {
            VTGraphicalConsole.setCaretRecoilCount(0);
            scrollCommandHistoryDown(echo);
          }
          else if (character == VTGraphicalConsole.VT_VK_HOME)
          {
            // resetCurrentLine(echo);
            VTGraphicalConsole.setCaretRecoilCount(currentLineBuffer.length());
            VTGraphicalConsole.updateCaretPosition();
          }
          else if (character == VTGraphicalConsole.VT_VK_END)
          {
            // completeCurrentLine(echo);
            VTGraphicalConsole.setCaretRecoilCount(0);
            VTGraphicalConsole.updateCaretPosition();
          }
          else if (character == VTGraphicalConsole.VT_VK_DELETE)
          {
            if (VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              boolean checkReplace = checkCurrentLineForReplace();
              VTGraphicalConsole.reduceCaretRecoilCount();
              int recoil = VTGraphicalConsole.getCaretRecoilCount();
              currentLineBuffer.deleteCharAt(currentLineBuffer.length() - 1 - VTGraphicalConsole.getCaretRecoilCount());
              // VTGraphicalConsole.setCaretRecoilCount(VTGraphicalConsole.getCaretRecoilCount()
              // - 1);
              if (!VTGraphicalConsole.isFlushInterrupted())
              {
                VTGraphicalConsole.setCaretRecoilCount(0);
                if (checkReplace)
                {
                  VTGraphicalConsole.backspace(recoil + 1, true);
                  VTGraphicalConsoleWriter.write(currentLineBuffer.substring(currentLineBuffer.length() - recoil, currentLineBuffer.length()));
                }
                else
                {
                  VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
                }
                VTGraphicalConsole.flushBuffered(false);
                VTGraphicalConsole.setCaretRecoilCount(recoil);
                VTGraphicalConsole.updateCaretPosition();
              }
              else
              {
                // VTGraphicalConsole.write('\b');
                // VTGraphicalConsole.setCaretRecoilCount(recoil);
                // VTGraphicalConsole.flushBuffered(true);
                // VTGraphicalConsole.write('\b');
                // VTGraphicalConsole.write(currentLineBuffer.toString());
              }
            }
          }
          else if (character == VTGraphicalConsole.VT_VK_RIGHT)
          {
            // continueCurrentLine(echo);
            if (!checkCurrentLineForCaretPositionChange())
            {
              VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
              VTGraphicalConsole.flushBuffered(false);
            }
            if (VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              VTGraphicalConsole.reduceCaretRecoilCount();
              VTGraphicalConsole.updateCaretPosition();
            }
          }
          else if (character == '\u0003')
          {
            /* out.write('\n'); out.flush(); */
            currentThread = null;
            if (!VTGraphicalConsole.ignoreClose)
            {
              VTRuntimeExit.exit(0);
            }
            return null;
            // VTExit.exit(0);
          }
          else if (character == '\u001A')
          {
            /* out.write('\n'); out.flush(); */
            VTConsole.toggleScrollMode();
            // VTExit.exit(0);
          }
          else if (character == '\u0018')
          {
            /* out.write('\n'); out.flush(); */
            VTConsole.toggleInputMode();
            // VTExit.exit(0);
          }
          /* else if (character == (char) -1) { return ""; } */
          else
          {
            if (VTGraphicalConsole.getCaretRecoilCount() == 0)
            {
              /*
               * VTGraphicalConsole.write(character);
               * VTGraphicalConsole.flushBuffered(true);
               */
              // int recoil =
              // VTGraphicalConsole.getCaretRecoilCount();
              if (!VTGraphicalConsole.isFlushInterrupted())
              {
                if (checkCurrentLineForReplace())
                {
                  currentLineBuffer.append(character);
                  /*
                   * if (currentLineBuffer.length() > VTGraphicalConsole.
                   * maxTerminalCharactersCount) { currentLineBuffer =
                   * currentLineBuffer.delete(0, currentLineBuffer.length() -
                   * VTGraphicalConsole. maxTerminalCharactersCount); }
                   */
                  VTGraphicalConsole.write(character);
                  // VTGraphicalConsole.backspace(currentLineBuffer.length(),
                  // true);
                }
                else
                {
                  currentLineBuffer.append(character);
                  /*
                   * if (currentLineBuffer.length() > VTGraphicalConsole.
                   * maxTerminalCharactersCount) { currentLineBuffer =
                   * currentLineBuffer.delete(0, currentLineBuffer.length() -
                   * VTGraphicalConsole. maxTerminalCharactersCount); }
                   */
                  VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
                  // VTGraphicalConsole.setCaretRecoilCount(recoil);
                }
                VTGraphicalConsole.flushBuffered(true);
              }
              else
              {
                currentLineBuffer.append(character);
                /*
                 * if (currentLineBuffer.length() > VTGraphicalConsole.
                 * maxTerminalCharactersCount) { currentLineBuffer =
                 * currentLineBuffer.delete(0, currentLineBuffer.length() -
                 * VTGraphicalConsole. maxTerminalCharactersCount); }
                 */
              }
            }
            else
            {
              int recoil = VTGraphicalConsole.getCaretRecoilCount();
              if (!VTGraphicalConsole.isFlushInterrupted())
              {
                VTGraphicalConsole.setCaretRecoilCount(0);
                if (checkCurrentLineForReplace())
                {
                  VTGraphicalConsole.backspace(recoil, true);
                }
                else
                {
                  
                }
              }
              if (VTGraphicalConsole.isReplaceActivated())
              {
                currentLineBuffer.setCharAt(currentLineBuffer.length() - recoil, character);
              }
              else
              {
                currentLineBuffer.insert(currentLineBuffer.length() - recoil, character);
                /*
                 * if (currentLineBuffer.length() > VTGraphicalConsole.
                 * maxTerminalCharactersCount) { currentLineBuffer =
                 * currentLineBuffer.delete(0, currentLineBuffer.length() -
                 * VTGraphicalConsole. maxTerminalCharactersCount); }
                 */
              }
              // VTGraphicalConsole.write(character);
              if (!VTGraphicalConsole.isFlushInterrupted())
              {
                if (VTGraphicalConsole.isReplaceActivated())
                {
                  VTGraphicalConsoleWriter.write(currentLineBuffer.substring(currentLineBuffer.length() - recoil, currentLineBuffer.length()));
                }
                else
                {
                  VTGraphicalConsoleWriter.write(currentLineBuffer.substring(currentLineBuffer.length() - recoil - 1, currentLineBuffer.length()));
                }
                VTGraphicalConsole.flushBuffered(false);
              }
              else
              {
                // VTGraphicalConsole.write(character);
              }
              VTGraphicalConsole.setCaretRecoilCount(recoil);
              if (VTGraphicalConsole.isReplaceActivated())
              {
                VTGraphicalConsole.reduceCaretRecoilCount();
              }
              if (!VTGraphicalConsole.isFlushInterrupted())
              {
                VTGraphicalConsole.updateCaretPosition();
              }
              // VTGraphicalConsole.reduceCaretRecoilCount();
            }
            
          }
        }
      }
      else
      {
        while (true)
        {
          key = VTGraphicalConsole.read();
          character = (char) key;
          if (character == '\n')
          {
            readingLine = false;
            VTGraphicalConsole.write('\n');
            VTGraphicalConsole.setCaretRecoilCount(0);
            // VTGraphicalConsole.flushBuffered();
            // VTGraphicalConsole.updateCaretPosition();
            break;
          }
          else if (character == '\b')
          {
            // VTGraphicalConsole.setCaretRecoilCount(0);
            if (currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              currentLineBuffer.deleteCharAt(currentLineBuffer.length() - 1 - VTGraphicalConsole.getCaretRecoilCount());
              // VTGraphicalConsole.increaseCaretRecoilCount();
              /*
               * if (VTGraphicalConsole.getCaretRecoilCount() > 0) {
               * VTGraphicalConsole.reduceCaretRecoilCount(); }
               */
              // VTGraphicalConsole.setCaretRecoilCount(VTGraphicalConsole.getCaretRecoilCount()
              // - 1);
              // VTGraphicalConsole.flushBuffered();
            }
          }
          else if (character == VTGraphicalConsole.VT_VK_LEFT)
          {
            /*
             * if (currentLineBuffer.length() > 0) {
             * currentLineBuffer.deleteCharAt(currentLineBuffer. length() - 1 -
             * VTGraphicalConsole.getCaretRecoilCount()); }
             */
            if (VTGraphicalConsole.getCaretRecoilCount() < currentLineBuffer.length())
            {
              VTGraphicalConsole.increaseCaretRecoilCount();
            }
          }
          else if (character == VTGraphicalConsole.VT_VK_UP)
          {
            VTGraphicalConsole.setCaretRecoilCount(0);
            scrollCommandHistoryUp(echo);
          }
          else if (character == VTGraphicalConsole.VT_VK_DOWN)
          {
            VTGraphicalConsole.setCaretRecoilCount(0);
            scrollCommandHistoryDown(echo);
          }
          else if (character == VTGraphicalConsole.VT_VK_HOME)
          {
            // resetCurrentLine(echo);
            VTGraphicalConsole.setCaretRecoilCount(currentLineBuffer.length());
            // VTGraphicalConsole.flushBuffered();
          }
          else if (character == VTGraphicalConsole.VT_VK_END)
          {
            // completeCurrentLine(echo);
            VTGraphicalConsole.setCaretRecoilCount(0);
            // VTGraphicalConsole.flushBuffered();
          }
          else if (character == VTGraphicalConsole.VT_VK_DELETE)
          {
            if (VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              VTGraphicalConsole.reduceCaretRecoilCount();
              int recoil = VTGraphicalConsole.getCaretRecoilCount();
              // VTGraphicalConsole.setCaretRecoilCount(VTGraphicalConsole.getCaretRecoilCount()
              // - 1);
              currentLineBuffer.deleteCharAt(currentLineBuffer.length() - 1 - VTGraphicalConsole.getCaretRecoilCount());
              VTGraphicalConsole.setCaretRecoilCount(recoil);
              // VTGraphicalConsole.flushBuffered(true);
              // VTGraphicalConsole.write('\b');
              // VTGraphicalConsole.write(currentLineBuffer.toString());
            }
          }
          else if (character == VTGraphicalConsole.VT_VK_RIGHT)
          {
            // continueCurrentLine(echo);
            if (VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              VTGraphicalConsole.reduceCaretRecoilCount();
            }
          }
          else if (character == '\u0003')
          {
            currentThread = null;
            if (!VTGraphicalConsole.ignoreClose)
            {
              VTRuntimeExit.exit(0);
            }
            return null;
            // VTExit.exit(0);
          }
          else if (character == '\u001A')
          {
            /* out.write('\n'); out.flush(); */
            VTConsole.toggleScrollMode();
            // VTExit.exit(0);
          }
          else if (character == '\u0018')
          {
            /* out.write('\n'); out.flush(); */
            VTConsole.toggleInputMode();
            // VTExit.exit(0);
          }
          else if (character == (char) -1)
          {
            return "";
          }
          else
          {
            if (VTGraphicalConsole.getCaretRecoilCount() == 0)
            {
              currentLineBuffer.append(character);
              /*
               * if (currentLineBuffer.length() >
               * VTGraphicalConsole.maxTerminalCharactersCount) {
               * currentLineBuffer = currentLineBuffer.delete(0,
               * currentLineBuffer.length() -
               * VTGraphicalConsole.maxTerminalCharactersCount); }
               */
            }
            else
            {
              if (VTGraphicalConsole.isReplaceActivated())
              {
                currentLineBuffer.setCharAt(currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount(), character);
              }
              else
              {
                currentLineBuffer.insert(currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount(), character);
                /*
                 * if (currentLineBuffer.length() > VTGraphicalConsole.
                 * maxTerminalCharactersCount) { currentLineBuffer =
                 * currentLineBuffer.delete(0, currentLineBuffer.length() -
                 * VTGraphicalConsole. maxTerminalCharactersCount); }
                 */
              }
              // currentLineBuffer.setCharAt(currentLineBuffer.length()
              // -
              // VTGraphicalConsole.getCaretRecoilCount(),
              // character);
            }
            if (VTGraphicalConsole.getCaretRecoilCount() > 0)
            {
              if (VTGraphicalConsole.isReplaceActivated())
              {
                VTGraphicalConsole.reduceCaretRecoilCount();
              }
            }
          }
          // System.out.println("[" + currentLineBuffer.toString() +
          // "]");
        }
      }
    }
    else
    {
      currentLineBuffer.append(removeFromPendingLine().replace("\n", ""));
      /*
       * if (currentLineBuffer.length() >
       * VTGraphicalConsole.maxTerminalCharactersCount) { currentLineBuffer =
       * currentLineBuffer.delete(0, currentLineBuffer.length() -
       * VTGraphicalConsole.maxTerminalCharactersCount); }
       */
      readingLine = false;
      if (echo)
      {
        if (VTGraphicalConsole.getDoubledOutput() != null)
        {
          VTGraphicalConsole.getDoubledOutput().println(currentLineBuffer.toString());
        }
        else
        {
          VTGraphicalConsoleWriter.write(currentLineBuffer.toString() + "\n");
        }
      }
      else
      {
        if (VTGraphicalConsole.getDoubledOutput() != null)
        {
          VTGraphicalConsole.getDoubledOutput().println();
        }
        else
        {
          VTGraphicalConsoleWriter.write("\n");
        }
      }
    }
    if (currentLineBuffer.toString().startsWith("\u001A"))
    {
      VTConsole.toggleScrollMode();
    }
    if (currentLineBuffer.toString().startsWith("\u0018"))
    {
      VTConsole.toggleInputMode();
    }
    if (echo)
    {
      if (currentLineBuffer.length() > 0)
      {
        if (!(commandHistory.size() < commandHistoryMaxSize))
        {
          commandHistory.remove(0);
          // System.out.println("removed from history: [" +
          // commandHistory.remove(0) +
          // "]");
        }
        commandHistory.remove(currentLineBuffer.toString());
        commandHistory.add(currentLineBuffer.toString());
        // System.out.println("added to history: [" +
        // currentLineBuffer.toString() +
        // "]");
      }
      commandHistoryPosition = commandHistory.size();
    }
    String currentLine = currentLineBuffer.toString().replace("\r", "");
    currentLineBuffer.setLength(0);
    currentThread = null;
    return currentLine;
  }
  
  /*
   * public synchronized static String readLine() throws InterruptedException {
   * return readLine(true); }
   */
  
  private static void scrollCommandHistoryUp(boolean echo)
  {
    // System.out.println("scrollCommandHistoryUp!");
    if (echo)
    {
      if (commandHistory.size() > 0)
      {
        if (commandHistoryPosition > 0)
        {
          commandHistoryPosition--;
          resetCurrentLine(echo);
          restoreCurrentLine(echo);
        }
      }
    }
  }
  
  private static void scrollCommandHistoryDown(boolean echo)
  {
    // System.out.println("scrollCommandHistoryDown!");
    if (echo)
    {
      if (commandHistory.size() > 0)
      {
        if (commandHistoryPosition < commandHistory.size() - 1)
        {
          commandHistoryPosition++;
          resetCurrentLine(echo);
          restoreCurrentLine(echo);
        }
      }
    }
  }
  
  private static void resetCurrentLine(boolean echo)
  {
    // System.out.println("currentLineBuffer.length: " +
    // currentLineBuffer.length());
    /*
     * for (int i = 0;i < currentLineBuffer.length();i++) {
     * VTGraphicalConsole.input('\b'); }
     */
    if (echo)
    {
      if (checkCurrentLineForReplace())
      {
        VTGraphicalConsole.backspace(currentLineBuffer.length(), true);
      }
    }
    currentLineBuffer.setLength(0);
    // currentLineBuffer.trimToSize();
    VTGraphicalConsole.updateCaretPosition();
  }
  
  private static void restoreCurrentLine(boolean echo)
  {
    // System.out.println("currentLineBuffer.length: " +
    // currentLineBuffer.length());
    if (echo)
    {
      currentLineBuffer.append(commandHistory.get(commandHistoryPosition));
      /*
       * if (currentLineBuffer.length() >
       * VTGraphicalConsole.maxTerminalCharactersCount) { currentLineBuffer =
       * currentLineBuffer.delete(0, currentLineBuffer.length() -
       * VTGraphicalConsole.maxTerminalCharactersCount); }
       */
      VTGraphicalConsoleWriter.write(commandHistory.get(commandHistoryPosition));
      VTGraphicalConsoleWriter.flush();
    }
    // VTGraphicalConsole.flush();
    // VTGraphicalConsole.input(commandHistory.get(commandHistoryPosition));
  }
  
  public static void appendToCurrentLine(String string)
  {
    /*
     * if (VTGraphicalConsole.getCaretRecoilCount() == 0) {
     * currentLineBuffer.append(string); if (echo) {
     * VTGraphicalConsole.print(string); } }
     */
    /* else { */
    int recoil = VTGraphicalConsole.getCaretRecoilCount();
    // int backspace = currentLineBuffer.length();
    int increment = currentLineBuffer.length();
    boolean checkBackspace = checkCurrentLineForReplace();
    if (VTGraphicalConsole.isReplaceActivated())
    {
      currentLineBuffer = currentLineBuffer.replace(currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount(), currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount() + string.length(), string);
      /*
       * if (currentLineBuffer.length() >
       * VTGraphicalConsole.maxTerminalCharactersCount) { currentLineBuffer =
       * currentLineBuffer.delete(0, currentLineBuffer.length() -
       * VTGraphicalConsole.maxTerminalCharactersCount); }
       */
      increment = currentLineBuffer.length() - increment;
    }
    else
    {
      currentLineBuffer = currentLineBuffer.insert(currentLineBuffer.length() - VTGraphicalConsole.getCaretRecoilCount(), string);
      /*
       * if (currentLineBuffer.length() >
       * VTGraphicalConsole.maxTerminalCharactersCount) { currentLineBuffer =
       * currentLineBuffer.delete(0, currentLineBuffer.length() -
       * VTGraphicalConsole.maxTerminalCharactersCount); }
       */
      // increment = string.length() + recoil;
    }
    if (echo && !VTGraphicalConsole.isFlushInterrupted())
    {
      if (!VTGraphicalConsole.isFlushInterrupted())
      {
        VTGraphicalConsole.setCaretRecoilCount(0);
        if (checkBackspace)
        {
          if (VTGraphicalConsole.isReplaceActivated())
          {
            VTGraphicalConsole.backspace(recoil, true);
            VTGraphicalConsoleWriter.write(currentLineBuffer.substring(currentLineBuffer.length() - recoil - increment, currentLineBuffer.length()));
          }
          else
          {
            VTGraphicalConsole.backspace(recoil, true);
            VTGraphicalConsoleWriter.write(currentLineBuffer.substring(currentLineBuffer.length() - recoil - string.length(), currentLineBuffer.length()));
          }
        }
        else
        {
          VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
        }
        // VTGraphicalConsole.write(currentLineBuffer.toString());
        VTGraphicalConsole.flushBuffered(false);
        VTGraphicalConsole.setCaretRecoilCount(recoil);
      }
      for (int i = 0; i < string.length(); i++)
      {
        if (VTGraphicalConsole.getCaretRecoilCount() > 0)
        {
          if (VTGraphicalConsole.isReplaceActivated())
          {
            VTGraphicalConsole.reduceCaretRecoilCount();
          }
        }
        else
        {
          break;
        }
      }
      if (!VTGraphicalConsole.isFlushInterrupted())
      {
        VTGraphicalConsole.updateCaretPosition();
      }
      // VTGraphicalConsole.write(currentLineBuffer.toString());
      // VTGraphicalConsole.flushBuffered(true);
    }
    else
    {
      for (int i = 0; i < string.length(); i++)
      {
        if (VTGraphicalConsole.getCaretRecoilCount() > 0)
        {
          if (VTGraphicalConsole.isReplaceActivated())
          {
            VTGraphicalConsole.reduceCaretRecoilCount();
          }
        }
        else
        {
          break;
        }
      }
    }
    // }
  }
  
  /*
   * private static void completeCurrentLine(boolean echo) { if (echo) { if
   * (commandHistory.size() > 0) { if (commandHistoryPosition >=
   * commandHistory.size()) { commandHistoryPosition = commandHistory.size() -
   * 1; } if (currentLineBuffer.length() <
   * commandHistory.get(commandHistoryPosition).length()) {
   * VTGraphicalConsole.print(commandHistory.get(commandHistoryPosition).
   * substring(currentLineBuffer.length()));
   * currentLineBuffer.append(commandHistory.get(commandHistoryPosition).
   * substring (currentLineBuffer.length())); } } } }
   */
  
  /*
   * private static void continueCurrentLine(boolean echo) {
   * //System.out.println("currentLineBuffer.length: " +
   * currentLineBuffer.length()); if (echo) { if (commandHistory.size() > 0) {
   * if (commandHistoryPosition >= commandHistory.size()) {
   * commandHistoryPosition = commandHistory.size() - 1; } if
   * (currentLineBuffer.length() <
   * commandHistory.get(commandHistoryPosition).length()) {
   * VTGraphicalConsole.write(commandHistory.get(commandHistoryPosition).
   * charAt( currentLineBuffer.length())); VTGraphicalConsole.flush();
   * currentLineBuffer.append(commandHistory.get(commandHistoryPosition).
   * charAt( currentLineBuffer.length())); } } } }
   */
  
  public static void appendToPendingLine(String line)
  {
    synchronized (pendingLineBuffer)
    {
      if (pendingLineBuffer.size() > 0)
      {
        if (pendingLineBuffer.get(pendingLineBuffer.size() - 1).contains("\n"))
        {
          pendingLineBuffer.add(line);
        }
        else
        {
          pendingLineBuffer.add(pendingLineBuffer.remove(pendingLineBuffer.size() - 1) + line);
        }
      }
      else
      {
        pendingLineBuffer.add(line);
      }
    }
  }
  
  private static String getFromPendingLine()
  {
    synchronized (pendingLineBuffer)
    {
      if (pendingLineBuffer.size() > 0)
      {
        return pendingLineBuffer.get(0);
      }
      else
      {
        return null;
      }
    }
  }
  
  private static String removeFromPendingLine()
  {
    synchronized (pendingLineBuffer)
    {
      if (pendingLineBuffer.size() > 0)
      {
        return pendingLineBuffer.remove(0);
      }
      else
      {
        return null;
      }
    }
  }
  
  private static int getPendingLineSize()
  {
    synchronized (pendingLineBuffer)
    {
      return pendingLineBuffer.size();
    }
  }
  
  public static void resumeOutputFlush()
  {
    if (echo && !VTGraphicalConsole.isFlushInterrupted())
    {
      if (checkCurrentLineForReplace())
      {
        VTGraphicalConsole.backspace(currentLineBuffer.length(), true);
      }
      VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
      VTGraphicalConsole.flushBuffered(false);
    }
  }
  
  private static boolean checkCurrentLineForCaretPositionChange()
  {
    String currentLine = currentLineBuffer.toString();
    String selected = "";
    int increment = 0;
    while (selected != null && selected.length() < currentLine.length())
    {
      selected = VTGraphicalConsole.getLastText(currentLine.length() + increment++);
    }
    if (selected == null)
    {
      return false;
    }
    if (selected.substring(0, currentLine.length()).equals(currentLine))
    {
      return true;
    }
    return false;
  }
  
  private static boolean checkCurrentLineForReplace()
  {
    String currentLine = currentLineBuffer.toString();
    String selected = "";
    int increment = 0;
    while (selected != null && selected.length() < currentLine.length())
    {
      selected = VTGraphicalConsole.getLastText(currentLine.length() + increment++);
    }
    if (selected == null)
    {
      return true;
    }
    if (selected.substring(0, currentLine.length()).equals(currentLine))
    {
      return true;
    }
    return false;
  }
  
  /*
   * public static void appendToPendingLine(String string) {
   * pendingLineBuffer.append(string); }
   */
}