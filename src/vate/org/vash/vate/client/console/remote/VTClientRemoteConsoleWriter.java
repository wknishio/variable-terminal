package org.vash.vate.client.console.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.charset.Charset;

import org.vash.vate.VT;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.console.VTConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;
import org.vash.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizerMKII;

public class VTClientRemoteConsoleWriter extends VTTask
{
  private VTClientSession session;
  private VTClientConnection connection;
  private VTClientRemoteConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor> selector;
  // private VTInterruptibleInputStream source;
  private BufferedReader sourceReader;
  private boolean reading = false;
  // private BufferedReader
  
  public VTClientRemoteConsoleWriter(VTClientSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
    this.selector = new VTClientRemoteConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor>(session);
  }
  
  public boolean isReading()
  {
    return reading;
  }
  
  public void setStopped(boolean stopped)
  {
    super.setStopped(stopped);
    if (stopped)
    {
      if (sourceReader != null)
      {
        try
        {
          sourceReader.close();
        }
        catch (Throwable e)
        {
          
        }
      }
    }
  }
  
  public OutputStream createCommandOutputStream() throws IOException
  {
    if (sourceReader != null)
    {
      return null;
    }
    VTPipedInputStream in = new VTPipedInputStream(VT.VT_STANDARD_BUFFER_SIZE_BYTES);
    VTPipedOutputStream out = new VTPipedOutputStream(in);
    // source = new VTInterruptibleInputStream(in, session.getSessionThreads());
    sourceReader = new BufferedReader(new InputStreamReader(in, "UTF-8"));
    return out;
  }
  
  public void setCommandInputStream(InputStream in, Charset charset)
  {
    if (sourceReader != null)
    {
      return;
    }
    sourceReader = new BufferedReader(new InputStreamReader(in, charset));
  }
  
  public void task()
  {
    // first run all commands in session commands parameter
    if (!isStopped())
    {
      String commands = session.getClient().getClientConnector().getSessionCommands();
      if (commands != null && commands.length() > 0)
      {
        executeStringScript(commands.replace("*;", "\n"), false);
      }
    }
    
    reading = true;
    while (!isStopped())
    {
      // String[] lines;
      try
      {
        if (VTConsole.isDaemon())
        {
          if (sourceReader != null)
          {
            String line = null;
            try
            {
              line = sourceReader.readLine();
            }
            catch (Throwable t)
            {
              
            }
            if (line == null)
            {
              sourceReader = null;
              continue;
            }
            executeCommand(line, true);
          }
          else
          {
            setStopped(true);
            // Thread.sleep(1);
            //synchronized (session)
            //{
              //session.wait();
            //}
          }
        }
        else
        {
          if (sourceReader != null)
          {
            String line = null;
            try
            {
              line = sourceReader.readLine();
            }
            catch (Throwable t)
            {
              
            }
            if (line == null)
            {
              sourceReader = null;
              continue;
            }
            executeCommand(line, true);
          }
          else
          {
            String line = VTConsole.readLine(true);
            executeCommand(line, false);
          }
        }
      }
      catch (InterruptedException e)
      {
        // e.printStackTrace();
        setStopped(true);
        break;
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
      session.notifyAll();
    }
  }
  
  public void executeStringScripts(String scriptsString, boolean echo)
  {
    // System.out.println("scripts:" + scripts);
    executeStringScript(scriptsString, echo);
  }
  
  private void executeStringScript(String script, boolean echo)
  {
    // if (script == null || script.length() < 1 || !stack.add(script))
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
        executeCommand(line, echo);
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
  
  public void executeFileScriptsSpaces(String scriptFiles, Charset charset, boolean echo)
  {
    // System.out.println("scripts:" + scripts);
    String[] scriptFilesArray = scriptFiles.split(" ");
    for (String scriptFile : scriptFilesArray)
    {
      executeFileScriptsCommas(scriptFile, charset, echo);
    }
  }
  
  public void executeFileScriptsCommas(String scriptFiles, Charset charset, boolean echo)
  {
    // System.out.println("scripts:" + scripts);
    String[] scriptFilesArray = scriptFiles.split(";");
    for (String scriptFile : scriptFilesArray)
    {
      executeFileScript(new File(scriptFile), charset, echo);
    }
  }
  
  private void executeFileScript(File script, Charset charset, boolean echo)
  {
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
      while (!isStopped() && (line = reader.readLine()) != null)
      {
        executeCommand(line, echo);
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
  
  private void executeCommand(String command, boolean echo) throws Throwable
  {
    String parsed[];
    if (session.getClient().getClientConnector().isSkipConfiguration())
    {
      setStopped(true);
      return;
    }
    else if (command != null)
    {
      if (echo)
      {
        VTConsole.println(command);
      }
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
      
      if (!VTConsole.isCommandEcho())
      {
        if (command != null && command.length() > 0)
        {
          if (selector.matchCommand(parsed[0]))
          {
            VTConsole.println(command);
          }
        }
      }
      
      if (!selector.selectCommand(command, parsed))
      {
        connection.getCommandWriter().writeLine(command);
        connection.getCommandWriter().flush();
      }
    }
    else
    {
      VTRuntimeExit.exit(0);
    }
  }
}