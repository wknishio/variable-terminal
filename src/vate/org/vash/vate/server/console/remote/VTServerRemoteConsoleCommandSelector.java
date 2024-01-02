package org.vash.vate.server.console.remote;

import java.util.LinkedHashSet;
import java.util.Set;

import org.vash.vate.console.command.VTConsoleCommandSelector;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.server.console.remote.standard.command.*;

public class VTServerRemoteConsoleCommandSelector<T> extends VTConsoleCommandSelector<VTServerRemoteConsoleCommandProcessor>
{
  protected VTServerSession session;
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
      standardCommandProcessorClasses.add(VTSETTING.class);
      standardCommandProcessorClasses.add(VTAUDIOLINK.class);
      standardCommandProcessorClasses.add(VTMIXERS.class);
      standardCommandProcessorClasses.add(VTBEEP.class);
      standardCommandProcessorClasses.add(VTBELL.class);
      standardCommandProcessorClasses.add(VTBROWSE.class);
      standardCommandProcessorClasses.add(VTCHAIN.class);
      standardCommandProcessorClasses.add(VTCOVER.class);
      standardCommandProcessorClasses.add(VTEXIT.class);
      standardCommandProcessorClasses.add(VTDISPLAYS.class);
      standardCommandProcessorClasses.add(VTFILECHECK.class);
      standardCommandProcessorClasses.add(VTFILEALTER.class);
      standardCommandProcessorClasses.add(VTFILETRANSFER.class);
      standardCommandProcessorClasses.add(VTGRAPHICSLINK.class);
      standardCommandProcessorClasses.add(VTHELP.class);
      standardCommandProcessorClasses.add(VTLOCK.class);
      standardCommandProcessorClasses.add(VTTEXT.class);
      standardCommandProcessorClasses.add(VTNETWORKS.class);
      standardCommandProcessorClasses.add(VTDISCTRAY.class);
      standardCommandProcessorClasses.add(VTPRINTERS.class);
      standardCommandProcessorClasses.add(VTPRINTDATA.class);
      standardCommandProcessorClasses.add(VTPROPERTY.class);
      standardCommandProcessorClasses.add(VTQUIT.class);
      standardCommandProcessorClasses.add(VTLIMIT.class);
      standardCommandProcessorClasses.add(VTHOSTS.class);
      // standardCommandProcessorClasses.add(VTCALLPRINT.class);
      standardCommandProcessorClasses.add(VTMAIL.class);
      standardCommandProcessorClasses.add(VTRUNTIME.class);
      standardCommandProcessorClasses.add(VTSCREENALERT.class);
      standardCommandProcessorClasses.add(VTSCREENSHOT.class);
      standardCommandProcessorClasses.add(VTUSERS.class);
      standardCommandProcessorClasses.add(VTSHELL.class);
      // standardCommandProcessorClasses.add(VTSOCKSTUNNEL.class);
      // standardCommandProcessorClasses.add(VTTCPTUNNEL.class);
      standardCommandProcessorClasses.add(VTTUNNEL.class);
      standardCommandProcessorClasses.add(VTSTOP.class);
      standardCommandProcessorClasses.add(VTTIME.class);
      standardCommandProcessorClasses.add(VTVARIABLE.class);
      // standardCommandProcessorClasses.add(VTZIP.class);
      // standardCommandProcessorClasses.add(VTURLGET.class);
      standardCommandProcessorClasses.add(VTECHO.class);
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
          VTServerRemoteConsoleCommandProcessor processor = ((VTServerRemoteConsoleCommandProcessor) instance);
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