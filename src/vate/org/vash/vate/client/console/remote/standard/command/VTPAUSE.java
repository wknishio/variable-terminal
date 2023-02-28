package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTConsole;
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
        VTConsole.print("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        return;
      }
    }
    
    try
    {
      if (VTConsole.isDaemon() && pause == 0)
      {
        return;
      }
      if (!VTConsole.isDaemon())
      {
        if (pause > 0)
        {
          VTConsole.print("\nVT>Pausing local console for: [" + pause + "] ms, wait or resume with enter\nVT>");
        }
        else
        {
          VTConsole.print("\nVT>Pausing local console, resume with enter\nVT>");
        }
      }
      else
      {
        VTConsole.print("\nVT>Pausing local console for: [" + pause + "] ms\nVT>");
      }
      long start = System.nanoTime();
      VTConsole.createInterruptibleReadline(false, new Runnable()
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
        VTConsole.interruptReadLine();
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
        VTConsole.print("\nVT>Resuming local console after: [" + elapsed + "] ms\nVT>");
      }
    }
    catch (Throwable t)
    {
      
    }
  }
  
  public void close()
  {
    
  }
}
