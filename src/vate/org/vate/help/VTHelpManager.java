package org.vate.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VTHelpManager
{
	private static Properties helpMap = new Properties();
	
	private static String ManualParameterHelp = ("\n -C: use client module") +
	(" | -S: use server module") +
	(" | -D: use daemon module");
	
	private static String ApplicationParametersHelp = ("available parameters usage:") +
	("\n -H: list available parameters");

	private static String ConnnectionParametersHelp = ("\n -LF: load connection settings file") + 
	("\n -CM: connection mode, passive(P), active(A)") +
	("\n -CH: connection host, default null") +
	("\n -CP: connection port, default 6060") +
	("\n -CL: connection login, default null") +
	("\n -CS: connection password, default null") +
	("\n -NP: NAT port, default null") +
	("\n -PT: proxy type, SOCKS(S), HTTP(H), disabled(D), default disabled") +
	("\n -PH: proxy host, default null") +
	("\n -PP: proxy port, default 1080 for SOCKS or 8080 for HTTP") +
	("\n -PA: proxy authentication, enabled(E), disabled(D), default disabled") +
	("\n -PU: proxy user, default null") +
	("\n -PS: proxy password, default null") +
	("\n -ET: encryption type, AES(A), RC4(R), disabled(D), default disabled") +
	("\n -ES: encryption password, default null") +
	("\n -SL: sessions limit, default 0, available in server") +
	("\n -SC: session commands, separated by \"*;\", default null, available in client");
	
	public static void initialize()
	{
		InputStream helpStream = null;
		try
		{
			helpStream = VTHelpManager.class.getResourceAsStream("/org/vate/help/resource/help.properties");
			helpMap.load(helpStream);
		}
		catch (IOException e)
		{
			
		}
		finally
		{
			if (helpStream != null)
			{
				try
				{
					helpStream.close();
				}
				catch (IOException e)
				{
					
				}
			}
		}
	}
	
	public static String getMainHelpForClientCommands()
	{
		return helpMap.getProperty("main.client");
	}
	
	public static String getMinHelpForClientCommands()
	{
		return helpMap.getProperty("min.client");
	}
	
	public static String getHelpForClientCommand(String command)
	{
		return helpMap.getProperty("client." + command.toLowerCase(), "\nVT>Client console internal command [" + command + "] not found!\nVT>");
	}
	
	public static String getMainHelpForServerCommands()
	{
		return helpMap.getProperty("main.server");
	}
	
	public static String getMinHelpForServerCommands()
	{
		return helpMap.getProperty("min.server");
	}
	
	public static String getHelpForServerCommand(String command)
	{
		return helpMap.getProperty("server." + command.toLowerCase(), "\nVT>Server console internal command [" + command + "] not found!\nVT>");
	}
	
	public static String printManualParameterHelp()
	{
		return ManualParameterHelp;
	}
	
	public static String printApplicationParametersHelp()
	{
		return ApplicationParametersHelp;
	}
	
	public static String printConnnectionParametersHelp()
	{
		return ConnnectionParametersHelp;
	}
}