package org.vash.vate.console.lanterna.separated;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Panel;
import java.awt.Scrollbar;
import java.awt.SystemColor;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DropTarget;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;
import org.vash.vate.VTSystem;
import org.vash.vate.console.VTConsoleBooleanToggleNotify;
import org.vash.vate.console.VTConsole;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleDropTargetListener;
import org.vash.vate.console.graphical.menu.VTGraphicalConsolePopupMenu;
import org.vash.vate.console.lanterna.separated.VTLanternaTextBoxModified.DefaultTextBoxRenderer;
import org.vash.vate.console.lanterna.separated.VTLanternaTextBoxModified.Style;
import org.vash.vate.graphics.font.VTFontManager;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.stream.filter.VTDoubledOutputStream;

import com.googlecode.lanternavt.TerminalPosition;
import com.googlecode.lanternavt.TerminalSize;
import com.googlecode.lanternavt.TextColor;
import com.googlecode.lanternavt.TextColor.ANSI;
import com.googlecode.lanternavt.graphics.PropertyTheme;
import com.googlecode.lanternavt.gui2.BasicWindow;
import com.googlecode.lanternavt.gui2.DefaultWindowManager;
import com.googlecode.lanternavt.gui2.InputFilter;
import com.googlecode.lanternavt.gui2.Interactable;
import com.googlecode.lanternavt.gui2.MultiWindowTextGUI;
import com.googlecode.lanternavt.gui2.Window.Hint;
import com.googlecode.lanternavt.input.KeyStroke;
import com.googlecode.lanternavt.input.KeyType;
import com.googlecode.lanternavt.input.MouseAction;
import com.googlecode.lanternavt.input.MouseActionType;
import com.googlecode.lanternavt.screen.Screen;
import com.googlecode.lanternavt.screen.TabBehaviour;
import com.googlecode.lanternavt.screen.TerminalScreen;
import com.googlecode.lanternavt.screen.Screen.RefreshType;
import com.googlecode.lanternavt.terminal.DefaultTerminalFactory;
import com.googlecode.lanternavt.terminal.MouseCaptureMode;
import com.googlecode.lanternavt.terminal.Terminal;
import com.googlecode.lanternavt.terminal.swing.AWTTerminal;
import com.googlecode.lanternavt.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanternavt.terminal.swing.AWTTerminalFrame;
import com.googlecode.lanternavt.terminal.swing.AWTTerminalPanel;
import com.googlecode.lanternavt.terminal.swing.TerminalEmulatorColorConfiguration;
import com.googlecode.lanternavt.terminal.swing.TerminalEmulatorPalette;

public class VTLanternaConsole extends VTConsole
{
  public static final TerminalEmulatorPalette CUSTOM_PALETTE = new TerminalEmulatorPalette(
  new Color(188, 188, 188), //defaultColor
  new Color(255, 255, 255), //defaultBrightColor
  new Color(0, 0, 0), //defaultBackgroundColor
  new Color(0, 0, 0), //normalBlack
  new Color(94, 94, 94), //brightBlack
  new Color(188, 0, 0), //normalRed
  new Color(255, 47, 47), //brightRed
  new Color(0, 188, 0), //normalGreen
  new Color(47, 255, 47), //brightGreen
  new Color(188, 188, 0), //normalYellow
  new Color(255, 255, 47), //brightYellow
  new Color(0, 0, 188), //normalBlue
  new Color(47, 47, 255), //brightBlue
  new Color(188, 0, 188), //normalMagenta
  new Color(255, 47, 255), //brightMagenta
  new Color(0, 188, 188), //normalCyan
  new Color(47, 255, 255), //brightCyan
  new Color(188, 188, 188), //normalWhite
  new Color(255, 255, 255)); //brightWhite
  
  private Frame frame;
  private Container container;
  private AWTTerminalPanel panel;
  private Panel spacer1;
  private Terminal terminal;
  private Screen screen;
  private AWTTerminal awtTerminal;
  private VTLanternaOutputTextBox outputBox;
  private VTLanternaOutputTextBox inputBox;
  private static final int consoleOutputColumns = 80;
  private static final int consoleOutputLines = 24;
  private static final int consoleInputColumns = 80;
  private static final int consoleInputLines = 1;
  private static final int consoleOutputLinesMaxSize = consoleOutputLines * 40;
  private static final int commandHistoryMaxSize = 40;
  private final List<String> commandHistory = new LinkedList<String>();
  private int commandHistoryPosition;
  private final List<String> inputLineBuffer = new LinkedList<String>();
  private final StringBuilder currentLineBuffer = new StringBuilder("");
  private Thread currentThread;
  private boolean echoInput = true;
  private boolean readingInput = false;
  private boolean flushInterrupted = false;
  private boolean frameDeactivated = false;
  private boolean replaceActivated = false;
  private boolean commandEcho = true;
  private Properties defaultInputProperties;
  private Properties defaultOutputProperties;
  private final StringBuilder inputBuffer = new StringBuilder();
  private final StringBuilder outputBuffer = new StringBuilder();
  private final Object inputSynchronizer = new Object();
  private final Object outputSynchronizer = new Object();
  // private final Object updateSynchronizer = new Object();
  private MultiWindowTextGUI gui;
  private BasicWindow window;
  private VTLanternaConsolePrintStream printStream;
  private VTLanternaConsoleInputStream inputStream;
  private boolean ignoreClose = false;
  private boolean build = false;
  private VTGraphicalConsolePopupMenu popupMenu;
  private boolean graphical;
  private Scrollbar horizontalScrollbar;
  private Scrollbar verticalScrollbar;
  private int pressedMouseButton;
  private int pressedMouseColumn;
  private int pressedMouseRow;
  private int menuMouseButton = -1;
  private boolean remoteIcon;
  private VTConsoleBooleanToggleNotify notifyFlushInterrupted;
  private VTConsoleBooleanToggleNotify notifyReplaceInput;
  private PrintStream logOutput = null;
  private BufferedWriter logReadLine = null;
  private Color lastLineBackgroundDefault = new Color(94, 94, 94);
  private PrintStream doubledOutput;
  private PrintStream doubledError;
  private final VTLanternaConsole console;
  
  // support command history
  // support echo input
  // support maximum line width in output
  // support special characters in input and output
  // support mouse scroll listener for awtframe
  // support flush interrupted output
  // support custom icon for awtframe
  // support window listener for awtframe
  // support command menubar for awtframe
  // replaced calls to VTGraphicalConsole static methods for VTConsole calls
  // support font options for awtframe
  // support context menu for awtframe
  // support command drag drop for awtframe
  // support keyboard shortcuts
  
  public VTLanternaConsole(boolean graphical, boolean remoteIcon, Container container)
  {
    // this.commandHistory.add("");
    // this.commandHistory.add(null);
    this.graphical = graphical;
    this.remoteIcon = remoteIcon;
    this.container = container;
    if (container instanceof Frame)
    {
      this.frame = (Frame) container;
    }
    this.console = this;
    Thread builderThread = new Thread()
    {
      public void run()
      {
        try
        {
          build();
        }
        catch (Throwable e)
        {
          //e.printStackTrace();
        }
      }
    };
    builderThread.setName(this.getClass().getSimpleName());
    builderThread.setDaemon(true);
    builderThread.start();
    
    synchronized (this)
    {
      while (!build)
      {
        try
        {
          wait();
        }
        catch (InterruptedException e)
        {
          
        }
      }
    }
  }
  
  private class VTLanternaConsoleWindowListener implements WindowListener
  {
    private VTLanternaConsole console;
    
    public VTLanternaConsoleWindowListener(VTLanternaConsole console)
    {
      this.console = console;
    }
    
    public void windowActivated(WindowEvent e)
    {
      console.activatedFrame();
      awtTerminal.requestFocusInWindow();
    }
    
    public void windowClosed(WindowEvent e)
    {
      
    }
    
    public void windowClosing(WindowEvent e)
    {
      if (console.getIgnoreClose())
      {
        console.getFrame().setExtendedState(Frame.ICONIFIED);
        return;
      }
      VTRuntimeExit.exit(0);
    }
    
    public void windowDeactivated(WindowEvent e)
    {
      console.deactivatedFrame();
    }
    
    public void windowDeiconified(WindowEvent e)
    {
      //console.activatedFrame();
      //awtterminal.requestFocusInWindow();
    }
    
    public void windowIconified(WindowEvent e)
    {
      //console.deactivatedFrame();
    }
    
    public void windowOpened(WindowEvent e)
    {
      awtTerminal.requestFocusInWindow();
    }
  }
  
  private class VTLanternaConsolePrintStream extends PrintStream
  {
    private PrintWriter outWriter;
    
    public VTLanternaConsolePrintStream(VTLanternaConsole console)
    {
      super(new OutputStream()
      {
        public void write(int b) throws IOException
        {
          
        }
      }, false);
      this.outWriter = new PrintWriter(new VTLanternaConsolePrintStreamWriter(console), false);
    }
    
    public PrintStream append(char c)
    {
      outWriter.append(c);
      return this;
    }
    
    public PrintStream append(CharSequence csq, int start, int end)
    {
      outWriter.append(csq, start, end);
      return this;
    }
    
    public PrintStream append(CharSequence csq)
    {
      outWriter.append(csq);
      return this;
    }
    
    public boolean checkError()
    {
      return super.checkError();
    }
    
