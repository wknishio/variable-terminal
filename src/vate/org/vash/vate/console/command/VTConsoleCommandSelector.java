package org.vash.vate.console.command;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Set;

public class VTConsoleCommandSelector<T extends VTConsoleCommandProcessor>
{
  protected Set<T> processors = new LinkedHashSet<T>();
  
  public VTConsoleCommandSelector()
  {
    
  }
  
  public synchronized boolean addAllCommands(Collection<T> commands)
  {
    return processors.addAll(commands);
  }
  
  public synchronized boolean addCommand(T command)
  {
    return processors.add(command);
  }
  
  public synchronized boolean matchCommand(String command)
  {
    for (VTConsoleCommandProcessor processor : processors)
    {
      if (processor.match(command))
      {
        return true;
      }
    }
    return false;
  }
  
  public synchronized boolean remoteCommand(String command)
  {
    for (VTConsoleCommandProcessor processor : processors)
    {
      if (processor.match(command))
      {
        return processor.remote();
      }
    }
    return false;
  }
  
  public synchronized boolean selectCommand(String command, String[] parsed) throws Exception
  {
    for (VTConsoleCommandProcessor processor : processors)
    {
      if (processor.select(command, parsed))
      {
        return true;
      }
    }
    return false;
  }
  
  public synchronized void closeSelector()
  {
    for (VTConsoleCommandProcessor processor : processors)
    {
      try
      {
        processor.close();
      }
      catch (Throwable t)
      {
        
      }
    }
  }
  
  public synchronized String findHelp(String command)
  {
    for (VTConsoleCommandProcessor processor : processors)
    {
      if (processor.getFullName().toUpperCase().equals(command.toUpperCase()))
      {
        return processor.help(command);
      }
      if (processor.getAbbreviatedName().toUpperCase().equals(command.toUpperCase()))
      {
        return processor.help(command);
      }
    }
    return null;
  }
}