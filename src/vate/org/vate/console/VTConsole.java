package org.vate.console;

import java.awt.GraphicsEnvironment;
import java.io.FileDescriptor;
import java.io.InputStream;
import java.io.PrintStream;
import java.util.Locale;

import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.console.standard.VTStandardConsole;

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
	
	private static boolean graphical;
	private static boolean daemon;
	// private static boolean split;
	private static VTConsoleImplementation console;
	
	public synchronized static void initialize()
	{
		if (console == null)
		{
			if (!daemon)
			{
				if (graphical)
				{
					// VTGraphicalConsole.setSplit(split);
					console = VTGraphicalConsole.getInstance();
					// setBold(true);
					// VTGraphicalConsole.setTitle(title);
					// initialized = true;
				}
				else
				{
					console = VTStandardConsole.getInstance();
					resetAttributes();
					setColors(VT_CONSOLE_COLOR_LIGHT_GREEN, VT_CONSOLE_COLOR_NORMAL_BLACK);
					// clear();
					// setBold(true);
					// setBright(true);
					// initialized = true;
				}
			}
			else
			{
				
			}
		}
	}
	
	public synchronized static boolean isGraphical()
	{
		return graphical;
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
	
	public synchronized static void setDaemon(boolean daemon)
	{
		if (console == null)
		{
			VTConsole.daemon = daemon;
		}
	}
	
	/* public synchronized static boolean isSplit() { return split; } */
	
	/* public synchronized static void setSplit(boolean split) { if (terminal ==
	 * null) { VTTerminal.split = split; } } */
	
	public static void setTitle(String title)
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.setTitle(title);
			clear();
		}
	}
	
	public static String readLine() throws InterruptedException
	{
		return readLine(true);
	}
	
	public static String readLine(boolean echo) throws InterruptedException
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			try
			{
				return console.readLine(echo);
			}
			catch (Throwable t)
			{
				return "";
			}
			// return console.readLine(echo);
		}
		else
		{
			return null;
		}
	}
	
	public static void interruptReadLine()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.interruptReadLine();
		}
		else
		{
			
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
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.print(str);
		}
	}
	
	public static void println(String str)
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.println(str);
		}
	}
	
	public static void printf(String format, Object... args)
	{
		console.printf(format, args);
	}
	
	public static void printfln(String format, Object... args)
	{
		console.printfln(format, args);
	}
	
	public static void printf(Locale l, String format, Object... args)
	{
		console.printf(l, format, args);
	}
	
	public static void printfln(Locale l, String format, Object... args)
	{
		console.printfln(l, format, args);
	}
	
	public static void write(String str)
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.write(str);
		}
	}
	
	public static void write(char[] cbuf, int off, int len)
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.write(cbuf, off, len);
		}
	}
	
	/* public static void write(byte[] buf, int off, int len) {
	 * terminal.trueWrite(buf, off, len); } */
	
	public static void flush()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.flush();
		}
	}
	
	public static void clear()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.clear();
		}
	}
	
	public static void bell()
	{
		try
		{
			if (!daemon)
			{
				if (console == null)
				{
					initialize();
				}
				console.bell();
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
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.setColors(foregroundColor, backgroundColor);
			clear();
		}
	}
	
	public static void setBold(boolean bold)
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.setBold(bold);
		}
	}
	
	public static void resetAttributes()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.resetAttributes();
		}
	}
	
	public static void setSystemIn()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.setSystemIn();
		}
	}
	
	public static void setSystemOut()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.setSystemOut();
		}
	}
	
	public static void setSystemErr()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			console.setSystemErr();
		}
	}
	
	public static InputStream getSystemIn()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			return console.getSystemIn();
		}
		return null;
	}
	
	public static PrintStream getSystemOut()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
			return console.getSystemOut();
		}
		return null;
	}
	
	public static PrintStream getSystemErr()
	{
		if (!daemon)
		{
			if (console == null)
			{
				initialize();
			}
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
}