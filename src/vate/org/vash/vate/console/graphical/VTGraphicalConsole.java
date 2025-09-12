package org.vash.vate.console.graphical;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Panel;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.dnd.DropTarget;
import java.io.BufferedWriter;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.util.Locale;
import java.util.TooManyListenersException;

import org.vash.vate.console.VTMainConsole;
import org.vash.vate.console.VTConsoleBooleanToggleNotify;
import org.vash.vate.VTSystem;
import org.vash.vate.console.VTConsole;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleDropTargetListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleKeyListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleMouseListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleWindowListener;
import org.vash.vate.console.graphical.menu.VTGraphicalConsolePopupMenu;
import org.vash.vate.graphics.font.VTFontManager;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.stream.filter.VTDoubledOutputStream;

public class VTGraphicalConsole extends VTConsole
{
  public static final char VT_VK_LEFT = '\uFFFF';
  public static final char VT_VK_UP = '\uFFFE';
  public static final char VT_VK_DOWN = '\uFFFD';
  public static final char VT_VK_HOME = '\uFFFC';
  public static final char VT_VK_END = '\uFFFB';
  public static final char VT_VK_RIGHT = '\uFFFA';
  public static final char VT_VK_DELETE = '\uFFF9';
  private static final int maxCharactersPerLine = 80;
  private static final int totalCharactersPerLine = maxCharactersPerLine + 1;
  private static final int tabSize = 8;
  private static final int maxLines = 200;
  private static final int areaLines = 26;
  private static final int maxCharactersCount = (maxLines * totalCharactersPerLine) - 1;
  // public static final int maxTerminalCharactersCount = (maxLines *
  // maxCharactersPerLine);
  public static boolean ignoreClose;
  private static boolean split;
  private static boolean readingInput;
  private static boolean updatingTerminal;
  private static boolean flushInterrupted;
  private static boolean replaceActivated;
  private static boolean changedTerminal;
  // private static final int replaceCharacterCountLimit = maxCharacterCount -
  // totalCharactersPerLine;
  private static int totalCharactersCount;
  private static int caretRecoilCount;
  private static int lineCount;
  private static int overwriteCharactersCount;
  private static int returnCharactersCount;
  private static int charactersInLineCount;
  private static int deletedLastLinesCount;
  private static int deleteLaterCharactersCount;
  private static Object inputSynchronizer;
  private static Object outputSynchronizer;
  private static Object updateSynchronizer;
  private static String trueBlankLine;
  // private static String replacedBlankLine;
  private static String trueBlankLineEnd;
  // private static String replacedBlankLineEnd;
  private static String trueBlankArea;
  private static String replacedBlankArea;
  private static StringBuilder inputBuffer;
  private static StringBuilder outputBuffer;
  private static StringBuilder spacesBuffer;
  private static StringBuilder flushBuffer;
  private static StringBuilder screenBuffer;
  private static VTGraphicalConsoleInputStream inputStream = new VTGraphicalConsoleInputStream();
  private static VTGraphicalConsolePrintStream printStream = new VTGraphicalConsolePrintStream();
  private static VTGraphicalConsole instance;
  private static VTGraphicalConsoleFrame frame;
  private static VTGraphicalConsoleTextArea textArea;
  private static VTGraphicalConsoleTextField textField;
  // private static VTGraphicalConsolePopupMenu popupMenu;
  private static VTGraphicalConsoleKeyListener keyListener;
  // private static VTGraphicalConsoleTextListener textListener;
  private static VTGraphicalConsoleMouseListener mouseListener;
  private static VTGraphicalConsoleDropTargetListener dropTargetListener;
  private static VTGraphicalConsoleWindowListener windowListener;
  private static boolean remoteIcon = false;
  private static UpdateTask updateTask = new UpdateTask();
  private static VTConsoleBooleanToggleNotify notifyFlushInterrupted;
  private static VTConsoleBooleanToggleNotify notifyReplaceInput;
  private static PrintStream logOutput;
  private static BufferedWriter readLineLog = null;
  // private static VTGraphicalConsoleReader reader;
  // private static VTGraphicalConsoleWriter writer;
  private static PrintStream doubledOutput;
  private static PrintStream doubledError;
  
  private VTGraphicalConsole()
  {
    // setShellBuilder();
    // split = true;
    inputSynchronizer = new Object();
    outputSynchronizer = new Object();
    updateSynchronizer = new Object();
    inputBuffer = new StringBuilder();
    outputBuffer = new StringBuilder();
    spacesBuffer = new StringBuilder();
    flushBuffer = new StringBuilder();
    screenBuffer = new StringBuilder();
    for (int i = 0; i < maxCharactersPerLine; i++)
    {
      outputBuffer.append('\b');
    }
    outputBuffer.append('\n');
    trueBlankLine = outputBuffer.toString();
    // replacedBlankLine = trueBlankLine.replace('\b', ' ');
    // outputBuffer.trimToSize();
    outputBuffer.setLength(0);
    // outputBuffer.trimToSize();
    outputBuffer.append('\n');
    for (int i = 0; i < maxCharactersPerLine; i++)
    {
      outputBuffer.append('\b');
    }
    trueBlankLineEnd = outputBuffer.toString();
    // replacedBlankLineEnd = trueBlankLineEnd.replace('\b', ' ');
    // outputBuffer.trimToSize();
    outputBuffer.setLength(0);
    // outputBuffer.trimToSize();
    for (int i = 0; i < maxLines; i++)
    {
      outputBuffer.append(trueBlankLine);
    }
    outputBuffer.deleteCharAt(outputBuffer.length() - 1);
    trueBlankArea = outputBuffer.toString();
    replacedBlankArea = trueBlankArea.replace('\b', ' ');
    // outputBuffer.trimToSize();
    // totalTextSize = blankArea.length();
    outputBuffer.setLength(0);
    outputBuffer.trimToSize();
    windowListener = new VTGraphicalConsoleWindowListener();
    frame = new VTGraphicalConsoleFrame(remoteIcon);
    frame.setLocationByPlatform(true);
    frame.addWindowListener(windowListener);
    textArea = new VTGraphicalConsoleTextArea("", areaLines, totalCharactersPerLine + 2, TextArea.SCROLLBARS_BOTH);
    textArea.setEditable(false);
    // frame.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    // textArea.setCursor(Cursor.getPredefinedCursor(Cursor.CROSSHAIR_CURSOR));
    // textArea.setFocusable(false);
    // bright = true;
    textArea.setForeground(Color.GREEN);
    // foregroundColor = VTTerminal.VT_CONSOLE_COLOR_LIGHT_GREEN;
    textArea.setBackground(Color.BLACK);
    // backgroundColor = VTTerminal.VT_CONSOLE_COLOR_NORMAL_BLACK;
    // textArea.setFont(new
    // Font(Font.decode(Font.MONOSPACED).getFamily(Locale.getDefault()),
    // Font.PLAIN,
    // 12));
    // int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
    // double factor = dpi / 96;
    // textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
    // textArea.setPreferredSize(new
    // Dimension(textArea.getGraphics().getFontMetrics().stringWidth(trueBlankLine),
    // textArea.getGraphics().getFontMetrics().getHeight() * 25));
    // textArea.setFont(Font.decode("Monospaced"));
    // textArea.setFont(Font.getFont("Monospaced"));
    // textArea.getInputContext().setCompositionEnabled(true);
    // listeningTextEvents = true;
    if (split)
    {
      textField = new VTGraphicalConsoleTextField("", totalCharactersPerLine + 2);
      textField.setForeground(Color.GREEN);
      textField.setBackground(Color.BLACK);
      textField.setFont(new Font("Monospaced", Font.PLAIN, 12));
    }
    updatingTerminal = false;
    readingInput = false;
    // popupMenu = new VTGraphicalConsolePopupMenu();
    VTGraphicalConsolePopupMenu popupMenu = new VTGraphicalConsolePopupMenu(this, frame);
    keyListener = new VTGraphicalConsoleKeyListener(popupMenu);
    mouseListener = new VTGraphicalConsoleMouseListener(popupMenu);
    dropTargetListener = new VTGraphicalConsoleDropTargetListener(this);
    // textListener = new VTGraphicalConsoleTextListener();
    textArea.addKeyListener(keyListener);
    // textArea.addTextListener(textListener);
    textArea.addMouseListener(mouseListener);
    textArea.setDropTarget(new DropTarget());
    textArea.getDropTarget().setActive(true);
    try
    {
      textArea.getDropTarget().addDropTargetListener(dropTargetListener);
    }
    catch (TooManyListenersException e)
    {
      // e.printStackTrace();
    }
    // textArea.setText(replacedBlankArea);
    // screenBuffer.append(trueBlankArea);
    // textArea.setCaretPosition(0);
    textArea.setText("__________________________________________________________________________________________");
    // textArea.setText("________________________________________________________________________________");
    textArea.setCaretPosition(0);
    // VTGraphicalConsoleTextAreaScrollPane scrolling = new
    // VTGraphicalConsoleTextAreaScrollPane(textArea);
    frame.add(textArea, BorderLayout.CENTER);
    // frame.add(scrolling, BorderLayout.CENTER);
    if (split)
    {
      frame.add(textField, BorderLayout.SOUTH);
    }
    frame.setResizable(true);
    // frame.setMenuBar(new MenuBar());
    frame.pack();
    frame.setVisible(true);
    textArea.setText(replacedBlankArea);
    screenBuffer.append(trueBlankArea);
    textArea.setCaretPosition(0);
    VTFontManager.registerWindow(frame);
    VTFontManager.registerMonospacedComponent(textArea);
    frame.pack();
    frame.toFront();
    updateTask.start();
    // updateTask.flush(true, 0, 0);
    // frame.createBufferStrategy(3);
    // reader = new VTGraphicalConsoleReader();
    // writer = new VTGraphicalConsoleWriter();
  }
  
