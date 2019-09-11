package org.vate.runtime;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.FileReader;

import org.vate.nativeutils.VTNativeUtils;

public class VTFileRuntimeRelauncherDaemon
{
	public static void main(String[] args) throws Exception
	{
		VTNativeUtils.detachConsole();
		String file = "relauncher.txt";
		if (args.length > 0)
		{
			file = args[0];
		}
		BufferedReader input = new BufferedReader(new FileReader(file));
		String command = input.readLine();
		input.close();
		try
		{
			while (true)
			{
				Thread.sleep(2000);
				try
				{
					Process process = Runtime.getRuntime().exec(command);
					VTLauncherOutputConsumer in = new VTLauncherOutputConsumer(new BufferedInputStream(process.getInputStream()));
					VTLauncherOutputConsumer err = new VTLauncherOutputConsumer(new BufferedInputStream(process.getErrorStream()));
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
		finally
		{
			System.exit(0);
		}
	}
}