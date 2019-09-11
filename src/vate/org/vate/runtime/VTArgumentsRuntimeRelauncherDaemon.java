package org.vate.runtime;

import java.io.BufferedInputStream;

import org.vate.nativeutils.VTNativeUtils;

public class VTArgumentsRuntimeRelauncherDaemon
{
	public static void main(String[] args) throws Exception
	{
		VTNativeUtils.detachConsole();
		try
		{
			while (true)
			{
				Thread.sleep(2000);
				try
				{
					Process process = Runtime.getRuntime().exec(args);
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