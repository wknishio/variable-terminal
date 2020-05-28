package org.vate.console;

import java.awt.EventQueue;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.console.lanterna.separated.VTLanternaConsole;
import org.vate.console.standard.VTStandardConsole;
import org.vate.nativeutils.VTNativeUtils;

public final class VTConsoleInstance
{
	// private static boolean initialized;
	public static final int VT_CONSOLE_COLOR_NORMAL_BLACK = 0;
	public static final int VT_CONSOLE_COLOR_NORMAL_RED = 1;
	public static final int VT_CONSOLE_COLOR_NORMAL_GREEN = 2;
	public static final int VT_CONSOLE_COLOR_NORMAL_YELLOW = 3;
	public static final int VT_CONSOLE_COLOR_NORMAL_BLUE = 4;
	public static final int VT_CONSOLE_COLOR_NORMAL_MAGENTA = 5;
	public static final int VT_CONSOLE_COLOR_NORMAL_CYAN = 6;
	public static final int VT_CONSOLE_COLOR_NORMAL_WHITE = 7;
	public static final int VT_CONSOLE_COLOR_LIGHT_RED = 11;
	public static final int VT_CONSOLE_COLOR_LIGHT_GREEN = 12;
	public static final int VT_CONSOLE_COLOR_LIGHT_YELLOW = 13;
	public static final int VT_CONSOLE_COLOR_LIGHT_BLUE = 14;
	public static final int VT_CONSOLE_COLOR_LIGHT_MAGENTA = 15;
	public static final int VT_CONSOLE_COLOR_LIGHT_CYAN = 16;
	public static final int VT_CONSOLE_COLOR_LIGHT_WHITE = 17;
	public static final int VT_CONSOLE_COLOR_DEFAULT = 9;
	
	private final boolean lanterna = true;
	private boolean graphical = true;
	private boolean daemon;
	private boolean remoteIcon;
	// private static boolean split;
	private volatile VTConsoleImplementation console;
	
	private Object synchronizationObject = new Object();
	
	static
	{
		VTNativeUtils.initialize();
	}
	
	public Object getSynchronizationObject()
	{
		return synchronizationObject;
	}
	
	public synchronized void initialize()
	{
		if (console == null)
		{
			if (!daemon)
			{
				setGraphical(true);
				if (graphical)
				{
					// VTGraphicalConsole.setSplit(split);
					VTNativeUtils.detachConsole();
					if (lanterna)
					{
						console = new VTLanternaConsole(graphical, remoteIcon);
					}
					else
					{
						console = VTGraphicalConsole.getInstance(remoteIcon);
					}
					//console = VTGraphicalConsole.getInstance();
					
					// setBold(true);
					// VTGraphicalConsole.setTitle(title);
					// initialized = true;
				}
				else
				{
					console = VTStandardConsole.getInstance();
					console.setRemoteIcon(remoteIcon);
					resetAttributes();
					setColors(VT_CONSOLE_COLOR_LIGHT_GREEN, VT_CONSOLE_COLOR_NORMAL_BLACK);
//					if (lanterna)
//					{
//						if (Platform.isWindows())
//						{
//							console = VTStandardConsole.getInstance();
//							resetAttributes();
//							setColors(VT_CONSOLE_COLOR_LIGHT_GREEN, VT_CONSOLE_COLOR_NORMAL_BLACK);
//						}
//						else
//						{
//							console = new VTLanternaConsole(graphical);
//						}
//					}
//					else
//					{
//						console = VTStandardConsole.getInstance();
//						resetAttributes();
//						setColors(VT_CONSOLE_COLOR_LIGHT_GREEN, VT_CONSOLE_COLOR_NORMAL_BLACK);
//					}
				}
			}
			else
			{
				VTNativeUtils.detachConsole();
			}
		}
	}
	
	public synchronized boolean isGraphical()
	{
		return graphical;
	}
	
	public synchronized void setLanterna(boolean lanterna)
	{
		//this.lanterna = lanterna;
	}
	
	public synchronized void setGraphical(boolean graphical)
	{
		if (console == null)
		{
			this.graphical = graphical;
			if (GraphicsEnvironment.isHeadless())
			{
				this.graphical = false;
			}
			else
			{
				if (!graphical)
				{
					try
					{
						Class.forName("java.io.Console");
						if (System.console() == null)
						{
							// VTTerminal.graphical = true;
							// File in = new File(FileDescriptor.in);
							try
							{
								if (FileDescriptor.in.valid())
								{
									FileDescriptor.in.sync();
								}
								else
								{
									this.graphical = true;
								}
							}
							catch (Throwable e)
							{
								this.graphical = true;
							}
						}
					}
					catch (Throwable e)
					{
						try
						{
							if (FileDescriptor.in.valid())
							{
								FileDescriptor.in.sync();
							}
							else
							{
								this.graphical = true;
							}
						}
						catch (Throwable e2)
						{
							this.graphical = true;
						}
					}
				}
			}
		}
	}
	