    public void close()
    {
      outWriter.close();
    }
    
    public void flush()
    {
      outWriter.flush();
    }
    
    public PrintStream format(Locale l, String format, Object... args)
    {
      outWriter.format(l, format, args);
      return this;
    }
    
    public PrintStream format(String format, Object... args)
    {
      outWriter.format(format, args);
      return this;
    }
    
    public void print(boolean b)
    {
      outWriter.print(b);
      outWriter.flush();
    }
    
    public void print(char c)
    {
      outWriter.print(c);
      outWriter.flush();
    }
    
    public void print(char[] s)
    {
      outWriter.print(s);
      outWriter.flush();
    }
    
    public void print(double d)
    {
      outWriter.print(d);
      outWriter.flush();
    }
    
    public void print(float f)
    {
      outWriter.print(f);
      outWriter.flush();
    }
    
    public void print(int i)
    {
      outWriter.print(i);
      outWriter.flush();
    }
    
    public void print(long l)
    {
      outWriter.print(l);
      outWriter.flush();
    }
    
    public void print(Object obj)
    {
      outWriter.print(obj);
      outWriter.flush();
    }
    
    public void print(String s)
    {
      outWriter.print(s);
      outWriter.flush();
    }
    
    public PrintStream printf(Locale l, String format, Object... args)
    {
      outWriter.printf(l, format, args);
      outWriter.flush();
      return this;
    }
    
    public PrintStream printf(String format, Object... args)
    {
      outWriter.printf(format, args);
      outWriter.flush();
      return this;
    }
    
    public void println()
    {
      outWriter.println();
      outWriter.flush();
    }
    
