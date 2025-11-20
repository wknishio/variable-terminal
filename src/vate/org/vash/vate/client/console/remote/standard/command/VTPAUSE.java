package org.vash.vate.client.console.remote.standard.command;

import org.vash.vate.client.console.remote.standard.VTClientStandardRemoteConsoleCommandProcessor;
import org.vash.vate.console.VTMainConsole;
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
        VTMainConsole.print("\rVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(parsed[0]));
        return;
      }
    }
    
    try
    {
      if (VTMainConsole.isDaemon() && pause == 0)
      {
        return;
      }
      if (!VTMainConsole.isDaemon())
      {
        if (pause > 0)
        {
          VTMainConsole.print("\rVT>Pausing local console for: [" + pause + "] ms, wait or resume with enter\nVT>");
        }
        else
        {
          VTMainConsole.print("\rVT>Pausing local console, resume with enter\nVT>");
        }
      }
      else
      {
        VTMainConsole.print("\rVT>Pausing local console for: [" + pause + "] ms\nVT>");
      }
      long start = System.nanoTime();
      VTMainConsole.createInterruptibleReadline(false, session.getExecutorService(), new Runnable()
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
        VTMainConsole.interruptReadLine();
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
        VTMainConsole.print("\rVT>Resuming local console after: [" + elapsed + "] ms\nVT>");
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
