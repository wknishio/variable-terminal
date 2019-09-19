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
		try
		{
			BufferedReader input = new BufferedReader(new FileReader(file));
			String command = input.readLine();
			while (true)
			{
				Thread.sleep(2000);
				try
				{
					Process process = Runtime.getRuntime().exec(command);
					VTLauncherOutputConsumer in = new VTLauncherOutputConsumer(new BufferedInputStream(process.getInputStream()));
					VTLauncherOutputConsumer err = new VTLauncherOutputConsumer(new BufferedInputStream(process.getErrorStream()));
					VTRuntimeProcessTextRedirector out = new VTRuntimeProcessTextRedirector(input, process.getOutputStream());
					Thread tin = new Thread(in);
					Thread terr = new Thread(err);
					Thread tout = new Thread(out);
					tin.start();
					terr.start();
					tout.start();
					process.waitFor();
					in.close();
					err.close();
					out.close();
					tin.join();
					terr.join();
					tout.join();
					input.close();
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