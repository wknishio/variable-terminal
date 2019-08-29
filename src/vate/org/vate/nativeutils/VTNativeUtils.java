package org.vate.nativeutils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.Map.Entry;

import org.vate.nativeutils.freebsd.VTFreeBSDNativeUtils;
import org.vate.nativeutils.linux.VTLinuxNativeUtils;
import org.vate.nativeutils.mac.VTMacNativeUtils;
import org.vate.nativeutils.openbsd.VTOpenBSDNativeUtils;
import org.vate.nativeutils.sunos.VTSunOSNativeUtils;
import org.vate.nativeutils.win32.VTWin32NativeUtils;

import com.sun.jna.Platform;

public class VTNativeUtils
{
	// private static String[] realEnvironmentVariablesBackup;
	private static String[] virtualEnvironmentVariablesBackup;
	private static String[] virtualEnvironmentVariables;
	private static Properties systemPropertiesBackup;
	private static VTNativeUtilsImplementation nativeUtils;
	// public abstract int true_isatty(int fd);
	
	public synchronized static void initialize()
	{
		if (nativeUtils != null)
		{
			return;
		}
		try
		{
			if (Platform.isWindows())
			{
				nativeUtils = new VTWin32NativeUtils();
			}
			else if (Platform.isLinux())
			{
				nativeUtils = new VTLinuxNativeUtils();
			}
			else if (Platform.isFreeBSD())
			{
				nativeUtils = new VTFreeBSDNativeUtils();
			}
			else if (Platform.isOpenBSD())
			{
				nativeUtils = new VTOpenBSDNativeUtils();
			}
			else if (Platform.isSolaris())
			{
				nativeUtils = new VTSunOSNativeUtils();
			}
			else if (Platform.isMac())
			{
				nativeUtils = new VTMacNativeUtils();
			}
			else if (Platform.isX11())
			{
				nativeUtils = new VTMinimalNativeUtils();
			}
			else
			{
				nativeUtils = new VTMinimalNativeUtils();
			}
		}
		catch (Throwable e)
		{
			nativeUtils = new VTNullNativeUtils();
		}
	}
	
	public static int system(String command)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.system(command);
	}
	
	public static int getchar()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.getchar();
	}
	
	public static int getch()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.getch();
	}
	
	public static void unbuffered()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.unbuffered();
	}
	
	public static void noecho()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.noecho();
	}
	
	public static void normal()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.normal();
	}
	
	public static void printf(String format, Object... args)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.printf(format, args);
	}
	
	public static boolean beep(int freq, int dur, boolean block)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.beep(freq, dur, block);
	}
	
	public static boolean openCD()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.openCD();
	}
	
	public static boolean closeCD()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.closeCD();
	}
	
	public static void exit(int status)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.exit(status);
	}
	
	public static void abort()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.abort();
	}
	
	public static int raise(int signal)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.raise(signal);
	}
	
	public static int rand()
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.rand();
	}
	
	public static void srand(int seed)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		nativeUtils.srand(seed);
	}
	
	public static String getenv(String env)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.getenv(env);
	}
	
	public static int putenv(String env)
	{
		if (nativeUtils == null)
		{
			initialize();
		}
		return nativeUtils.putenv(env);
	}
	
	/* public static int isatty(int fd) { return nativeUtils.true_isatty(fd);
	 * } */
	
	public static int putvirtualenv(String name, String newValue)
	{
		initvirtualenvs();
		virtualEnvironmentVariables = changeEnvArray(virtualEnvironmentVariables, name, newValue);
		return 0;
	}
	
	public static String[] getvirtualenvs()
	{
		initvirtualenvs();
		return virtualEnvironmentVariables;
	}
	
	public static Map<String, String> getvirtualenv()
	{
		initvirtualenvs();
		return getEnvMapAsMap(virtualEnvironmentVariables);
	}
	
	public static String getvirtualenv(String name)
	{
		initvirtualenvs();
		return getEnv(virtualEnvironmentVariables, name);
	}
	
	public static boolean backupvirtualenvs()
	{
		try
		{
			initvirtualenvs();
			virtualEnvironmentVariablesBackup = cloneArray(virtualEnvironmentVariables);
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
	
	public static boolean restorevirtualenvs()
	{
		try
		{
			initvirtualenvs();
			if (virtualEnvironmentVariablesBackup != null)
			{
				virtualEnvironmentVariables = cloneArray(virtualEnvironmentVariablesBackup);
			}
			else
			{
				virtualEnvironmentVariables = getEnvMapAsArray(System.getenv());
			}
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
	
	public static boolean backupsystemproperties()
	{
		try
		{
			systemPropertiesBackup = new Properties();
			systemPropertiesBackup.putAll(System.getProperties());
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
	
	public static boolean restoresystemproperties()
	{
		try
		{
			// System.getProperties().putAll(systemPropertiesBackup);
			System.setProperties(systemPropertiesBackup);
			return true;
		}
		catch (Throwable e)
		{
			return false;
		}
	}
	
	private static void initvirtualenvs()
	{
		if (virtualEnvironmentVariables == null)
		{
			virtualEnvironmentVariables = getEnvMapAsArray(System.getenv());
		}
	}
	
	private static String[] cloneArray(String[] array)
	{
		String[] clone = new String[array.length];
		System.arraycopy(array, 0, clone, 0, array.length);
		return clone;
	}
	
	private static String[] getEnvMapAsArray(Map<String, String> envMap)
	{
		String[] envs = new String[envMap.size()];
		int count = 0;
		for (Entry<String, String> variable : envMap.entrySet())
		{
			envs[count++] = variable.getKey() + "=" + variable.getValue();
		}
		return envs;
	}
	
	private static Map<String, String> getEnvMapAsMap(String[] envArray)
	{
		Map<String, String> envs = new LinkedHashMap<String, String>();
		for (String variable : envArray)
		{
			if (variable.indexOf('=') != -1)
			{
				envs.put(variable.substring(0, variable.indexOf('=')), variable.substring(variable.indexOf('=') + 1));
			}
		}
		return envs;
	}
	
	private static String getEnv(String[] envs, String name)
	{
		int i = 0;
		for (i = 0; i < envs.length; i++)
		{
			if (envs[i].indexOf('=') != -1)
			{
				if (envs[i].substring(0, envs[i].indexOf('=')).equalsIgnoreCase(name))
				{
					return envs[i].substring(envs[i].indexOf('=') + 1);
				}
			}
		}
		return null;
	}
	
	private static String[] changeEnvArray(String[] envs, String name, String newValue)
	{
		boolean found = false;
		int i = 0;
		for (i = 0; i < envs.length; i++)
		{
			if (envs[i].indexOf('=') != -1)
			{
				if (envs[i].substring(0, envs[i].indexOf('=')).equalsIgnoreCase(name))
				{
					found = true;
					envs[i] = envs[i].substring(0, envs[i].indexOf('=')) + "=" + newValue;
				}
			}
		}
		if (!found)
		{
			String[] newEnvs = new String[envs.length + 1];
			System.arraycopy(envs, 0, newEnvs, 0, envs.length);
			newEnvs[envs.length] = name + "=" + newValue;
			return newEnvs;
		}
		else
		{
			return envs;
		}
	}
	
	public static int getpid()
	{
		return nativeUtils.getpid();
	}
	
	public static int isatty(int fd)
	{
		return nativeUtils.isatty(fd);
	}
	
	public static boolean hide_console()
	{
		return nativeUtils.hide_console();
	}
	
	public static boolean detach_console()
	{
		return nativeUtils.detach_console();
	}
	
	public static boolean attach_console()
	{
		return nativeUtils.attach_console();
	}
}