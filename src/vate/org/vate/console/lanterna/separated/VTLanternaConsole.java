package org.vate.console.lanterna.separated;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Locale;
import java.util.Properties;

import org.vate.VT;
import org.vate.console.VTConsoleImplementation;
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
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.MouseCaptureMode;
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;

public class VTLanternaConsole implements VTConsoleImplementation
{
	private volatile Frame awtframe;
	private Terminal terminal;
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
	private static Object inputSynchronizer = new Object();
	private static Object outputSynchronizer = new Object();
	private MultiWindowTextGUI gui;
	private BasicWindow window;
	
	//support command history
	//support echo input
	//support support for maximum line width in output
	//support special characters in input and output
	//support mouse scroll listener for awtframe
	//support flush interrupted output
	//TODO:support keyboard shortcuts
	//TODO:support window listener for awtframe
	//TODO:support custom icon for awtframe
	//TODO:support font options for awtframe
	//TODO:support command drag drop for awtframe
	//TODO:support context menu for awtframe
	//TODO:support command menubar for awtframe
	
	public VTLanternaConsole()
	{
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
					
				}
			}
		};
		builderThread.start();
	}
	
	public void build() throws Exception
	{
		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		factory.setForceAWTOverSwing(true);
		//factory.addTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
		factory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
		factory.setPreferTerminalEmulator(true);
		//factory.setForceTextTerminal(true);
        terminal = factory.createTerminal();
		//ScrollingAWTTerminal terminal = new ScrollingAWTTerminal();
        if (terminal instanceof Frame)
        {
        	try
        	{
        		Toolkit.getDefaultToolkit().setDynamicLayout(false);
        	}
        	catch (Throwable t)
        	{
        		
        	}
        	awtframe = (Frame) terminal;
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
					int x = e.getX();
					int y = e.getY();
					int fontWidth = awtterminal.getTerminalImplementation().getFontWidth();
					int fontHeight = awtterminal.getTerminalImplementation().getFontHeight();
					TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
					MouseAction mouseAction = new MouseAction(MouseActionType.CLICK_DOWN, e.getButton(), pos);
					awtterminal.addInput(mouseAction);
				}
				public void mousePressed(MouseEvent e)
				{
					
				}
				public void mouseReleased(MouseEvent e)
				{
					
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
        	awtterminal.addMouseWheelListener(new MouseWheelListener()
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
					awtterminal.addInput(mouseAction);
				}
        	});
//        	awtframe.addMouseWheelListener(new MouseWheelListener()
//        	{
//				public void mouseWheelMoved(MouseWheelEvent e)
//				{
//					outputBox.takeFocus();
//					int x = e.getX();
//					int y = e.getY();
//					int fontWidth = awtterminal.getTerminalImplementation().getFontWidth();
//					int fontHeight = awtterminal.getTerminalImplementation().getFontHeight();
//					TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
//					int rotation = e.getWheelRotation();
//					MouseAction mouseAction = new MouseAction(rotation > 0 ? MouseActionType.SCROLL_DOWN : MouseActionType.SCROLL_UP, e.getButton(), pos);
//					awtterminal.addInput(mouseAction);
//				}
//        	});
        }
        else
        {
        	awtterminal = null;
        }		
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setForegroundColor(TextColor.ANSI.GREEN);
        Screen screen = new TerminalScreen(terminal);
        //screen.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_4);
        screen.startScreen();

        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        //outputBox.setReadOnly(true);
        outputBox.setTerminal(terminal);
        outputBox.setCaretWarp(true);
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
					registerCurrentLine();
					currentLineBuffer.setLength(0);
					
					//System.out.println("command:[" + command + "]");
					input(command + "\n");
			
					inputBox.setHiddenColumn(0);
					inputBox.setText("");
					inputBox.setCaretPosition(0);
					inputBox.invalidate();
					//terminal.getTerminalSize();
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.PageDown
				|| keyStroke.getKeyType() == KeyType.PageUp)
				{
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
					if (keyStroke.isAltDown()
					|| keyStroke.isCtrlDown()
					|| keyStroke.isShiftDown())
					{
						return true;
					}
					toggleReplace();
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.ArrowLeft
				|| keyStroke.getKeyType() == KeyType.ArrowRight
				|| keyStroke.getKeyType() == KeyType.Backspace
				|| keyStroke.getKeyType() == KeyType.Delete)
				{
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
					inputBox.handleDataInput(currentLineBuffer, keyStroke.getCharacter(), replaceActivated);
					if (echoInput)
					{
						inputBox.setText(currentLineBuffer.toString());
						inputBox.setCaretPosition(inputBox.getHiddenColumn());
						inputBox.invalidate();
						return false;
					}
					else
					{
						return false;
					}
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
        
        setTitle("Variable-Terminal " + VT.VT_VERSION + " - Console");
        
        if (awtframe != null)
        {
        	awtframe.setVisible(true);
        	awtframe.pack();
        	//awtframe.getInsets().set(4, 4, 4, 4);
        	//awtframe.pack();
        }
        
        gui.addWindowAndWait(window);
	}
	
	public void start()
	{
		Thread startThread = new Thread()
		{
			public void run()
			{
				gui.addWindowAndWait(window);
			}
		};
		startThread.start();
	}
	
	public void toggleReplace()
	{
		replaceActivated = !replaceActivated;
		//updateCaretPosition();
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
	
	private void registerCurrentLine()
	{
		if (!echoInput)
		{
			return;
		}
		if (currentLineBuffer.length() > 0)
		{
			if (!(commandHistory.size() < commandHistoryMaxSize))
			{
				commandHistory.remove(0);
			}
			commandHistory.remove(currentLineBuffer.toString());
			commandHistory.add(currentLineBuffer.toString());
		}
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
	
	public void resumeOutputFlush()
	{
		if (!flushInterrupted)
		{
			flush();
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
					currentLineBuffer.append(string);
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
		echoInput = echo;
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
			write(data + "\n");
			flush();
		}
		else
		{
			write("\n");
			flush();
		}
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
		// TODO Auto-generated method stub
	}

	public void println(String str)
	{
		// TODO Auto-generated method stub
	}

	public void printf(String format, Object... args)
	{
		// TODO Auto-generated method stub
	}

	public void printfln(String format, Object... args)
	{
		// TODO Auto-generated method stub
	}

	public void printf(Locale l, String format, Object... args)
	{
		// TODO Auto-generated method stub
	}

	public void printfln(Locale l, String format, Object... args)
	{
		// TODO Auto-generated method stub
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
				outputBox.output(data);
				outputBuffer.setLength(0);
			}
		}
	}

	public void clear()
	{
		outputBox.setText("");
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
			//TODO:use unix and windows console skills
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
		// TODO Auto-generated method stub
	}

	public void setSystemOut()
	{
		// TODO Auto-generated method stub
	}

	public void setSystemErr()
	{
		// TODO Auto-generated method stub
	}

	public InputStream getSystemIn()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public PrintStream getSystemOut()
	{
		// TODO Auto-generated method stub
		return null;
	}

	public PrintStream getSystemErr()
	{
		// TODO Auto-generated method stub
		return null;
	}
	
	public static void main(String[] args) throws Throwable
	{
		final VTLanternaConsole console = new VTLanternaConsole();
		while (true)
		{
			String line = console.readLine(true);
			System.out.println("line:[" + line + "]");
		}
	}
}