  private static class UpdateTask implements Runnable
  {
    private boolean running = true;
    private boolean updated = false;
    private boolean caret = false;
    private int start = 0;
    private int end = 0;
    
    public void start()
    {
      Thread newThread = new Thread(this);
      newThread.setDaemon(true);
      newThread.start();
    }
    
    @SuppressWarnings("unused")
    public void stop()
    {
      synchronized (updateSynchronizer)
      {
        running = false;
        updateSynchronizer.notifyAll();
      }
    }
    
    public void flush(boolean caret, int start, int end)
    {
      this.caret = caret;
      // System.out.println("flush:" + caret);
      synchronized (updateSynchronizer)
      {
        if (!updated)
        {
          this.start = Integer.MAX_VALUE;
          this.end = 0;
        }
        this.updated = true;
        if (start <= this.start)
        {
          this.start = start;
        }
        if (end >= this.end)
        {
          this.end = end;
        }
        updateSynchronizer.notifyAll();
      }
    }
    
    public void run()
    {
      while (running)
      {
        try
        {
          synchronized (updateSynchronizer)
          {
            while (!updated)
            {
              updateSynchronizer.wait();
            }
            updated = false;
          }
          update(caret, start, end);
          Thread.yield();
          Thread.sleep(1);
        }
        catch (Throwable e)
        {
          
        }
      }
    }
    
  }
  
  public synchronized static VTGraphicalConsole getInstance(boolean remoteIcon)
  {
    VTGraphicalConsole.remoteIcon = remoteIcon;
    if (instance == null)
    {
      instance = new VTGraphicalConsole();
    }
    return instance;
  }
  
  public void setIgnoreClose(boolean ignoreClose)
  {
    VTGraphicalConsole.ignoreClose = ignoreClose;
  }
  
  public static void setCaretRecoilCount(int caretRecoilCount)
  {
    if (caretRecoilCount == 0)
    {
      overwriteCharactersCount = 0;
      // overwriteBufferedCharactersCount =
      // Math.min(overwriteCharactersCount,
      // outputBuffer.length());
      VTGraphicalConsole.caretRecoilCount = 0;
      return;
    }
    /*
     * if (caretRecoilCount >= totalCharactersCount) { caretRecoilCount =
     * totalCharactersCount - 1; }
     */
    if (caretRecoilCount > VTGraphicalConsole.caretRecoilCount)
    {
      while (caretRecoilCount > VTGraphicalConsole.caretRecoilCount && overwriteCharactersCount < totalCharactersCount)
      {
        increaseCaretRecoilCount();
      }
    }
    else if (caretRecoilCount < VTGraphicalConsole.caretRecoilCount)
    {
      while (caretRecoilCount < VTGraphicalConsole.caretRecoilCount && overwriteCharactersCount > 0)
      {
        reduceCaretRecoilCount();
      }
    }
    // overwriteCharactersCount = caretRecoilCount;
  }
  
  public static int getCaretRecoilCount()
  {
    return caretRecoilCount;
    // return overwriteCharactersCount;
  }
  
  /*
   * public static void setCaretRecoilLines(int caretRecoilLines) {
   * overwriteLineCount = caretRecoilLines; } public static int
   * getCaretRecoilLines() { return overwriteLineCount; }
   */
  
