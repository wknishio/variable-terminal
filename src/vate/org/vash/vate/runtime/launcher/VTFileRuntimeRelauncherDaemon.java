package org.vash.vate.runtime.launcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

public class VTFileRuntimeRelauncherDaemon
{
  public static void main(String[] args) throws Exception
  {
    // VTNativeUtils.detachConsole();
    String[] files =
    { "relauncher.txt" };
    if (args.length > 0)
    {
      files = args;
    }
    for (String file : files)
    {
      BufferedReader input = null;
      try
      {
        input = new BufferedReader(new FileReader(file));
        String command = "";
        while (command != null)
        {
          command = input.readLine();
          final String currentCommand = command;
          Thread commandThread = new Thread()
          {
            public void run()
            {
              command(currentCommand);
            }
          };
          commandThread.start();
        }
        input.close();
      }
      catch (Throwable t)
      {
        
      }
      finally
      {
        if (input != null)
        {
          try
          {
            input.close();
          }
          catch (Throwable t)
          {
            
          }
        }
      }
    }
    BufferedReader input = null;
    try
    {
      input = new BufferedReader(new InputStreamReader(System.in));
      String command = "";
      while (command != null)
      {
        command = input.readLine();
        final String currentCommand = command;
        Thread commandThread = new Thread()
        {
          public void run()
          {
            command(currentCommand);
          }
        };
        commandThread.start();
      }
      input.close();
    }
    catch (Throwable t)
    {
      
    }
    finally
    {
      if (input != null)
      {
        try
        {
          input.close();
        }
        catch (Throwable t)
        {
          
        }
      }
    }
  }
  
  public static void command(String command)
  {
    while (true)
    {
      try
      {
        Thread.sleep(1000);
        Process process = Runtime.getRuntime().exec(command);
        VTRuntimeLauncherOutputConsumer cin = new VTRuntimeLauncherOutputConsumer(process.getInputStream());
        VTRuntimeLauncherOutputConsumer cerr = new VTRuntimeLauncherOutputConsumer(process.getErrorStream());
        Thread tin = new Thread(cin);
        Thread terr = new Thread(cerr);
        tin.start();
        terr.start();
        process.waitFor();
        cin.close();
        cerr.close();
        tin.join();
        terr.join();
      }
      catch (Throwable e)
      {
        
      }
    }
  }
}