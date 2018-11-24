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
}