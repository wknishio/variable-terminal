package org.vate.runtime;

import org.vate.console.standard.VTStandardConsoleInterruptibleInputStreamByte;
import org.vate.nativeutils.VTNativeUtils;

public class VTArgumentsRuntimeRelauncherDaemon
{
	public static void main(String[] args) throws Exception
	{
		VTStandardConsoleInterruptibleInputStreamByte stream = new VTStandardConsoleInterruptibleInputStreamByte();
		VTNativeUtils.detachConsole();
		try
		{
			while (true)
			{
				try
				{
					Thread.sleep(2000);
					Process process = Runtime.getRuntime().exec(args);
					VTRuntimeProcessInputRedirector in = new VTRuntimeProcessInputRedirector(process.getInputStream(), System.out);
					VTRuntimeProcessInputRedirector err = new VTRuntimeProcessInputRedirector(process.getErrorStream(), System.err);
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