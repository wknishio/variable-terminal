package org.vate.runtime;

import java.io.BufferedReader;
import java.io.FileReader;

public class VTFileRuntimeRelauncher
{
	public static void main(String[] args) throws Exception
	{
		String file = "relauncher.txt";
		if (args.length > 0)
		{
			file = args[0];
		}
		try
		{
			while (true)
			{
				Thread.sleep(2000);
				try
				{
					BufferedReader input = new BufferedReader(new FileReader(file));
					String command = input.readLine();
					Process process = Runtime.getRuntime().exec(command);
					VTRuntimeProcessInputRedirector in = new VTRuntimeProcessInputRedirector(process.getInputStream(), System.out);
					VTRuntimeProcessInputRedirector err = new VTRuntimeProcessInputRedirector(process.getErrorStream(), System.err);
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