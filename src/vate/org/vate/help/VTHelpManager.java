package org.vate.help;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class VTHelpManager
{
	private static Properties helpMap = new Properties();
	
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
		return helpMap.getProperty("client." + command.toLowerCase(), "\nVT>Client console command [" + command + "] not found!\nVT>");
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
		return helpMap.getProperty("server." + command.toLowerCase(), "\nVT>Server console command [" + command + "] not found!\nVT>");
	}
	
	public static String printManualParameterHelp()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("available parameters usage:");
		builder.append("\n -H: list available parameters");
		return builder.toString();
	}
	
	public static String printApplicationParametersHelp()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("\n -C: use client module");
		builder.append(" | -S: use server module");
		builder.append(" | -D: use daemon module");
		return builder.toString();
	}
	
	public static String printConnnectionParametersHelp()
	{
		StringBuilder builder = new StringBuilder();
		builder.append("\n -LF: load connection settings file");
		builder.append("\n -CM: connection mode, passive(P), active(A)");
		builder.append("\n -CH: connection host, default null");
		builder.append("\n -CP: connection port, default 6060");
		builder.append("\n -NP: NAT port, default null");
		builder.append("\n -ET: encryption type, AES(A), RC4(R), disabled(D), default disabled");
		builder.append("\n -EK: encryption password, default null");
		builder.append("\n -PT: proxy type, SOCKS(S), HTTP(H), disabled(D), default disabled");
		builder.append("\n -PH: proxy host, default null");
		builder.append("\n -PP: proxy port, default 1080 for SOCKS or 8080 for HTTP");
		builder.append("\n -PA: proxy authentication, enabled(E), disabled(D), default disabled");
		builder.append("\n -PU: proxy user, default null");
		builder.append("\n -PK: proxy password, default null");
		builder.append("\n -AL: authentication login, default null");
		builder.append("\n -AK: authentication password, default null");
		builder.append("\n -SL: sessions limit, default 0, available in server");
		builder.append("\n -SC: session commands, separated by \"*;\", default null, available in client");
		return builder.toString();
	}
}