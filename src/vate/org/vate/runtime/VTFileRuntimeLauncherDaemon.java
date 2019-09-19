package org.vate.runtime;

import java.io.BufferedReader;
import java.io.FileReader;

import org.vate.nativeutils.VTNativeUtils;

public class VTFileRuntimeLauncherDaemon
{
	public static void main(String[] args) throws Exception
	{
		VTNativeUtils.detachConsole();
		String file = "launcher.txt";
		if (args.length > 0)
		{
			file = args[0];
		}
		BufferedReader input = new BufferedReader(new FileReader(file));
		String command = input.readLine();
		Thread.sleep(2000);
		try
		{
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
		
		System.exit(0);
	}
}