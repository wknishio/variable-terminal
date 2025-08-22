package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTSystemConsole;
import org.vash.vate.help.VTHelpManager;

public class VTPAUSE extends VTClientStandardRemoteConsoleCommandProcessor
{
  public VTPAUSE()
  {
    this.setFullName("*VTPAUSE");
    this.setAbbreviatedName("*VTPS");
    this.setFullSyntax("*VTPAUSE [TIME]");
    this.setAbbreviatedSyntax("*VTPS [TM]");
  }
  
  public void execute(String command, String[] parsed) throws Exception
  {
    long pause = 0;
    if (parsed.length >= 2)
    {
      try
      {
        pause = Long.parseLong(parsed[1]);
        if (pause < 0)
        {
          pause = 0;
        }
      }
      catch (Throwable t)
      {
        VTSystemConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        return;
      }
    }
    
    try
    {
      if (VTSystemConsole.isDaemon() && pause == 0)
      {
        return;
      }
      if (!VTSystemConsole.isDaemon())
      {
        if (pause > 0)
        {
          VTSystemConsole.print("\nVT>Pausing local console for: [" + pause + "] ms, wait or resume with enter\nVT>");
        }
        else
        {
          VTSystemConsole.print("\nVT>Pausing local console, resume with enter\nVT>");
        }
      }
      else
      {
        VTSystemConsole.print("\nVT>Pausing local console for: [" + pause + "] ms\nVT>");
      }
      long start = System.nanoTime();
      VTSystemConsole.createInterruptibleReadline(false, session.getExecutorService(), new Runnable()
      {
        public void run()
        {
          synchronized (session)
          {
            session.notifyAll();
          }
        }
      });
      synchronized (session)
      {
        session.wait(pause);
      }
      try
      {
        VTSystemConsole.interruptReadLine();
      }
      catch (Throwable t)
      {
        
      }
      long end = System.nanoTime();
      long elapsed = pause;
      if (end > start)
      {
        elapsed = (end - start) / 1000000;
      }
      if (!session.isStopped())
      {
        VTSystemConsole.print("\nVT>Resuming local console after: [" + elapsed + "] ms\nVT>");
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close()
  {
    
  }
  
  public boolean remote()
  {
    return false;
  }
}
