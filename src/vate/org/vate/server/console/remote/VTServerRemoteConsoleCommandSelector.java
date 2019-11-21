package org.vate.server.console.remote;

import java.util.LinkedHashSet;
import java.util.Set;

import org.vate.console.command.VTConsoleCommandSelector;
import org.vate.reflection.VTReflectionUtils;
import org.vate.server.session.VTServerSession;
import org.vate.server.console.remote.standard.command.*;

public class VTServerRemoteConsoleCommandSelector <T> extends VTConsoleCommandSelector<VTServerRemoteConsoleCommandProcessor>
{
	protected VTServerSession session;
	private static Set<Class<?>> knownStandardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<Class<?>> staticCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<String> additionalCustomCommandProcessorClasses = new LinkedHashSet<String>();
	//private static Set<String> additionalCustomCommandProcessorPackages = new LinkedHashSet<String>();
	
	static
	{
		try
		{
			knownStandardCommandProcessorClasses.add(VTACCESS.class);
			knownStandardCommandProcessorClasses.add(VTAUDIOLINK.class);
			knownStandardCommandProcessorClasses.add(VTAUDIOMIXERS.class);
			knownStandardCommandProcessorClasses.add(VTBEEP.class);
			knownStandardCommandProcessorClasses.add(VTBELL.class);
			knownStandardCommandProcessorClasses.add(VTBROWSE.class);
			knownStandardCommandProcessorClasses.add(VTCHAINS.class);
			knownStandardCommandProcessorClasses.add(VTCOVER.class);
			knownStandardCommandProcessorClasses.add(VTDISCONNECT.class);
			knownStandardCommandProcessorClasses.add(VTDISPLAYDEVICES.class);
			knownStandardCommandProcessorClasses.add(VTFILEINSPECT.class);
			knownStandardCommandProcessorClasses.add(VTFILEMODIFY.class);
			knownStandardCommandProcessorClasses.add(VTFILETRANSFER.class);
			knownStandardCommandProcessorClasses.add(VTGRAPHICSLINK.class);
			knownStandardCommandProcessorClasses.add(VTHELP.class);
			knownStandardCommandProcessorClasses.add(VTLOCK.class);
			knownStandardCommandProcessorClasses.add(VTMESSAGE.class);
			knownStandardCommandProcessorClasses.add(VTNETWORKINTERFACES.class);
			knownStandardCommandProcessorClasses.add(VTOPTICALDRIVE.class);
			knownStandardCommandProcessorClasses.add(VTPRINTDATA.class);
			knownStandardCommandProcessorClasses.add(VTPRINTERS.class);
			knownStandardCommandProcessorClasses.add(VTPROPERTY.class);
			knownStandardCommandProcessorClasses.add(VTQUIT.class);
			knownStandardCommandProcessorClasses.add(VTRATELIMIT.class);
			knownStandardCommandProcessorClasses.add(VTRESOLVEHOST.class);
			knownStandardCommandProcessorClasses.add(VTPRINTRUN.class);
			knownStandardCommandProcessorClasses.add(VTRUNTIME.class);
			knownStandardCommandProcessorClasses.add(VTSCREENALERT.class);
			knownStandardCommandProcessorClasses.add(VTSCREENSHOT.class);
			knownStandardCommandProcessorClasses.add(VTSESSIONS.class);
			knownStandardCommandProcessorClasses.add(VTSHELL.class);
			knownStandardCommandProcessorClasses.add(VTSOCKSTUNNEL.class);
			knownStandardCommandProcessorClasses.add(VTTCPTUNNEL.class);
			knownStandardCommandProcessorClasses.add(VTSTOP.class);
			knownStandardCommandProcessorClasses.add(VTTIME.class);
			knownStandardCommandProcessorClasses.add(VTVARIABLE.class);
			knownStandardCommandProcessorClasses.add(VTZIP.class);
			knownStandardCommandProcessorClasses.add(VTECHO.class);
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
//			Set<String> customClassNames = VTReflectionUtils.findClassNamesFromPackage("org.vate.server.console.remote.custom.command");
//			customClassNames.addAll(additionalCustomCommandProcessorClasses);
//			for (String pkg : additionalCustomCommandProcessorPackages)
//			{
//				customClassNames.addAll(VTReflectionUtils.findClassNamesFromPackage(pkg));
//			}
//			Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(customClassNames);
//			for (Class<?> clazz : customClasses)
//			{
//				if (VTServerRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
//				{
//					staticCommandProcessorClasses.add(clazz);
//				}
//			}
			Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(additionalCustomCommandProcessorClasses);
			for (Class<?> clazz : customClasses)
			{
				if (VTServerRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
				{
					staticCommandProcessorClasses.add(clazz);
				}
			}
		}
	}
	
	public VTServerRemoteConsoleCommandSelector(VTServerSession session)
	{
		this.session = session;
		Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(staticCommandProcessorClasses);
		for (Object instance : instances)
		{
			try
			{
				if (instance instanceof VTServerRemoteConsoleCommandProcessor)
				{
					VTServerRemoteConsoleCommandProcessor processor = ((VTServerRemoteConsoleCommandProcessor)instance);
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