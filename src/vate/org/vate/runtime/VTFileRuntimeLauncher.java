package org.vate.runtime;

import java.io.BufferedReader;
import java.io.FileReader;

public class VTFileRuntimeLauncher
{
	public static void main(String[] args) throws Exception
	{
		String file = "launcher.txt";
		if (args.length > 0)
		{
			file = args[0];
		}
		BufferedReader input = new BufferedReader(new FileReader(file));
		String command = input.readLine();
		input.close();
		Thread.sleep(2000);
		Runtime.getRuntime().exec(command);
		System.exit(0);
	}
}