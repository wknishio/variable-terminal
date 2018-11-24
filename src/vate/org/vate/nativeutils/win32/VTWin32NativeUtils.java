package org.vate.nativeutils.win32;

import org.vate.nativeutils.VTNativeUtilsImplementation;

import com.sun.jna.Native;

public class VTWin32NativeUtils implements VTNativeUtilsImplementation
{
	private class VTWin32AsynchronousBeeper implements Runnable
	{
		private volatile boolean playing;
		private volatile boolean started;
		private int freq;
		private int dur;
		
		public VTWin32AsynchronousBeeper(int freq, int dur)
		{
			this.freq = freq;
			this.dur = dur;
			this.playing = true;
			this.started = false;
		}
		
		public boolean isPlaying()
		{
			return playing;
		}
		
		public boolean isStarted()
		{
			return started;
		}
		
		public void run()
		{
			synchronized (this)
			{
				started = true;
				notify();
			}
			playing = beep(freq, dur, true);
		}
	}
	
	// Win32 constants
	private static int EWX_LOGOFF = 0;
	private static int EWX_POWEROFF = 0x00000008;
	private static int EWX_REBOOT = 0x00000002;
	private static int EWX_RESTARTAPPS = 0x00000040;
	private static int EWX_SHUTDOWN = 0x00000001;
	private static int EWX_FORCE = 0x00000004;
	private static int EWX_FORCEIFHUNG = 0x00000010;
	// private static int WM_SYSCOMMAND = 0x0112;
	// private static int SC_RESTORE = 0xF120;
	
	private VTUser32 user32Lib;
	private VTKernel32 kernel32Lib;
	private VTWinmm winmmLib;
	// private VTPsapi psapiLib;
	private VTWin32CLibrary win32CLibrary;
	// private VTWin32Isatty win32Isatty;
	
	public VTWin32NativeUtils()
	{
		user32Lib = (VTUser32) Native.loadLibrary("User32", VTUser32.class);
		kernel32Lib = (VTKernel32) Native.loadLibrary("Kernel32", VTKernel32.class);
		winmmLib = (VTWinmm) Native.loadLibrary("WinMM", VTWinmm.class);
		win32CLibrary = (VTWin32CLibrary) Native.loadLibrary("msvcrt", VTWin32CLibrary.class);
		// may not be able to load isatty
		// psapiLib = (VTPsapi) Native.loadLibrary("Psapi", VTPsapi.class);
	}
	
	public int system(String command)
	{
		return win32CLibrary.system(command);
	}
	
	public int getchar()
	{
		return win32CLibrary.getchar();
	}
	
	public void printf(String format, Object... args)
	{
		win32CLibrary.printf(format, args);
	}
	
	/* public boolean beep(int freq, int dur) { return kernel32Lib.Beep(freq,
	 * dur); } */
	
	public boolean beep(int freq, int dur, boolean block)
	{
		if (block)
		{
			return kernel32Lib.Beep(freq, dur);
		}
		else
		{
			VTWin32AsynchronousBeeper beeper = new VTWin32AsynchronousBeeper(freq, dur);
			Thread beepThread = new Thread(null, beeper, beeper.getClass().getSimpleName());
			beepThread.setDaemon(true);
			beepThread.start();
			synchronized (beeper)
			{
				while (!beeper.isStarted())
				{
					try
					{
						beeper.wait();
					}
					catch (InterruptedException e)
					{
						return false;
					}
				}
			}
			return beeper.isPlaying();
		}
	}
	
	public boolean openCD()
	{
		// winmmLib.mciSendStringA("close cdaudio", null, 0, 0);
		// winmmLib.mciExecute("close cdaudio");
		// winmmLib.mciSendStringA("open cdaudio wait shareable", null, 0, 0);
		// winmmLib.mciExecute("open cdaudio shareable");
		int status = winmmLib.mciSendStringA("set CDAudio door open", null, 0, 0);
		// boolean status = winmmLib.mciExecute("set CDAudio door open");
		// winmmLib.mciSendStringA("close cdaudio", null, 0, 0);
		// winmmLib.mciExecute("close cdaudio");
		return status == 0;
	}
	
	public boolean closeCD()
	{
		// winmmLib.mciSendStringA("close cdaudio", null, 0, 0);
		// winmmLib.mciExecute("close cdaudio");
		// winmmLib.mciSendStringA("open cdaudio wait shareable", null, 0, 0);
		// winmmLib.mciExecute("open cdaudio shareable");
		int status = winmmLib.mciSendStringA("set CDAudio door closed", null, 0, 0);
		// boolean status = winmmLib.mciExecute("set CDAudio door closed");
		// winmmLib.mciSendStringA("close cdaudio", null, 0, 0);
		// winmmLib.mciExecute("close cdaudio");
		return status == 0;
	}
	
