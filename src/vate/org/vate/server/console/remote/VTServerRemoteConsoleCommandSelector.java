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
	private static Set<Class<?>> installedCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	private static Set<Class<?>> knownStandardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
	//private static Set<String> additionalCustomCommandProcessorClasses = new LinkedHashSet<String>();
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
			knownStandardCommandProcessorClasses.add(VTDISPLAYS.class);
			knownStandardCommandProcessorClasses.add(VTFILEINSPECT.class);
			knownStandardCommandProcessorClasses.add(VTFILEMODIFY.class);
			knownStandardCommandProcessorClasses.add(VTFILETRANSFER.class);
			knownStandardCommandProcessorClasses.add(VTGRAPHICSLINK.class);
			knownStandardCommandProcessorClasses.add(VTHELP.class);
			knownStandardCommandProcessorClasses.add(VTLOCK.class);
			knownStandardCommandProcessorClasses.add(VTMESSAGE.class);
			knownStandardCommandProcessorClasses.add(VTNETWORKS.class);
			knownStandardCommandProcessorClasses.add(VTOPTICALDRIVE.class);
			knownStandardCommandProcessorClasses.add(VTDATAPRINT.class);
			knownStandardCommandProcessorClasses.add(VTPRINTERS.class);
			knownStandardCommandProcessorClasses.add(VTPROPERTY.class);
			knownStandardCommandProcessorClasses.add(VTQUIT.class);
			knownStandardCommandProcessorClasses.add(VTRATELIMIT.class);
			knownStandardCommandProcessorClasses.add(VTHOSTRESOLVE.class);
			knownStandardCommandProcessorClasses.add(VTRUNPRINT.class);
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
		installedCommandProcessorClasses.addAll(knownStandardCommandProcessorClasses);
	}
	
	public static void addCustomCommandProcessorClass(String className)
	{
		try
		{
			Class<?> clazz = Class.forName(className);
			if (VTServerRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
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
			if (VTServerRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
			{
				installedCommandProcessorClasses.add(clazz);
			}
		}
	}
		
	public VTServerRemoteConsoleCommandSelector(VTServerSession session)
	{
		this.session = session;
		Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(installedCommandProcessorClasses);
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