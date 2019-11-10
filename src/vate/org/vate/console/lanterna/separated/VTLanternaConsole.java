package org.vate.console.lanterna.separated;

import java.awt.Frame;
import java.awt.Scrollbar;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DropTarget;
import java.awt.event.AdjustmentEvent;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import javax.imageio.ImageIO;

import org.vate.VT;
import org.vate.console.VTConsole;
import org.vate.console.VTConsoleImplementation;
import org.vate.console.graphical.VTGraphicalConsoleNullOutputStream;
import org.vate.console.graphical.listener.VTGraphicalConsoleDropTargetListener;
import org.vate.console.graphical.menu.VTGraphicalConsolePopupMenu;
import org.vate.graphics.font.VTGlobalTextStyleManager;
import org.vate.nativeutils.VTNativeUtils;

import com.googlecode.lanterna.TerminalPosition;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.InputFilter;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox.DefaultTextBoxRenderer;
import com.googlecode.lanterna.gui2.TextBox.Style;
import com.googlecode.lanterna.gui2.Window.Hint;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.input.MouseAction;
import com.googlecode.lanterna.input.MouseActionType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.Screen.RefreshType;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFontConfiguration;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;
import com.sun.jna.Platform;

public class VTLanternaConsole implements VTConsoleImplementation
{
	private volatile AWTTerminalFrame awtframe;
	private Terminal terminal;
	private Screen screen;
	private AWTTerminal awtterminal;
	private VTLanternaOutputTextBox outputBox = new VTLanternaOutputTextBox(new TerminalSize(80, 24), "", Style.MULTI_LINE, 200);
	private VTLanternaOutputTextBox inputBox = new VTLanternaOutputTextBox(new TerminalSize(80, 1), "", Style.SINGLE_LINE, 1);
	private static final int commandHistoryMaxSize = 50;
	private List<String> commandHistory = new LinkedList<String>();
	private volatile int commandHistoryPosition;
	private List<String> inputLineBuffer = new LinkedList<String>();
	private StringBuilder currentLineBuffer = new StringBuilder("");
	private volatile Thread inputThread;
	//private volatile boolean readingLine = false;
	private volatile boolean echoInput = true;
	private volatile boolean readingInput = false;
	//private volatile boolean updatingTerminal = false;
	private volatile boolean flushInterrupted = false;
	private volatile boolean frameIconified = false;
	private volatile boolean replaceActivated = false;
	//private volatile int caretRecoilCount = 0;
	private StringBuilder inputBuffer = new StringBuilder();
	private StringBuilder outputBuffer = new StringBuilder();
	private final Object inputSynchronizer = new Object();
	private final Object outputSynchronizer = new Object();
	//private final Object updateSynchronizer = new Object();
	private MultiWindowTextGUI gui;
	private BasicWindow window;
	private VTLanternaConsolePrintStream printStream;
	private VTLanternaConsoleInputStream inputStream;
	private volatile boolean ignoreClose = false;
	private volatile boolean started = false;
	private VTGraphicalConsolePopupMenu popupMenu;
	private volatile boolean graphical;
	//support command history
	//support echo input
	//support maximum line width in output
	//support special characters in input and output
	//support mouse scroll listener for awtframe
	//support flush interrupted output
	//support custom icon for awtframe
	//support window listener for awtframe
	//support command menubar for awtframe
	//replaced calls to VTGraphicalConsole static methods for VTConsole calls
	//support font options for awtframe
	//support context menu for awtframe
	//support command drag drop for awtframe
	//support keyboard shortcuts
	
	public VTLanternaConsole(boolean graphical)
	{
		this.graphical = graphical;
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
		builderThread.start();
		synchronized (this)
		{
			while (!started)
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
			console.deiconifyFrame();
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
			System.exit(0);
		}
		
		public void windowDeactivated(WindowEvent e)
		{
			console.iconifyFrame();
		}
		
		public void windowDeiconified(WindowEvent e)
		{
			//VTGraphicalConsole.deiconifyFrame();
		}
		
		public void windowIconified(WindowEvent e)
		{
			//VTGraphicalConsole.iconifyFrame();
		}
		
		public void windowOpened(WindowEvent e)
		{
			
		}
	}
	
	private class VTLanternaConsolePrintStream extends PrintStream
	{
		private PrintWriter outWriter;
		
