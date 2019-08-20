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
	private static Set<Class<?>> knownStandardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<Class<?>> staticCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<String> additionalCustomCommandProcessorClasses = new LinkedHashSet<String>();
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
			knownStandardCommandProcessorClasses.add(VTTERMINATE.class);
			knownStandardCommandProcessorClasses.add(VTZIP.class);
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
//			Set<Class<?>> standardClasses = VTReflectionUtils.findClassesFromNames(VTReflectionUtils.findClassNamesFromPackage("org.vate.server.console.remote.standard.command"));
//			if (standardClasses.size() > 0)
//			{
//				staticCommandProcessorClasses.addAll(standardClasses);
//			}
//			else
//			{
//				staticCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
//			}
			staticCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
//			Set<String> customClassNames = VTReflectionUtils.findClassNamesFromPackage("org.vate.client.console.remote.custom.command");
//			customClassNames.addAll(additionalCustomCommandProcessorClasses);
//			for (String pkg : additionalCustomCommandProcessorPackages)
//			{
//				customClassNames.addAll(VTReflectionUtils.findClassNamesFromPackage(pkg));
//			}
//			Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(customClassNames);
//			for (Class<?> clazz : customClasses)
//			{
//				if (VTClientRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
//				{
//					staticCommandProcessorClasses.add(clazz);
//				}
//			}
			Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(additionalCustomCommandProcessorClasses);
			for (Class<?> clazz : customClasses)
			{
				if (VTClientRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
				{
					staticCommandProcessorClasses.add(clazz);
				}
			}
		}
	}
	
	public VTClientRemoteConsoleCommandSelector(VTClientSession session)
	{
		this.session = session;
		Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(staticCommandProcessorClasses);
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