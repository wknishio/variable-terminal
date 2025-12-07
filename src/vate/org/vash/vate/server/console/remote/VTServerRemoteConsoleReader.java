package org.vash.vate.server.console.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;

import org.vash.vate.VTSystem;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

import vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;

public class VTServerRemoteConsoleReader extends VTTask
{
  private VTServerSession session;
  private VTServerConnection connection;
  private VTServerRemoteConsoleCommandSelector<VTServerRemoteConsoleCommandProcessor> selector;
  private final byte[] buffer = new byte[VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES];
  
  public VTServerRemoteConsoleReader(VTServerSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
    this.selector = new VTServerRemoteConsoleCommandSelector<VTServerRemoteConsoleCommandProcessor>(session);
  }
  
  public void task()
  {
    // int p = 0;
    while (!isStopped())
    {
      try
      {
        String line = connection.getCommandReader().readLine(buffer);
        executeCommand(line);
      }
      catch (Throwable e)
      {
        // e.printStackTrace();
        setStopped(true);
        break;
      }
    }
    synchronized (session)
    {
      session.notify();
    }
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
    if (!(command.length() == 0))
    {
      parsed = CommandLineTokenizerMKII.tokenize(command);
      if (parsed.length < 1)
      {
        parsed = new String[] { command };
      }
    }
    else
    {
      parsed = new String[] { "" };
    }
    
    // System.out.println("echoState:" + session.getEchoState());
    
    if (session.isEchoCommands())
    {
      if (command != null && !selector.remoteCommand(parsed[0]))
      {
        if (session.getEchoState() == 2)
        {
          connection.getResultWriter().write(command + "\n");
          connection.getResultWriter().flush();
        }
      }
    }
    else
    {
      if (command != null && !selector.remoteCommand(parsed[0]))
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
      if (!isStopped())
      {
        if (command != null && command.toUpperCase().startsWith("**VT"))
        {
          try
          {
            if (session.getEchoState() != 1)
            {
              session.getOutputWriter().setCommandFilter(command.substring(1));
            }
            session.getShellCommandExecutor().write(command.substring(1) + "\n");
            session.getShellCommandExecutor().flush();
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
          }
        }
        else
        {
          try
          {
            if (session.getEchoState() != 1)
            {
              session.getOutputWriter().setCommandFilter(command);
            }
            session.getShellCommandExecutor().write(command + "\n");
            session.getShellCommandExecutor().flush();
          }
          catch (Throwable e)
          {
            // e.printStackTrace();
          }
        }
      }
    }
  }
}