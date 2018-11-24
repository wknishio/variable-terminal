package org.vate.runtime;

import java.io.BufferedInputStream;

public class VTArgumentsRuntimeRelauncher
{
	public static void main(String[] args) throws Exception
	{
		try
		{
			while (true)
			{
				Thread.sleep(2000);
				try
				{
					Process process = Runtime.getRuntime().exec(args);
					VTInputConsumer in = new VTInputConsumer(new BufferedInputStream(process.getInputStream()));
					VTInputConsumer err = new VTInputConsumer(new BufferedInputStream(process.getErrorStream()));
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