		public VTLanternaConsolePrintStream(VTLanternaConsole console)
		{
			super(new VTGraphicalConsoleNullOutputStream(), false);
			this.outWriter = new PrintWriter(new VTLanternaConsolePrintStreamWriter(console));
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
		
		protected void clearError()
		{
			try
			{
				//super.clearError();
			}
			catch (Throwable t)
			{
				
			}
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
			return super.append(c);
		}
		
		public Writer append(CharSequence csq, int start, int end) throws IOException
		{
			return super.append(csq, start, end);
		}
		
		public Writer append(CharSequence csq) throws IOException
		{
			return super.append(csq);
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
		private volatile byte[] lineBuffer;
		private volatile int readed;
		private VTLanternaConsole console;
		
		public VTLanternaConsoleInputStream(VTLanternaConsole console)
		{
			lineBuffer = new byte[0];
			this.console = console;
		}
		
		public int read() throws IOException
		{
			if (lineBuffer == null || readed >= lineBuffer.length)
			{
				try
				{
					lineBuffer = (console.readLine(true) + "\n").getBytes("UTF-8");
					readed = 0;
				}
				catch (InterruptedException e)
				{
					return -1;
				}
			}
			return lineBuffer[readed++];
		}
		
		public int available() throws IOException
		{
			return lineBuffer.length - readed;
		}
				
		public int read(byte[] b, int off, int len) throws IOException
		{
			int transferred;
			for (transferred = 0; transferred < len; transferred++)
			{
				if (lineBuffer == null || readed >= lineBuffer.length)
				{
					if (transferred == 0)
					{
						try
						{
							lineBuffer = (console.readLine(true) + "\n").getBytes("UTF-8");
							readed = 0;
						}
						catch (InterruptedException e)
						{
							return transferred;
						}
					}
					else
					{
						return transferred;
					}
				}
				b[off + transferred] = lineBuffer[readed++];
			}
			return transferred;
		}
		
		public int read(byte[] b) throws IOException
		{
			return super.read(b);
		}
		
		/* public synchronized void reset() throws IOException { super.reset(); } */
		
		public long skip(long n) throws IOException
		{
			return super.skip(n);
		}
	}
	
	public void build() throws Exception
	{
		AWTTerminalFontConfiguration.setFontScalingFactor(VTGlobalTextStyleManager.FONT_SCALING_FACTOR);
		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		factory.setForceAWTOverSwing(true);
		//factory.addTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
		factory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
		//factory.setTerminalEmulatorDeviceConfiguration(deviceConfiguration)
		factory.setAutoOpenTerminalEmulatorWindow(false);
		if (graphical)
		{
			factory.setPreferTerminalEmulator(true);
		}
		else
		{
			factory.setForceTextTerminal(true);
		}
		//final Scrollbar scrollBar = null;
		
		//factory.setForceTextTerminal(true);
        terminal = factory.createTerminal();
		//ScrollingAWTTerminal terminal = new ScrollingAWTTerminal();
        if (terminal instanceof AWTTerminalFrame)
        {
        	try
        	{
        		Toolkit.getDefaultToolkit().setDynamicLayout(false);
        	}
        	catch (Throwable t)
        	{
        		
        	}
        	awtframe = (AWTTerminalFrame) terminal;
        	//awtframe.setLocationByPlatform(true);
        	try
			{
				awtframe.setIconImage(ImageIO.read(this.getClass().getResourceAsStream("/org/vate/console/graphical/resource/remote.png")));
			}
			catch (Throwable t)
			{
				
			}
        	awtframe.addWindowListener(new VTLanternaConsoleWindowListener(this));
        	if (terminal instanceof AWTTerminalFrame)
        	{
        		awtterminal = ((AWTTerminalFrame) awtframe).getTerminal();
        	}
        	else
        	{
        		awtterminal = null;
        	}
        	awtterminal.addMouseListener(new MouseListener()
        	{        		
				public void mouseClicked(MouseEvent e)
				{
					
				}
				public void mousePressed(MouseEvent e)
				{
					if (e.isPopupTrigger())
					{
						e.consume();
						popupMenu.show(awtterminal, e.getX(), e.getY());
					}
					e.consume();
					int x = e.getX();
					int y = e.getY();
					int fontWidth = awtterminal.getTerminalImplementation().getFontWidth();
					int fontHeight = awtterminal.getTerminalImplementation().getFontHeight();
					TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
					MouseAction mouseAction = new MouseAction(MouseActionType.CLICK_DOWN, e.getButton(), pos);
					awtterminal.addInput(mouseAction);
				}
				public void mouseReleased(MouseEvent e)
				{
					if (e.isPopupTrigger())
					{
						e.consume();
						popupMenu.show(awtterminal, e.getX(), e.getY());
					}
				}
				public void mouseEntered(MouseEvent e)
				{

				}
				public void mouseExited(MouseEvent e) 
				{
					
				}
        	});
        	awtterminal.addMouseMotionListener(new MouseMotionListener()
        	{
				public void mouseDragged(MouseEvent e)
				{
					
				}

				public void mouseMoved(MouseEvent e)
				{
					
				}
        	});
        	awtframe.addMouseWheelListener(new MouseWheelListener()
        	{
				public void mouseWheelMoved(MouseWheelEvent e)
				{
					int x = e.getX();
					int y = e.getY();
					int fontWidth = awtterminal.getTerminalImplementation().getFontWidth();
					int fontHeight = awtterminal.getTerminalImplementation().getFontHeight();
					TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
					int rotation = e.getWheelRotation();
					MouseAction mouseAction = new MouseAction(rotation > 0 ? MouseActionType.SCROLL_DOWN : MouseActionType.SCROLL_UP, e.getButton(), pos);
					outputBox.handleInput(mouseAction);
				}
        	});
        	VTGlobalTextStyleManager.registerWindow(awtframe);
        	VTGlobalTextStyleManager.registerFontList(awtterminal.getTerminalFontConfiguration().getFontPriority());
        	popupMenu = new VTGraphicalConsolePopupMenu(awtframe);
        	awtterminal.setDropTarget(new DropTarget());
        	awtterminal.getDropTarget().setActive(true);
        	awtterminal.getDropTarget().addDropTargetListener(new VTGraphicalConsoleDropTargetListener());
        }
        else
        {
        	awtterminal = null;
        }		
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setForegroundColor(TextColor.ANSI.GREEN);
        screen = new TerminalScreen(terminal);
        //screen.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_4);
        screen.startScreen();

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        //outputBox.setReadOnly(true);
        outputBox.setTerminal(terminal);
        outputBox.setCaretWarp(true);
        if (awtframe != null)
        {
        	final Scrollbar scrollBar = new Scrollbar(Scrollbar.VERTICAL);
        	scrollBar.setUnitIncrement(1);
        	outputBox.setVerticalAdjustable(scrollBar);
        	awtframe.add(scrollBar, java.awt.BorderLayout.EAST);
        	scrollBar.addKeyListener(new KeyListener()
        	{
				public void keyTyped(KeyEvent e)
				{
					awtterminal.dispatchEvent(e);
				}
				public void keyPressed(KeyEvent e)
				{
					awtterminal.dispatchEvent(e);
				}
				public void keyReleased(KeyEvent e)
				{
					awtterminal.dispatchEvent(e);
				}
        	});
//        	scrollBar.addMouseWheelListener(new MouseWheelListener()
//        	{
//				public void mouseWheelMoved(MouseWheelEvent e)
//				{
//					//int x = e.getX();
//					//int y = e.getY();
//					//int fontWidth = awtterminal.getTerminalImplementation().getFontWidth();
//					//int fontHeight = awtterminal.getTerminalImplementation().getFontHeight();
//					TerminalPosition pos = new TerminalPosition(0, 0);
//					int rotation = e.getWheelRotation();
//					MouseAction mouseAction = new MouseAction(rotation > 0 ? MouseActionType.SCROLL_DOWN : MouseActionType.SCROLL_UP, e.getButton(), pos);
//					outputBox.handleInput(mouseAction);
//				}
//        	});
        }
        //outputBox.setEnabled(false);
        ((DefaultTextBoxRenderer) outputBox.getRenderer()).setHideScrollBars(true);
        
        inputBox.setTerminal(terminal);
        inputBox.setHiddenColumn(0);
        ((DefaultTextBoxRenderer) inputBox.getRenderer()).setHideScrollBars(true);
        
        outputBox.setInputFilter(new InputFilter()
		{
			public boolean onInput(Interactable interactable, KeyStroke keyStroke)
			{
				if (keyStroke.getKeyType() == KeyType.MouseEvent)
				{
					MouseAction mouse = (MouseAction)keyStroke;
					TerminalPosition topLeft = outputBox.getTopLeft();
					
					if (mouse.getActionType() == MouseActionType.CLICK_DOWN)
					{
						outputBox.takeFocus();
						outputBox.setCaretPosition(topLeft.getRow() + mouse.getPosition().getRow(), topLeft.getColumn() + mouse.getPosition().getColumn());
						outputBox.invalidate();
						return false;
					}
					
					if (mouse.getActionType() == MouseActionType.SCROLL_UP)
					{
						outputBox.takeFocus();
						outputBox.scrollup();
						outputBox.invalidate();
						return false;
					}
					
					if (mouse.getActionType() == MouseActionType.SCROLL_DOWN)
					{
						outputBox.takeFocus();
						outputBox.scrolldown();
						outputBox.invalidate();
						return false;
					}
				}
				if (keyStroke.getKeyType() == KeyType.Escape)
				{
					//inputBox.takeFocus();
					//return false;
				}
				if (keyStroke.isShiftDown())
				{
					if (keyStroke.getKeyType() == KeyType.Insert)
					{
						VTConsole.pasteText();
						return false;
					}
				}
				if (keyStroke.isCtrlDown())
				{
					if (keyStroke.getKeyType() == KeyType.Insert)
					{
						VTConsole.copyText();
						return false;
					}
					if (keyStroke.getKeyType() == KeyType.PageUp)
					{
						VTGlobalTextStyleManager.increaseFontSize();
						return false;
					}
					if (keyStroke.getKeyType() == KeyType.PageDown)
					{
						VTGlobalTextStyleManager.decreaseFontSize();
						return false;
					}
					if (keyStroke.getKeyType() == KeyType.Home)
					{
						VTGlobalTextStyleManager.defaultFontSize();
						return false;
					}
					if (keyStroke.getKeyType() == KeyType.End)
					{
						if (VTGlobalTextStyleManager.isFontStyleBold())
						{
							VTGlobalTextStyleManager.disableFontStyleBold();
						}
						else
						{
							VTGlobalTextStyleManager.enableFontStyleBold();
						}
						return false;
					}
				}
				if (keyStroke.getKeyType() != KeyType.ArrowDown
				&& keyStroke.getKeyType() != KeyType.ArrowUp
				&& keyStroke.getKeyType() != KeyType.ArrowLeft
				&& keyStroke.getKeyType() != KeyType.ArrowRight
				&& keyStroke.getKeyType() != KeyType.PageDown
				&& keyStroke.getKeyType() != KeyType.PageUp
				&& keyStroke.getKeyType() != KeyType.Home
				&& keyStroke.getKeyType() != KeyType.End)
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
				if (keyStroke.getKeyType() == KeyType.MouseEvent)
				{
					MouseAction mouse = (MouseAction)keyStroke;
					TerminalPosition topLeft = inputBox.getTopLeft();
					
					if (mouse.getActionType() == MouseActionType.CLICK_DOWN)
					{
						int max = inputBox.getLastLine().length();
						int location = topLeft.getColumn();
						int position = Math.min(location + mouse.getPosition().getColumn(), max);
						
						inputBox.takeFocus();
						inputBox.setHiddenColumn(position);
						inputBox.setCaretPosition(position);
						inputBox.invalidate();
						return false;
					}
					
					if (mouse.getActionType() == MouseActionType.SCROLL_UP)
					{
						outputBox.takeFocus();
						outputBox.scrollup();
						outputBox.invalidate();
						return false;
					}
					
					if (mouse.getActionType() == MouseActionType.SCROLL_DOWN)
					{
						outputBox.takeFocus();
						outputBox.scrolldown();
						outputBox.invalidate();
						return false;
					}
					
					//return false;
				}
				if (keyStroke.getKeyType() == KeyType.Enter)
				{
					String command = currentLineBuffer.toString();
					currentLineBuffer.setLength(0);
					//String command = inputBox.getText();
					
					input(command + "\n");
			
					inputBox.setText("");
					inputBox.setHiddenColumn(0);
					inputBox.setCaretPosition(0);
					inputBox.invalidate();
					//terminal.getTerminalSize();
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.PageDown
				|| keyStroke.getKeyType() == KeyType.PageUp)
				{
					if (keyStroke.isCtrlDown())
					{
						if (keyStroke.getKeyType() == KeyType.PageUp)
						{
							VTGlobalTextStyleManager.increaseFontSize();
						}
						else
						{
							VTGlobalTextStyleManager.decreaseFontSize();
						}
						return false;
					}
					outputBox.takeFocus();
					outputBox.handleInput(keyStroke);
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.ArrowUp)
				{
					if (scrollCommandHistoryUp(echoInput))
					{
						return false;
					}
				}
				if (keyStroke.getKeyType() == KeyType.ArrowDown)
				{
					if (scrollCommandHistoryDown(echoInput))
					{
						return false;
					}
				}
				if (keyStroke.getKeyType() == KeyType.Escape)
				{
					//outputBox.output("12345\r678\b\b");
					//outputBox.takeFocus();
					//outputBox.setCarriageColumn(0);
					//toggleEcho();
					//return false;
				}
				if (keyStroke.getKeyType() == KeyType.Insert)
				{
					if (!keyStroke.isAltDown()
					&& !keyStroke.isCtrlDown()
					&& !keyStroke.isShiftDown())
					{
						toggleReplace();
						return false;
					}
					if (!keyStroke.isAltDown()
					&& !keyStroke.isCtrlDown()
					&& keyStroke.isShiftDown())
					{
						VTConsole.pasteText();
						return false;
					}
					if (!keyStroke.isAltDown()
					&& keyStroke.isCtrlDown()
					&& !keyStroke.isShiftDown())
					{
						VTConsole.copyText();
						return false;
					}
				}
				if (keyStroke.getKeyType() == KeyType.Pause)
				{
					toggleFlush();
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.ContextMenu)
				{
					if (popupMenu != null)
					{
						popupMenu.show(awtterminal, 0, 0);
					}
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.ArrowLeft
				|| keyStroke.getKeyType() == KeyType.ArrowRight
				|| keyStroke.getKeyType() == KeyType.Backspace
				|| keyStroke.getKeyType() == KeyType.Delete
				|| keyStroke.getKeyType() == KeyType.Home
				|| keyStroke.getKeyType() == KeyType.End)
				{
					if (keyStroke.getKeyType() == KeyType.Delete
					&& keyStroke.isCtrlDown())
					{
						toggleFlush();
						return false;
					}
					if (keyStroke.getKeyType() == KeyType.Home
					&& keyStroke.isCtrlDown())
					{
						VTGlobalTextStyleManager.defaultFontSize();
						return false;
					}
					if (keyStroke.getKeyType() == KeyType.End
					&& keyStroke.isCtrlDown())
					{
						if (VTGlobalTextStyleManager.isFontStyleBold())
						{
							VTGlobalTextStyleManager.disableFontStyleBold();
						}
						else
						{
							VTGlobalTextStyleManager.enableFontStyleBold();
						}
					}
					inputBox.handleDataInput(currentLineBuffer, keyStroke.getKeyType());
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
				if (keyStroke.getKeyType() == KeyType.Character)
				{
					if (keyStroke.getCharacter() == '\u001A')
					{
						toggleFlush();
						return false;
					}
					if (keyStroke.getCharacter() == '\u001C')
					{
						System.exit(0);
						return false;
					}
					if (keyStroke.getCharacter() == '\u0003')
					{
						System.exit(0);
						return false;
					}
					if (keyStroke.isCtrlDown())
					{
						if (keyStroke.getCharacter() == 'C'
						|| keyStroke.getCharacter() == 'c')
						{
							System.exit(0);
							return false;
						}
						if (keyStroke.getCharacter() == '\\')
						{
							System.exit(0);
							return false;
						}
						if (keyStroke.getCharacter() == 'Z'
						|| keyStroke.getCharacter() == 'z')
						{
							//System.out.println("ctrl+z");
							toggleFlush();
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
        
        Panel bottonPanel = new Panel();
        bottonPanel.setLayoutManager(new BorderLayout());
        
        //bottonPanel.addComponent(promptLabel, BorderLayout.Location.LEFT);
        bottonPanel.addComponent(inputBox, BorderLayout.Location.CENTER);
        
        mainPanel.addComponent(outputBox, BorderLayout.Location.CENTER);
        mainPanel.addComponent(bottonPanel, BorderLayout.Location.BOTTOM);
        
        // Create window to hold the panel
        window = new BasicWindow();
        Collection<Hint> hints = new LinkedList<Hint>();
        hints.add(Hint.FULL_SCREEN);
        hints.add(Hint.NO_DECORATIONS);
        hints.add(Hint.NO_POST_RENDERING);
        
        window.setComponent(mainPanel);
        window.setHints(hints);

        // Create gui and start gui
        gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        
        Properties inputproperties = new Properties();
        inputproperties.load(this.getClass().getResourceAsStream("/input-theme.properties"));
        PropertyTheme inputtheme = new PropertyTheme(inputproperties);
        
        Properties outputproperties = new Properties();
        outputproperties.load(this.getClass().getResourceAsStream("/output-theme.properties"));
        PropertyTheme outputtheme = new PropertyTheme(outputproperties);
        
        inputBox.setTheme(inputtheme);
        outputBox.setTheme(outputtheme);
        
        //gui.setTheme(theme);
        
        inputStream = new VTLanternaConsoleInputStream(this);
        printStream = new VTLanternaConsolePrintStream(this);
        
        setTitle("");
        
        if (awtframe != null)
        {
        	awtframe.setLocationByPlatform(true);
        	awtframe.pack();
        	awtframe.setVisible(true);
        	awtframe.setDefaultTerminalSize(awtframe.getTerminalSize());
        }
        
        synchronized (this)
        {
        	this.started = true;
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
	
	public void iconifyFrame()
	{
		synchronized (outputSynchronizer)
		{
			frameIconified = true;
		}
	}
	
	public void deiconifyFrame()
	{
		synchronized (outputSynchronizer)
		{
			frameIconified = false;
		}
		flush();
	}
	
	public void toggleReplace()
	{
		replaceActivated = !replaceActivated;
		//updateCaretPosition();
	}
	
	public void toggleFlush()
	{
		flushInterrupted = !flushInterrupted;
		flush();
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
		//updateCaretPosition();
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
		if (command == null || command.length() == 0)
		{
			return;
		}
		if (!echoInput)
		{
			return;
		}
		if (!(commandHistory.size() < commandHistoryMaxSize))
		{
			commandHistory.remove(0);
		}
		commandHistory.remove(command.toString());
		commandHistory.add(command.toString());
		commandHistoryPosition = commandHistory.size();
	}
	
	private void restoreCurrentLine(boolean echo)
	{
		// System.out.println("currentLineBuffer.length: " +
		// currentLineBuffer.length());
		currentLineBuffer.append(commandHistory.get(commandHistoryPosition));
		inputBox.setHiddenColumn(currentLineBuffer.length());
		if (echo)
		{
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
			inputBox.setText("");
			inputBox.setCaretPosition(0);
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
			if (commandHistoryPosition < commandHistory.size() - 1)
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
	
	public void appendToPendingInputLine(String line)
	{
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
		
	public Frame getFrame()
	{
		return awtframe;
	}
	
	public void input(String string)
	{
		synchronized (inputSynchronizer)
		{
			if (readingInput)
			{
				if (string.indexOf('\n') == -1)
				{
					inputBox.handleDataInput(currentLineBuffer, string, replaceActivated);
					//inputBox.setHiddenColumn(currentLineBuffer.length());
					if (echoInput)
					{
						inputBox.setText(currentLineBuffer.toString());
						inputBox.setCaretPosition(inputBox.getHiddenColumn());
						inputBox.invalidate();
					}
				}
				else
				{
					int i = 0, j = 0;
					i = string.indexOf('\n');
					inputBuffer.append(string.substring(0, i) + "\n");
					//appendToPendingInputLine(string.substring(0, i));
					j = i + 1;
					i = string.indexOf('\n', j);
					while (i != -1)
					{
						appendToPendingInputLine(string.substring(j, i) + "\n");
						j = i + 1;
						i = string.indexOf('\n', j);
					}
					if (j < string.length())
					{
						appendToPendingInputLine(string.substring(j));
					}
					//System.out.println("inputBuffer:[" + inputBuffer.toString() + "]");
					inputSynchronizer.notifyAll();
				}
			}
			else
			{
				if (string.indexOf('\n') == -1)
				{
					appendToPendingInputLine(string);
				}
				else
				{
					int i = 0, j = 0;
					i = string.indexOf('\n');
					appendToPendingInputLine(string.substring(0, i) + "\n");
					j = i + 1;
					i = string.indexOf('\n', j);
					while (i != -1)
					{
						appendToPendingInputLine(string.substring(j, i) + "\n");
						j = i + 1;
						i = string.indexOf('\n', j);
					}
					if (j < string.length())
					{
						appendToPendingInputLine(string.substring(j));
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
			//System.out.println("readString():[" + data + "]");
			return data;
		}
	}

	public String readLine(boolean echo) throws InterruptedException
	{
		//System.out.println("readLine()");
		inputThread = Thread.currentThread();
		readingInput = true;
		setEchoInput(echo);
		String data = null;
		if (getPendingInputLineSize() > 0 && !getFromPendingInputLine().contains("\n"))
		{
			currentLineBuffer.append(removeFromPendingInputLine());
		}
		else if (getPendingInputLineSize() > 0 && getFromPendingInputLine().contains("\n"))
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
			write(data + "\n");
			flush();
		}
		else
		{
			write("\n");
			flush();
		}
		readingInput = false;
		return data;
	}

	public void interruptReadLine()
	{
		try
		{
			inputThread.interrupt();
		}
		catch (Throwable t)
		{
			
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
			outputBuffer.append(str);
		}
	}

	public void write(char[] buf, int off, int len)
	{
		synchronized (outputSynchronizer)
		{
			outputBuffer.append(buf, off, len);
		}
	}

	public void flush()
	{
		if (!flushInterrupted && !frameIconified)
		{
			synchronized (outputSynchronizer)
			{
				String data = outputBuffer.toString();
				if (data.length() > 0)
				{
					outputBox.output(data);
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
			terminal.bell();
		}
		catch (Throwable e)
		{
			
		}
	}

	public void setTitle(String title)
	{
		if (awtframe != null)
		{
			awtframe.setTitle(title);
		}
		else
		{
			if (Platform.isWindows())
			{
				System.out.print("\u001B]0;" + title + "\u0007");
				VTNativeUtils.system("title " + title);
			}
			else
			{
				// VTNativeUtils.system("echo -n \"\\033]0;" + title + "\\007\"");
				// VTNativeUtils.printf("\u001B]0;" + title + "\u0007");
				// VTNativeUtils.printf("\u001B]1;" + title + "\u0007");
				// VTNativeUtils.printf("\u001B]2;" + title + "\u0007");
				System.out.print("\u001B]0;" + title + "\u0007");
				// System.out.print("\u001B]1;" + title + "\u0007");
				// System.out.print("\u001B]2;" + title + "\u0007");
			}
		}
	}

	public void setColors(int backgroundColor, int foregroundColor)
	{
		// TODO Auto-generated method stub
	}

	public void setBold(boolean bold)
	{
		// TODO Auto-generated method stub
	}

	public void resetAttributes()
	{
		// TODO Auto-generated method stub
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
		System.setErr(printStream);
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

	public void toggleScrollMode()
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
		//System.out.println("selectedText:" + selectedText);
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
		VTConsole.flush();
	}

	public void pasteText()
	{
		try
		{
			Clipboard systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
			if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
			{
				String text = systemClipboard.getData(DataFlavor.stringFlavor).toString();
				VTConsole.input(text);
			}
			else if (systemClipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
			{
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) systemClipboard.getData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0)
				{
					StringBuilder fileList = new StringBuilder();
					for (File file : files)
					{
						fileList.append(file.getAbsolutePath() + ";");
					}
					fileList.deleteCharAt(fileList.length() - 1);
					VTConsole.input(fileList.toString());
				}
			}
		}
		catch (UnsupportedFlavorException e1)
		{
			
		}
		catch (IOException e1)
		{
			
		}
		catch (Throwable e1)
		{
			
		}
	}

	public String getSelectedText()
	{
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
		if (outputBox.isFocused())
		{
			try
			{
				int number = outputBox.getCaretPosition().getRow();
				return outputBox.getLine(number);
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
}