	/* public boolean blockInput() { return user32Lib.BlockInput(true) != 0; }
	 * public boolean unblockInput() { return user32Lib.BlockInput(false) != 0;
	 * } */
	
	public void sleep(int delay)
	{
		kernel32Lib.Sleep(delay);
	}
	
	public boolean shutdown(boolean ignoreAllApplications, boolean waitApplicationsTimeout)
	{
		int flag = EWX_SHUTDOWN;
		if (ignoreAllApplications)
		{
			flag |= EWX_FORCE;
		}
		else if (waitApplicationsTimeout)
		{
			flag |= EWX_FORCEIFHUNG;
		}
		return user32Lib.ExitWindowsEx(flag, 0x00040000);
	}
	
	public boolean reboot(boolean ignoreAllApplications, boolean waitApplicationsTimeout)
	{
		int flag = EWX_REBOOT;
		if (ignoreAllApplications)
		{
			flag |= EWX_FORCE;
		}
		else if (waitApplicationsTimeout)
		{
			flag |= EWX_FORCEIFHUNG;
		}
		return user32Lib.ExitWindowsEx(flag, 0x00040000);
	}
	
	public boolean powerOff(boolean ignoreAllApplications, boolean waitApplicationsTimeout)
	{
		int flag = EWX_POWEROFF;
		if (ignoreAllApplications)
		{
			flag |= EWX_FORCE;
		}
		else if (waitApplicationsTimeout)
		{
			flag |= EWX_FORCEIFHUNG;
		}
		return user32Lib.ExitWindowsEx(flag, 0x00040000);
	}
	
	public boolean logoff(boolean ignoreAllApplications, boolean waitApplicationsTimeout)
	{
		int flag = EWX_LOGOFF;
		if (ignoreAllApplications)
		{
			flag |= EWX_FORCE;
		}
		else if (waitApplicationsTimeout)
		{
			flag |= EWX_FORCEIFHUNG;
		}
		return user32Lib.ExitWindowsEx(flag, 0x00040000);
	}
	
	public boolean restartApplications(boolean ignoreApplications, boolean waitApplicationsTimeout)
	{
		int flag = EWX_RESTARTAPPS;
		if (ignoreApplications)
		{
			flag |= EWX_FORCE;
		}
		else if (waitApplicationsTimeout)
		{
			flag |= EWX_FORCEIFHUNG;
		}
		return user32Lib.ExitWindowsEx(flag, 0x00040000);
	}
	
	public void exit(int status)
	{
		win32CLibrary.exit(status);
	}
	
	public void abort()
	{
		win32CLibrary.abort();
	}
	
	public int raise(int signal)
	{
		return win32CLibrary.raise(signal);
	}
	
	public int rand()
	{
		return win32CLibrary.rand();
	}
	
	public void srand(int seed)
	{
		win32CLibrary.srand(seed);
	}
	
	public String getenv(String env)
	{
		return win32CLibrary.getenv(env);
	}
	
	public int putenv(String env)
	{
		return win32CLibrary._putenv(env);
	}
	
	public int getpid()
	{
		return kernel32Lib.GetCurrentProcessId();
	}
	
	public int isatty(int fd)
	{
		return win32CLibrary._isatty(fd);
	}
	
	/* public void changeFocusToWindow(String windowTitle) { int hWnd =
	 * user32Lib.FindWindowA(null, windowTitle); if (hWnd != 0) {
	 * user32Lib.SendMessageA(hWnd, WM_SYSCOMMAND, SC_RESTORE, 0);
	 * user32Lib.SetForegroundWindow(hWnd); } } */
	
	/* public List<Integer> listProcessIds() { List<Integer> processIds = new
	 * LinkedList<Integer>(); int[] pProcessIds = new int[1024]; int[]
	 * pBytesReturned = new int[1]; psapiLib.EnumProcesses(pProcessIds, 2048,
	 * pBytesReturned); int processCount = pBytesReturned[0] /
	 * Native.POINTER_SIZE; //System.out.println("ProcessCount: " +
	 * processCount); for (int i = 0;i < processCount;i++) {
	 * //System.out.println("PID: " + pProcessIds[i]);
	 * processIds.add(pProcessIds[i]); } return processIds; } */
	
	/* public static void main(String[] args) throws InterruptedException {
	 * //new VTWin32NativeUtils().changeFocusToWindow("homebrews"); //new
	 * VTWin32NativeUtils().listProcessIds(); } */
}