package org.vash.vate.client.console.remote;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.StringReader;
import java.nio.ByteBuffer;
import java.nio.charset.CharsetDecoder;

import org.vash.vate.VTSystem;
import org.vash.vate.client.connection.VTClientConnection;
import org.vash.vate.client.session.VTClientSession;
import org.vash.vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;
import org.vash.vate.console.VTMainConsole;
import org.vash.vate.runtime.VTRuntimeExit;
import org.vash.vate.stream.pipe.VTPipedInputStream;
import org.vash.vate.stream.pipe.VTPipedOutputStream;
import org.vash.vate.task.VTTask;

public class VTClientRemoteConsoleWriter extends VTTask
{
  private VTClientSession session;
  private VTClientConnection connection;
  private VTClientRemoteConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor> selector;
  private BufferedReader sourceReader;
  private final byte[] buffer = new byte[VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES * 4];
  private final CharsetDecoder decoder = VTSystem.getStrictCharsetDecoder("UTF-8");
  private InputStream commandInputStream;
  
  public VTClientRemoteConsoleWriter(VTClientSession session)
  {
    super(session.getExecutorService());
    this.session = session;
    this.connection = session.getConnection();
    this.selector = new VTClientRemoteConsoleCommandSelector<VTClientRemoteConsoleCommandProcessor>(session);
  }
  
  public void setCommandInputStream(InputStream stream)
  {
    this.commandInputStream = stream;
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
    VTPipedInputStream in = new VTPipedInputStream(VTSystem.VT_STANDARD_BUFFER_SIZE_BYTES);
    VTPipedOutputStream out = new VTPipedOutputStream(in);
    // source = new VTInterruptibleInputStream(in, session.getSessionThreads());
    sourceReader = new BufferedReader(new InputStreamReader(in, VTSystem.getFlexibleCharsetDecoder("UTF-8")));
    return out;
  }
  
  public void setCommandInputStream(InputStream in, String charsetName)
  {
    if (sourceReader != null)
    {
      return;
    }
    sourceReader = new BufferedReader(new InputStreamReader(in, VTSystem.getFlexibleCharsetDecoder(charsetName)));
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
    
    int length = 0;
    String line = null;
    StringBuilder quit = new StringBuilder();
    
    //reading = true;
    while (!isStopped())
    {
      // String[] lines;
      try
      {
        if (VTMainConsole.isDaemon())
        {
          if (sourceReader != null)
          {
            line = null;
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
            if (commandInputStream == null)
            {
              setStopped(true);
            }
            else
            {
              //pipe bytes from local data inputstream to remote data outputstream
              line = null;
              length = commandInputStream.read(buffer, 0, buffer.length);
              if (length > 0)
              {
                if (buffer[length - 1] == '\n' || buffer[length - 1] == '\r')
                {
                  try
                  {
                    if (length > 1 && buffer[length - 2] == '\r' && buffer[length - 1] == '\n')
                    {
                      line = decoder.decode(ByteBuffer.wrap(buffer, 0, length - 2)).toString();
                    }
                    else
                    {
                      line = decoder.decode(ByteBuffer.wrap(buffer, 0, length - 1)).toString();
                    }
                  }
                  catch (Throwable t)
                  {
                    
                  }
                  if (line != null)
                  {
                    quit.append(line);
                    if (quit.toString().equalsIgnoreCase("*VTQUIT") || quit.toString().equalsIgnoreCase("*VTQT"))
                    {
                      setStopped(true);
                      VTMainConsole.closeConsole();
                      session.getClient().stop();
                    }
                    quit.setLength(0);
                    executeCommand(line, false);
                  }
                  else
                  {
                    connection.getCommandWriter().writeData(buffer, 0, length);
                    connection.getCommandWriter().flush();
                  }
                }
                else
                {
                  if (quit.length() < "*VTQUIT".length())
                  {
                    try
                    {
                      quit.append(decoder.decode(ByteBuffer.wrap(buffer, 0, length)).toString());
                    }
                    catch (Throwable t)
                    {
                      
                    }
                  }
                  connection.getCommandWriter().writeData(buffer, 0, length);
                  connection.getCommandWriter().flush();
                }
              }
            }
          }
        }
        else
        {
          if (sourceReader != null)
          {
            line = null;
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
            line = VTMainConsole.readLine(true);
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
  
  public void executeFileScriptsSpaces(String scriptFiles, String charsetName, boolean echo)
  {
    // System.out.println("scripts:" + scripts);
    String[] scriptFilesArray = scriptFiles.split(" ");
    for (String scriptFile : scriptFilesArray)
    {
      executeFileScriptsCommas(scriptFile, charsetName, echo);
    }
  }
  
  public void executeFileScriptsCommas(String scriptFiles, String charsetName, boolean echo)
  {
    // System.out.println("scripts:" + scripts);
    String[] scriptFilesArray = scriptFiles.split(";");
    for (String scriptFile : scriptFilesArray)
    {
      executeFileScript(new File(scriptFile), charsetName, echo);
    }
  }
  
  private void executeFileScript(File script, String charsetName, boolean echo)
  {
    if (script == null || !script.exists())
    {
      // protection for recursion and bad file paths
      return;
    }
    BufferedReader reader = null;
    try
    {
      reader = new BufferedReader(new InputStreamReader(new FileInputStream(script), VTSystem.getFlexibleCharsetDecoder(charsetName)));
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
        VTMainConsole.println(command);
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
      
      if (!VTMainConsole.isCommandEcho())
      {
        if (command != null && command.length() > 0)
        {
          if (selector.matchCommand(parsed[0]))
          {
            VTMainConsole.println(command);
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