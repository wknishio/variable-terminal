package org.vash.vate.server.console.local;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.vash.vate.VTSystem;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.server.VTServer;
import org.vash.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizerMKII;

public class VTServerLocalConsoleReader extends VTTask
{
  // private String command;
  // private String[] splitCommand;
  private VTServer server;
  private VTServerLocalConsoleCommandSelector<VTServerLocalConsoleCommandProcessor> selector;
  
  public VTServerLocalConsoleReader(VTServer server)
  {
    super(server.getExecutorService());
    this.server = server;
    this.selector = new VTServerLocalConsoleCommandSelector<VTServerLocalConsoleCommandProcessor>(server);
  }
  
  public void task()
  {
    // int p = 0;
    VTMainConsole.print("\rVT>Enter *VTHELP or *VTHL to list available commands in server console\nVT>");
    while (server.isRunning())
    {
      try
      {
        String line = VTMainConsole.readLine(true);
        executeCommand(line);
//        String[] commands = line.split("\\*;");
//        for (String command : commands)
//        {
//          executeCommand(command);
//        }
      }
      catch (InterruptedException e)
      {
        // e.printStackTrace();
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        VTMainConsole.print("\rVT>Error while processing command!\nVT>");
        // e.printStackTrace(VTConsole.getSystemOut());
        // return;
      }
    }
//    try
//    {
//      selector.closeSelector();
//    }
//    catch (Throwable e)
//    {
//      
//    }
  }
  
  @SuppressWarnings("unused")
  private void executeStringScript(String script)
  {
    if (script == null || script.length() < 1)
    {
      // protection for recursion and bad script string
      return;
    }
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new StringReader(script));
      String line = "";
      while (!isStopped() && (line = reader.readLine()) != null)
      {
        executeCommand(line);
//        String[] commands = line.split("\\*;");
//        for (String command : commands)
//        {
//          executeCommand(command);
//        }
      }
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
//      if (script != null)
//      {
//        stack.remove(script);
//      }
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
  private void executeFileScript(File script, String charsetName)
  {
    if (script == null || !script.exists())
    {
      // protection for recursion and bad file paths
      return;
    }
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(script), VTSystem.getCharsetDecoder(charsetName)));
      String line = "";
      while (!isStopped() && (line = reader.readLine()) != null)
      {
        executeCommand(line);
//        String[] commands = line.split("\\*;");
//        for (String command : commands)
//        {
//          executeCommand(command);
//        }
      }
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
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
  
  private void executeCommand(String command) throws Throwable
  {
    String parsed[];
    if (command != null)
    {
      if (!(command.length() == 0))
      {
        parsed = CommandLineTokenizerMKII.tokenize(command);
        if (parsed.length < 1)
        {
          parsed = new String[]
          { command };
          // p = 0;
          /*
           * for (String part : splitCommand) { splitCommand[p++] =
           * StringEscapeUtils.unescapeJava(part); }
           */
        }
      }
      else
      {
        parsed = new String[]
        { "" };
      }
      if (server.isEchoCommands())
      {
        VTMainConsole.print("VT>" + command + "\n");
      }
      if (!selector.selectCommand(command, parsed))
      {
        VTMainConsole.print("\rVT>");
      }
    }
    else
    {
      // System.out.println("bug?");
      // return;
      VTRuntimeExit.exit(0);
    }
  }
}