package org.vate.runtime;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

public class VTFileRuntimeRelauncher
{
	public static void main(String[] args) throws Exception
	{
		String[] files = {"relauncher.txt"};
		if (args.length > 0)
		{
			files = args;
		}
		for (String file : files)
		{
			try
			{
				BufferedReader input = new BufferedReader(new FileReader(file));
				String command = "";
				while (command != null)
				{
					command = input.readLine();
					final String currentCommand = command;
					Thread commandThread = new Thread()
					{
						public void run()
						{
							command(currentCommand);
						}
					};
					commandThread.start();
				}
				input.close();
			}
			catch (Throwable t)
			{
				
			}
		}
		try
		{
			BufferedReader input = new BufferedReader(new InputStreamReader(System.in));
			String command = "";
			while (command != null)
			{
				command = input.readLine();
				final String currentCommand = command;
				Thread commandThread = new Thread()
				{
					public void run()
					{
						command(currentCommand);
					}
				};
				commandThread.start();
			}
			input.close();
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public static void command(String command)
	{
		while (true)
		{
			try
			{
				Thread.sleep(2000);
				Process process = Runtime.getRuntime().exec(command);
				VTLauncherOutputConsumer in = new VTLauncherOutputConsumer(process.getInputStream());
				VTLauncherOutputConsumer err = new VTLauncherOutputConsumer(process.getErrorStream());
				Thread tin = new Thread(in);
				Thread terr = new Thread(err);
				tin.start();
				terr.start();
				process.waitFor();
				in.close();
				err.close();
				tin.join();
				terr.join();
			}
			catch (Throwable e)
			{
				
			}
		}
	}
}