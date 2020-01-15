package org.vate.client.console.remote;

import java.util.LinkedHashSet;
import java.util.Set;

import org.vate.client.session.VTClientSession;
import org.vate.console.command.VTConsoleCommandSelector;
import org.vate.reflection.VTReflectionUtils;
import org.vate.client.console.remote.standard.command.*;

public class VTClientRemoteConsoleCommandSelector<T> extends VTConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor>
{
	protected VTClientSession session;
	private static Set<Class<?>> installedCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<Class<?>> knownStandardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	//private static Set<String> additionalCustomCommandProcessorClasses = new LinkedHashSet<String>();
	//private static Set<String> additionalCustomCommandProcessorPackages = new LinkedHashSet<String>();
	
	static
	{
		try
		{
			knownStandardCommandProcessorClasses.add(VTAUDIOLINK.class);
			knownStandardCommandProcessorClasses.add(VTAUDIOMIXERS.class);
			knownStandardCommandProcessorClasses.add(VTCLEAR.class);
			knownStandardCommandProcessorClasses.add(VTDISCONNECT.class);
			knownStandardCommandProcessorClasses.add(VTFILETRANSFER.class);
			knownStandardCommandProcessorClasses.add(VTGRAPHICSLINK.class);
			knownStandardCommandProcessorClasses.add(VTPING.class);
			knownStandardCommandProcessorClasses.add(VTQUIT.class);
			knownStandardCommandProcessorClasses.add(VTRATELIMIT.class);
			knownStandardCommandProcessorClasses.add(VTSAVE.class);
			knownStandardCommandProcessorClasses.add(VTSOCKSTUNNEL.class);
			knownStandardCommandProcessorClasses.add(VTTCPTUNNEL.class);
			knownStandardCommandProcessorClasses.add(VTSTOP.class);
			knownStandardCommandProcessorClasses.add(VTZIP.class);
			knownStandardCommandProcessorClasses.add(VTECHO.class);
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public static void addCustomCommandProcessorClass(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if (VTClientRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
			{
				installedCommandProcessorClasses.add(clazz);
			}
		}
		catch (Throwable t)
		{
			
		}
	}
	
	public static void addCustomCommandProcessorPackage(String pkg)
	{
		//additionalCustomCommandProcessorPackages.add(pkg);
		Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(VTReflectionUtils.findClassNamesFromPackage(pkg));
		for (Class<?> clazz : customClasses)
		{
			if (VTClientRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
			{
				installedCommandProcessorClasses.add(clazz);
			}
		}
	}
	
	public static void initialize()
	{
		if (installedCommandProcessorClasses.isEmpty())
		{
			installedCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
		}
	}
	
	public VTClientRemoteConsoleCommandSelector(VTClientSession session)
	{
		this.session = session;
		Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(installedCommandProcessorClasses);
		for (Object instance : instances)
		{
			try
			{
				if (instance instanceof VTClientRemoteConsoleCommandProcessor)
				{
					VTClientRemoteConsoleCommandProcessor processor = ((VTClientRemoteConsoleCommandProcessor)instance);
					processor.setSession(session);
					this.addCommand(processor);
				}
			}
			catch (Throwable t)
			{
				
			}
		}
	}
}