	public synchronized boolean isDaemon()
	{
		return daemon;
	}
	
	private final class HideGraphicalConsole implements Runnable
	{
		public void run()
		{
			try
			{
				getFrame().setVisible(false);
			}
			catch (Throwable t)
			{
				//t.printStackTrace();
			}
		}
	}
	
	private final class ShowGraphicalConsole implements Runnable
	{
		public void run()
		{
			try
			{
				getFrame().setVisible(true);
				Object waiter = getSynchronizationObject();
				synchronized (waiter)
				{
					waiter.notifyAll();
				}
			}
			catch (Throwable t)
			{
				//t.printStackTrace();
			}
		}
	}
	
	private final HideGraphicalConsole daemonizeThread = new HideGraphicalConsole();
	private final ShowGraphicalConsole undaemonizeThread = new ShowGraphicalConsole();
	
	public synchronized void setDaemon(boolean daemon)
	{
		if (this.daemon == daemon)
		{
			return;
		}
		if (console == null)
		{
			this.daemon = daemon;
		}
		else
		{
			if (isGraphical())
			{
				if (daemon == true)
				{
					this.daemon = daemon;
					EventQueue.invokeLater(daemonizeThread);
				}
				else
				{
					this.daemon = daemon;
					EventQueue.invokeLater(undaemonizeThread);
				}
			}
			else
			{
				if (daemon == true)
				{
					this.daemon = daemon;
					VTNativeUtils.hideConsole();
				}
				else
				{
					this.daemon = daemon;
					VTNativeUtils.attachConsole();
					Object waiter = this.getSynchronizationObject();
					synchronized (waiter)
					{
						waiter.notifyAll();
					}
					//if using a tty console, it may be unable to reattach
				}
			}
		}
	}
	
	/* public synchronized static boolean isSplit() { return split; } */
	
	/* public synchronized static void setSplit(boolean split) { if (terminal ==
	 * null) { VTTerminal.split = split; } } */
	
	public void setTitle(String title)
	{
		if (checkConsole())
		{
			console.setTitle(title);
			console.clear();
		}
	}
	
	public String readLine() throws InterruptedException
	{
		return readLine(true);
	}
	
	public String readLine(boolean echo) throws InterruptedException
	{
		if (checkConsole())
		{
			if (!daemon)
			{
				try
				{
					return console.readLine(echo);
				}
				catch (Throwable t)
				{
					return "";
				}
			}
			else
			{
				return "";
			}
		}
		return null;
	}
	
	public void interruptReadLine()
	{
		if (checkConsole())
		{
			if (!daemon)
			{
				console.interruptReadLine();
			}
			else
			{
				
			}
		}
	}
	
	// public static boolean isReadingLine()
	// {
	// if (!daemon)
	// {
	// if (console == null)
	// {
	// initialize();
	// }
	// return console.isReadingLine();
	// }
	// else
	// {
	// return false;
	// }
	// }
	
	public void print(String str)
	{
		if (checkConsole())
		{
			console.print(str);
		}
	}
	
	public void println(String str)
	{
		if (checkConsole())
		{
			console.println(str);
		}
	}
	
	public void printf(String format, Object... args)
	{
		if (checkConsole())
		{
			console.printf(format, args);
		}
	}
	
	public void printfln(String format, Object... args)
	{
		if (checkConsole())
		{
			console.printfln(format, args);
		}
	}
	
	public void printf(Locale l, String format, Object... args)
	{
		if (checkConsole())
		{
			console.printf(l, format, args);
		}
	}
	
	public void printfln(Locale l, String format, Object... args)
	{
		if (checkConsole())
		{
			console.printfln(l, format, args);
		}
	}
	
	public void write(String str)
	{
		if (checkConsole())
		{
			console.write(str);
		}
	}
	
	public void write(char[] cbuf, int off, int len)
	{
		if (checkConsole())
		{
			console.write(cbuf, off, len);
		}
	}
	
	/* public static void write(byte[] buf, int off, int len) {
	 * terminal.trueWrite(buf, off, len); } */
	
