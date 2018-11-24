package org.vate.runtime;

public class VTArgumentsRuntimeLauncher
{
	public static void main(String[] args) throws Exception
	{
		Thread.sleep(2000);
		Runtime.getRuntime().exec(args);
		System.exit(0);
	}
}