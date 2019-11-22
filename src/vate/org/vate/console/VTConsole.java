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

public class VTConsole
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
	
	private static boolean lanterna = false;
	private static boolean graphical;
	private static boolean daemon;
	// private static boolean split;
	private static volatile VTConsoleImplementation console;
	
	private static Object synchronizationObject = new Object();
	
	static
	{
		VTNativeUtils.initialize();
	}
	
	public static Object getSynchronizationObject()
	{
		return synchronizationObject;
	}
	
	public synchronized static void initialize()
	{
		if (console == null)
		{
			if (!daemon)
			{
				if (graphical)
				{
					// VTGraphicalConsole.setSplit(split);
					VTNativeUtils.detachConsole();
					if (lanterna)
					{
						console = new VTLanternaConsole(graphical);
					}
					else
					{
						console = VTGraphicalConsole.getInstance();
					}
					//console = VTGraphicalConsole.getInstance();
					
					// setBold(true);
					// VTGraphicalConsole.setTitle(title);
					// initialized = true;
				}
				else
				{
					console = VTStandardConsole.getInstance();
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
	
	public synchronized static boolean isGraphical()
	{
		return graphical;
	}
	
	public synchronized static void setLanterna(boolean lanterna)
	{
		VTConsole.lanterna = lanterna;
	}
	
	public synchronized static void setGraphical(boolean graphical)
	{
		if (console == null)
		{
			VTConsole.graphical = graphical;
			if (GraphicsEnvironment.isHeadless())
			{
				VTConsole.graphical = false;
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
									VTConsole.graphical = true;
								}
							}
							catch (Throwable e)
							{
								VTConsole.graphical = true;
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
								VTConsole.graphical = true;
							}
						}
						catch (Throwable e2)
						{
							VTConsole.graphical = true;
						}
					}
				}
			}
		}
	}
	
	public synchronized static boolean isDaemon()
	{
		return daemon;
	}
	
	private static class HideGraphicalConsole implements Runnable
	{
		public void run()
		{
			try
			{
				VTConsole.getFrame().setVisible(false);
			}
			catch (Throwable t)
			{
				//t.printStackTrace();
			}
		}
	}
	
	private static class ShowGraphicalConsole implements Runnable
	{
		public void run()
		{
			try
			{
				VTConsole.getFrame().setVisible(true);
				Object waiter = VTConsole.getSynchronizationObject();
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
	
	private static HideGraphicalConsole daemonizeThread = new HideGraphicalConsole();
	private static ShowGraphicalConsole undaemonizeThread = new ShowGraphicalConsole();
	
	public synchronized static void setDaemon(boolean daemon)
	{
		if (VTConsole.daemon == daemon)
		{
			return;
		}
		if (console == null)
		{
			VTConsole.daemon = daemon;
		}
		else
		{
			if (isGraphical())
			{
				if (daemon == true)
				{
					VTConsole.daemon = daemon;
					EventQueue.invokeLater(daemonizeThread);
				}
				else
				{
					VTConsole.daemon = daemon;
					EventQueue.invokeLater(undaemonizeThread);
				}
			}
			else
			{
				if (daemon == true)
				{
					VTConsole.daemon = daemon;
					VTNativeUtils.hideConsole();
				}
				else
				{
					VTConsole.daemon = daemon;
					VTNativeUtils.attachConsole();
					Object waiter = VTConsole.getSynchronizationObject();
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
	
	public static void setTitle(String title)
	{
		if (checkConsole())
		{
			console.setTitle(title);
			console.clear();
		}
	}
	
	public static String readLine() throws InterruptedException
	{
		return readLine(true);
	}
	
	public static String readLine(boolean echo) throws InterruptedException
	{
		if (checkConsole())
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
		return null;
	}
	
	public static void interruptReadLine()
	{
		if (checkConsole())
		{
			console.interruptReadLine();
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
	
	public static void print(String str)
	{
		if (checkConsole())
		{
			console.print(str);
		}
	}
	
	public static void println(String str)
	{
		if (checkConsole())
		{
			console.println(str);
		}
	}
	
	public static void printf(String format, Object... args)
	{
		if (checkConsole())
		{
			console.printf(format, args);
		}
	}
	
	public static void printfln(String format, Object... args)
	{
		if (checkConsole())
		{
			console.printfln(format, args);
		}
	}
	
	public static void printf(Locale l, String format, Object... args)
	{
		if (checkConsole())
		{
			console.printf(l, format, args);
		}
	}
	
	public static void printfln(Locale l, String format, Object... args)
	{
		if (checkConsole())
		{
			console.printfln(l, format, args);
		}
	}
	
	public static void write(String str)
	{
		if (checkConsole())
		{
			console.write(str);
		}
	}
	
	public static void write(char[] cbuf, int off, int len)
	{
		if (checkConsole())
		{
			console.write(cbuf, off, len);
		}
	}
	
	/* public static void write(byte[] buf, int off, int len) {
	 * terminal.trueWrite(buf, off, len); } */
	
	public static void flush()
	{
		if (checkConsole())
		{
			console.flush();
		}
	}
	
	public static void clear()
	{
		if (checkConsole())
		{
			console.clear();
		}
	}
	
	public static void bell()
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
	
	public static void setColors(int foregroundColor, int backgroundColor)
	{
		if (checkConsole())
		{
			console.setColors(foregroundColor, backgroundColor);
			console.clear();
		}
	}
	
	public static void setBold(boolean bold)
	{
		if (checkConsole())
		{
			console.setBold(bold);
		}
	}
	
	public static void resetAttributes()
	{
		if (checkConsole())
		{
			console.resetAttributes();
		}
	}
	
	public static void setSystemIn()
	{
		if (checkConsole())
		{
			console.setSystemIn();
		}
	}
	
	public static void setSystemOut()
	{
		if (checkConsole())
		{
			console.setSystemOut();
		}
	}
	
	public static void setSystemErr()
	{
		if (checkConsole())
		{
			console.setSystemErr();
		}
	}
	
	public static InputStream getSystemIn()
	{
		if (checkConsole())
		{
			return console.getSystemIn();
		}
		return null;
	}
	
	public static PrintStream getSystemOut()
	{
		if (checkConsole())
		{
			return console.getSystemOut();
		}
		return null;
	}
	
	public static PrintStream getSystemErr()
	{
		if (checkConsole())
		{
			return console.getSystemErr();
		}
		return null;
	}
	
	public static void createInterruptibleReadline(final boolean echo, final Runnable interrupt)
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
	
	private static boolean checkConsole()
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
	
	public static Frame getFrame()
	{
		if (checkConsole())
		{
			return console.getFrame();
		}
		return null;
	}
	
	public static void toggleScrollMode()
	{
		if (checkConsole())
		{
			console.toggleScrollMode();
		}
	}
	
	public static void toggleInputMode()
	{
		if (checkConsole())
		{
			console.toggleInputMode();
		}
	}
		
	public static void input(String text)
	{
		if (checkConsole())
		{
			console.input(text);
		}
	}
	
	public static void copyText()
	{
		if (checkConsole())
		{
			console.copyText();
		}
	}
	
	public static void pasteText()
	{
		if (checkConsole())
		{
			console.pasteText();
		}
	}
	
	public static void clearInput()
	{
		if (checkConsole())
		{
			console.clearInput();
		}
	}
	
	public static String getSelectedText()
	{
		if (checkConsole())
		{
			return console.getSelectedText();
		}
		return "";
	}
	
	public static void refreshText()
	{
		if (checkConsole())
		{
			console.refreshText();
		}
	}
	
	public static boolean isCommandEcho()
	{
		if (checkConsole())
		{
			return console.isCommandEcho();
		}
		return true;
	}
	
	public static void setCommandEcho(boolean commandEcho)
	{
		if (checkConsole())
		{
			console.setCommandEcho(commandEcho);
		}
	}
}