	public void flush()
	{
		if (checkConsole())
		{
			console.flush();
		}
	}
	
	public void clear()
	{
		if (checkConsole())
		{
			console.clear();
		}
	}
	
	public void bell()
	{
		try
		{
			if (!daemon)
			{
				if (checkConsole())
				{
					console.bell();
				}
			}
			else
			{
				if (graphical)
				{
					VTGraphicalConsole.staticBell();
				}
				else
				{
					System.out.print("\u0007");
				}
			}
		}
		catch (Throwable e)
		{
			
		}
	}
	
	public void setColors(int foregroundColor, int backgroundColor)
	{
		if (checkConsole())
		{
			console.setColors(foregroundColor, backgroundColor);
			console.clear();
		}
	}
	
	public void setBold(boolean bold)
	{
		if (checkConsole())
		{
			console.setBold(bold);
		}
	}
	
	public void resetAttributes()
	{
		if (checkConsole())
		{
			console.resetAttributes();
		}
	}
	
	public void setSystemIn()
	{
		if (checkConsole())
		{
			console.setSystemIn();
		}
	}
	
	public void setSystemOut()
	{
		if (checkConsole())
		{
			console.setSystemOut();
		}
	}
	
	public void setSystemErr()
	{
		if (checkConsole())
		{
			console.setSystemErr();
		}
	}
	
	public InputStream getSystemIn()
	{
		if (checkConsole())
		{
			return console.getSystemIn();
		}
		return null;
	}
	
	public PrintStream getSystemOut()
	{
		if (checkConsole())
		{
			return console.getSystemOut();
		}
		return null;
	}
	
	public PrintStream getSystemErr()
	{
		if (checkConsole())
		{
			return console.getSystemErr();
		}
		return null;
	}
	
	public void createInterruptibleReadline(final boolean echo, final Runnable interrupt)
	{
		Thread thread = new Thread("VTConsole")
		{
			public void run()
			{
				try
				{
					String line = readLine(echo);
					if (line != null)
					{
						interrupt.run();
						return;
					}
				}
				catch (Throwable t)
				{
					// t.printStackTrace();
				}
			}
		};
		thread.start();
	}
	
	private boolean checkConsole()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
		}
		return console != null;
	}
	
	public Frame getFrame()
	{
		if (checkConsole())
		{
			return console.getFrame();
		}
		return null;
	}
	
	public void toggleScrollMode()
	{
		if (checkConsole())
		{
			console.toggleScrollMode();
		}
	}
	
	public void toggleInputMode()
	{
		if (checkConsole())
		{
			console.toggleInputMode();
		}
	}
		
	public void input(String text)
	{
		if (checkConsole())
		{
			console.input(text);
		}
	}
	
	public void copyText()
	{
		if (checkConsole())
		{
			console.copyText();
		}
	}
	
	public void pasteText()
	{
		if (checkConsole())
		{
			console.pasteText();
		}
	}
	
	public void clearInput()
	{
		if (checkConsole())
		{
			console.clearInput();
		}
	}
	
	public String getSelectedText()
	{
		if (checkConsole())
		{
			return console.getSelectedText();
		}
		return "";
	}
	
	public String getAllText()
	{
		if (checkConsole())
		{
			return console.getAllText();
		}
		return "";
	}
	
	public void refreshText()
	{
		if (checkConsole())
		{
			console.refreshText();
		}
	}
	
	public boolean isCommandEcho()
	{
		if (checkConsole())
		{
			return console.isCommandEcho();
		}
		return true;
	}
	
	public void setCommandEcho(boolean commandEcho)
	{
		if (checkConsole())
		{
			console.setCommandEcho(commandEcho);
		}
	}
	
	public void copyAllText()
	{
		if (checkConsole())
		{
			console.copyAllText();
		}
	}
	
	public void setIgnoreClose(boolean ignoreClose)
	{
		if (checkConsole())
		{
			console.setIgnoreClose(ignoreClose);
		}
	}
	
	public void setRemoteIcon(boolean remoteIcon)
	{
		this.remoteIcon = remoteIcon;
	}
	
	public void addToggleReplaceInputNotify(VTConsoleBooleanToggleNotify notifyReplaceInput)
	{
		if (checkConsole())
		{
			console.addToggleReplaceInputNotify(notifyReplaceInput);
		}
	}
	
	public void addToggleFlushInterruptNotify(VTConsoleBooleanToggleNotify notifyFlushInterrupted)
	{
		if (checkConsole())
		{
			console.addToggleFlushInterruptNotify(notifyFlushInterrupted);
		}
	}

}