package org.vash.vate.runtime.launcher;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.InputStreamReader;

import org.vash.vate.VTSystem;
import org.vash.vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;
import org.vash.vate.nativeutils.VTMainNativeUtils;

public class VTFileRuntimeLauncher
{
  public static void main(String[] args) throws Exception
  {
    String[] files =
    { "launcher.txt" };
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
              command(CommandLineTokenizerMKII.tokenize(currentCommand));
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
      input = new BufferedReader(new InputStreamReader(System.in, VTSystem.getFlexibleCharsetDecoder(null)));
      String command = "";
      while (command != null)
      {
        command = input.readLine();
        final String currentCommand = command;
        Thread commandThread = new Thread()
        {
          public void run()
          {
            command(CommandLineTokenizerMKII.tokenize(currentCommand));
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
  
  public static void command(String[] command)
  {
    try
    {
      Thread.sleep(1000);
      VTMainNativeUtils.executeRuntime(command);
    }
    catch (Throwable e)
    {
      
    }
  }
}