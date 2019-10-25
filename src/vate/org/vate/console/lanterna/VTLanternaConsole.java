package org.vate.console.lanterna;

import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Collection;
import java.util.LinkedList;
import java.util.Locale;
import java.util.Properties;
import org.vate.console.VTConsoleImplementation;
import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.PropertyTheme;
import com.googlecode.lanterna.gui2.BasicWindow;
import com.googlecode.lanterna.gui2.BorderLayout;
import com.googlecode.lanterna.gui2.DefaultWindowManager;
import com.googlecode.lanterna.gui2.EmptySpace;
import com.googlecode.lanterna.gui2.InputFilter;
import com.googlecode.lanterna.gui2.Interactable;
import com.googlecode.lanterna.gui2.Label;
import com.googlecode.lanterna.gui2.MultiWindowTextGUI;
import com.googlecode.lanterna.gui2.Panel;
import com.googlecode.lanterna.gui2.TextBox.DefaultTextBoxRenderer;
import com.googlecode.lanterna.gui2.TextBox.Style;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TabBehaviour;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;

public class VTLanternaConsole implements VTConsoleImplementation
{
	public VTLanternaConsole() throws IOException
	{
		DefaultTerminalFactory factory = new DefaultTerminalFactory();
		factory.setForceAWTOverSwing(true);
		//factory.addTerminalEmulatorFrameAutoCloseTrigger(TerminalEmulatorAutoCloseTrigger.CloseOnExitPrivateMode);
		//factory.setMouseCaptureMode(MouseCaptureMode.CLICK_RELEASE_DRAG_MOVE);
		factory.setPreferTerminalEmulator(true);
		//factory.setForceTextTerminal(true);
        Terminal terminal = factory.createTerminal();
        terminal.setBackgroundColor(TextColor.ANSI.BLACK);
        terminal.setForegroundColor(TextColor.ANSI.GREEN);
        Screen screen = new TerminalScreen(terminal);
        //screen.setTabBehaviour(TabBehaviour.ALIGN_TO_COLUMN_4);
        screen.startScreen();

        // Create panel to hold components
        Panel mainPanel = new Panel();
        mainPanel.setLayoutManager(new BorderLayout());

        final VTLanternaOutputTextBox outputBox = new VTLanternaOutputTextBox(new TerminalSize(80, 24), "", Style.MULTI_LINE, 300);
        //outputBox.setReadOnly(true);
        outputBox.setCaretWarp(true);
        ((DefaultTextBoxRenderer) outputBox.getRenderer()).setHideScrollBars(true);
        //outputBox.setEnabled(false);
        
        //final Label promptLabel = new Label(">");
        //promptLabel.setBackgroundColor(TextColor.ANSI.BLACK);
        
        final VTLanternaOutputTextBox commandBox = new VTLanternaOutputTextBox(new TerminalSize(80, 1), "", Style.SINGLE_LINE, 2);
        ((DefaultTextBoxRenderer) commandBox.getRenderer()).setHideScrollBars(true);
        
        outputBox.setInputFilter(new InputFilter()
		{
			public boolean onInput(Interactable interactable, KeyStroke keyStroke)
			{
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
//				if (keyStroke.getKeyType() == KeyType.Escape)
//				{
//					commandBox.takeFocus();
//					return false;
//				}
				return true;
			}
		});
        
        commandBox.setInputFilter(new InputFilter()
        {
			public boolean onInput(Interactable interactable, KeyStroke keyStroke)
			{
				if (keyStroke.getKeyType() == KeyType.Enter)
				{
					String command = commandBox.getText();
					//System.out.println("line:[" + command + "]");
					commandBox.setText("");
					//outputBox.addLine(command + "\t" + command + "\t");
					outputBox.addLine(command);
					return false;
				}
//				if (keyStroke.getKeyType() == KeyType.Tab)
//				{
//					commandBox.handleKeyStroke(keyStroke);
//					return false;
//				}
//				if (keyStroke.getKeyType() == KeyType.Escape)
//				{
//					outputBox.takeFocus();
//					return false;
//				}
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
        Collection<com.googlecode.lanterna.gui2.Window.Hint> hints = new LinkedList<com.googlecode.lanterna.gui2.Window.Hint>();
        hints.add(com.googlecode.lanterna.gui2.Window.Hint.FULL_SCREEN);
        hints.add(com.googlecode.lanterna.gui2.Window.Hint.NO_DECORATIONS);
        hints.add(com.googlecode.lanterna.gui2.Window.Hint.FIT_TERMINAL_WINDOW);
        
        window.setComponent(mainPanel);
        window.setHints(hints);

        // Create gui and start gui
        MultiWindowTextGUI gui = new MultiWindowTextGUI(screen, new DefaultWindowManager(), new EmptySpace(TextColor.ANSI.BLACK));
        Properties properties = new Properties();
        properties.load(this.getClass().getResourceAsStream("/default-theme.properties"));
        PropertyTheme theme = new PropertyTheme(properties);
        gui.setTheme(theme);
        gui.addWindowAndWait(window);
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
