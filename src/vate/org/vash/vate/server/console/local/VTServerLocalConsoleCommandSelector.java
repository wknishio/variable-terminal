package org.vash.vate.server.console.local;

import java.util.LinkedHashSet;
import java.util.Set;

import org.vash.vate.console.command.VTConsoleCommandSelector;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.server.VTServer;
import org.vash.vate.server.console.local.standard.command.*;

public class VTServerLocalConsoleCommandSelector<T> extends VTConsoleCommandSelector<VTServerLocalConsoleCommandProcessor>
{
  protected VTServer server;
  private static Set<Class<?>> installedCommandProcessorClasses = new LinkedHashSet<Class<?>>();
  private static Set<Class<?>> standardCommandProcessorClasses = new LinkedHashSet<Class<?>>();
  // private static Set<String> additionalCustomCommandProcessorPackages = new
  // LinkedHashSet<String>();
  
  static
  {
    try
    {
      standardCommandProcessorClasses.add(VTSETTING.class);
      standardCommandProcessorClasses.add(VTMIXER.class);
      standardCommandProcessorClasses.add(VTCLEAR.class);
      standardCommandProcessorClasses.add(VTCOVER.class);
      standardCommandProcessorClasses.add(VTKICK.class);
      standardCommandProcessorClasses.add(VTDISPLAY.class);
      standardCommandProcessorClasses.add(VTFILEROOT.class);
      standardCommandProcessorClasses.add(VTHELP.class);
      //standardCommandProcessorClasses.add(VTLOCK.class);
      standardCommandProcessorClasses.add(VTTEXT.class);
      standardCommandProcessorClasses.add(VTNETWORK.class);
      standardCommandProcessorClasses.add(VTPING.class);
      standardCommandProcessorClasses.add(VTPRINTER.class);
      standardCommandProcessorClasses.add(VTPROPERTY.class);
      standardCommandProcessorClasses.add(VTUSER.class);
      standardCommandProcessorClasses.add(VTSTOP.class);
      standardCommandProcessorClasses.add(VTDATE.class);
      standardCommandProcessorClasses.add(VTVARIABLE.class);
      standardCommandProcessorClasses.add(VTCONFIGURE.class);
      // knownStandardCommandProcessorClasses.add(VTECHO.class);
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
          VTServerLocalConsoleCommandProcessor processor = ((VTServerLocalConsoleCommandProcessor) instance);
          processor.setServer(server);
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