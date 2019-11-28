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
	private static Set<Class<?>> knownStandardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<Class<?>> staticCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<String> additionalCustomCommandProcessorClasses = new LinkedHashSet<String>();
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
	}
	
	public static void addCustomCommandProcessorClass(String clazz)
	{
		additionalCustomCommandProcessorClasses.add(clazz);
	}
	
	//public static void addCustomCommandProcessorPackage(String pkg)
	//{
		//additionalCustomCommandProcessorPackages.add(pkg);
	//}
	
	public static void initialize()
	{
		if (staticCommandProcessorClasses.isEmpty())
		{
//			Set<Class<?>> standardClasses = VTReflectionUtils.findClassesFromNames(VTReflectionUtils.findClassNamesFromPackage("org.vate.server.console.local.standard.command"));
//			if (standardClasses.size() > 0)
//			{
//				staticCommandProcessorClasses.addAll(standardClasses);
//			}
//			else
//			{
//				staticCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
//			}
			staticCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
//			Set<String> customClassNames = VTReflectionUtils.findClassNamesFromPackage("org.vate.server.console.local.custom.command");
//			customClassNames.addAll(additionalCustomCommandProcessorClasses);
//			for (String pkg : additionalCustomCommandProcessorPackages)
//			{
//				customClassNames.addAll(VTReflectionUtils.findClassNamesFromPackage(pkg));
//			}
//			Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(customClassNames);
//			for (Class<?> clazz : customClasses)
//			{
//				if (VTServerLocalConsoleCommandProcessor.class.isAssignableFrom(clazz))
//				{
//					staticCommandProcessorClasses.add(clazz);
//				}
//			}
			Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(additionalCustomCommandProcessorClasses);
			for (Class<?> clazz : customClasses)
			{
				if (VTServerLocalConsoleCommandProcessor.class.isAssignableFrom(clazz))
				{
					staticCommandProcessorClasses.add(clazz);
				}
			}
		}
	}

	public VTServerLocalConsoleCommandSelector(VTServer server)
	{
		this.server = server;
		Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(staticCommandProcessorClasses);
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