    public void println(boolean x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(char x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(char[] x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(double x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(float x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(int x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(long x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(Object x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    public void println(String x)
    {
      outWriter.println(x);
      outWriter.flush();
    }
    
    protected void setError()
    {
      super.setError();
    }
    
    public void write(byte[] buf, int off, int len)
    {
      outWriter.write(new String(buf, off, len));
    }
    
    public void write(int b)
    {
      outWriter.write(b);
    }
    
    public void write(byte[] b) throws IOException
    {
      outWriter.write(new String(b, 0, b.length));
    }
  }
  
  private class VTLanternaConsolePrintStreamWriter extends Writer
  {
    private VTLanternaConsole console;
    
    public VTLanternaConsolePrintStreamWriter(VTLanternaConsole console)
    {
      this.console = console;
    }
    
    public void close() throws IOException
    {
      
    }
    
    public void flush() throws IOException
    {
      console.flush();
    }
    
    public void write(char[] cbuf, int off, int len) throws IOException
    {
      console.write(cbuf, off, len);
    }
    
    public Writer append(char c) throws IOException
    {
      super.append(c);
      return this;
    }
    
    public Writer append(CharSequence csq, int start, int end) throws IOException
    {
      super.append(csq, start, end);
      return this;
    }
    
    public Writer append(CharSequence csq) throws IOException
    {
      super.append(csq);
      return this;
    }
    
    public void write(char[] cbuf) throws IOException
    {
      console.write(cbuf, 0, cbuf.length);
    }
    
    public void write(int c) throws IOException
    {
      console.write(String.valueOf((char) c));
    }
    
    public void write(String str, int off, int len) throws IOException
    {
      console.write(str.toCharArray(), off, len);
    }
    
    public void write(String str) throws IOException
    {
      console.write(str);
    }
  }
  
  public class VTLanternaConsoleInputStream extends InputStream
  {
    private byte[] inputBuffer;
    private int readed;
    private VTLanternaConsole console;
    
    public VTLanternaConsoleInputStream(VTLanternaConsole console)
    {
      inputBuffer = new byte[0];
      this.console = console;
    }
    
    private int readInputBuffer() throws IOException
    {
      try
      {
        inputBuffer = (console.readLine(true) + "\n").getBytes("UTF-8");
        readed = 0;
        return inputBuffer.length;
      }
      catch (InterruptedException e)
      {
        
      }
      return -1;
    }
    
    public int read() throws IOException
    {
      if (inputBuffer == null || readed >= inputBuffer.length)
      {
        int lineSize = readInputBuffer();
        if (lineSize <= 0)
        {
          return lineSize;
        }
      }
      return inputBuffer[readed++];
    }
    
    public int read(byte[] b, int off, int len) throws IOException
    {
      int usable = 0;
      if (inputBuffer == null || readed >= inputBuffer.length)
      {
        int lineSize = readInputBuffer();
        if (lineSize <= 0)
        {
          return lineSize;
        }
      }
      usable = Math.min(inputBuffer.length - readed, len);
      System.arraycopy(inputBuffer, readed, b, off, usable);
      readed += usable;
      return usable;
    }
    
    public int read(byte[] b) throws IOException
    {
      return read(b, 0, b.length);
    }
    
    public int available() throws IOException
    {
      return inputBuffer.length - readed;
    }
    
    /* public synchronized void reset() throws IOException { super.reset(); } */
    
    public long skip(long n) throws IOException
    {
      return super.skip(n);
    }
  }
  
  public void build() throws Exception
  {
    AWTTerminalFontConfiguration.setBaseFontSize(VTFontManager.BASE_FONT_SIZE_MONOSPACED);
    AWTTerminalFontConfiguration.setFontScalingFactor(VTFontManager.FONT_SCALING_FACTOR_MONOSPACED);
    DefaultTerminalFactory factory = new DefaultTerminalFactory();
    factory.setLastLineBackground(new Color(94, 94, 94));
    factory.setForceAWTOverSwing(true);
    // factory.addTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
    factory.setInitialTerminalSize(new TerminalSize(consoleOutputColumns, consoleOutputLines + consoleInputLines));
    // factory.setTerminalEmulatorColorConfiguration(TerminalEmulatorColorConfiguration.newInstance(TerminalEmulatorPalette.STANDARD_VGA));
    factory.setTerminalEmulatorColorConfiguration(TerminalEmulatorColorConfiguration.newInstance(VTLanternaConsole.CUSTOM_PALETTE));
    // factory.setTerminalEmulatorDeviceConfiguration(deviceConfiguration)
    factory.setAutoOpenTerminalEmulatorWindow(false);
    if (graphical)
    {
      factory.setPreferTerminalEmulator(true);
    }
    else
    {
      factory.setForceTextTerminal(true);
      factory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
    }
    // final Scrollbar scrollBar = null;
    
    // factory.setForceTextTerminal(true);
    if (graphical && container != null)
    {
      terminal = factory.createAWTTerminalPanel(container, new Color(94, 94, 94));
    }
    else
    {
      terminal = factory.createTerminal();
    }
    
    outputBox = new VTLanternaOutputTextBox(new TerminalSize(consoleOutputColumns, consoleOutputLines), "", Style.MULTI_LINE, consoleOutputLinesMaxSize);
    inputBox = new VTLanternaOutputTextBox(new TerminalSize(consoleInputColumns, consoleInputLines), "", Style.SINGLE_LINE, consoleInputLines);
    
    outputBox.setTerminal(terminal);
    inputBox.setTerminal(terminal);
    
    // ScrollingAWTTerminal terminal = new ScrollingAWTTerminal();
    if (terminal instanceof AWTTerminalFrame || terminal instanceof AWTTerminalPanel)
    {
      if (terminal instanceof AWTTerminalFrame)
      {
        frame = (AWTTerminalFrame) terminal;
        panel = ((AWTTerminalFrame) terminal).getTerminalPanel();
      }
      else if (terminal instanceof AWTTerminalPanel)
      {
        panel = (AWTTerminalPanel) terminal;
        if (frame != null)
        {
          frame.add(panel);
          frame.pack();
        }
      }
      
      if (remoteIcon && frame != null)
      {
        try
        {
          frame.setIconImage(VTSystem.remoteIcon);
        }
        catch (Throwable t)
        {
          
        }
      }
      else if (frame != null)
      {
        try
        {
          frame.setIconImage(VTSystem.terminalIcon);
        }
        catch (Throwable t)
        {
          
        }
      }
      
      if (frame != null)
      {
        frame.addWindowListener(new VTLanternaConsoleWindowListener(this));
      }
      
      if (frame != null && frame instanceof AWTTerminalFrame)
      {
        awtTerminal = ((AWTTerminalFrame) frame).getTerminal();
      }
      else if (panel != null)
      {
        awtTerminal = panel.getTerminal();
      }
      
      awtTerminal.addMouseListener(new MouseListener()
      {
        public void mouseClicked(MouseEvent e)
        {
          
        }
        
        public void mousePressed(MouseEvent e)
        {
          if (e.isPopupTrigger())
          {
            e.consume();
            pressedMouseButton = e.getButton();
            menuMouseButton = e.getButton();
            //popupMenu.show(awtTerminal, e.getX(), e.getY());
            return;
          }
          e.consume();
          int x = e.getX();
          int y = e.getY();
          int fontWidth = awtTerminal.getTerminalImplementation().getFontWidth();
          int fontHeight = awtTerminal.getTerminalImplementation().getFontHeight();
          TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
          if (e.isShiftDown())
          {
            if (!inputBox.selectingText() && inputBox.getGlobalPosition().getRow() == pos.getRow())
            {
              MouseAction startAction = new MouseAction(MouseActionType.DRAG, e.getButton(), new TerminalPosition(inputBox.getCaretPosition().getColumn(), inputBox.getGlobalPosition().getRow()));
              awtTerminal.addInput(startAction);
            }
            else if (!outputBox.selectingText())
            {
              MouseAction startAction = new MouseAction(MouseActionType.DRAG, e.getButton(), outputBox.getCursorLocation());
              awtTerminal.addInput(startAction);
            }
            MouseAction endAction = new MouseAction(MouseActionType.DRAG, e.getButton(), pos);
            awtTerminal.addInput(endAction);
          }
          else
          {
            MouseAction mouseAction = new MouseAction(MouseActionType.CLICK_DOWN, e.getButton(), pos);
            awtTerminal.addInput(mouseAction);
          }
        }
        
        public void mouseReleased(MouseEvent e)
        {
          if (e.isPopupTrigger() || e.getButton() == menuMouseButton)
          {
            e.consume();
            pressedMouseButton = 0;
            menuMouseButton = e.getButton();
            popupMenu.show(awtTerminal, e.getX(), e.getY());
            return;
          }
          e.consume();
          int x = e.getX();
          int y = e.getY();
          int fontWidth = awtTerminal.getTerminalImplementation().getFontWidth();
          int fontHeight = awtTerminal.getTerminalImplementation().getFontHeight();
          TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
          MouseAction mouseAction = new MouseAction(MouseActionType.CLICK_RELEASE, e.getButton(), pos);
          awtTerminal.addInput(mouseAction);
        }
        
        public void mouseEntered(MouseEvent e)
        {
          
        }
        
        public void mouseExited(MouseEvent e)
        {
          
        }
      });
      
      awtTerminal.addMouseMotionListener(new MouseMotionListener()
      {
        public void mouseDragged(MouseEvent e)
        {
          e.consume();
          int x = e.getX();
          int y = e.getY();
          int fontWidth = awtTerminal.getTerminalImplementation().getFontWidth();
          int fontHeight = awtTerminal.getTerminalImplementation().getFontHeight();
          TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
          MouseAction mouseAction = new MouseAction(MouseActionType.DRAG, e.getButton(), pos);
          awtTerminal.addInput(mouseAction);
        }
        
        public void mouseMoved(MouseEvent e)
        {
          e.consume();
          int x = e.getX();
          int y = e.getY();
          int fontWidth = awtTerminal.getTerminalImplementation().getFontWidth();
          int fontHeight = awtTerminal.getTerminalImplementation().getFontHeight();
          TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
          if (e.isShiftDown())
          {
            MouseAction mouseAction = new MouseAction(MouseActionType.DRAG, e.getButton(), pos);
            awtTerminal.addInput(mouseAction);
          }
          else
          {
            MouseAction mouseAction = new MouseAction(MouseActionType.MOVE, e.getButton(), pos);
            awtTerminal.addInput(mouseAction);
          }
//          MouseAction mouseAction = new MouseAction(MouseActionType.MOVE, e.getButton(), pos);
//          awtTerminal.addInput(mouseAction);
        }
      });
      
      panel.addMouseWheelListener(new MouseWheelListener()
      {
        public void mouseWheelMoved(MouseWheelEvent e)
        {
          int x = e.getX();
          int y = e.getY();
          int fontWidth = awtTerminal.getTerminalImplementation().getFontWidth();
          int fontHeight = awtTerminal.getTerminalImplementation().getFontHeight();
          TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
          int rotation = e.getWheelRotation();
          MouseAction mouseAction = new MouseAction(rotation > 0 ? MouseActionType.SCROLL_DOWN : MouseActionType.SCROLL_UP, e.getButton(), pos);
          outputBox.handleInput(mouseAction);
        }
      });
      
      panel.addKeyListener(new KeyListener()
      {
        public void keyTyped(KeyEvent e)
        {
          awtTerminal.requestFocusInWindow();
          //awtTerminal.dispatchEvent(e);
        }
        
        public void keyReleased(KeyEvent e)
        {
          awtTerminal.requestFocusInWindow();
          //awtTerminal.dispatchEvent(e);
        }
        
        public void keyPressed(KeyEvent e)
        {
          awtTerminal.requestFocusInWindow();
          //awtTerminal.dispatchEvent(e);
        }
      });
      //
      if (frame != null)
      {
        VTFontManager.registerWindow(frame);
      }
      
      //VTGlobalTextStyleManager.registerMonospacedComponent(panel);
      //VTGlobalTextStyleManager.registerMonospacedComponent(awtTerminal);
      VTFontManager.registerFontList(awtTerminal.getTerminalFontConfiguration().getFontPriority());
      VTFontManager.registerConsole(this);
      popupMenu = new VTGraphicalConsolePopupMenu(this, panel);
    }
    else
    {
      awtTerminal = null;
    }
    //terminal.setBackgroundColor(TextColor.ANSI.BLACK);
    //terminal.setForegroundColor(TextColor.ANSI.GREEN_BRIGHT);
    
    com.googlecode.lanternavt.gui2.Panel mainPanel = new com.googlecode.lanternavt.gui2.Panel();
    mainPanel.setLayoutManager(new com.googlecode.lanternavt.gui2.BorderLayout());
    
    // outputBox.setReadOnly(true);
    outputBox.setTerminal(terminal);
    outputBox.setEditable(false);
    outputBox.setHorizontalFocusSwitching(false);
    outputBox.setVerticalFocusSwitching(false);
    // outputBox.setCaretWarp(true);
    inputBox.setTerminal(terminal);
    inputBox.setEditable(true);
    inputBox.setHiddenColumn(0);
    inputBox.setHorizontalFocusSwitching(false);
    inputBox.setVerticalFocusSwitching(false);
    
    if (awtTerminal != null)
    {
      String java_specification_version = System.getProperty("java.specification.version");
      
      verticalScrollbar = new Scrollbar(Scrollbar.VERTICAL);
      if (java_specification_version != null && java_specification_version.toLowerCase().startsWith("1.5"))
      {
        //Do nothing
      }
      else
      {
        verticalScrollbar.setBackground(SystemColor.control);
      }
      
      // verticalScrollbar.setForeground(SystemColor.controlText);
      verticalScrollbar.setUnitIncrement(1);
      verticalScrollbar.setFocusable(false);
      outputBox.setVerticalAdjustable(verticalScrollbar);
      
      verticalScrollbar.addKeyListener(new KeyListener()
      {
        public void keyTyped(KeyEvent e)
        {
          awtTerminal.dispatchEvent(e);
        }
        
        public void keyPressed(KeyEvent e)
        {
          awtTerminal.dispatchEvent(e);
        }
        
        public void keyReleased(KeyEvent e)
        {
          awtTerminal.dispatchEvent(e);
        }
      });
      
      horizontalScrollbar = new Scrollbar(Scrollbar.HORIZONTAL);
      if (java_specification_version != null && java_specification_version.toLowerCase().startsWith("1.5"))
      {
        //Do nothing
      }
      else
      {
        horizontalScrollbar.setBackground(SystemColor.control);
      }
      
      // horizontalScrollbar.setForeground(SystemColor.controlText);
      horizontalScrollbar.setUnitIncrement(1);
      horizontalScrollbar.setFocusable(false);
      outputBox.setHorizontalAdjustable(horizontalScrollbar);
      
      horizontalScrollbar.addKeyListener(new KeyListener()
      {
        public void keyTyped(KeyEvent e)
        {
          awtTerminal.dispatchEvent(e);
        }
        
        public void keyPressed(KeyEvent e)
        {
          awtTerminal.dispatchEvent(e);
        }
        
        public void keyReleased(KeyEvent e)
        {
          awtTerminal.dispatchEvent(e);
        }
      });
      
      spacer1 = new Panel();
      spacer1.setBackground(SystemColor.control);
      spacer1.setFocusable(false);
      
      panel.getCenterPanel().add(verticalScrollbar, BorderLayout.EAST);
      panel.getBottomPanel().add(horizontalScrollbar, BorderLayout.CENTER);
      panel.getBottomPanel().add(spacer1, BorderLayout.EAST);
      
      panel.setDropTarget(new DropTarget());
      panel.getDropTarget().setActive(true);
      panel.getDropTarget().addDropTargetListener(new VTGraphicalConsoleDropTargetListener(this));
      
      // outputBox.setVerticalAdjustable(frame.getScrollPane().getVAdjustable());
      // outputBox.setHorizontalAdjustable(frame.getScrollPane().getHAdjustable());
      ((DefaultTextBoxRenderer) outputBox.getRenderer()).setHideScrollBars(true);
      ((DefaultTextBoxRenderer) inputBox.getRenderer()).setHideScrollBars(true);
    }
    else
    {
      ((DefaultTextBoxRenderer) outputBox.getRenderer()).setHideScrollBars(true);
      ((DefaultTextBoxRenderer) inputBox.getRenderer()).setHideScrollBars(true);
    }
    
    outputBox.setInputFilter(new InputFilter()
    {
      public boolean onInput(Interactable interactable, KeyStroke keyStroke)
      {
        TerminalPosition topleft = outputBox.getTopLeft();
        
        if (keyStroke.getKeyType() == KeyType.MOUSE_EVENT)
        {
          MouseAction mouse = (MouseAction) keyStroke;
          // System.out.println(mouse.toString());
          
          if (mouse.getActionType() == MouseActionType.CLICK_DOWN)
          {
            if (mouse.getButton() != 0)
            {
              pressedMouseButton = mouse.getButton();
              pressedMouseColumn = mouse.getPosition().getColumn();
              pressedMouseRow = mouse.getPosition().getRow();
              outputBox.takeFocus();
              outputBox.setCaretPosition(topleft.getRow() + mouse.getPosition().getRow(), topleft.getColumn() + mouse.getPosition().getColumn());
              if (!outputBox.selectingText())
              {
                outputBox.setSelectionStartPosition(new TerminalPosition(topleft.getColumn() + mouse.getPosition().getColumn(), topleft.getRow() + mouse.getPosition().getRow()));
              }
              outputBox.invalidate();
            }
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.CLICK_RELEASE)
          {
            if (mouse.getButton() != 0)
            {
              if (outputBox.selectingText()
              && pressedMouseButton == mouse.getButton()
              && pressedMouseColumn == mouse.getPosition().getColumn()
              && pressedMouseRow == mouse.getPosition().getRow())
              {
                resetSelection(outputBox);
                outputBox.invalidate();
              }
              pressedMouseButton = 0;
              pressedMouseColumn = -1;
              pressedMouseRow = -1;
            }
            return false;
            // outputBox.invalidate();
          }
          
          if (mouse.getActionType() == MouseActionType.SCROLL_UP)
          {
            //outputBox.takeFocus();
            outputBox.scrollup();
            int row = outputBox.getRenderer().getViewTopLeft().getRow();
            if (row > 0)
            {
              outputBox.getRenderer().setViewTopLeft(outputBox.getRenderer().getViewTopLeft().withRow(row - 1));
            }
            outputBox.invalidate();
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.SCROLL_DOWN)
          {
            //outputBox.takeFocus();
            outputBox.scrolldown();
            int row = outputBox.getRenderer().getViewTopLeft().getRow();
            if (row < outputBox.getLineCount())
            {
              outputBox.getRenderer().setViewTopLeft(outputBox.getRenderer().getViewTopLeft().withRow(row + 1));
            }
            outputBox.invalidate();
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.DRAG)
          {
            outputBox.takeFocus();
            outputBox.updateSelection(mouse, topleft.getColumn() + mouse.getPosition().getColumn(), topleft.getRow() + mouse.getPosition().getRow());
            outputBox.setCaretPosition(topleft.getRow() + mouse.getPosition().getRow(), topleft.getColumn() + mouse.getPosition().getColumn());
            outputBox.setSelectionEndPosition(new TerminalPosition(topleft.getColumn() + mouse.getPosition().getColumn(), topleft.getRow() + mouse.getPosition().getRow()));
            outputBox.invalidate();
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.MOVE)
          {
            return false;
          }
        }
        if (keyStroke.getKeyType() == KeyType.ESCAPE)
        {
          inputBox.takeFocus();
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.PAUSE)
        {
          toggleFlush();
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.CONTEXT_MENU)
        {
          if (popupMenu != null)
          {
            popupMenu.show(awtTerminal, 0, 0);
          }
          return false;
        }
        // if (keyStroke.getKeyType() == KeyType.Tab)
        // {
        // outputBox.output("\t");
        // return false;
        // }
        if (keyStroke.isShiftDown() && !keyStroke.isCtrlDown())
        {
          if (keyStroke.getKeyType() == KeyType.INSERT)
          {
            console.pasteText();
            return false;
          }
          else
          {
            if (outputBox.isMovementKeyStroke(keyStroke))
            {
              TerminalPosition position = outputBox.getCaretPosition();
              outputBox.updateSelection(keyStroke, position.getColumn(), position.getRow());
            }
            else
            {
//              if (keyStroke.getKeyType() == KeyType.Shift)
//              {
//                if (outputBox.getSelectionStartPosition() != null)
//                {
//                  resetSelection();
//                }
//                return false;
//              }
            }
          }
        }
        else
        {
          if (outputBox.isMovementKeyStroke(keyStroke))
          {
            if (outputBox.getSelectionStartPosition() != null)
            {
              resetSelection(outputBox);
            }
          }
        }
        if (keyStroke.isCtrlDown() && !keyStroke.isShiftDown())
        {
          if (keyStroke.getKeyType() == KeyType.INSERT)
          {
            console.copyText();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.BACKSPACE)
          {
            //console.copyAllText();
            console.selectAllText();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.DELETE)
          {
            VTFontManager.defaultFontSize();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.PAGE_UP)
          {
            VTFontManager.increaseFontSize();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.PAGE_DOWN)
          {
            VTFontManager.decreaseFontSize();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.HOME)
          {
            VTFontManager.packComponentSize();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.END)
          {
            if (VTFontManager.isFontStyleBold())
            {
              VTFontManager.disableFontStyleBold();
            }
            else
            {
              VTFontManager.enableFontStyleBold();
            }
            return false;
          }
        }
        if (keyStroke.getKeyType() != KeyType.ALT_GRAPH && keyStroke.getKeyType() != KeyType.WINDOWS && keyStroke.getKeyType() != KeyType.META && keyStroke.getKeyType() != KeyType.ARROW_DOWN && keyStroke.getKeyType() != KeyType.ARROW_UP && keyStroke.getKeyType() != KeyType.ARROW_LEFT && keyStroke.getKeyType() != KeyType.ARROW_RIGHT && keyStroke.getKeyType() != KeyType.PAGE_DOWN && keyStroke.getKeyType() != KeyType.PAGE_UP && keyStroke.getKeyType() != KeyType.HOME && keyStroke.getKeyType() != KeyType.END)
        {
          inputBox.takeFocus();
          inputBox.handleInput(keyStroke);
          return false;
        }
        return true;
      }
    });
    
    inputBox.setInputFilter(new InputFilter()
    {
      public boolean onInput(Interactable interactable, KeyStroke keyStroke)
      {
        TerminalPosition topleft = inputBox.getTopLeft();
        
        if (keyStroke.getKeyType() == KeyType.MOUSE_EVENT)
        {
          MouseAction mouse = (MouseAction) keyStroke;
          // System.out.println(mouse.toString());
          
          if (mouse.getActionType() == MouseActionType.CLICK_DOWN)
          {
            if (mouse.getButton() != 0)
            {
              pressedMouseButton = mouse.getButton();
              pressedMouseColumn = mouse.getPosition().getColumn();
              pressedMouseRow = mouse.getPosition().getRow();
              int position = Math.min(topleft.getColumn() + mouse.getPosition().getColumn(), inputBox.getLastLine().length());
              inputBox.takeFocus();
              inputBox.setHiddenColumn(position);
              inputBox.setCaretPosition(position);
              if (!inputBox.selectingText())
              {
                inputBox.setSelectionStartPosition(new TerminalPosition(position, 0));
              }
              inputBox.invalidate();
            }
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.CLICK_RELEASE)
          {
            if (mouse.getButton() != 0)
            {
              if (inputBox.selectingText()
              && pressedMouseButton == mouse.getButton()
              && pressedMouseColumn == mouse.getPosition().getColumn()
              && pressedMouseRow == mouse.getPosition().getRow())
              {
                resetSelection(inputBox);
                inputBox.invalidate();
              }
              pressedMouseButton = 0;
              pressedMouseColumn = -1;
              pressedMouseRow = -1;
            }
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.SCROLL_UP)
          {
            //outputBox.takeFocus();
            outputBox.scrollup();
            outputBox.invalidate();
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.SCROLL_DOWN)
          {
            //outputBox.takeFocus();
            outputBox.scrolldown();
            outputBox.invalidate();
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.DRAG)
          {
            int max = inputBox.getLastLine().length();
            int location = topleft.getColumn();
            int column = Math.min(location + mouse.getPosition().getColumn(), max);
            
            inputBox.takeFocus();
            inputBox.updateSelection(mouse, topleft.getColumn() + mouse.getPosition().getColumn(), topleft.getRow() + mouse.getPosition().getRow());
            inputBox.setHiddenColumn(column);
            inputBox.setCaretPosition(column);
            inputBox.setSelectionEndPosition(new TerminalPosition(column, 0));
            inputBox.invalidate();
            return false;
          }
          
          if (mouse.getActionType() == MouseActionType.MOVE)
          {
            return false;
          }
        }
        if (keyStroke.isShiftDown())
        {
          if (inputBox.isMovementKeyStroke(keyStroke))
          {
            TerminalPosition position = inputBox.getCaretPosition();
            inputBox.updateSelection(keyStroke, position.getColumn(), position.getRow());
          }
          else
          {
//            if (keyStroke.getKeyType() == KeyType.Shift)
//            {
//              if (inputBox.getSelectionStartPosition() != null)
//              {
//                resetSelection();
//              }
//              return false;
//            }
          }
        }
        else
        {
          if (inputBox.isMovementKeyStroke(keyStroke))
          {
            if (inputBox.getSelectionStartPosition() != null)
            {
              resetSelection(inputBox);
            }
          }
        }
        
        if (keyStroke.getKeyType() == KeyType.ENTER)
        {
          String command = currentLineBuffer.toString();
          currentLineBuffer.setLength(0);
          // String command = inputBox.getText();
          
          input(command + "\n");
          
          inputBox.setText("");
          inputBox.setHiddenColumn(0);
          inputBox.setCaretPosition(0);
          inputBox.invalidate();
          // terminal.getTerminalSize();
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.PAGE_DOWN || keyStroke.getKeyType() == KeyType.PAGE_UP)
        {
          if (keyStroke.isCtrlDown())
          {
            if (keyStroke.getKeyType() == KeyType.PAGE_UP)
            {
              VTFontManager.increaseFontSize();
            }
            else
            {
              VTFontManager.decreaseFontSize();
            }
            return false;
          }
          //outputBox.takeFocus();
          outputBox.handleInput(keyStroke);
          outputBox.invalidate();
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.ARROW_UP)
        {
          if (scrollCommandHistoryUp(echoInput))
          {
            return false;
          }
        }
        if (keyStroke.getKeyType() == KeyType.ARROW_DOWN)
        {
          if (scrollCommandHistoryDown(echoInput))
          {
            return false;
          }
        }
        if (keyStroke.getKeyType() == KeyType.ESCAPE)
        {
          outputBox.takeFocus();
          return false;
        }
        // if (keyStroke.getKeyType() == KeyType.Tab)
        // {
        // inputBox.output("\t");
        // return false;
        // }
        if (keyStroke.getKeyType() == KeyType.BACKSPACE)
        {
          if (keyStroke.isCtrlDown())
          {
            //console.copyAllText();
            console.selectAllText();
            return false;
          }
        }
        if (keyStroke.getKeyType() == KeyType.INSERT)
        {
          if (!keyStroke.isCtrlDown() && !keyStroke.isShiftDown())
          {
            toggleReplace();
            return false;
          }
          if (!keyStroke.isCtrlDown() && keyStroke.isShiftDown())
          {
            console.pasteText();
            return false;
          }
          if (keyStroke.isCtrlDown() && !keyStroke.isShiftDown())
          {
            console.copyText();
            return false;
          }
        }
        if (keyStroke.getKeyType() == KeyType.PAUSE)
        {
          toggleFlush();
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.CONTEXT_MENU)
        {
          if (popupMenu != null)
          {
            popupMenu.show(awtTerminal, 0, 0);
          }
          return false;
        }
        if (keyStroke.getKeyType() == KeyType.ARROW_LEFT || keyStroke.getKeyType() == KeyType.ARROW_RIGHT || keyStroke.getKeyType() == KeyType.BACKSPACE || keyStroke.getKeyType() == KeyType.DELETE || keyStroke.getKeyType() == KeyType.HOME || keyStroke.getKeyType() == KeyType.END || keyStroke.getKeyType() == KeyType.TAB || keyStroke.getKeyType() == KeyType.REVERSE_TAB)
        {
          if (keyStroke.getKeyType() == KeyType.DELETE && keyStroke.isCtrlDown())
          {
            VTFontManager.defaultFontSize();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.HOME && keyStroke.isCtrlDown())
          {
            VTFontManager.packComponentSize();
            return false;
          }
          if (keyStroke.getKeyType() == KeyType.END && keyStroke.isCtrlDown())
          {
            if (VTFontManager.isFontStyleBold())
            {
              VTFontManager.disableFontStyleBold();
            }
            else
            {
              VTFontManager.enableFontStyleBold();
            }
          }
          inputBox.handleDataInput(currentLineBuffer, keyStroke, replaceActivated);
          if (!echoInput)
          {
            return false;
          }
          else
          {
            inputBox.setText(currentLineBuffer.toString());
            inputBox.setCaretPosition(inputBox.getHiddenColumn());
            inputBox.invalidate();
            return false;
          }
        }
        if (keyStroke.getKeyType() == KeyType.CHARACTER)
        {
          if (keyStroke.getCharacter() == '\u001A')
          {
            toggleFlush();
            return false;
          }
          if (keyStroke.getCharacter() == '\u0018')
          {
            toggleReplace();
            return false;
          }
          if (keyStroke.getCharacter() == '\u001C')
          {
            if (!ignoreClose)
            {
              VTRuntimeExit.exit(0);
              return false;
            }
          }
          if (keyStroke.getCharacter() == '\u0003')
          {
            if (!ignoreClose)
            {
              VTRuntimeExit.exit(0);
              return false;
            }
          }
          if (keyStroke.isCtrlDown())
          {
            if (keyStroke.getCharacter() == 'C' || keyStroke.getCharacter() == 'c')
            {
              if (!ignoreClose)
              {
                VTRuntimeExit.exit(0);
                return false;
              }
            }
            if (keyStroke.getCharacter() == '\\')
            {
              if (!ignoreClose)
              {
                VTRuntimeExit.exit(0);
                return false;
              }
            }
            if (keyStroke.getCharacter() == 'Z' || keyStroke.getCharacter() == 'z')
            {
              // System.out.println("ctrl+z");
              toggleFlush();
              return false;
            }
            if (keyStroke.getCharacter() == 'X' || keyStroke.getCharacter() == 'x')
            {
              // System.out.println("ctrl+z");
              toggleReplace();
              return false;
            }
          }
          inputBox.handleDataInput(currentLineBuffer, keyStroke.getCharacter(), replaceActivated);
          if (echoInput)
          {
            inputBox.setText(currentLineBuffer.toString());
            inputBox.setCaretPosition(inputBox.getHiddenColumn());
            inputBox.invalidate();
          }
          return false;
        }
        return true;
      }
    });
    
    com.googlecode.lanternavt.gui2.Panel bottonPanel = new com.googlecode.lanternavt.gui2.Panel();
    bottonPanel.setLayoutManager(new com.googlecode.lanternavt.gui2.BorderLayout());
    
    // bottonPanel.addComponent(promptLabel, BorderLayout.Location.LEFT);
    bottonPanel.addComponent(inputBox, com.googlecode.lanternavt.gui2.BorderLayout.Location.CENTER);
    
    mainPanel.addComponent(outputBox, com.googlecode.lanternavt.gui2.BorderLayout.Location.CENTER);
    mainPanel.addComponent(bottonPanel, com.googlecode.lanternavt.gui2.BorderLayout.Location.BOTTOM);
    
    // Create window to hold the panel
    window = new BasicWindow();
    Collection<Hint> hints = new ArrayList<Hint>();
    hints.add(Hint.FULL_SCREEN);
    hints.add(Hint.NO_DECORATIONS);
    hints.add(Hint.NO_POST_RENDERING);
    
    window.setComponent(mainPanel);
    window.setHints(hints);
    
    defaultInputProperties = new Properties();
    defaultInputProperties.load(this.getClass().getResourceAsStream("/org/vash/vate/console/lanterna/separated/vtlanternaconsole-input-theme.properties"));
    PropertyTheme inputTheme = new PropertyTheme(defaultInputProperties);
    
    defaultOutputProperties = new Properties();
    defaultOutputProperties.load(this.getClass().getResourceAsStream("/org/vash/vate/console/lanterna/separated/vtlanternaconsole-output-theme.properties"));
    PropertyTheme outputTheme = new PropertyTheme(defaultOutputProperties);
    
    inputBox.setTheme(inputTheme);
    outputBox.setTheme(outputTheme);
    
    inputStream = new VTLanternaConsoleInputStream(this);
    printStream = new VTLanternaConsolePrintStream(this);
    
    setTitle("");
    
    if (panel != null)
    {
      spacer1.setSize(verticalScrollbar.getPreferredSize().width, spacer1.getSize().height);
      spacer1.setPreferredSize(new Dimension(verticalScrollbar.getPreferredSize().width, spacer1.getSize().height));
      
      panel.setDefaultTerminalSize(panel.getTerminalSize());
    }
    
    if (frame != null)
    {
      frame.setLocationByPlatform(true);
      frame.pack();
      
      frame.setVisible(true);
      
      if (frame instanceof AWTTerminalFrame)
      {
        AWTTerminalFrame awtTerminalFrame = (AWTTerminalFrame) frame;
        awtTerminalFrame.setDefaultTerminalSize(awtTerminalFrame.getTerminalSize());
      }
    }
    
    setColors(VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN, VTConsole.VT_CONSOLE_COLOR_DARK_BLACK);
    
    screen = new TerminalScreen(terminal);
    screen.setTabBehaviour(TabBehaviour.CONVERT_TO_ONE_SPACE);
    screen.startScreen();
    
    // Create gui and start gui
    gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), null);
    
    synchronized (this)
    {
      this.build = true;
      this.notifyAll();
    }
    
    gui.addWindowAndWait(window);
  }
  
  public void setIgnoreClose(boolean ignoreClose)
  {
    this.ignoreClose = ignoreClose;
  }
  
  public boolean getIgnoreClose()
  {
    return ignoreClose;
  }
  
  public void deactivatedFrame()
  {
    synchronized (outputSynchronizer)
    {
      frameDeactivated = true;
    }
  }
  
  public void activatedFrame()
  {
    synchronized (outputSynchronizer)
    {
      frameDeactivated = false;
    }
    flush();
  }
  
  public void toggleReplace()
  {
    replaceActivated = !replaceActivated;
    if (notifyReplaceInput != null)
    {
      notifyReplaceInput.notify(replaceActivated);
    }
  }
  
  public void toggleFlush()
  {
    flushInterrupted = !flushInterrupted;
    flush();
    if (notifyFlushInterrupted != null)
    {
      notifyFlushInterrupted.notify(flushInterrupted);
    }
  }
  
  public void toggleEcho()
  {
    echoInput = !echoInput;
    if (echoInput)
    {
      inputBox.setText(currentLineBuffer.toString());
      inputBox.setCaretPosition(inputBox.getHiddenColumn());
      inputBox.invalidate();
    }
    else
    {
      inputBox.setText("");
      inputBox.setCaretPosition(0);
      inputBox.invalidate();
    }
  }
  
  public void setEchoInput(boolean echo)
  {
    echoInput = echo;
    if (echoInput)
    {
      inputBox.setText(currentLineBuffer.toString());
      inputBox.setCaretPosition(inputBox.getHiddenColumn());
      inputBox.invalidate();
    }
    else
    {
      inputBox.setText("");
      inputBox.setCaretPosition(0);
      inputBox.invalidate();
    }
  }
  
  public void clearInput()
  {
    synchronized (inputLineBuffer)
    {
      inputLineBuffer.clear();
    }
    resetCurrentLine(echoInput);
  }
  
  private void registerLine(String command)
  {
    if (echoInput
    && (command != null && command.length() > 0)
    && (commandHistory.size() == 0 || commandHistory.lastIndexOf(command) != commandHistory.size() - 1))
    {
      if (!(commandHistory.size() < commandHistoryMaxSize))
      {
        commandHistory.remove(0);
      }
      commandHistory.add(command.toString());
    }
    commandHistoryPosition = commandHistory.size();
  }
  
  private void restoreCurrentLine(boolean echo)
  {
    currentLineBuffer.append(commandHistory.get(commandHistoryPosition));
    inputBox.setHiddenColumn(currentLineBuffer.length());
    if (echo)
    {
      inputBox.setSelectionStartPosition(null);
      inputBox.setSelectionEndPosition(null);
      inputBox.setText(currentLineBuffer.toString());
      inputBox.setCaretPosition(inputBox.getHiddenColumn());
      inputBox.invalidate();
    }
  }
  
  private void resetCurrentLine(boolean echo)
  {
    currentLineBuffer.setLength(0);
    inputBox.setHiddenColumn(0);
    if (echo)
    {
      inputBox.setSelectionStartPosition(null);
      inputBox.setSelectionEndPosition(null);
      inputBox.setText("");
      inputBox.setCaretPosition(0);
      inputBox.invalidate();
    }
  }
  
  private void updateCurrentLine(String line, boolean echo)
  {
    currentLineBuffer.setLength(0);
    currentLineBuffer.append(line);
    inputBox.setHiddenColumn(currentLineBuffer.length());
    if (echo)
    {
      inputBox.setSelectionStartPosition(null);
      inputBox.setSelectionEndPosition(null);
      inputBox.setText(currentLineBuffer.toString());
      inputBox.setCaretPosition(inputBox.getHiddenColumn());
      inputBox.invalidate();
    }
  }
  
  private void appendCurrentLine(String text, boolean echo)
  {
    inputBox.handleDataInput(currentLineBuffer, text, replaceActivated);
    if (echo)
    {
      inputBox.setText(currentLineBuffer.toString());
      inputBox.setCaretPosition(inputBox.getHiddenColumn());
      inputBox.invalidate();
    }
  }
  
  private boolean scrollCommandHistoryUp(boolean echo)
  {
    if (commandHistory.size() > 0)
    {
      if (commandHistoryPosition > 0)
      {
        commandHistoryPosition--;
        resetCurrentLine(echo);
        restoreCurrentLine(echo);
        return true;
      }
      if (commandHistoryPosition == 0)
      {
        commandHistoryPosition--;
        resetCurrentLine(echo);
        return true;
      }
    }
    return false;
  }
  
  private boolean scrollCommandHistoryDown(boolean echo)
  {
    if (commandHistory.size() > 0)
    {
      if (commandHistoryPosition < (commandHistory.size() - 1))
      {
        commandHistoryPosition++;
        resetCurrentLine(echo);
        restoreCurrentLine(echo);
        return true;
      }
      if (commandHistoryPosition == commandHistory.size() - 1)
      {
        commandHistoryPosition++;
        resetCurrentLine(echo);
        return true;
      }
    }
    return false;
  }
  
  private void appendToPendingInputLine(String line)
  {
    // line = line.replace('\u000b', '\n');
    if (line == null || line.length() == 0)
    {
      return;
    }
    synchronized (inputLineBuffer)
    {
      if (inputLineBuffer.size() > 0)
      {
        if (inputLineBuffer.get(inputLineBuffer.size() - 1).contains("\n"))
        {
          inputLineBuffer.add(line);
        }
        else
        {
          inputLineBuffer.add(inputLineBuffer.remove(inputLineBuffer.size() - 1) + line);
        }
      }
      else
      {
        inputLineBuffer.add(line);
      }
    }
  }
  
  private String getFromPendingInputLine()
  {
    synchronized (inputLineBuffer)
    {
      if (inputLineBuffer.size() > 0)
      {
        return inputLineBuffer.get(0);
      }
      else
      {
        return null;
      }
    }
  }
  
  private String removeFromPendingInputLine()
  {
    synchronized (inputLineBuffer)
    {
      if (inputLineBuffer.size() > 0)
      {
        return inputLineBuffer.remove(0);
      }
      else
      {
        return null;
      }
    }
  }
  
  private int getPendingInputLineSize()
  {
    synchronized (inputLineBuffer)
    {
      return inputLineBuffer.size();
    }
  }
  
  public void input(String text)
  {
    if (text == null || text.length() == 0)
    {
      return;
    }
    text = text.replace('\u000b', '\n');
    synchronized (inputSynchronizer)
    {
      if (readingInput)
      {
        if (text.indexOf('\n') == -1)
        {
          appendCurrentLine(text, echoInput);
        }
        else
        {
          int i = 0, j = 0;
          i = text.indexOf('\n');
          inputBuffer.append(text.substring(0, i) + "\n");
          j = i + 1;
          while ((i = text.indexOf('\n', j)) != -1)
          {
            appendToPendingInputLine(text.substring(j, i) + "\n");
            j = i + 1;
          }
          if (j < text.length())
          {
            text = text.substring(j);
            //appendToPendingInputLine(text);
            updateCurrentLine(text, echoInput);
          }
          inputSynchronizer.notifyAll();
        }
      }
      else
      {
        if (text.indexOf('\n') == -1)
        {
          appendToPendingInputLine(text);
        }
        else
        {
          int i = 0, j = 0;
          i = text.indexOf('\n');
          appendToPendingInputLine(text.substring(0, i) + "\n");
          j = i + 1;
          while ((text.indexOf('\n', j)) != -1)
          {
            appendToPendingInputLine(text.substring(j, i) + "\n");
            j = i + 1;
          }
          if (j < text.length())
          {
            text = text.substring(j);
            //appendToPendingInputLine(text);
            updateCurrentLine(text, echoInput);
          }
        }
      }
    }
  }
  
  public char readChar() throws InterruptedException
  {
    synchronized (inputSynchronizer)
    {
      readingInput = true;
      char key;
      while (inputBuffer.length() == 0)
      {
        inputSynchronizer.wait();
      }
      key = inputBuffer.charAt(0);
      inputBuffer.deleteCharAt(0);
      readingInput = false;
      return key;
    }
  }
  
  public String readString() throws InterruptedException
  {
    synchronized (inputSynchronizer)
    {
      readingInput = true;
      String data;
      while (inputBuffer.length() == 0 || inputBuffer.charAt(inputBuffer.length() - 1) != '\n')
      {
        inputSynchronizer.wait();
      }
      data = inputBuffer.toString();
      inputBuffer.setLength(0);
      readingInput = false;
      return data;
    }
  }
  
  public String readLine(boolean echo) throws InterruptedException
  {
    currentThread = Thread.currentThread();
    readingInput = true;
    setEchoInput(echo);
    String data = null;
//    if (getPendingInputLineSize() > 0 && !getFromPendingInputLine().contains("\n"))
//    {
//      updateCurrentLine(removeFromPendingInputLine(), echoInput);
//    }
    if (getPendingInputLineSize() > 0 && getFromPendingInputLine().contains("\n"))
    {
      data = removeFromPendingInputLine();
      data = data.substring(0, data.length() - 1);
    }
    else
    {
      data = readString();
      data = data.substring(0, data.length() - 1);
    }
    if (echoInput)
    {
      registerLine(data);
      if (commandEcho)
      {
        if (doubledOutput != null)
        {
          doubledOutput.println(data);
        }
        else
        {
          write(data + "\n");
          flush();
        }
      }
      else
      {
        
      }
    }
    else
    {
      if (commandEcho)
      {
        if (doubledOutput != null)
        {
          doubledOutput.println();
        }
        else
        {
          write("\n");
          flush();
        }
      }
      else
      {
        
      }
    }
    readingInput = false;
    currentThread = null;
    writeLogReadLine(data);
    return data;
  }
  
  public void interruptReadLine()
  {
    if (currentThread != null)
    {
      try
      {
        currentThread.interrupt();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public void print(String str)
  {
    printStream.print(str);
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
    synchronized (outputSynchronizer)
    {
      if (outputBuffer.length() + str.length() <= 65536)
      {
        outputBuffer.append(str);
      }
      else
      {
        int truncate = Math.max(0, str.length() - 65536);
        int size = Math.min(65536, str.length());
        
        outputBuffer.delete(0, size);
        if (truncate == 0)
        {
          outputBuffer.append(str);
        }
        else
        {
          outputBuffer.append(str.substring(truncate, truncate + size));
        }
      }
    }
  }
  
  public void write(char[] buf, int off, int len)
  {
    synchronized (outputSynchronizer)
    {
      if (outputBuffer.length() + len <= 65536)
      {
        outputBuffer.append(buf, off, len);
      }
      else
      {
        int truncate = Math.max(0, len - 65536);
        int size = Math.min(65536, len);
        // String truncated = String.valueOf(buf, truncate, size);
        
        outputBuffer.delete(0, size);
        outputBuffer.append(buf, truncate, size);
      }
    }
  }
  
  public void flush()
  {
    // if (!flushInterrupted && !frameIconified && !outputBox.selectingText())
    if (!flushInterrupted && !frameDeactivated)
    {
      synchronized (outputSynchronizer)
      {
        if (outputBuffer.length() > 0)
        {
          // outputBox.output(VTStringBuilderValue.getValue(outputBuffer), 0,
          // outputBuffer.length());
          String data = outputBuffer.toString();
          outputBox.output(data);
          if (logOutput != null)
          {
            writeLogOutput(data);
          }
          outputBuffer.setLength(0);
        }
      }
    }
  }
  
  public void clear()
  {
    synchronized (outputSynchronizer)
    {
      outputBuffer.setLength(0);
      outputBox.setText("");
      outputBox.setCaretPosition(0, 0);
      outputBox.invalidate();
    }
  }
  
  public void bell()
  {
    try
    {
      Toolkit.getDefaultToolkit().beep();
    }
    catch (Throwable e)
    {
      System.out.print("\u0007");
    }
  }
  
  public void setTitle(String title)
  {
    if (graphical && frame != null)
    {
      frame.setTitle(title);
    }
    else
    {
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
        VTMainNativeUtils.printf("\u001B]0;" + title + "\u0007");
        // System.out.print("\u001B]0;" + title + "\u0007");
        // System.out.print("\u001B]1;" + title + "\u0007");
        // System.out.print("\u001B]2;" + title + "\u0007");
      }
    }
  }
  
  public void setColors(int foregroundColor, int backgroundColor)
  {
    Properties copyInputProperties = new Properties();
    copyInputProperties.putAll(defaultInputProperties);
    
    Properties copyOutputProperties = new Properties();
    copyOutputProperties.putAll(defaultOutputProperties);
    
    String foregroundColorStringOutput = "green_bright";
    String foregroundColorStringInput = "green_bright";
    String backgroundColorStringOutput = "black";
    String backgroundColorStringInput = "black_bright";
    Color lastLineBackgroundColor = null;
    Color spacerBackgroundColor = null;
    
    switch (foregroundColor)
    {
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
      {
        foregroundColorStringOutput = "black";
        foregroundColorStringInput = "black";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
      {
        foregroundColorStringOutput = "red";
        foregroundColorStringInput = "red";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
      {
        foregroundColorStringOutput = "green";
        foregroundColorStringInput = "green";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
      {
        foregroundColorStringOutput = "yellow";
        foregroundColorStringInput = "yellow";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
      {
        foregroundColorStringOutput = "blue";
        foregroundColorStringInput = "blue";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
      {
        foregroundColorStringOutput = "magenta";
        foregroundColorStringInput = "magenta";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
      {
        foregroundColorStringOutput = "cyan";
        foregroundColorStringInput = "cyan";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
      {
        foregroundColorStringOutput = "white";
        foregroundColorStringInput = "white";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLACK:
      {
        foregroundColorStringOutput = "black_bright";
        foregroundColorStringInput = "black_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
      {
        foregroundColorStringOutput = "red_bright";
        foregroundColorStringInput = "red_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
      {
        foregroundColorStringOutput = "green_bright";
        foregroundColorStringInput = "green_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
      {
        foregroundColorStringOutput = "yellow_bright";
        foregroundColorStringInput = "yellow_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
      {
        foregroundColorStringOutput = "blue_bright";
        foregroundColorStringInput = "blue_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
      {
        foregroundColorStringOutput = "magenta_bright";
        foregroundColorStringInput = "magenta_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
      {
        foregroundColorStringOutput = "cyan_bright";
        foregroundColorStringInput = "cyan_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
      {
        foregroundColorStringOutput = "white_bright";
        foregroundColorStringInput = "white_bright";
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
      {
        foregroundColorStringOutput = "green_bright";
        foregroundColorStringInput = "green_bright";
        break;
      }
      default:
      {
        foregroundColorStringOutput = "green_bright";
        foregroundColorStringInput = "green_bright";
        break;
      }
    }
    
    TextColor awtTerminalBackgroundColor = null;
    switch (backgroundColor)
    {
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLACK:
      {
        backgroundColorStringOutput = "black";
        backgroundColorStringInput = "black_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK, false, false);
        awtTerminalBackgroundColor = ANSI.BLACK;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_RED:
      {
        backgroundColorStringOutput = "red";
        backgroundColorStringInput = "red_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.RED_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.RED, false, false);
        awtTerminalBackgroundColor = ANSI.RED;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_GREEN:
      {
        backgroundColorStringOutput = "green";
        backgroundColorStringInput = "green_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.GREEN_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.GREEN, false, false);
        awtTerminalBackgroundColor = ANSI.GREEN;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_YELLOW:
      {
        backgroundColorStringOutput = "yellow";
        backgroundColorStringInput = "yellow_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.YELLOW_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.YELLOW, false, false);
        awtTerminalBackgroundColor = ANSI.YELLOW;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_BLUE:
      {
        backgroundColorStringOutput = "blue";
        backgroundColorStringInput = "blue_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLUE_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLUE, false, false);
        awtTerminalBackgroundColor = ANSI.BLUE;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_MAGENTA:
      {
        backgroundColorStringOutput = "magenta";
        backgroundColorStringInput = "magenta_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.MAGENTA_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.MAGENTA, false, false);
        awtTerminalBackgroundColor = ANSI.MAGENTA;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_CYAN:
      {
        backgroundColorStringOutput = "cyan";
        backgroundColorStringInput = "cyan_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.CYAN_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.CYAN, false, false);
        awtTerminalBackgroundColor = ANSI.CYAN;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DARK_WHITE:
      {
        backgroundColorStringOutput = "white";
        backgroundColorStringInput = "white_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.WHITE_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.WHITE, false, false);
        awtTerminalBackgroundColor = ANSI.WHITE;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLACK:
      {
        backgroundColorStringOutput = "black_bright";
        backgroundColorStringInput = "black";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.BLACK_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_RED:
      {
        backgroundColorStringOutput = "red_bright";
        backgroundColorStringInput = "red";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.RED, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.RED_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.RED_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_GREEN:
      {
        backgroundColorStringOutput = "green_bright";
        backgroundColorStringInput = "green";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.GREEN, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.GREEN_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.GREEN_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_YELLOW:
      {
        backgroundColorStringOutput = "yellow_bright";
        backgroundColorStringInput = "yellow";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.YELLOW, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.YELLOW_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.YELLOW_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_BLUE:
      {
        backgroundColorStringOutput = "blue_bright";
        backgroundColorStringInput = "blue";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLUE, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLUE_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.BLUE_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_MAGENTA:
      {
        backgroundColorStringOutput = "magenta_bright";
        backgroundColorStringInput = "magenta";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.MAGENTA, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.MAGENTA_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.MAGENTA_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_CYAN:
      {
        backgroundColorStringOutput = "cyan_bright";
        backgroundColorStringInput = "cyan";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.CYAN, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.CYAN_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.CYAN_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_LIGHT_WHITE:
      {
        backgroundColorStringOutput = "white_bright";
        backgroundColorStringInput = "white";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.WHITE, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.WHITE_BRIGHT, false, false);
        awtTerminalBackgroundColor = ANSI.WHITE_BRIGHT;
        break;
      }
      case VTConsole.VT_CONSOLE_COLOR_DEFAULT:
      {
        backgroundColorStringOutput = "black";
        backgroundColorStringInput = "black_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK, false, false);
        awtTerminalBackgroundColor = ANSI.BLACK;
        break;
      }
      default:
      {
        backgroundColorStringOutput = "black";
        backgroundColorStringInput = "black_bright";
        lastLineBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK_BRIGHT, false, false);
        spacerBackgroundColor = CUSTOM_PALETTE.get(ANSI.BLACK, false, false);
        awtTerminalBackgroundColor = ANSI.BLACK;
        break;
      }
    }
    
    if (awtTerminal != null)
    {
      awtTerminal.setBackgroundColor(awtTerminalBackgroundColor);
    }
    
    for (Object inputKey : copyInputProperties.keySet().toArray())
    {
      if (inputKey.toString().toLowerCase().contains("foreground"))
      {
        copyInputProperties.remove(inputKey);
        copyInputProperties.put(inputKey, foregroundColorStringInput);
      }
      if (inputKey.toString().toLowerCase().contains("background"))
      {
        copyInputProperties.remove(inputKey);
        copyInputProperties.put(inputKey, backgroundColorStringInput);
      }
    }
    
    for (Object outputKey : copyOutputProperties.keySet().toArray())
    {
      if (outputKey.toString().toLowerCase().contains("foreground"))
      {
        copyOutputProperties.remove(outputKey);
        copyOutputProperties.put(outputKey, foregroundColorStringOutput);
      }
      if (outputKey.toString().toLowerCase().contains("background"))
      {
        copyOutputProperties.remove(outputKey);
        copyOutputProperties.put(outputKey, backgroundColorStringOutput);
      }
    }
    
    PropertyTheme inputTheme = new PropertyTheme(copyInputProperties);
    PropertyTheme outputTheme = new PropertyTheme(copyOutputProperties);
    
    inputBox.setTheme(inputTheme);
    outputBox.setTheme(outputTheme);
    
    if (panel != null)
    {
      awtTerminal.getTerminalImplementation().setLastLineBackground(lastLineBackgroundColor);
      awtTerminal.getTerminalImplementation().setDefaultBackground(spacerBackgroundColor);
      
      panel.setLastLineBackgroundColor(lastLineBackgroundColor);
      panel.setSpacerBackgroundColor(spacerBackgroundColor);
      panel.setBackground(spacerBackgroundColor);
      
      if (frame != null)
      {
        frame.setBackground(spacerBackgroundColor);
      }
    }
  }
  
  public void setBold(boolean bold)
  {
    
  }
  
  public void resetAttributes()
  {
    PropertyTheme inputTheme = new PropertyTheme(defaultInputProperties);
    PropertyTheme outputTheme = new PropertyTheme(defaultOutputProperties);
    
    inputBox.setTheme(inputTheme);
    outputBox.setTheme(outputTheme);
    
    if (awtTerminal != null)
    {
      awtTerminal.getTerminalImplementation().setLastLineBackground(lastLineBackgroundDefault);
    }
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
  
//	public static void main(String[] args) throws Throwable
//	{
//		final VTLanternaConsole console = new VTLanternaConsole();
//		while (true)
//		{
//			String line = console.readLine(true);
//			//System.out.println("line:[" + line + "]");
//		}
//	}
  
  public void toggleFlushMode()
  {
    toggleFlush();
  }
  
  public void toggleInputMode()
  {
    toggleReplace();
  }
  
  public void copyText()
  {
    String selectedText = getSelectedText();
    // System.out.println("selectedText:" + selectedText);
    StringSelection text = null;
    if (selectedText != null)
    {
      text = new StringSelection(selectedText);
    }
    else
    {
      text = new StringSelection("");
    }
    try
    {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, null);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void pasteText()
  {
    try
    {
      Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
      if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
      {
        String text = systemClipboard.getData(DataFlavor.stringFlavor).toString();
        input(text);
      }
      else if (systemClipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
      {
        @SuppressWarnings("unchecked")
        List<File> files = (List<File>) systemClipboard.getData(DataFlavor.javaFileListFlavor);
        if (files.size() > 0)
        {
          StringBuilder fileList = new StringBuilder();
          String fileListString = "";
          for (File file : files.toArray(new File[] {}))
          {
            fileList.append(" " + file.getAbsolutePath());
          }
          fileListString = fileList.substring(1);
          input(fileListString);
        }
      }
    }
    catch (Throwable t)
    {
      //t.printStackTrace();
    }
  }
  
  public String getSelectedText()
  {
    if (outputBox.isFocused() && outputBox.selectingText())
    {
      try
      {
        return outputBox.getSelectedText();
      }
      catch (Throwable e)
      {
        
      }
    }
    else if (inputBox.isFocused() && inputBox.selectingText())
    {
      try
      {
        return inputBox.getSelectedText();
      }
      catch (Throwable e)
      {
        
      }
    }
    else if (outputBox.selectingText())
    {
      try
      {
        return outputBox.getSelectedText();
      }
      catch (Throwable e)
      {
        
      }
    }
    else if (inputBox.selectingText())
    {
      try
      {
        return inputBox.getSelectedText();
      }
      catch (Throwable e)
      {
        
      }
    }
    return "";
  }
  
  public void refreshText()
  {
    try
    {
      screen.refresh(RefreshType.COMPLETE);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public boolean isCommandEcho()
  {
    return this.commandEcho;
  }
  
  public void setCommandEcho(boolean commandEcho)
  {
    this.commandEcho = commandEcho;
  }
  
  private void resetSelection(VTLanternaOutputTextBox element)
  {
    if (element == inputBox && inputBox.selectingText())
    {
      inputBox.setSelectionStartPosition(null);
      inputBox.setSelectionEndPosition(null);
      inputBox.invalidate();
    }
    
    if (element == outputBox && outputBox.selectingText())
    {
      outputBox.setSelectionStartPosition(null);
      outputBox.setSelectionEndPosition(null);
      flush();
      outputBox.invalidate();
    }
  }
  
  public String getAllText()
  {
    if (outputBox.isFocused())
    {
      try
      {
        StringBuilder text = new StringBuilder();
        for (String line : outputBox.lines.toArray(new String[] {}))
        {
          text.append(line + "\n");
        }
        if (text.length() > 0)
        {
          text.deleteCharAt(text.length() - 1);
        }
        return text.toString();
      }
      catch (Throwable e)
      {
        
      }
    }
    if (inputBox.isFocused())
    {
      try
      {
        return inputBox.getLine(0);
      }
      catch (Throwable e)
      {
        
      }
    }
    return null;
  }
  
  public void copyAllText()
  {
    String allText = getAllText();
    // System.out.println("selectedText:" + selectedText);
    StringSelection text = null;
    if (allText != null)
    {
      text = new StringSelection(allText);
    }
    else
    {
      text = new StringSelection("");
    }
    try
    {
      Toolkit.getDefaultToolkit().getSystemClipboard().setContents(text, null);
    }
    catch (Throwable e)
    {
      
    }
  }
  
  public void selectAllText()
  {
    if (outputBox.isFocused())
    {
      try
      {
        outputBox.setSelectionStartPosition(new TerminalPosition(0, 0));
        outputBox.setSelectionEndPosition(new TerminalPosition(outputBox.longestRow, outputBox.getLineCount() - 1));
        outputBox.invalidate();
      }
      catch (Throwable e)
      {
        
      }
    }
    if (inputBox.isFocused())
    {
      try
      {
        inputBox.setSelectionStartPosition(new TerminalPosition(0, 0));
        inputBox.setSelectionEndPosition(new TerminalPosition(inputBox.getLastLine().length(), 0));
        inputBox.invalidate();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
  
  public void setRemoteIcon(boolean remoteIcon)
  {
    this.remoteIcon = remoteIcon;
  }
  
  public void addToggleFlushModePauseNotify(VTConsoleBooleanToggleNotify notifyFlushInterrupted)
  {
    this.notifyFlushInterrupted = notifyFlushInterrupted;
  }
  
  public void addToggleInputModeReplaceNotify(VTConsoleBooleanToggleNotify notifyReplaceInput)
  {
    this.notifyReplaceInput = notifyReplaceInput;
  }
  
  public void requestFocus()
  {
    // System.out.println("console.requestFocus()");
    try
    {
      if (awtTerminal != null)
      {
        awtTerminal.requestFocusInWindow();
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void closeConsole()
  {
    try
    {
      terminal.close();
    }
    catch (Throwable t)
    {
      
    }
    
    if (panel != null)
    {
      try
      {
        panel.close();
      }
      catch (Throwable t)
      {
        
      }
    }
    
    if (frame != null)
    {
      try
      {
        frame.setVisible(false);
      }
      catch (Throwable t)
      {
        
      }
      
      try
      {
        frame.dispose();
      }
      catch (Throwable t)
      {
        
      }
      
      VTFontManager.unregisterConsole(this);
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
        logReadLine = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(path, true), VTSystem.getCharsetEncoder("UTF-8")));
        return true;
      }
      catch (Throwable t)
      {
        closeLogReadLine();
      }
    }
    else
    {
      closeLogReadLine();
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
    if (logReadLine != null)
    {
      try
      {
        logReadLine.write(line + "\r\n");
        logReadLine.flush();
      }
      catch (Throwable t)
      {
        closeLogReadLine();
      }
    }
  }
  
  private void closeLogReadLine()
  {
    if (logReadLine != null)
    {
      try
      {
        logReadLine.close();
      }
      catch (Throwable e)
      {
        
      }
      logReadLine = null;
    }
  }
  
  private void writeLogOutput(String data)
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
  
  private void closeLogOutput()
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
  
  public String getLastOutputLine()
  {
    if (outputBuffer.length() > 0)
    {
      String bufferData = outputBuffer.toString();
      if (bufferData.indexOf('\n') >= 0)
      {
        return bufferData.substring(bufferData.lastIndexOf('\n'), bufferData.length());
      }
      else
      {
        return this.outputBox.getLastLine() + bufferData;
      }
    }
    return this.outputBox.getLastLine();
  }
  
  public int getLastOutputLineLength()
  {
    String lastOutputLine = getLastOutputLine();
    if (lastOutputLine != null)
    {
      return lastOutputLine.length();
    }
    return -1;
  }
  
  public boolean isFlushModePause()
  {
    return flushInterrupted;
  }
  
  public boolean isInputModeReplace()
  {
    return replaceActivated;
  }
  
  public Frame getFrame()
  {
    if (frame != null)
    {
      return frame;
    }
    if (container != null)
    {
      Container parent = container;
      while (parent != null)
      {
        if (parent instanceof Frame)
        {
          return (Frame) parent;
        }
        parent = parent.getParent();
      }
    }
    return null;
  }
  
  public Panel getPanel()
  {
    return panel;
  }
}