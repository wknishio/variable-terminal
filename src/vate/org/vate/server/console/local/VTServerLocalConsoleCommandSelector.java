package org.vate.server.console.local;

import java.util.LinkedHashSet;
import java.util.Set;

import org.vate.console.command.VTConsoleCommandSelector;
import org.vate.reflection.VTReflectionUtils;
import org.vate.server.VTServer;
import org.vate.server.console.local.standard.command.*;

public class VTServerLocalConsoleCommandSelector <T> extends VTConsoleCommandSelector<VTServerLocalConsoleCommandProcessor>
{
	protected VTServer server;
	private static Set<Class<?>> installedCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<Class<?>> knownStandardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	//private static Set<String> additionalCustomCommandProcessorPackages = new LinkedHashSet<String>();
	
	static
	{
		try
		{
			knownStandardCommandProcessorClasses.add(VTACCESS.class);
			knownStandardCommandProcessorClasses.add(VTAUDIOMIXERS.class);
			knownStandardCommandProcessorClasses.add(VTCLEAR.class);
			knownStandardCommandProcessorClasses.add(VTCOVER.class);
			knownStandardCommandProcessorClasses.add(VTDISCONNECT.class);
			knownStandardCommandProcessorClasses.add(VTDISPLAYS.class);
			knownStandardCommandProcessorClasses.add(VTFILEROOTS.class);
			knownStandardCommandProcessorClasses.add(VTHELP.class);
			knownStandardCommandProcessorClasses.add(VTLOCK.class);
			knownStandardCommandProcessorClasses.add(VTMESSAGE.class);
			knownStandardCommandProcessorClasses.add(VTNETWORKS.class);
			knownStandardCommandProcessorClasses.add(VTPING.class);
			knownStandardCommandProcessorClasses.add(VTPRINTERS.class);
			knownStandardCommandProcessorClasses.add(VTPROPERTY.class);
			knownStandardCommandProcessorClasses.add(VTSESSIONS.class);
			knownStandardCommandProcessorClasses.add(VTSTOP.class);
			knownStandardCommandProcessorClasses.add(VTTIME.class);
			knownStandardCommandProcessorClasses.add(VTVARIABLE.class);
			//knownStandardCommandProcessorClasses.add(VTECHO.class);
		}
		catch (Throwable t)
		{
			
		}
		installedCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
	}
		
	public static void addCustomCommandProcessorClass(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if (VTServerLocalConsoleCommandProcessor.class.isAssignableFrom(clazz))
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
		Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(VTReflectionUtils.findClassNamesFromPackage(pkg));
		for (Class<?> clazz : customClasses)
		{
			if (VTServerLocalConsoleCommandProcessor.class.isAssignableFrom(clazz))
			{
				installedCommandProcessorClasses.add(clazz);
			}
		}
	}

	
	public VTServerLocalConsoleCommandSelector(VTServer server)
	{
		this.server = server;
		Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(installedCommandProcessorClasses);
		for (Object instance : instances)
		{
			try
			{
				if (instance instanceof VTServerLocalConsoleCommandProcessor)
				{
					VTServerLocalConsoleCommandProcessor processor = ((VTServerLocalConsoleCommandProcessor)instance);
					processor.setServer(server);
					this.addCommand(processor);
				}
			}
			catch (Throwable t)
			{
				
			}
		}
	}
}