package org.vate.runtime;

import java.io.BufferedInputStream;

import org.vate.console.standard.VTStandardConsoleInterruptibleInputStreamByte;
import org.vate.nativeutils.VTNativeUtils;

public class VTArgumentsRuntimeRelauncherDaemon
{
	public static void main(String[] args) throws Exception
	{
		VTNativeUtils.detachConsole();
		try
		{
			VTStandardConsoleInterruptibleInputStreamByte stream = new VTStandardConsoleInterruptibleInputStreamByte();
			while (true)
			{
				Thread.sleep(2000);
				try
				{
					Process process = Runtime.getRuntime().exec(args);
					VTLauncherOutputConsumer in = new VTLauncherOutputConsumer(new BufferedInputStream(process.getInputStream()));
					VTLauncherOutputConsumer err = new VTLauncherOutputConsumer(new BufferedInputStream(process.getErrorStream()));
					VTRuntimeProcessInputRedirector out = new VTRuntimeProcessInputRedirector(stream, process.getOutputStream());
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