  public static void increaseCaretRecoilCount()
  {
    if (overwriteCharactersCount >= totalCharactersCount)
    {
      return;
    }
    if (getCaretRecoilCount() == 0)
    {
      overwriteCharactersCount = 0;
    }
    overwriteCharactersCount++;
    // overwriteBufferedCharactersCount++;
    // overwriteBufferedCharactersCount = Math.min(overwriteCharactersCount,
    // outputBuffer.length());
    if (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount) == '\t')
    {
      returnTabInScreenBuffer();
    }
    if ((totalCharactersCount - overwriteCharactersCount) % (totalCharactersPerLine) == maxCharactersPerLine)
    {
      overwriteCharactersCount++;
      // overwriteBufferedCharactersCount =
      // Math.min(overwriteCharactersCount,
      // outputBuffer.length());
      // overwriteBufferedCharactersCount++;
      // overwriteLineCount++;
    }
    caretRecoilCount++;
    // System.out.println(screenBuffer.charAt(totalCharactersCount -
    // overwriteCharactersCount - 1));
    // System.out.println(overwriteLineCount);
  }
  
  public static void reduceCaretRecoilCount()
  {
    if (getCaretRecoilCount() == 0)
    {
      overwriteCharactersCount = 0;
      return;
    }
    overwriteCharactersCount--;
    if (overwriteCharactersCount < 0)
    {
      overwriteCharactersCount = 0;
      return;
    }
    // overwriteBufferedCharactersCount--;
    // overwriteBufferedCharactersCount = Math.min(overwriteCharactersCount,
    // outputBuffer.length());
    if (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount) == '\t')
    {
      advanceTabInScreenBuffer();
    }
    // if ((totalCharactersCount - overwriteCharactersCount) %
    // (totalCharactersPerLine) == maxCharactersPerLine)
    if ((totalCharactersCount - overwriteCharactersCount) % (totalCharactersPerLine) == maxCharactersPerLine)
    {
      overwriteCharactersCount--;
      // overwriteBufferedCharactersCount =
      // Math.min(overwriteCharactersCount,
      // outputBuffer.length());
      // overwriteBufferedCharactersCount--;
      // overwriteLineCount--;
    }
    caretRecoilCount--;
    // System.out.println(screenBuffer.charAt(totalCharactersCount -
    // overwriteCharactersCount - 1));
  }
  
  /*
   * public static void setSplit(boolean split) { VTGraphicalConsole.split =
   * split; }
   */
  
  public static int available()
  {
    return inputBuffer.length();
  }
  
  public static boolean isFlushInterrupted()
  {
    synchronized (outputSynchronizer)
    {
      return flushInterrupted && frame.isActive();
    }
    // System.out.println("flushInterrupted:" + flushInterrupted);
    // System.out.println("frameIconified:" + frameIconified);
    // return flushInterrupted || frameIconified;
  }
  
  public static void interruptOutputFlush()
  {
    // System.out.println("interruptOutputFlush");
    // VTGraphicalConsole.setCaretRecoilCount(0);
    synchronized (outputSynchronizer)
    {
      flushInterrupted = true;
    }
    
    if (notifyFlushInterrupted != null)
    {
      notifyFlushInterrupted.notify(flushInterrupted);
    }
    // flushInterrupted = true;
  }
  
  public static void resumeOutputFlush()
  {
    synchronized (outputSynchronizer)
    {
      flushInterrupted = false;
    }
    
    if (notifyFlushInterrupted != null)
    {
      notifyFlushInterrupted.notify(flushInterrupted);
    }
    
    // flushInterrupted = false;
    VTGraphicalConsoleReader.resumeOutputFlush();
    flushBuffered(true);
  }
  
  public static void iconifyFrame()
  {
    // System.out.println("iconifyFrame");
//		synchronized (outputSynchronizer)
//		{
//			frameIconified = true;
//		}
    // frameIconified = true;
    VTGraphicalConsole.setCaretRecoilCount(0);
//		synchronized (outputSynchronizer)
//		{
//			frameIconified = true;
//		}
  }
  
  public static void deiconifyFrame()
  {
    // System.out.println("deiconifyFrame");
//		synchronized (outputSynchronizer)
//		{
//			frameIconified = false;
//		}
    // frameIconified = false;
    // frameIconified = false;
    VTGraphicalConsoleReader.resumeOutputFlush();
    flushBuffered(true);
  }
  
  public static boolean isReplaceActivated()
  {
    synchronized (outputSynchronizer)
    {
      return replaceActivated;
    }
  }
  
  public void toggleInputMode()
  {
    synchronized (outputSynchronizer)
    {
      replaceActivated = !replaceActivated;
      updateCaretPosition();
    }
    
    if (notifyReplaceInput != null)
    {
      notifyReplaceInput.notify(replaceActivated);
    }
  }
  
  public static void updateCaretPosition()
  {
    // System.out.println("updateCaretPosition():" +
    // Thread.currentThread().getStackTrace()[2]);
    if (!isFlushInterrupted())
    {
      if (replaceActivated)
      {
        textArea.select(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + 1);
      }
      else
      {
        textArea.setCaretPosition(totalCharactersCount - overwriteCharactersCount);
      }
      // textArea.setTrueCaretPosition(totalCharactersCount -
      // overwriteCharactersCount);
      // textArea.setSelectionStart(totalCharactersCount -
      // overwriteCharactersCount);
      // textArea.setSelectionEnd(totalCharactersCount -
      // overwriteCharactersCount +
      // 1);
      // textArea.select(totalCharactersCount - overwriteCharactersCount,
      // totalCharactersCount - overwriteCharactersCount + 1);
    }
    /*
     * else if (flushInterrupted && !frameIconified) {
     * textArea.setSelectionEnd(textArea.getSelectionStart()); }
     */
  }
  
  /*
   * public static void refreshCaretPosition() {
   * textArea.setSelectionStart(textArea.getCaretPosition());
   * textArea.setSelectionEnd(textArea.getCaretPosition() + 1); }
   */
  
  public static void clearCaretPosition()
  {
    if (replaceActivated)
    {
      textArea.select(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + 1);
    }
    else
    {
      textArea.setCaretPosition(totalCharactersCount - overwriteCharactersCount);
    }
    // textArea.select(totalCharactersCount - overwriteCharactersCount,
    // totalCharactersCount - overwriteCharactersCount + 1);
    // textArea.setTrueCaretPosition(totalCharactersCount -
    // overwriteCharactersCount);
    // textArea.setSelectionStart(totalCharactersCount -
    // overwriteCharactersCount);
    // textArea.setSelectionEnd(totalCharactersCount -
    // overwriteCharactersCount +
    // 1);
  }
  
  public static boolean isCaretPositionUpdated()
  {
    return (textArea.getCaretPosition() == totalCharactersCount - overwriteCharactersCount);
    // && textArea.getTrueCaretPosition() == totalCharactersCount -
    // overwriteCharactersCount
    // && textArea.getSelectionStart() == totalCharactersCount -
    // overwriteCharactersCount
    // && textArea.getSelectionEnd() == totalCharactersCount -
    // overwriteCharactersCount + 1);
  }
  
  public static void increaseCharacterCount()
  {
    if (overwriteCharactersCount == 0)
    {
      /*
       * if (outputBuffer.length() >= maxCharacterCount) { deleteLineInBuffer();
       * }
       */
      if (charactersInLineCount == maxCharactersPerLine)
      {
        // flushBufferedAsynchronous();
        completeLineInBuffer();
        // textArea.setCaretPosition(totalCharactersCount);
        increaseLineCount();
        // textArea.setCaretPosition(totalCharactersCount);
      }
      totalCharactersCount++;
      charactersInLineCount++;
    }
    else
    {
      if (getCaretRecoilCount() > 0)
      {
        reduceCaretRecoilCount();
      }
      else
      {
        overwriteCharactersCount--;
      }
    }
  }
  
  private static void reduceCharacterCount()
  {
    /*
     * if (flushInterrupted) { }
     */
    /* if (overwriteCharactersCount > 0) { overwriteCharactersCount--; } */
    if (charactersInLineCount > 0)
    {
      totalCharactersCount--;
      charactersInLineCount--;
    }
    else
    {
      reduceLineCount();
      charactersInLineCount--;
      if (charactersInLineCount < 0)
      {
        charactersInLineCount = 0;
      }
      // textArea.setCaretPosition(totalCharactersCount);
      // output('\b');
    }
  }
  
  private static void increaseLineCount()
  {
    charactersInLineCount = 0;
    // overwriteInLineCount = 0;
    lineCount++;
    totalCharactersCount = totalCharactersPerLine * lineCount;
    if (lineCount == maxLines)
    {
      deleteLastLine();
    }
  }
  
  private static void reduceLineCount()
  {
    // overwriteInLineCount = 0;
    // deleteLaterCharactersCount = 0;
    /* if (flushInterrupted) { deleteLaterCharactersCount = 0; } */
    if (lineCount > 0)
    {
      lineCount--;
      totalCharactersCount = totalCharactersPerLine * lineCount + maxCharactersPerLine - 1;
      charactersInLineCount = maxCharactersPerLine;
    }
    else
    {
      // totalCharactersCount = 0;
      charactersInLineCount = 0;
    }
    if (!isFlushInterrupted())
    {
      if (outputBuffer.lastIndexOf("\n") != -1)
      {
        outputBuffer.deleteCharAt(outputBuffer.lastIndexOf("\n"));
      }
      /*
       * if (deletedLastLinesCount > 0) { deletedLastLinesCount--;
       * //totalCharactersCount += totalCharactersPerLine; //lineCount++;
       * //System.out.println("deletedLastLinesCount: " +
       * deletedLastLinesCount); }
       */
    }
  }
  
  private static void deleteLastLine()
  {
    // textArea.replaceRange(textArea.getText().substring(totalCharactersPerLine),
    // 0, replaceCharacterCountLimit);
    // textArea.replaceRange(blankLineEnd, replaceCharacterCountLimit,
    // maxCharacterCount);
    // overwriteInLineCount = 0;
    totalCharactersCount -= totalCharactersPerLine;
    lineCount--;
    if (deletedLastLinesCount < maxLines)
    {
      deletedLastLinesCount++;
    }
    // reduceLineCount();
    // charactersInLineCount = 0;
  }
  
  /*
   * private static void buildReplacement(StringBuilder str, int start, int end,
   * StringBuilder out, String replacement) { //System.out.println(start);
   * //System.out.println(end); //System.out.println(str.substring(start, end));
   * String[] values = str.substring(start, end).split("\n");
   * System.out.println("substring: [" + str.substring(start, end) + "]");
   * System.out.println("replacement: [" + replacement + "]"); int appended = 0;
   * int remaining = replacement.length(); int min = 0;
   * System.out.println("values: " + values.length); for (String value : values)
   * { out.append("\n"); if (remaining > 0) { System.out.println("appended!");
   * System.out.println("value.length() :" + value.length()); min =
   * Math.min(remaining, value.length());
   * out.append(replacement.substring(appended, appended + min)); appended +=
   * min; remaining -= min; } else { System.out.println("else!");
   * out.append(value); } } out.deleteCharAt(0);
   * //System.out.println(out.toString()); }
   */
  
  /*
   * private static int countOccurencesInString(char c, String str) { int count
   * = 0; for (int i = 0;i < str.length();i++) { if (str.charAt(i) == c) {
   * count++; } } return count; }
   */
  
  /*
   * public static void replaceAtEnd(String str) { try { synchronized
   * (outputSynchronizer) { while (updatingTerminal) { try {
   * outputSynchronizer.wait(); } catch (InterruptedException e) { } }
   * updatingTerminal = true; int targetLength = str.length(); int currentLength
   * = targetLength; if (targetLength > (currentLength -
   * countOccurencesInString('\n', screenBuffer.substring(totalCharactersCount -
   * currentLength, totalCharactersCount)))) { while (targetLength >
   * (currentLength - countOccurencesInString('\n',
   * screenBuffer.substring(totalCharactersCount - currentLength,
   * totalCharactersCount)))) { currentLength++; } } spacesBuffer.setLength(0);
   * //System.out.println(countOccurencesInString('\n',
   * screenBuffer.substring(totalCharactersCount - currentLength,
   * totalCharactersCount))); //System.out.println("[" +
   * screenBuffer.substring(totalCharactersCount - currentLength,
   * totalCharactersCount) + "]"); buildReplacement(screenBuffer,
   * totalCharactersCount - currentLength, totalCharactersCount, spacesBuffer,
   * str); System.out.println("spacesBuffer.length: " + spacesBuffer.length());
   * System.out.println("spacesBuffer: [" + spacesBuffer.toString() + "]");
   * screenBuffer.replace(totalCharactersCount - spacesBuffer.length(),
   * totalCharactersCount, spacesBuffer.toString()); String spacesBufferContents
   * = spacesBuffer.toString().replace('\r', ' ').replace('\t', '
   * ').replace('\b', ' ').replace('\f', ' ');
   * System.out.println("spacesBufferContents: [" + spacesBufferContents + "]");
   * textArea.replaceRange(spacesBufferContents, totalCharactersCount -
   * spacesBufferContents.length(), totalCharactersCount);
   * //textArea.replaceRange(spacesBuffer.toString(), totalCharactersCount -
   * spacesBuffer.length(), totalCharactersCount); spacesBuffer.setLength(0); }
   * } finally { synchronized (outputSynchronizer) { updatingTerminal = false;
   * outputSynchronizer.notify(); } } }
   */
  
  /*
   * private static void clearLastDeletedLines() { if (deletedLastLinesCount >
   * 0) { if (deletedLastLinesCount < maxLines) { spacesBuffer.setLength(0);
   * spacesBuffer.append(textArea.getText().substring(totalCharactersPerLine *
   * deletedLastLinesCount)); for (int i = 0;i < deletedLastLinesCount;i++) {
   * spacesBuffer.append(blankLineEnd); }
   * textArea.replaceRange(spacesBuffer.toString(), maxCharactersCount -
   * spacesBuffer.length(), maxCharactersCount); spacesBuffer.setLength(0); }
   * else { textArea.setText(blankArea); } deletedLastLinesCount = 0; } }
   */
  
  public static void input(char c)
  {
    if (c == '\u0003')
    {
      if (ignoreClose)
      {
        return;
      }
      VTRuntimeExit.exit(0);
    }
    if (c == '\u001A')
    {
      VTMainConsole.toggleFlushMode();
    }
    if (c == '\u0018')
    {
      VTMainConsole.toggleInputMode();
    }
    if (c != VT_VK_DELETE && c != VT_VK_RIGHT && c != VT_VK_LEFT && c != VT_VK_UP && c != VT_VK_DOWN && c != VT_VK_HOME && c != VT_VK_END)
    {
      synchronized (inputSynchronizer)
      {
        
        inputBuffer.append(c);
        if (readingInput && inputBuffer.length() == 1)
        {
          inputSynchronizer.notify();
        }
      }
    }
  }
  
  public static void inputSpecial(char c)
  {
    if (c == '\u0003')
    {
      if (ignoreClose)
      {
        return;
      }
      VTRuntimeExit.exit(0);
    }
    if (c == '\u001A')
    {
      VTMainConsole.toggleFlushMode();
    }
    if (c == '\u0018')
    {
      VTMainConsole.toggleInputMode();
    }
    synchronized (inputSynchronizer)
    {
      inputBuffer.append(c);
      if (readingInput && inputBuffer.length() == 1)
      {
        inputSynchronizer.notify();
      }
    }
  }
  
  public void clearInput()
  {
    synchronized (inputSynchronizer)
    {
      VTGraphicalConsoleReader.clearInput();
      // inputBuffer.setLength(0);
      inputSynchronizer.notify();
    }
  }
  
  public void input(String string)
  {
    // System.out.println("input: [" + string + "]");
    synchronized (inputSynchronizer)
    {
      if (readingInput || VTGraphicalConsoleReader.isReadingLine())
      {
        if (string.indexOf('\n') == -1)
        {
          VTGraphicalConsoleReader.appendToCurrentLine(string);
        }
        else
        {
          int i = 0, j = 0;
          i = string.indexOf('\n');
          VTGraphicalConsoleReader.appendToCurrentLine(string.substring(0, i));
          j = i + 1;
          i = string.indexOf('\n', j);
          while (i != -1)
          {
            // System.out.println("[" + string.substring(j, i) +
            // "]");
            VTGraphicalConsoleReader.appendToPendingLine(string.substring(j, i) + "\n");
            j = i + 1;
            i = string.indexOf('\n', j);
          }
          if (j < string.length())
          {
            // System.out.println("[" + string.substring(j) + "]");
            VTGraphicalConsoleReader.appendToPendingLine(string.substring(j));
          }
          
          inputBuffer.append('\n');
          if (inputBuffer.length() == 1)
          {
            inputSynchronizer.notify();
          }
        }
      }
      else
      {
        if (string.indexOf('\n') == -1)
        {
          VTGraphicalConsoleReader.appendToPendingLine(string);
        }
        else
        {
          int i = 0, j = 0;
          i = string.indexOf('\n');
          VTGraphicalConsoleReader.appendToPendingLine(string.substring(0, i) + "\n");
          j = i + 1;
          i = string.indexOf('\n', j);
          while (i != -1)
          {
            // System.out.println("[" + string.substring(j, i) +
            // "]");
            VTGraphicalConsoleReader.appendToPendingLine(string.substring(j, i) + "\n");
            j = i + 1;
            i = string.indexOf('\n', j);
          }
          if (j < string.length())
          {
            // System.out.println("[" + string.substring(j) + "]");
            VTGraphicalConsoleReader.appendToPendingLine(string.substring(j));
          }
        }
      }
    }
  }
  
  public static void output(char c)
  {
    try
    {
      synchronized (outputSynchronizer)
      {
        while (updatingTerminal)
        {
          try
          {
            outputSynchronizer.wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
        updatingTerminal = true;
        if (c == '\b')
        {
          if (totalCharactersCount - overwriteCharactersCount > 0)
          {
            reduceCharacterCount();
            if (isFlushInterrupted())
            {
              if (outputBuffer.length() > 0)
              {
                if (outputBuffer.charAt(outputBuffer.length() - 1 - overwriteCharactersCount) == '\t')
                {
                  outputBuffer.deleteCharAt(outputBuffer.length() - 1 - overwriteCharactersCount);
                  deleteTabInBuffer();
                }
                else
                {
                  outputBuffer.deleteCharAt(outputBuffer.length() - 1 - overwriteCharactersCount);
                }
              }
              else
              {
                if (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount) == '\t')
                {
                  deleteTabInScreenBufferLater();
                }
                else
                {
                  screenBuffer.replace(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + 1, "\b");
                  deleteLaterCharactersCount++;
                }
              }
            }
            else
            {
              if (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount) == '\t')
              {
                deleteTabInScreenBuffer();
              }
              else
              {
                screenBuffer.replace(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + 1, "\b");
                textArea.replaceRange(" ", totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + 1);
                if (overwriteCharactersCount > 0)
                {
                  overwriteCharactersCount--;
                }
              }
            }
          }
        }
        else if (c == '\u007f')
        {
          // Do nothing
        }
        else if (c == '\u0007')
        {
          staticBell();
          c = ' ';
        }
        else if (c == '\t')
        {
          if (isFlushInterrupted())
          {
            if (deleteLaterCharactersCount > 0)
            {
              deleteLaterCharactersCount--;
            }
          }
          if (returnCharactersCount > 0 && returnCharactersCount <= outputBuffer.length())
          {
            increaseCharacterCount();
            outputBuffer.setCharAt(outputBuffer.length() - returnCharactersCount--, '\t');
          }
          else
          {
            if (outputBuffer.length() == maxCharactersCount)
            {
              deleteLineInBuffer();
            }
            increaseCharacterCount();
            outputBuffer.append('\t');
          }
          completeTabInBuffer();
        }
        else if (c == '\f')
        {
          /*
           * outputBuffer.setLength(0); textArea.replaceRange(replacedBlankArea,
           * 0, maxCharactersCount); screenBuffer.replace(0, maxCharactersCount,
           * trueBlankArea); //textArea.setCaretPosition(0);
           * totalCharactersCount = 0; lineCount = 0; charactersInLineCount = 0;
           * overwriteCharactersCount = 0; overwriteBufferedCharactersCount = 0;
           * deletedLastLinesCount = 0; updateCaretPosition();
           */
          if (isFlushInterrupted())
          {
            if (deleteLaterCharactersCount > 0)
            {
              deleteLaterCharactersCount--;
            }
          }
          if (returnCharactersCount > 0 && returnCharactersCount <= outputBuffer.length())
          {
            increaseCharacterCount();
            outputBuffer.setCharAt(outputBuffer.length() - returnCharactersCount--, '\f');
          }
          else
          {
            if (outputBuffer.length() == maxCharactersCount)
            {
              deleteLineInBuffer();
            }
            increaseCharacterCount();
            outputBuffer.append('\f');
          }
        }
        else if (c == '\r')
        {
          // flushBufferedAsynchronous();
          setCaretRecoilCount(0);
          // setCaretRecoilCount(charactersInLineCount);
          // setCaretRecoilCount(0);
          overwriteCharactersCount = charactersInLineCount;
          // setCaretRecoilCount(charactersInLineCount);
          returnCharactersCount = Math.min(overwriteCharactersCount, outputBuffer.length());
          // System.out.println(returnCharactersCount);
          // textArea.setCaretPosition(totalCharactersCount -
          // charactersInLineCount);
        }
        else if (c == '\n')
        {
          // flushBufferedAsynchronous();
          // overwriteCharactersCount = 0;
          setCaretRecoilCount(0);
          returnCharactersCount = 0;
          // overwriteBufferedCharactersCount = 0;
          // caretRecoilCount = 0;
          completeLineInBuffer();
          increaseLineCount();
          // textArea.setCaretPosition(totalCharactersCount);
        }
        else
        {
          // System.out.println("overwriteCharactersCount: " +
          // overwriteCharactersCount);
          if (isFlushInterrupted())
          {
            if (deleteLaterCharactersCount > 0)
            {
              deleteLaterCharactersCount--;
            }
          }
          if (returnCharactersCount > 0 && returnCharactersCount <= outputBuffer.length())
          {
            increaseCharacterCount();
            outputBuffer.setCharAt(outputBuffer.length() - returnCharactersCount--, c);
          }
          else
          {
            if (outputBuffer.length() == maxCharactersCount)
            {
              deleteLineInBuffer();
            }
            increaseCharacterCount();
            outputBuffer.append(c);
          }
        }
      }
    }
    finally
    {
      synchronized (outputSynchronizer)
      {
        updatingTerminal = false;
        outputSynchronizer.notify();
      }
    }
  }
  
  private static void deleteLineInBuffer()
  {
    outputBuffer.delete(0, outputBuffer.indexOf("\n") + 1);
  }
  
  /*
   * private static void deleteTabInOutputBuffer() { int deleted = 1; while
   * (outputBuffer.charAt(totalCharactersCount - 1) == '\t' && deleted <
   * tabSize) { reduceCharacterCount(); deleted++; } }
   */
  
  private static void deleteTabInBuffer()
  {
    int deleted = 1;
    while (outputBuffer.length() > 0 && outputBuffer.charAt(outputBuffer.length() - 1) == '\t' && deleted < tabSize)
    {
      reduceCharacterCount();
      outputBuffer.deleteCharAt(outputBuffer.length() - 1);
      deleteLaterCharactersCount++;
      deleted++;
    }
  }
  
  private static void deleteTabInScreenBuffer()
  {
    int deleted = 1;
    while (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount - 1) == '\t' && deleted < tabSize)
    {
      reduceCharacterCount();
      deleted++;
    }
  }
  
  private static void deleteTabInScreenBufferLater()
  {
    int deleted = 1;
    while (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount - 1) == '\t' && deleted < tabSize)
    {
      reduceCharacterCount();
      screenBuffer.replace(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + 1, "\b");
      deleteLaterCharactersCount++;
      deleted++;
    }
  }
  
  private static void advanceTabInScreenBuffer()
  {
    int advanced = 1;
    while (advanced < tabSize && screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount - 1) == '\t' && (((totalCharactersCount - overwriteCharactersCount) % totalCharactersPerLine) & (tabSize - 1)) != 0)
    {
      overwriteCharactersCount--;
      // overwriteBufferedCharactersCount++;
      // overwriteBufferedCharactersCount =
      // Math.min(overwriteCharactersCount,
      // outputBuffer.length());
      advanced++;
    }
  }
  
  private static void returnTabInScreenBuffer()
  {
    int returned = 1;
    while (returned < tabSize && screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount - 1) == '\t' && (((totalCharactersCount - overwriteCharactersCount) % totalCharactersPerLine) & (tabSize - 1)) != 0)
    {
      overwriteCharactersCount++;
      // overwriteBufferedCharactersCount++;
      // overwriteBufferedCharactersCount =
      // Math.min(overwriteCharactersCount,
      // outputBuffer.length());
      returned++;
    }
  }
  
  /*
   * private static int deleteTabInBufferBackspacing() { int deleted = 1; while
   * (outputBuffer.length() > 0 && outputBuffer.charAt(outputBuffer.length()) ==
   * '\t' && deleted < tabSize) { reduceCharacterCount(); deleted++;
   * outputBuffer.insert(0, '\b'); } return deleted - 1; }
   */
  
  private static int deleteTabInScreenBufferBackspacing()
  {
    int deleted = 1;
    while (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount - 1) == '\t' && deleted < tabSize)
    {
      reduceCharacterCount();
      deleted++;
      spacesBuffer.insert(0, '\b');
    }
    return deleted - 1;
  }
  
  private static void completeTabInBuffer()
  {
    while ((charactersInLineCount & (tabSize - 1)) != 0)
    {
      if (isFlushInterrupted())
      {
        if (deleteLaterCharactersCount > 0)
        {
          deleteLaterCharactersCount--;
        }
      }
      if (returnCharactersCount > 0 && returnCharactersCount <= outputBuffer.length())
      {
        increaseCharacterCount();
        outputBuffer.setCharAt(outputBuffer.length() - returnCharactersCount--, '\t');
      }
      else
      {
        if (outputBuffer.length() == maxCharactersCount)
        {
          deleteLineInBuffer();
        }
        increaseCharacterCount();
        outputBuffer.append('\t');
      }
    }
  }
  
  private static void completeLineInBuffer()
  {
    boolean marked = false;
    while (charactersInLineCount < maxCharactersPerLine)
    {
      if (outputBuffer.length() == maxCharactersCount)
      {
        deleteLineInBuffer();
      }
      totalCharactersCount++;
      charactersInLineCount++;
      if (marked)
      {
        outputBuffer.append('\b');
      }
      else
      {
        outputBuffer.append('\r');
        marked = true;
      }
    }
    if (outputBuffer.length() == maxCharactersCount)
    {
      deleteLineInBuffer();
    }
    outputBuffer.append('\n');
  }
  
  public static void backspace(int count, boolean flush)
  {
    // flushBufferedAsynchronous();
    try
    {
      synchronized (outputSynchronizer)
      {
        while (updatingTerminal)
        {
          try
          {
            outputSynchronizer.wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
        updatingTerminal = true;
        if (count > totalCharactersCount - overwriteCharactersCount)
        {
          count = totalCharactersCount - overwriteCharactersCount;
        }
        if (totalCharactersCount - overwriteCharactersCount > 0)
        {
          spacesBuffer.setLength(0);
          // int currentLines = lineCount;
          int deleted = 0;
          for (int i = 0; i < count; i++)
          {
            /*
             * if (flushInterrupted) { if (outputBuffer.length() > 0) { if
             * (outputBuffer.charAt(outputBuffer.length() - 1 -
             * overwriteCharactersCount) == '\t') {
             * outputBuffer.deleteCharAt(outputBuffer.length() - 1 -
             * overwriteCharactersCount); deleteTabInBuffer(); } else {
             * outputBuffer.deleteCharAt(outputBuffer.length() - 1 -
             * overwriteCharactersCount); } } else { if
             * (screenBuffer.charAt(totalCharactersCount -
             * overwriteCharactersCount) == '\t') {
             * deleteTabInScreenBufferLater(); } else {
             * screenBuffer.replace(totalCharactersCount -
             * overwriteCharactersCount, totalCharactersCount -
             * overwriteCharactersCount + 1, "\b");
             * deleteLaterCharactersCount++; } } } else {
             */
            if (lineCount > 0 && charactersInLineCount == 0)
            {
              spacesBuffer.insert(0, '\n');
              deleted++;
            }
            if (lineCount == 0 && charactersInLineCount == 0)
            {
              break;
            }
            reduceCharacterCount();
            if (screenBuffer.charAt(totalCharactersCount - overwriteCharactersCount) == '\t')
            {
              spacesBuffer.insert(0, '\b');
              deleted++;
              deleted += deleteTabInScreenBufferBackspacing();
            }
            else
            {
              spacesBuffer.insert(0, '\b');
              deleted++;
            }
            // }
          }
          if (deleted > 0)
          {
            String spacesBufferContents = spacesBuffer.toString();
            if (flush && (!isFlushInterrupted()))
            {
              textArea.replaceRange(spacesBufferContents.replace('\b', ' '), totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + deleted);
            }
            screenBuffer.replace(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + deleted, spacesBufferContents);
          }
          spacesBuffer.setLength(0);
          // textArea.setCaretPosition(totalCharactersCount);
        }
      }
    }
    finally
    {
      synchronized (outputSynchronizer)
      {
        updatingTerminal = false;
        outputSynchronizer.notify();
      }
    }
  }
  
  public static char read() throws InterruptedException
  {
    synchronized (inputSynchronizer)
    {
      readingInput = true;
      char key;
      while (inputBuffer.length() == 0)
      {
        /*
         * try { inputSynchronizer.wait(); } catch (InterruptedException e) {
         * //VTTerminal.println(e.getMessage()); }
         */
        inputSynchronizer.wait();
      }
      key = inputBuffer.charAt(0);
      inputBuffer.deleteCharAt(0);
      readingInput = false;
      return key;
    }
  }
  
  public static void write(char c)
  {
    output(c);
  }
  
  private static void update(boolean caret, int start, int end)
  {
    // System.out.println("update:" + caret);
    // System.out.println("updateCaretPosition:" + updateCaretPosition);
    if (end > 0)
    {
      String data = screenBuffer.substring(start, end).replace('\r', ' ').replace('\t', ' ').replace('\b', ' ').replace('\f', ' ');
      textArea.replaceRange(data, start, end);
      updateCaretPosition();
    }
    else if (caret)
    {
      updateCaretPosition();
    }
  }
  
  public static void flushBuffered(boolean caret)
  {
    changedTerminal = false;
    int start = 0;
    int end = 0;
    try
    {
      synchronized (outputSynchronizer)
      {
        while (updatingTerminal)
        {
          try
          {
            outputSynchronizer.wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
        updatingTerminal = true;
        if (!isFlushInterrupted())
        {
          if (deleteLaterCharactersCount > 0)
          {
            changedTerminal = true;
            flushBuffer.setLength(0);
            for (int i = 0; i < deleteLaterCharactersCount; i++)
            {
              flushBuffer.append(" ");
            }
            String flushBufferContents = flushBuffer.toString();
            start = totalCharactersCount - overwriteCharactersCount;
            end = totalCharactersCount - overwriteCharactersCount + deleteLaterCharactersCount;
            screenBuffer.replace(start, end, flushBufferContents);
            // textArea.replaceRange(flushBufferContents.replace('\r', '
            // ').replace('\t', '
            // ').replace('\b', ' ').replace('\f', ' '), totalCharactersCount -
            // overwriteCharactersCount, totalCharactersCount -
            // overwriteCharactersCount +
            // deleteLaterCharactersCount);
            flushBuffer.setLength(0);
            
            if (logOutput != null)
            {
              writeLogOutput(flushBufferContents);
            }
          }
          deleteLaterCharactersCount = 0;
          if (deletedLastLinesCount > 0)
          {
            changedTerminal = true;
            if (deletedLastLinesCount < maxLines)
            {
              flushBuffer.setLength(0);
              flushBuffer.append(screenBuffer.substring(totalCharactersPerLine * deletedLastLinesCount));
              for (int i = 0; i < deletedLastLinesCount; i++)
              {
                flushBuffer.append(trueBlankLineEnd);
              }
              if (outputBuffer.length() > maxCharactersCount)
              {
                outputBuffer.setLength(maxCharactersCount);
              }
              flushBuffer.replace(Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0), totalCharactersCount - overwriteCharactersCount, outputBuffer.toString());
              if (flushBuffer.length() > maxCharactersCount)
              {
                flushBuffer.setLength(maxCharactersCount);
              }
              String flushBufferContents = flushBuffer.toString();
              start = Math.max(maxCharactersCount - flushBuffer.length(), 0);
              end = maxCharactersCount;
              screenBuffer.replace(start, end, flushBufferContents);
              // textArea.replaceRange(flushBufferContents.replace('\r', '
              // ').replace('\t', '
              // ').replace('\b', ' ').replace('\f', ' '),
              // Math.max(maxCharactersCount -
              // flushBuffer.length(), 0), maxCharactersCount);
              flushBuffer.setLength(0);
              outputBuffer.setLength(0);
              
              if (logOutput != null)
              {
                writeLogOutput(flushBufferContents);
              }
            }
            else
            {
              flushBuffer.setLength(0);
              flushBuffer.append(trueBlankArea);
              if (outputBuffer.length() > maxCharactersCount)
              {
                outputBuffer.setLength(maxCharactersCount);
              }
              flushBuffer.replace(Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0), totalCharactersCount - overwriteCharactersCount, outputBuffer.toString());
              if (flushBuffer.length() > maxCharactersCount)
              {
                flushBuffer.setLength(maxCharactersCount);
              }
              String flushBufferContents = flushBuffer.toString();
              start = Math.max(maxCharactersCount - flushBuffer.length(), 0);
              end = maxCharactersCount;
              screenBuffer.replace(start, end, flushBufferContents);
              // textArea.replaceRange(flushBufferContents.replace('\r', '
              // ').replace('\t', '
              // ').replace('\b', ' ').replace('\f', ' '),
              // Math.max(maxCharactersCount -
              // flushBuffer.length(), 0), maxCharactersCount);
              flushBuffer.setLength(0);
              outputBuffer.setLength(0);
              
              if (logOutput != null)
              {
                writeLogOutput(flushBufferContents);
              }
            }
            deletedLastLinesCount = 0;
          }
          else
          {
            if (outputBuffer.length() > 0)
            {
              changedTerminal = true;
              if (outputBuffer.length() > maxCharactersCount)
              {
                outputBuffer.setLength(maxCharactersCount);
              }
              String outputBufferContents = outputBuffer.toString();
              start = Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0);
              end = totalCharactersCount - overwriteCharactersCount;
              screenBuffer.replace(start, end, outputBufferContents);
              // textArea.replaceRange(outputBufferContents.replace('\r', '
              // ').replace('\t', '
              // ').replace('\b', ' ').replace('\f', ' '),
              // Math.max(totalCharactersCount -
              // overwriteCharactersCount - outputBuffer.length(), 0),
              // totalCharactersCount -
              // overwriteCharactersCount);
              outputBuffer.setLength(0);
              
              if (logOutput != null)
              {
                writeLogOutput(outputBufferContents);
              }
            }
          }
        }
        if (changedTerminal)
        {
          updateTask.flush(caret, start, end);
        }
        // if (changedTerminal && updateCaretPosition)
        // {
        // updateCaretPosition();
        // }
      }
    }
    finally
    {
      synchronized (outputSynchronizer)
      {
        updatingTerminal = false;
        outputSynchronizer.notify();
      }
    }
  }
  
//	public static void flushBuffered(boolean updateCaretPosition)
//	{
//		updateTask.flush(updateCaretPosition);
//	}
  
//	public static void flushBuffered(boolean updateCaretPosition)
//	{
//		changedTerminal = false;
//		try
//		{
//			synchronized (outputSynchronizer)
//			{
//				while (updatingTerminal)
//				{
//					try
//					{
//						outputSynchronizer.wait();
//					}
//					catch (InterruptedException e)
//					{
//						
//					}
//				}
//				updatingTerminal = true;
//				if (!isFlushInterrupted())
//				{
//					if (deleteLaterCharactersCount > 0)
//					{
//						changedTerminal = true;
//						flushBuffer.setLength(0);
//						for (int i = 0; i < deleteLaterCharactersCount; i++)
//						{
//							flushBuffer.append(" ");
//						}
//						String flushBufferContents = flushBuffer.toString();
//						screenBuffer.replace(totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + deleteLaterCharactersCount, flushBufferContents);
//						textArea.replaceRange(flushBufferContents.replace('\r', ' ').replace('\t', ' ').replace('\b', ' ').replace('\f', ' '), totalCharactersCount - overwriteCharactersCount, totalCharactersCount - overwriteCharactersCount + deleteLaterCharactersCount);
//					}
//					deleteLaterCharactersCount = 0;
//					if (deletedLastLinesCount > 0)
//					{
//						changedTerminal = true;
//						if (deletedLastLinesCount < maxLines)
//						{
//							flushBuffer.setLength(0);
//							flushBuffer.append(screenBuffer.substring(totalCharactersPerLine * deletedLastLinesCount));
//							for (int i = 0; i < deletedLastLinesCount; i++)
//							{
//								flushBuffer.append(trueBlankLineEnd);
//							}
//							if (outputBuffer.length() > maxCharactersCount)
//							{
//								outputBuffer.setLength(maxCharactersCount);
//							}
//							flushBuffer.replace(Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0), totalCharactersCount - overwriteCharactersCount, outputBuffer.toString());
//							if (flushBuffer.length() > maxCharactersCount)
//							{
//								flushBuffer.setLength(maxCharactersCount);
//							}
//							String flushBufferContents = flushBuffer.toString();
//							textArea.replaceRange(flushBufferContents.replace('\r', ' ').replace('\t', ' ').replace('\b', ' ').replace('\f', ' '), Math.max(maxCharactersCount - flushBuffer.length(), 0), maxCharactersCount);
//							screenBuffer.replace(Math.max(maxCharactersCount - flushBuffer.length(), 0), maxCharactersCount, flushBufferContents);
//							flushBuffer.setLength(0);
//							outputBuffer.setLength(0);
//						}
//						else
//						{
//							flushBuffer.setLength(0);
//							flushBuffer.append(trueBlankArea);
//							if (outputBuffer.length() > maxCharactersCount)
//							{
//								outputBuffer.setLength(maxCharactersCount);
//							}
//							flushBuffer.replace(Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0), totalCharactersCount - overwriteCharactersCount, outputBuffer.toString());
//							if (flushBuffer.length() > maxCharactersCount)
//							{
//								flushBuffer.setLength(maxCharactersCount);
//							}
//							String flushBufferContents = flushBuffer.toString();
//							textArea.replaceRange(flushBufferContents.replace('\r', ' ').replace('\t', ' ').replace('\b', ' ').replace('\f', ' '), Math.max(maxCharactersCount - flushBuffer.length(), 0), maxCharactersCount);
//							screenBuffer.replace(Math.max(maxCharactersCount - flushBuffer.length(), 0), maxCharactersCount, flushBufferContents);
//							flushBuffer.setLength(0);
//							outputBuffer.setLength(0);
//						}
//						deletedLastLinesCount = 0;
//					}
//					else
//					{
//						if (outputBuffer.length() > 0)
//						{
//							changedTerminal = true;
//							if (outputBuffer.length() > maxCharactersCount)
//							{
//								outputBuffer.setLength(maxCharactersCount);
//							}
//							String outputBufferContents = outputBuffer.toString();
//							textArea.replaceRange(outputBufferContents.replace('\r', ' ').replace('\t', ' ').replace('\b', ' ').replace('\f', ' '), Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0), totalCharactersCount - overwriteCharactersCount);
//							screenBuffer.replace(Math.max(totalCharactersCount - overwriteCharactersCount - outputBuffer.length(), 0), totalCharactersCount - overwriteCharactersCount, outputBufferContents);
//							outputBuffer.setLength(0);
//						}
//					}
//				}
//				if (changedTerminal && updateCaretPosition)
//				{
//					updateCaretPosition();
//				}
//			}
//		}
//		finally
//		{
//			synchronized (outputSynchronizer)
//			{
//				updatingTerminal = false;
//				outputSynchronizer.notify();
//			}
//		}
//	}
  
  /*
   * public static void test() { char key; while (true) { key = read(); if (key
   * == '*') { break; } write(key); flushBuffered(); } frame.dispose(); }
   */
  
  public static void close()
  {
    updateTask.running = false;
    if (frame != null)
    {
      frame.setVisible(false);
      frame.dispose();
    }
  }
  
  public static void staticClear()
  {
    try
    {
      synchronized (outputSynchronizer)
      {
        while (updatingTerminal)
        {
          try
          {
            outputSynchronizer.wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
        updatingTerminal = true;
        // flushBufferedAsync();
        // modifyingTextArea = false;
        outputBuffer.setLength(0);
        textArea.replaceRange(replacedBlankArea, 0, maxCharactersCount);
        screenBuffer.replace(0, maxCharactersCount, trueBlankArea);
        // textArea.setCaretPosition(0);
        totalCharactersCount = 0;
        lineCount = 0;
        charactersInLineCount = 0;
        caretRecoilCount = 0;
        overwriteCharactersCount = 0;
        returnCharactersCount = 0;
        deletedLastLinesCount = 0;
        deleteLaterCharactersCount = 0;
        updateCaretPosition();
        /* updatingTerminal = false; outputSynchronizer.notify(); */
      }
    }
    finally
    {
      synchronized (outputSynchronizer)
      {
        updatingTerminal = false;
        outputSynchronizer.notify();
      }
    }
  }
  
  public static String getLastText(int selectionSize)
  {
    try
    {
      synchronized (outputSynchronizer)
      {
        while (updatingTerminal)
        {
          try
          {
            outputSynchronizer.wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
        updatingTerminal = true;
        int selectionStart = totalCharactersCount - selectionSize;
        int selectionEnd = totalCharactersCount;
        if (selectionStart < 0)
        {
          return null;
        }
        if (selectionStart == selectionEnd)
        {
          return null;
        }
        return screenBuffer.substring(selectionStart, selectionEnd).replace("\b", "").replace("\n", "").replaceAll("\\t{1," + tabSize + "}", "\t").replace('\r', '\n');
        // return screenBuffer.substring(selectionStart,
        // selectionEnd).replace("\b",
        // "").replace("\n", "").replace('\r', '\n');
      }
    }
    catch (Throwable e)
    {
      return null;
    }
    finally
    {
      synchronized (outputSynchronizer)
      {
        updatingTerminal = false;
        outputSynchronizer.notify();
      }
    }
  }
  
  public String getSelectedText()
  {
    try
    {
      synchronized (outputSynchronizer)
      {
        while (updatingTerminal)
        {
          try
          {
            outputSynchronizer.wait();
          }
          catch (InterruptedException e)
          {
            
          }
        }
        updatingTerminal = true;
        int selectionStart = textArea.getSelectionStart();
        int selectionEnd = textArea.getSelectionEnd();
        if (/*
             * textArea.getCaretPosition() == totalCharactersCount -
             * overwriteCharactersCount //&& textArea.getTrueCaretPosition() ==
             * totalCharactersCount - overwriteCharactersCount && selectionStart
             * == totalCharactersCount - overwriteCharactersCount &&
             * selectionEnd == totalCharactersCount - overwriteCharactersCount +
             * 1 ||
             */ selectionStart == selectionEnd)
        {
          return null;
        }
        return screenBuffer.substring(selectionStart, selectionEnd).replace("\b", "").replace("\n", "").replaceAll("\\t{1," + tabSize + "}", "\t").replace('\r', '\n');
        // return screenBuffer.substring(selectionStart,
        // selectionEnd).replace("\b",
        // "").replace("\n", "").replace('\r', '\n');
      }
    }
    catch (Throwable e)
    {
      return null;
    }
    finally
    {
      synchronized (outputSynchronizer)
      {
        updatingTerminal = false;
        outputSynchronizer.notify();
      }
    }
  }
  
  public static VTGraphicalConsoleFrame getStaticFrame()
  {
    return frame;
  }
  
  public void toggleFlushMode()
  {
    if (keyListener != null)
    {
      keyListener.toggleFlushMode();
    }
  }
  
  public void copyText()
  {
    if (textArea != null)
    {
      textArea.copyText();
    }
    
  }
  
  public void pasteText()
  {
    if (textArea != null)
    {
      textArea.pasteText();
    }
  }
  
  /*
   * public static void test() { deletedLastLinesCount = 299;
   * VTTerminal.flush(); }
   */
  
  /*
   * public static void main(String[] args) throws InterruptedException {
   * VTTerminal.setGraphical(true); VTGraphicalConsole.split = true;
   * VTTerminal.initialize(); VTTerminal.print("abc"); }
   */
  
  public String readLine(boolean echo) throws InterruptedException
  {
    String data = VTGraphicalConsoleReader.readLine(echo);
    writeLogReadLine(data);
    return data;
  }
  
  public void print(String str)
  {
    VTGraphicalConsoleWriter.write(str);
    VTGraphicalConsoleWriter.flush();
  }
  
  public void println(String str)
  {
    VTGraphicalConsoleWriter.write(str + "\n");
    VTGraphicalConsoleWriter.flush();
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
    VTGraphicalConsoleWriter.write(str);
  }
  
  public void write(char[] buf, int off, int len)
  {
    VTGraphicalConsoleWriter.write(buf, off, len);
  }
  
  public void flush()
  {
    VTGraphicalConsoleWriter.flush();
  }
  
  public void clear()
  {
    VTGraphicalConsole.staticClear();
  }
  
  public void bell()
  {
    frame.getToolkit().beep();
    // System.out.print("\u0007");
  }
  
  public void setTitle(String title)
  {
    frame.setTitle(title);
  }
  
  public static void staticBell()
  {
    Toolkit.getDefaultToolkit().beep();
    // System.out.print("\u0007");
  }
  
  public void setColors(int foregroundColor, int backgroundColor)
  {
    switch (foregroundColor)
    {
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
      {
        textArea.setForeground(Color.BLACK);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
      {
        textArea.setForeground(Color.RED.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
      {
        textArea.setForeground(Color.GREEN.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
      {
        textArea.setForeground(Color.YELLOW.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
      {
        textArea.setForeground(Color.BLUE.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
      {
        textArea.setForeground(Color.MAGENTA.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
      {
        textArea.setForeground(Color.CYAN.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
      {
        textArea.setForeground(Color.WHITE.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
      {
        textArea.setForeground(Color.RED);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
      {
        textArea.setForeground(Color.GREEN);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
      {
        textArea.setForeground(Color.YELLOW);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
      {
        textArea.setForeground(Color.BLUE);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
      {
        textArea.setForeground(Color.MAGENTA);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
      {
        textArea.setForeground(Color.CYAN);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
      {
        textArea.setForeground(Color.WHITE);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
      {
        textArea.setForeground(Color.GREEN);
        break;
      }
    }
    switch (backgroundColor)
    {
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
      {
        textArea.setBackground(Color.BLACK);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
      {
        textArea.setBackground(Color.RED.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
      {
        textArea.setBackground(Color.GREEN.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
      {
        textArea.setBackground(Color.YELLOW.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
      {
        textArea.setBackground(Color.BLUE.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
      {
        textArea.setBackground(Color.MAGENTA.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
      {
        textArea.setBackground(Color.CYAN.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
      {
        textArea.setBackground(Color.WHITE.darker());
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
      {
        textArea.setBackground(Color.RED);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
      {
        textArea.setBackground(Color.GREEN);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
      {
        textArea.setBackground(Color.YELLOW);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
      {
        textArea.setBackground(Color.BLUE);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
      {
        textArea.setBackground(Color.MAGENTA);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
      {
        textArea.setBackground(Color.CYAN);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
      {
        textArea.setBackground(Color.WHITE);
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
      {
        textArea.setBackground(Color.BLACK);
        break;
      }
    }
    // VTGraphicalConsole.foregroundColor = foregroundColor;
    // VTGraphicalConsole.backgroundColor = backgroundColor;
  }
  
  public void setSystemIn()
  {
    System.setIn(inputStream);
  }
  
  public void setSystemOut()
  {
    // System.setOut(printStream);
    doubledOutput = new PrintStream(new VTDoubledOutputStream(printStream, new PrintStream(new FileOutputStream(FileDescriptor.out)), false), true);
    System.setOut(doubledOutput);
  }
  
  public void setSystemErr()
  {
    // System.setErr(printStream);
    doubledError = new PrintStream(new VTDoubledOutputStream(printStream, new PrintStream(new FileOutputStream(FileDescriptor.err)), false), true);
    System.setErr(doubledError);
  }
  
  public InputStream getSystemIn()
  {
    return inputStream;
  }
  
  public PrintStream getSystemOut()
  {
    if (doubledOutput != null)
    {
      return doubledOutput;
    }
    return printStream;
  }
  
  public PrintStream getSystemErr()
  {
    if (doubledError != null)
    {
      return doubledError;
    }
    return printStream;
  }
  
  public void interruptReadLine()
  {
    VTGraphicalConsoleReader.interruptRead();
  }
  
  public void setBold(boolean bold)
  {
    if (bold)
    {
      textArea.setFont(textArea.getFont().deriveFont(Font.BOLD, textArea.getFont().getSize2D()));
      frame.pack();
      // textArea.setSize(textArea.getSize().width + 1,
      // textArea.getSize().height +
      // 1);
    }
    else
    {
      textArea.setFont(textArea.getFont().deriveFont(Font.PLAIN, textArea.getFont().getSize2D()));
      frame.pack();
      // textArea.setSize(textArea.getSize().width + 1,
      // textArea.getSize().height +
      // 1);
    }
  }
  
  public void resetAttributes()
  {
    setColors(VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN, VTConsole.VT_CONSOLE_COLOR_DARK_BLACK);
    setBold(true);
  }
  
  public static VTGraphicalConsoleTextArea getTextArea()
  {
    return textArea;
  }
  
  public void setRemoteIcon(boolean remoteIcon)
  {
    VTGraphicalConsole.remoteIcon = remoteIcon;
  }
  
  public void refreshText()
  {
    
  }
  
  public boolean isCommandEcho()
  {
    return false;
  }
  
  public void setCommandEcho(boolean commandEcho)
  {
    
  }
  
  public void copyAllText()
  {
    textArea.copyAllText();
  }
  
  public void selectAllText()
  {
    textArea.select(0, textArea.getText().length());
  }
  
  public String getAllText()
  {
    return screenBuffer.substring(0).replace("\b", "").replace("\n", "").replaceAll("\\t{1," + tabSize + "}", "\t").replace('\r', '\n');
  }
  
  public void addToggleFlushModePauseNotify(VTConsoleBooleanToggleNotify notifyFlushInterrupted)
  {
    VTGraphicalConsole.notifyFlushInterrupted = notifyFlushInterrupted;
  }
  
  public void addToggleInputModeReplaceNotify(VTConsoleBooleanToggleNotify notifyReplaceInput)
  {
    VTGraphicalConsole.notifyReplaceInput = notifyReplaceInput;
  }
  
  public void requestFocus()
  {
    try
    {
      textArea.requestFocusInWindow();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void closeConsole()
  {
    try
    {
      close();
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public boolean setLogOutput(String path)
  {
    if (path != null)
    {
      try
      {
        logOutput = new PrintStream(new FileOutputStream(path, true), true, "UTF-8");
        return true;
      }
      catch (Throwable t)
      {
        closeLogOutput();
      }
    }
    else
    {
      closeLogOutput();
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
      return true;
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
  
  private static void writeLogOutput(String data)
  {
    if (data == null || data.length() < 0)
    {
      return;
    }
    if (logOutput != null)
    {
      try
      {
        logOutput.print(data);
        logOutput.flush();
      }
      catch (Throwable t)
      {
        closeLogOutput();
      }
    }
  }
  
  private static void closeLogOutput()
  {
    if (logOutput != null)
    {
      try
      {
        logOutput.close();
      }
      catch (Throwable e)
      {
        
      }
      logOutput = null;
    }
  }
  
  public static PrintStream getDoubledOutput()
  {
    return doubledOutput;
  }
  
  public static PrintStream getDoubledError()
  {
    return doubledError;
  }
  
  public String getLastOutputLine()
  {
    return "";
  }
  
  public boolean isFlushModePause()
  {
    return isFlushInterrupted();
  }
  
  public boolean isInputModeReplace()
  {
    return isReplaceActivated();
  }
  
  public VTGraphicalConsoleFrame getFrame()
  {
    return frame;
  }
  
  public Panel getPanel()
  {
    return null;
  }
  
  // public boolean isReadingLine()
  // {
  // return VTGraphicalConsoleReader.isReadingLine();
  // }
  
  // public static void setIconifyToTray(boolean iconifyToTray)
  // {
  // VTGraphicalConsole.iconifyToTray = iconifyToTray;
  // }
  
  // public static boolean getIconifyToTray()
  // {
  // return VTGraphicalConsole.iconifyToTray;
  // }
}