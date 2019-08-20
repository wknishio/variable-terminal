package org.vate.nativeutils.openbsd;

import org.vate.nativeutils.VTNativeUtilsImplementation;

import com.sun.jna.Native;

public class VTOpenBSDNativeUtils implements VTNativeUtilsImplementation
{
	// Device opening flags
	private static int O_RDONLY = 0;
	private static int O_WRONLY = 1;
	private static int O_NONBLOCK = 0x0004;
	private static int CLOCK_TICK_RATE = 1193180;
	// private static int IOCPARM_MASK = 0x1fff;
	// IOCTLs
	// private static int KIOCSOUND = ('K' << 8) | 63;
	private static int KDMKTONE = ('K' << 8) | 8;
	private static int CDIOCEJECT = ('c' << 8) | 24;
	private static int CDIOCCLOSE = ('c' << 8) | 28;
	
	private VTOpenBSDCLibrary openbsdCLibrary;
	
	public VTOpenBSDNativeUtils()
	{
		openbsdCLibrary = (VTOpenBSDCLibrary) Native.loadLibrary("c", VTOpenBSDCLibrary.class);
	}
	
	public int system(String command)
	{
		return openbsdCLibrary.system(command);
	}
	
	public int getchar()
	{
		return openbsdCLibrary.getchar();
	}
	
	public void printf(String format, Object... args)
	{
		openbsdCLibrary.printf(format, args);
	}
	
	/* public boolean beep(int freq, int dur) { return beep(freq, dur, true);
	 * } */
	
	public boolean beep(int freq, int dur, boolean block)
	{
		boolean returnFlag = false;
		int fd = openbsdCLibrary.open("/dev/console", O_WRONLY);
		if (fd == -1)
		{
			returnFlag = false;
		}
		else
		{
			returnFlag = openbsdCLibrary.ioctl(fd, KDMKTONE, ((int) ((dur << 16) | (CLOCK_TICK_RATE / freq)))) == 0;
			if (returnFlag && block)
			{
				try
				{
					Thread.sleep(dur);
				}
				catch (InterruptedException e)
				{
					returnFlag = false;
				}
			}
			openbsdCLibrary.close(fd);
		}
		return returnFlag;
	}
	
	public boolean openCD()
	{
		int cdrom = openbsdCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
		if (cdrom == -1)
		{
			return false;
		}
		if (openbsdCLibrary.ioctl(cdrom, CDIOCEJECT) == -1)
		{
			openbsdCLibrary.close(cdrom);
			return false;
		}
		else
		{
			openbsdCLibrary.close(cdrom);
			return true;
		}
	}
	
	public boolean closeCD()
	{
		int cdrom = openbsdCLibrary.open("/dev/cdrom", O_RDONLY | O_NONBLOCK);
		if (cdrom == -1)
		{
			return false;
		}
		if (openbsdCLibrary.ioctl(cdrom, CDIOCCLOSE) == -1)
		{
			openbsdCLibrary.close(cdrom);
			return false;
		}
		else
		{
			openbsdCLibrary.close(cdrom);
			return true;
		}
	}
	
	public void exit(int status)
	{
		openbsdCLibrary.exit(status);
	}
	
	public void abort()
	{
		openbsdCLibrary.abort();
	}
	
	public int raise(int signal)
	{
		return openbsdCLibrary.raise(signal);
	}
	
	public int rand()
	{
		return openbsdCLibrary.rand();
	}
	
	public void srand(int seed)
	{
		openbsdCLibrary.srand(seed);
	}
	
	public String getenv(String env)
	{
		return openbsdCLibrary.getenv(env);
	}
	
	public int putenv(String env)
	{
		return openbsdCLibrary.putenv(env);
	}
	
	public int getpid()
	{
		return openbsdCLibrary.getpid();
	}
	
	public int isatty(int fd)
	{
		return openbsdCLibrary.isatty(fd);
	}
	
	public boolean detach_console()
	{
		try
		{
			openbsdCLibrary.system("disown -h");
			try
			{
				//openbsdCLibrary.system("exit");
			}
			catch (Throwable t)
			{
				
			}
		}
		catch (Throwable t)
		{
			
		}
		return true;
	}
	
	public boolean attach_console()
	{
		return false;
	}

	public boolean hide_console()
	{
		try
		{
			openbsdCLibrary.system("disown -h");
			try
			{
				//openbsdCLibrary.system("exit");
			}
			catch (Throwable t)
			{
				
			}
		}
		catch (Throwable t)
		{
			
		}
		return true;
	}
	
	public int getch()
	{
		return getchar();
	}
	
	public void raw()
	{
		try
		{
			Runtime.getRuntime().exec(new String[]{"/bin/sh","-c","stty -icanon min 1 < /dev/tty"});
		}
		catch (Throwable t)
		{
			
		}
	}

	public void icanon()
	{
		try
		{
			Runtime.getRuntime().exec(new String[]{"/bin/sh","-c","stty icanon < /dev/tty"});
		}
		catch (Throwable t)
		{
			
		}
	}
}