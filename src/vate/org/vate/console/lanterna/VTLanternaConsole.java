package org.vate.console.lanterna;

import java.awt.Frame;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.io.IOException;
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
import com.googlecode.lanterna.terminal.Terminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminal;
import com.googlecode.lanterna.terminal.swing.AWTTerminalFrame;

public class VTLanternaConsole implements VTConsoleImplementation
{
	private volatile Frame awtframe;
	private final AWTTerminal awtterminal;
	private final VTLanternaOutputTextBox outputBox = new VTLanternaOutputTextBox(new TerminalSize(80, 24), "", Style.MULTI_LINE, 200);
	private final VTLanternaOutputTextBox commandBox = new VTLanternaOutputTextBox(new TerminalSize(80, 1), "", Style.SINGLE_LINE, 1);
	private static final int commandHistoryMaxSize = 50;
	private List<String> commandHistory = new LinkedList<String>();
	private volatile int commandHistoryPosition;
	private List<String> inputLineBuffer = new LinkedList<String>();
	private StringBuilder currentLineBuffer = new StringBuilder("");
	private volatile Thread currentThread;
	//private volatile boolean readingLine = false;
	private volatile boolean echoInput = true;
	private volatile boolean readingInput = false;
	private volatile boolean updatingTerminal = false;
	private volatile boolean flushInterrupted = false;
	private volatile boolean frameIconified = false;
	private volatile boolean replaceActivated = false;
	private volatile int caretRecoilCount = 0;
	private StringBuilder inputBuffer = new StringBuilder();
	private StringBuilder outputBuffer = new StringBuilder();
	
	//added command history
	//added echo input
	//added support for maximum line width in output
	//TODO:add support for special characters in input and output
	//TODO:add support for flush interrupted output
	//TODO:add support for keyboard shortcuts
	//TODO:add support for window listener for awtframe
	//TODO:add support for custom icon for awtframe
	//TODO:add support for mouse scroll listener for awtframe
	//TODO:add support for font options for awtframe
	//TODO:add support for command drag drop for awtframe
	//TODO:add support for context menu for awtframe
	//TODO:add support for command menubar for awtframe
	
	public VTLanternaConsole() throws IOException
	{
		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		factory.setForceAWTOverSwing(true);
		//factory.addTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
		//factory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
		factory.setPreferTerminalEmulator(true);
		//factory.setForceTextTerminal(true);
        final Terminal terminal = factory.createTerminal();
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
					//System.out.println("mouseClicked:" + e.getX() + "," + e.getY());
					
					//System.out.println("MouseAction.CLICK_DOWN:" + pos.getColumn() + "," + pos.getRow());
					//awtterminal.getTerminalImplementation().setCursorPosition(pos);
				}

				public void mousePressed(MouseEvent e)
				{
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
					int x = e.getX();
					int y = e.getY();
					int fontWidth = awtterminal.getTerminalImplementation().getFontWidth();
					int fontHeight = awtterminal.getTerminalImplementation().getFontHeight();
					TerminalPosition pos = new TerminalPosition(x / fontWidth, y / fontHeight);
					MouseAction mouseAction = new MouseAction(MouseActionType.CLICK_RELEASE, e.getButton(), pos);
					awtterminal.addInput(mouseAction);
				}

				public void mouseEntered(MouseEvent e)
				{
					// TODO Auto-generated method stub
				}

				public void mouseExited(MouseEvent e) 
				{
					// TODO Auto-generated method stub
				}
        	});
        	awtterminal.addMouseMotionListener(new MouseMotionListener()
        	{
				public void mouseDragged(MouseEvent e)
				{
					// TODO Auto-generated method stub
				}

				public void mouseMoved(MouseEvent e)
				{
					// TODO Auto-generated method stub
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
					//System.out.println("rotation:" + rotation);
					MouseAction mouseAction = new MouseAction(rotation > 0 ? MouseActionType.SCROLL_DOWN : MouseActionType.SCROLL_UP, e.getButton(), pos);
					awtterminal.addInput(mouseAction);
				}
        	});
        }
        else
        {
        	awtterminal = null;
        }
		
