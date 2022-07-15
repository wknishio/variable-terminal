package org.vash.vate.server.console.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTServerRemoteConsoleReader extends VTTask
{
  // private String command = "";
  // private String[] splitCommand;
  // private StringBuilder message;
  // private GregorianCalendar clock;
  // private DateFormat firstDateTimeFormat;
  // private DateFormat secondDateTimeFormat;
  private VTServerSession session;
  private VTServerConnection connection;
  private VTServerRemoteConsoleCommandSelector<VTServerRemoteConsoleCommandProcessor> selector;

  public VTServerRemoteConsoleReader(VTServerSession session)
  {
    this.session = session;
    this.connection = session.getConnection();
    this.stopped = false;
    // this.clock = new GregorianCalendar();
    // this.firstDateTimeFormat = new SimpleDateFormat("G", Locale.ENGLISH);
    // this.secondDateTimeFormat = new SimpleDateFormat("MM-dd][HH:mm:ss:SSS-z]");
    // this.command = "";
    // this.message = new StringBuilder();
    this.selector = new VTServerRemoteConsoleCommandSelector<VTServerRemoteConsoleCommandProcessor>(session);
  }

  public boolean isStopped()
  {
    return stopped;
  }

  public void setStopped(boolean stopped)
  {
    this.stopped = stopped;
  }

  public void run()
  {
    // int p = 0;
    while (!stopped)
    {
      try
      {
        String line = connection.getCommandReader().readLine();
        executeCommand(line);
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        stopped = true;
        break;
      }
    }
    synchronized (session)
    {
      session.notify();
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
//    if (stack == null)
//    {
//      stack = new HashSet<String>();
//    }
//    if (script == null || script.length() < 1 || !stack.add(script))
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
      while (!stopped && (line = reader.readLine()) != null)
      {
        executeCommand(line.trim());
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
  private void executeFileScript(File script, Charset charset)
  {
//    if (stack == null)
//    {
//      stack = new HashSet<String>();
//    }
//    if (script == null || !script.exists() || !stack.add(script.getAbsolutePath()))
    if (script == null || !script.exists())
    {
      // protection for recursion and bad file paths
      return;
    }
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(script), charset));
      String line = "";
      while (!stopped && (line = reader.readLine()) != null)
      {
        executeCommand(line.trim());
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
//        stack.remove(script.getAbsolutePath());
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

  private void executeCommand(String command) throws Throwable
  {
    String parsed[];
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
    
    //System.out.println("echoState:" + session.getEchoState());
    
    if (session.isEchoCommands())
    {
      if (command != null && !command.toUpperCase().startsWith("*VTECHO"))
      {
        connection.getResultWriter().write(command + "\n");
        connection.getResultWriter().flush();
      }
    }
    else
    {
      if (command != null && !command.toUpperCase().startsWith("*VTECHO"))
      {
        if (session.getEchoState() == 1)
        {
          if (selector.matchCommand(parsed[0]))
          {
            connection.getResultWriter().write(command + "\n");
            connection.getResultWriter().flush();
          }
          else
          {
            
          }
        }
      }
    }
    
    if (!selector.selectCommand(command, parsed))
    {
      if (!stopped)
      {
        if (command.startsWith("**") && command.toUpperCase().contains("**VT"))
        {
          try
          {
            session.getShellCommandExecutor().write(command.substring(1) + "\n");
            session.getShellCommandExecutor().flush();
          }
          catch (Throwable e)
          {
            //e.printStackTrace();
          }
        }
        else
        {
          try
          {
            session.getShellCommandExecutor().write(command + "\n");
            session.getShellCommandExecutor().flush();
          }
          catch (Throwable e)
          {
            //e.printStackTrace();
          }
        }
      }
    }
  }
}