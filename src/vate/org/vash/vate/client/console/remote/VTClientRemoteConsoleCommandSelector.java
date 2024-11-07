package org.vash.vate.client.console.remote;

import java.util.LinkedHashSet;
import java.util.Set;

import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.command.VTConsoleCommandSelector;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.client.console.remote.standard.command.*;

public class VTClientRemoteConsoleCommandSelector<T> extends VTConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor>
{
  protected VTClientSession session;
  private static Set<Class<?>> installedCommandProcessorClasses = new LinkedHashSet<Class<?>>();
  private static Set<Class<?>> standardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
  // private static Set<String> additionalCustomCommandProcessorClasses = new
  // LinkedHashSet<String>();
  // private static Set<String> additionalCustomCommandProcessorPackages = new
  // LinkedHashSet<String>();
  
  static
  {
    try
    {
      standardCommandProcessorClasses.add(VTAUDIOLINK.class);
      standardCommandProcessorClasses.add(VTMIXER.class);
      standardCommandProcessorClasses.add(VTCLEAR.class);
      standardCommandProcessorClasses.add(VTEXIT.class);
      standardCommandProcessorClasses.add(VTFILETRANSFER.class);
      standardCommandProcessorClasses.add(VTGRAPHICSLINK.class);
      standardCommandProcessorClasses.add(VTPING.class);
      standardCommandProcessorClasses.add(VTQUIT.class);
      standardCommandProcessorClasses.add(VTLIMIT.class);
      standardCommandProcessorClasses.add(VTSAVE.class);
      // standardCommandProcessorClasses.add(VTSOCKSTUNNEL.class);
      // standardCommandProcessorClasses.add(VTTCPTUNNEL.class);
      standardCommandProcessorClasses.add(VTTUNNEL.class);
      standardCommandProcessorClasses.add(VTSTOP.class);
      // standardCommandProcessorClasses.add(VTZIP.class);
      standardCommandProcessorClasses.add(VTPAUSE.class);
      standardCommandProcessorClasses.add(VTHELP.class);
      standardCommandProcessorClasses.add(VTREAD.class);
      standardCommandProcessorClasses.add(VTLOG.class);
      standardCommandProcessorClasses.add(VTOUT.class);
      standardCommandProcessorClasses.add(VTECHO.class);
      standardCommandProcessorClasses.add(VTNETWORK.class);
    }
    catch (Throwable t)
    {
      
    }
    installedCommandProcessorClasses.addAll(standardCommandProcessorClasses);
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
    // additionalCustomCommandProcessorPackages.add(pkg);
    Set<Class<?>> customClasses = VTReflectionUtils.findClassesFromNames(VTReflectionUtils.findClassNamesFromPackage(pkg));
    for (Class<?> clazz : customClasses.toArray(new Class<?>[] {}))
    {
      if (VTClientRemoteConsoleCommandProcessor.class.isAssignableFrom(clazz))
      {
        installedCommandProcessorClasses.add(clazz);
      }
    }
  }
  
  public VTClientRemoteConsoleCommandSelector(VTClientSession session)
  {
    this.session = session;
    Set<Object> instances = VTReflectionUtils.createClassInstancesFromClasses(installedCommandProcessorClasses);
    for (Object instance : instances.toArray())
    {
      try
      {
        if (instance instanceof VTClientRemoteConsoleCommandProcessor)
        {
          VTClientRemoteConsoleCommandProcessor processor = ((VTClientRemoteConsoleCommandProcessor) instance);
          processor.setSession(session);
          processor.setSelector(this);
          this.addCommand(processor);
        }
      }
      catch (Throwable t)
      {
        
      }
    }
  }
}