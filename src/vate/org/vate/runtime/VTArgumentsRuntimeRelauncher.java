package org.vate.runtime;

import org.vate.console.standard.VTStandardConsoleInterruptibleInputStreamByte;

public class VTArgumentsRuntimeRelauncher
{
	public static void main(String[] args) throws Exception
	{
		try
		{
			VTStandardConsoleInterruptibleInputStreamByte stream = new VTStandardConsoleInterruptibleInputStreamByte();
			while (true)
			{
				Thread.sleep(2000);
				try
				{
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