//		awtframe = new Frame();
//		awtframe.add(terminal, java.awt.BorderLayout.CENTER);
//		if (awtframe != null)
//        {
//        	awtframe.setVisible(true);
//        	awtframe.pack();
//        }
		
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setForegroundColor(TextColor.ANSI.GREEN);
        Screen screen = new TerminalScreen(terminal);
        //screen.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_4);
        screen.startScreen();

        // Create panel to hold components
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        //outputBox.setReadOnly(true);
        outputBox.setTerminal(terminal);
        //outputBox.setCaretWarp(true);
        ((DefaultTextBoxRenderer) outputBox.getRenderer()).setHideScrollBars(true);
        
        commandBox.setTerminal(terminal);
        commandBox.setHiddenColumn(0);
        ((DefaultTextBoxRenderer) commandBox.getRenderer()).setHideScrollBars(true);
        
        outputBox.setInputFilter(new InputFilter()
		{
			public boolean onInput(Interactable interactable, KeyStroke keyStroke)
			{
				if (keyStroke.getKeyType() == KeyType.MouseEvent)
				{
					outputBox.takeFocus();
					MouseAction mouse = (MouseAction)keyStroke;
					TerminalPosition topLeft = outputBox.getTopLeft();
					
					outputBox.setCaretPosition(topLeft.getRow() + mouse.getPosition().getRow(), topLeft.getColumn() + mouse.getPosition().getColumn());
					outputBox.invalidate();
					
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.Escape)
				{
					commandBox.takeFocus();
					return false;
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
					commandBox.takeFocus();
					commandBox.handleInput(keyStroke);
					return false;
				}
				return true;
			}
		});
        
        commandBox.setInputFilter(new InputFilter()
        {
			public boolean onInput(Interactable interactable, KeyStroke keyStroke)
			{
				if (keyStroke.getKeyType() == KeyType.MouseEvent)
				{
					MouseAction mouse = (MouseAction)keyStroke;
					TerminalPosition topLeft = commandBox.getTopLeft();
					
					int max = commandBox.getLastLine().length();
					int location = topLeft.getColumn();
					int position = Math.min(location + mouse.getPosition().getColumn(), max);
					
					commandBox.takeFocus();
					commandBox.setHiddenColumn(position);
					commandBox.setCaretPosition(position);
					commandBox.invalidate();
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.Enter)
				{
					String command = currentLineBuffer.toString();
					if (echoInput)
					{
						outputBox.output(command + "\n");
					}
					else
					{
						outputBox.output("\n");
					}
					registerCurrentLine();
					currentLineBuffer.setLength(0);
					commandBox.setHiddenColumn(0);
					commandBox.setText("");
					commandBox.setCaretPosition(0);
					commandBox.invalidate();
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
					scrollCommandHistoryUp(echoInput);
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.ArrowDown)
				{
					scrollCommandHistoryDown(echoInput);
					return false;
				}
				if (keyStroke.getKeyType() == KeyType.Escape)
				{
					//outputBox.takeFocus();
					//outputBox.setCarriageColumn(0);
					toggleEcho();
					return false;
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
				|| keyStroke.getKeyType() == KeyType.Delete
				)
				{
					commandBox.handleDataInput(currentLineBuffer, keyStroke.getKeyType());
					if (!echoInput)
					{
						return false;
					}
					else
					{
						commandBox.setText(currentLineBuffer.toString());
						commandBox.setCaretPosition(commandBox.getHiddenColumn());
						commandBox.invalidate();
						return false;
					}
				}
				if (keyStroke.getKeyType() == KeyType.Character)
				{
					commandBox.handleDataInput(currentLineBuffer, keyStroke.getCharacter(), replaceActivated);
					if (echoInput)
					{
						commandBox.setText(currentLineBuffer.toString());
						commandBox.setCaretPosition(commandBox.getHiddenColumn());
						commandBox.invalidate();
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
        bottonPanel.addComponent(commandBox, BorderLayout.Location.CENTER);
        
        mainPanel.addComponent(outputBox, BorderLayout.Location.CENTER);
        mainPanel.addComponent(bottonPanel, BorderLayout.Location.BOTTOM);
        
        // Create window to hold the panel
        BasicWindow window = new BasicWindow();
        Collection<Hint> hints = new LinkedList<Hint>();
        hints.add(Hint.FULL_SCREEN);
        hints.add(Hint.NO_DECORATIONS);
        hints.add(Hint.NO_POST_RENDERING);
        
        window.setComponent(mainPanel);
        window.setHints(hints);

        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/default-theme.properties"));
        PropertyTheme theme = new PropertyTheme(properties);
        
        gui.setTheme(theme);
        
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
			commandBox.setText(currentLineBuffer.toString());
			commandBox.setCaretPosition(commandBox.getHiddenColumn());
			commandBox.invalidate();
		}
		else
		{
			commandBox.setText("");
			commandBox.setCaretPosition(0);
			commandBox.invalidate();
		}
		//updateCaretPosition();
	}
	
	public void setEchoInput(boolean echo)
	{
		echoInput = echo;
		if (echoInput)
		{
			commandBox.setText(currentLineBuffer.toString());
			commandBox.setCaretPosition(commandBox.getHiddenColumn());
			commandBox.invalidate();
		}
		else
		{
			commandBox.setText("");
			commandBox.setCaretPosition(0);
			commandBox.invalidate();
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
	
	private void restoreCurrentLine(boolean echo)
	{
		// System.out.println("currentLineBuffer.length: " +
		// currentLineBuffer.length());
		currentLineBuffer.append(commandHistory.get(commandHistoryPosition));
		commandBox.setHiddenColumn(currentLineBuffer.length());
		if (echo)
		{
			commandBox.setText(currentLineBuffer.toString());
			commandBox.setCaretPosition(commandBox.getHiddenColumn());
			commandBox.invalidate();
			/* if (currentLineBuffer.length() >
			 * VTGraphicalConsole.maxTerminalCharactersCount) {
			 * currentLineBuffer = currentLineBuffer.delete(0,
			 * currentLineBuffer.length() -
			 * VTGraphicalConsole.maxTerminalCharactersCount); } */
			//VTGraphicalConsoleWriter.write(commandHistory.get(commandHistoryPosition));
			//VTGraphicalConsoleWriter.flush();
		}
		// VTGraphicalConsole.flush();
		// VTGraphicalConsole.input(commandHistory.get(commandHistoryPosition));
	}
	
	private void resetCurrentLine(boolean echo)
	{
		// System.out.println("currentLineBuffer.length: " +
		// currentLineBuffer.length());
		/* for (int i = 0;i < currentLineBuffer.length();i++) {
		 * VTGraphicalConsole.input('\b'); } */
		currentLineBuffer.setLength(0);
		commandBox.setHiddenColumn(0);
		if (echo)
		{
			commandBox.setText("");
			commandBox.setCaretPosition(0);
			commandBox.invalidate();
		}
		// currentLineBuffer.trimToSize();
	}
	
	private void scrollCommandHistoryUp(boolean echo)
	{
		// System.out.println("scrollCommandHistoryUp!");
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
	
	private void scrollCommandHistoryDown(boolean echo)
	{
		// System.out.println("scrollCommandHistoryDown!");
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
		//TODO:
		if (echoInput && !flushInterrupted)
		{
			//if (checkCurrentLineForReplace())
			//{
				//VTGraphicalConsole.backspace(currentLineBuffer.length(), true);
			//}
			//VTGraphicalConsoleWriter.write(currentLineBuffer.toString());
			//VTGraphicalConsole.flushBuffered(false);
		}
	}
	
	public Frame getFrame()
	{
		return awtframe;
	}

	public String readLine(boolean echo) throws InterruptedException
	{
		// TODO Auto-generated method stub
		return null;
	}

	public void interruptReadLine()
	{
		// TODO Auto-generated method stub
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
		// TODO Auto-generated method stub
	}

	public void write(char[] buf, int off, int len)
	{
		// TODO Auto-generated method stub
	}

	public void flush()
	{
		// TODO Auto-generated method stub
	}

	public void clear()
	{
		// TODO Auto-generated method stub
	}

	public void bell()
	{
		// TODO Auto-generated method stub
	}

	public void setTitle(String title)
	{
		if (awtframe != null)
		{
			//System.out.println("awtframe");
			awtframe.setTitle(title);
		}
		else
		{
			//use unix and windows console skills
		}
		// TODO Auto-generated method stub
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
	
	public static void main(String[] args) throws IOException
	{
		new VTLanternaConsole();
	}
}
