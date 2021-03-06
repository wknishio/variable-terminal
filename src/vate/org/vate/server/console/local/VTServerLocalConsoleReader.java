package org.vate.server.console.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.util.HashSet;
import java.util.Set;

import org.vate.console.VTConsole;
import org.vate.server.VTServer;
import org.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTServerLocalConsoleReader extends VTTask
{
  // private String command;
  // private String[] splitCommand;
  private VTServer server;
  private VTServerLocalConsoleCommandSelector<VTServerLocalConsoleCommandProcessor> selector;

  public VTServerLocalConsoleReader(VTServer server)
  {
    this.server = server;
    this.selector = new VTServerLocalConsoleCommandSelector<VTServerLocalConsoleCommandProcessor>(server);
  }

  public void run()
  {
    // int p = 0;
    VTConsole.print("\rVT>Enter *VTHELP or *VTHLP to list available commands in server console\nVT>");
    while (true)
    {
      try
      {
        String line = VTConsole.readLine(true);
        String[] commands = line.split("\\*;");
        for (String command : commands)
        {
          executeCommand(command, null);
        }
      }
      catch (InterruptedException e)
      {
        // e.printStackTrace();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        VTConsole.print("\rVT>Error while processing command!\nVT>");
        // e.printStackTrace(VTConsole.getSystemOut());
        // return;
        /*
         * VTTerminal.setSystemErr(); VTTerminal.setSystemOut();
         * VTTerminal.setSystemIn(); e.printStackTrace();
         */
      }
      if (VTConsole.isDaemon())
      {
        Object waiter = VTConsole.getSynchronizationObject();
        synchronized (waiter)
        {
          while (VTConsole.isDaemon())
          {
            try
            {
              waiter.wait();
            }
            catch (Throwable e)
            {

            }
          }
        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void executeStringScript(String script, Set<String> stack)
  {
    if (stack == null)
    {
      stack = new HashSet<String>();
    }
    if (script == null || script.length() < 1 || !stack.add(script))
    {
      // protection for recursion and bad script string
      return;
    }
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new StringReader(script));
      String line = "";
      while (!stopped && (line = reader.readLine()) != null)
      {
        String[] commands = line.split("\\*;");
        for (String command : commands)
        {
          executeCommand(command, stack);
        }
      }
    }
    catch (Throwable t)
    {

    }
    finally
    {
      if (script != null)
      {
        stack.remove(script);
      }
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (Throwable t)
        {

        }
      }
    }
  }

  @SuppressWarnings("unused")
  private void executeFileScript(File script, Set<String> stack)
  {
    if (stack == null)
    {
      stack = new HashSet<String>();
    }
    if (script == null || !script.exists() || !stack.add(script.getAbsolutePath()))
    {
      // protection for recursion and bad file paths
      return;
    }
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(script)));
      String line = "";
      while (!stopped && (line = reader.readLine()) != null)
      {
        String[] commands = line.split("\\*;");
        for (String command : commands)
        {
          executeCommand(command, stack);
        }
      }
    }
    catch (Throwable t)
    {

    }
    finally
    {
      if (script != null)
      {
        stack.remove(script.getAbsolutePath());
      }
      if (reader != null)
      {
        try
        {
          reader.close();
        }
        catch (Throwable t)
        {

        }
      }
    }
  }

  private void executeCommand(String command, Set<String> stack) throws Throwable
  {
    String parsed[];
    if (command != null)
    {
      if (!(command.length() == 0))
      {
        parsed = CommandLineTokenizer.tokenize(command);
        if (parsed.length < 1)
        {
          parsed = new String[] { command };
          // p = 0;
          /*
           * for (String part : splitCommand) { splitCommand[p++] =
           * StringEscapeUtils.unescapeJava(part); }
           */
        }
      }
      else
      {
        parsed = new String[] { "" };
      }
      if (server.isEchoCommands())
      {
        VTConsole.print("VT>" + command + "\n");
      }
      if (!selector.selectCommand(command, parsed))
      {
        VTConsole.print("\rVT>");
      }
    }
    else
    {
      // System.out.println("bug?");
      // return;
      System.exit(0);
    }
  }
}