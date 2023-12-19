package org.vash.vate.server.runtime;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.vash.vate.help.VTHelpManager;
import org.vash.vate.nativeutils.VTNativeUtils;
import org.vash.vate.runtime.VTRuntimeProcess;
import org.vash.vate.server.connection.VTServerConnection;
import org.vash.vate.server.session.VTServerSession;
import org.vash.vate.task.VTTask;

import com.martiansoftware.jsap.CommandLineTokenizer;

public class VTServerRuntimeExecutor extends VTTask
{
  private volatile boolean finished;
  private boolean found;
  private int i;
  /* private int x; private int y; private int z; */
  private String command;
  private String[] splitCommand;
  private StringBuilder message;
  // private VTServer server;
  private VTServerSession session;
  private VTServerConnection connection;
  private List<VTRuntimeProcess> processList;
  private List<VTRuntimeProcess> removedProcessStack;
  private File runtimeDirectory;
  
  private static int PROCESS_COMMAND_UNKNOWN = -1; // ?
  private static int PROCESS_COMMAND_MANAGED = 1; // M
  private static int PROCESS_COMMAND_FREE = 2; // F
  private static int PROCESS_COMMAND_LIST = 3; // L
  private static int PROCESS_COMMAND_STOP = 4; // S
  private static int PROCESS_COMMAND_DESTROY = 5; // D
  private static int PROCESS_COMMAND_INPUT = 6; // I
  //private static int PROCESS_COMMAND_LINE = 7; // E
  private static int PROCESS_COMMAND_PATH = 7; // P
  private static int PROCESS_COMMAND_BASE64 = 8; // B
  private static int PROCESS_COMMAND_UTF8 = 9; // U
  
  private static int PROCESS_SCOPE_NOT_FOUND = -1; // ?
  private static int PROCESS_SCOPE_ALL = 1; // A
  private static int PROCESS_SCOPE_COMMAND = 2; // C
  private static int PROCESS_SCOPE_NUMBER = 3; // N
  
  // private static int PROCESS_VERBOSE_OFF = 1; //?
  // private static int PROCESS_VERBOSE_ON = 2; //V
  
  // private static int PROCESS_RESTART_OFF = 1; //?
  // private static int PROCESS_RESTART_ON = 2; //R
  
  public VTServerRuntimeExecutor(VTServerSession session)
  {
    // this.server = session.getServer();
    this.session = session;
    this.connection = session.getConnection();
    this.finished = true;
//    this.processList = Collections.synchronizedList(new ArrayList<VTRuntimeProcess>());
//    this.removedProcessStack = Collections.synchronizedList(new ArrayList<VTRuntimeProcess>());
    this.processList = new ArrayList<VTRuntimeProcess>();
    this.removedProcessStack = new ArrayList<VTRuntimeProcess>();
    this.message = new StringBuilder();
  }
  
  public File getRuntimeBuilderWorkingDirectory()
  {
    return runtimeDirectory;
  }
  
  public void setRuntimeBuilderWorkingDirectory(File runtimeDirectory)
  {
    this.runtimeDirectory = runtimeDirectory;
  }
  
  public boolean isFinished()
  {
    return finished;
  }
  
  public void setFinished(boolean finished)
  {
    this.finished = finished;
  }
  
  public String getCommand()
  {
    return command;
  }
  
  public void setCommand(String command)
  {
    this.command = command;
  }
  
  public void clear()
  {
    for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
    {
      //process.setRestart(false);
      //process.stop();
      process.destroy();
    }
    processList.clear();
  }
  
  /*
   * private void split() { y = 0; z = 0; for (x = 0; x < command.length(); x++)
   * { if (command.charAt(x) == '|') { y++; } } splitCommand = new String[++y];
   * y = 0; for (x = 0; x <= command.length(); x++) { if ((x ==
   * command.length()) || (command.charAt(x) == '|')) { splitCommand[y++] =
   * command.substring(z, x); z = ++x; } } return; }
   */
  
  public void run()
  {
    try
    {
      splitCommand = CommandLineTokenizer.tokenize(command);
      // i = 0;
      /*
       * for (String part : splitCommand) { splitCommand[i++] =
       * StringEscapeUtils.unescapeJava(part); }
       */
      int process_command = PROCESS_COMMAND_UNKNOWN;
      int process_scope = PROCESS_SCOPE_NOT_FOUND;
      boolean process_verbose = false;
      boolean process_restart = false;
      // boolean process_timeout = false;
      boolean need_managed_scope = false;
      int parameter_amount = 2;
      int timeout_value = 0;
      
      if (splitCommand.length >= 2)
      {
        String main_command_string = splitCommand[1].toUpperCase();
        
        if (main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_MANAGED;
        }
        else if (!main_command_string.contains("M")
        && main_command_string.contains("F")
        &&!main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_FREE;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_LIST;
          need_managed_scope = true;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_STOP;
          need_managed_scope = true;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_DESTROY;
          need_managed_scope = true;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_INPUT;
          need_managed_scope = true;
          parameter_amount += 1;
        }
//        else if (!main_command_string.contains("M")
//        && !main_command_string.contains("F")
//        && !main_command_string.contains("L")
//        && !main_command_string.contains("S")
//        && !main_command_string.contains("D")
//        && !main_command_string.contains("I")
//        && main_command_string.contains("E")
//        && !main_command_string.contains("P")
//        && !main_command_string.contains("B")
//        && !main_command_string.contains("U"))
//        {
//          process_command = PROCESS_COMMAND_LINE;
//          need_managed_scope = true;
//          parameter_amount += 1;
//        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_PATH;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_BASE64;
          need_managed_scope = true;
          parameter_amount += 1;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        && !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_UTF8;
          need_managed_scope = true;
          parameter_amount += 1;
        }
        
        if (need_managed_scope)
        {
          if (main_command_string.contains("A")
          && !main_command_string.contains("C")
          && !main_command_string.contains("O"))
          {
            process_scope = PROCESS_SCOPE_ALL;
          }
          else if (!main_command_string.contains("A")
          && main_command_string.contains("C")
          && !main_command_string.contains("O"))
          {
            process_scope = PROCESS_SCOPE_COMMAND;
            parameter_amount += 1;
          }
          else if (!main_command_string.contains("A")
          && !main_command_string.contains("C")
          && main_command_string.contains("N"))
          {
            process_scope = PROCESS_SCOPE_NUMBER;
            parameter_amount += 1;
          }
        }
        
        if (main_command_string.contains("V"))
        {
          process_verbose = true;
        }
        
        if (main_command_string.contains("R"))
        {
          process_restart = true;
        }
        
        if (main_command_string.contains("T"))
        {
          // process_timeout = true;
          try
          {
            timeout_value = Integer.parseInt(main_command_string.replaceAll("[\\D]", ""));
          }
          catch (Throwable t)
          {
            
          }
          
          // parameter_amount += 1;
        }
        
        if (splitCommand.length < parameter_amount)
        {
          synchronized (this)
          {
            try
            {
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
              connection.getResultWriter().flush();
            }
            catch (Throwable e)
            {
              
            }
            finished = true;
            return;
          }
        }
        
        if (need_managed_scope)
        {
          if (process_scope == PROCESS_SCOPE_NOT_FOUND)
          {
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
                connection.getResultWriter().flush();
              }
              catch (Throwable e)
              {
                
              }
              finished = true;
              return;
            }
          }
          
          if (processList.size() <= 0)
          {
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>There are no managed processes on session list!\nVT>");
                connection.getResultWriter().flush();
              }
              catch (Throwable e)
              {
                
              }
              finished = true;
              return;
            }
          }
        }
        
        if (process_command == PROCESS_COMMAND_UNKNOWN)
        {
          synchronized (this)
          {
            try
            {
              connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
              connection.getResultWriter().flush();
            }
            catch (Throwable e)
            {
              
            }
            finished = true;
            return;
          }
        }
        
        if (process_command == PROCESS_COMMAND_PATH)
        {
          if (splitCommand.length == 2)
          {
            if (getRuntimeBuilderWorkingDirectory() != null)
            {
              connection.getResultWriter().write("\nVT>Server runtime working directory: [" + getRuntimeBuilderWorkingDirectory() + "]\nVT>");
              connection.getResultWriter().flush();
            }
            else
            {
              connection.getResultWriter().write("\nVT>Server runtime working directory: []\nVT>");
              connection.getResultWriter().flush();
            }
          }
          else if (splitCommand.length >= 3)
          {
            if (splitCommand[2].length() > 0)
            {
              File workingDirectory = new File(splitCommand[2]);
              if (workingDirectory.isDirectory())
              {
                session.setRuntimeBuilderWorkingDirectory(workingDirectory);
                connection.getResultWriter().write("\nVT>Server runtime working directory set to: [" + workingDirectory + "]\nVT>");
                connection.getResultWriter().flush();
              }
              else
              {
                connection.getResultWriter().write("\nVT>File path: [" + workingDirectory + "] is not a valid directory on server!\nVT>");
                connection.getResultWriter().flush();
              }
            }
            else
            {
              session.setRuntimeBuilderWorkingDirectory(null);
              connection.getResultWriter().write("\nVT>Server runtime working directory set to: []\nVT>");
              connection.getResultWriter().flush();
            }
          }
          else
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
            connection.getResultWriter().flush();
          }
        }
        
        if (process_command == PROCESS_COMMAND_MANAGED)
        {
          try
          {
            command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
            ProcessBuilder processBuilder = new ProcessBuilder(CommandLineTokenizer.tokenize(command));
            processBuilder.directory(getRuntimeBuilderWorkingDirectory());
            processBuilder.environment().clear();
            processBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
            // processBuilder.environment().putAll(System.getenv());
            processBuilder.redirectErrorStream(true);
            VTRuntimeProcess process = new VTRuntimeProcess(command, processBuilder, session.getSessionThreads(), connection.getResultWriter(), process_verbose, process_restart, timeout_value);
            process.start();
            processList.add(process);
            synchronized (this)
            {
              connection.getResultWriter().write("\nVT>Managed process with command [" + command + "] created!\nVT>");
              connection.getResultWriter().flush();
              finished = true;
            }
          }
          catch (Throwable e)
          {
            // VTTerminal.print("\rVT>Native runtime
            // failed!\nVT>");
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>Native runtime failed!\nVT>Error message: [" + e.toString() + "]\nVT>");
                connection.getResultWriter().flush();
              }
              catch (Throwable e1)
              {
                
              }
              finished = true;
              return;
            }
          }
          return;
        }
        
        if (process_command == PROCESS_COMMAND_FREE)
        {
          try
          {
            command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
            ProcessBuilder processBuilder = new ProcessBuilder(CommandLineTokenizer.tokenize(command));
            processBuilder.directory(getRuntimeBuilderWorkingDirectory());
            processBuilder.environment().clear();
            processBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
            // processBuilder.environment().putAll(System.getenv());
            processBuilder.redirectErrorStream(true);
            VTRuntimeProcess process = new VTRuntimeProcess(command, processBuilder, session.getSessionThreads(), connection.getResultWriter(), process_verbose, process_restart, timeout_value);
            process.start();
            synchronized (this)
            {
              connection.getResultWriter().write("\nVT>Free process with command [" + command + "] executed!\nVT>");
              connection.getResultWriter().flush();
              finished = true;
            }
          }
          catch (Throwable e)
          {
            // VTTerminal.print("\rVT>Native runtime0
            // failed!\nVT>");
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>Native runtime failed!\nVT>Error message: [" + e.toString() + "]\nVT>");
                connection.getResultWriter().flush();
              }
              catch (Throwable e1)
              {
                
              }
              finished = true;
              return;
            }
          }
          return;
        }
        
        if (process_command == PROCESS_COMMAND_LIST)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            message.setLength(0);
            message.append("\nVT>List of managed processes on session list:\nVT>");
            i = 0;
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                Integer exitValue = process.getExitValue();
                message.append("\nVT>Number: [" + (i++) + "]" + "\nVT>Command: [" + process.getCommand() + "]" + "\nVT>Verbose: [" + (process.isVerbose() ? "Enabled" : "Disabled") + "]" + "\nVT>Restart: [" + (process.isRestart() ? "Enabled" : "Disabled") + "]" + "\nVT>Timeout: [" + (process.getTimeout() > 0 ? process.getTimeout() : "Disabled") + "]" + "\nVT>State: [" + (exitValue == null ? "Running" : "Terminated]\nVT>Return Code: [" + exitValue) + "]" + "\nVT>");
              }
              catch (Throwable e)
              {
                
              }
            }
            message.append("\nVT>End of managed processes list\nVT>");
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write(message.toString());
                connection.getResultWriter().flush();
              }
              catch (Throwable e)
              {
                
              }
              finished = true;
            }
          }
          
          if (process_scope == PROCESS_SCOPE_NUMBER)
          {
            found = false;
            try
            {
              VTRuntimeProcess process = processList.get(Integer.parseInt(splitCommand[2]));
              if (!found)
              {
                message.setLength(0);
                message.append("\nVT>List of managed processes on session list with number [" + splitCommand[2] + "]:\nVT>");
              }
              found = true;
              Integer exitValue = process.getExitValue();
              message.append("\nVT>Number: [" + splitCommand[2] + "]" + "\nVT>Command: [" + process.getCommand() + "]" + "\nVT>Verbose: [" + (process.isVerbose() ? "Enabled" : "Disabled") + "]" + "\nVT>Restart: [" + (process.isRestart() ? "Enabled" : "Disabled") + "]" + "\nVT>Timeout: [" + (process.getTimeout() > 0 ? process.getTimeout() : "Disabled") + "]" + "\nVT>State: [" + (exitValue == null ? "Running" : "Terminated]\nVT>Return Code: [" + exitValue) + "]" + "\nVT>");
              synchronized (this)
              {
                try
                {
                  message.append("\nVT>End of managed processes list\nVT>");
                  connection.getResultWriter().write(message.toString());
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (NumberFormatException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
          }
          
          if (process_scope == PROCESS_SCOPE_COMMAND)
          {
            found = false;
            i = 0;
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                if (process.getCommand().contains(splitCommand[2]))
                {
                  if (!found)
                  {
                    message.setLength(0);
                    message.append("\nVT>List of managed processes on session list with command [" + splitCommand[2] + "]:\nVT>");
                  }
                  found = true;
                  Integer exitValue = process.getExitValue();
                  message.append("\nVT>Number: [" + (i++) + "]" + "\nVT>Command: [" + process.getCommand() + "]" + "\nVT>Verbose: [" + (process.isVerbose() ? "Enabled" : "Disabled") + "]" + "\nVT>Restart: [" + (process.isRestart() ? "Enabled" : "Disabled") + "]" + "\nVT>Timeout: [" + (process.getTimeout() > 0 ? process.getTimeout() : "Disabled") + "]" + "\nVT>State: [" + (exitValue == null ? "Running" : "Terminated]\nVT>Return Code: [" + exitValue) + "]" + "\nVT>");
                }
                else
                {
                  i++;
                }
              }
              catch (Throwable e)
              {
                
              }
            }
            if (!found)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  
                }
                finished = true;
              }
            }
            else
            {
              synchronized (this)
              {
                try
                {
                  message.append("\nVT>End of managed processes list\nVT>");
                  connection.getResultWriter().write(message.toString());
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
          }
          
          return;
        }
        
        if (process_command == PROCESS_COMMAND_STOP)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              process.destroy();
            }
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>Stopped all managed processes on session list!\nVT>");
                connection.getResultWriter().flush();
              }
              catch (Throwable e)
              {
                
              }
              finished = true;
            }
          }
          
          if (process_scope == PROCESS_SCOPE_NUMBER)
          {
            try
            {
              processList.get(Integer.parseInt(splitCommand[2])).destroy();
              synchronized (this)
              {
                connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] stopped!\nVT>");
                connection.getResultWriter().flush();
                finished = true;
              }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (NumberFormatException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
          }
          
          if (process_scope == PROCESS_SCOPE_COMMAND)
          {
            found = false;
            i = 0;
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              if (process.getCommand().contains(splitCommand[2]))
              {
                found = true;
                process.destroy();
              }
              i++;
            }
            if (!found)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  
                }
                finished = true;
              }
            }
            else
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] stopped!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  
                }
                finished = true;
              }
            }
          }
          
          return;
        }
        
        if (process_command == PROCESS_COMMAND_DESTROY)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            clear();
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>Destroyed all managed processes on session list!\nVT>");
                connection.getResultWriter().flush();
              }
              catch (Throwable e)
              {
                
              }
              finished = true;
            }
          }
          
          if (process_scope == PROCESS_SCOPE_NUMBER)
          {
            try
            {
              processList.remove(Integer.parseInt(splitCommand[2])).setRestart(false);
              synchronized (this)
              {
                connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] destroyed from list!\nVT>");
                connection.getResultWriter().flush();
                finished = true;
              }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (NumberFormatException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
          }
          
          if (process_scope == PROCESS_SCOPE_COMMAND)
          {
            removedProcessStack.clear();
            found = false;
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              if (process.getCommand().contains(splitCommand[2]))
              {
                found = true;
                removedProcessStack.add(process);
                process.setRestart(false);
              }
            }
            if (!found)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  
                }
                finished = true;
              }
            }
            else
            {
              processList.removeAll(removedProcessStack);
              removedProcessStack.clear();
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] destroyed from list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e)
                {
                  
                }
                finished = true;
              }
            }
          }
          
          return;
        }
        
        if (process_command == PROCESS_COMMAND_INPUT)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
                command += "\n";
                process.getOut().write(command.getBytes());
                process.getOut().flush();
              }
              catch (Throwable e)
              {
                
              }
            }
            synchronized (this)
            {
              connection.getResultWriter().write("\nVT>All managed processes received line!\nVT>");
              connection.getResultWriter().flush();
              finished = true;
            }
          }
          
          if (process_scope == PROCESS_SCOPE_NUMBER)
          {
            try
            {
              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              command += "\n";
              processList.get(Integer.parseInt(splitCommand[2])).getOut().write((command).getBytes());
              processList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
              synchronized (this)
              {
                connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] received line!\nVT>");
                connection.getResultWriter().flush();
                finished = true;
              }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (NumberFormatException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
          }
          
          if (process_scope == PROCESS_SCOPE_COMMAND)
          {
            try
            {
              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              command += "\n";
              found = false;
              // VTTerminal.println(splitCommand[1]);
              for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
              {
                if (process.getCommand().contains(splitCommand[2]))
                {
                  found = true;
                  process.getOut().write((command).getBytes());
                  process.getOut().flush();
                }
              }
              if (!found)
              {
                synchronized (this)
                {
                  try
                  {
                    connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
                    connection.getResultWriter().flush();
                  }
                  catch (Throwable e)
                  {
                    
                  }
                  finished = true;
                }
              }
              else
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] received line!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
            finished = true;
          }
          
          return;
        }
        
//        if (process_command == PROCESS_COMMAND_LINE)
//        {
//          if (process_scope == PROCESS_SCOPE_ALL)
//          {
//            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
//            {
//              try
//              {
//                command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
//                process.getOut().write((command + "\n").getBytes());
//                process.getOut().flush();
//              }
//              catch (Throwable e)
//              {
//                
//              }
//            }
//            synchronized (this)
//            {
//              connection.getResultWriter().write("\nVT>All managed processes received line!\nVT>");
//              connection.getResultWriter().flush();
//              finished = true;
//            }
//          }
//          
//          if (process_scope == PROCESS_SCOPE_NUMBER)
//          {
//            try
//            {
//              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
//              processList.get(Integer.parseInt(splitCommand[2])).getOut().write((command + "\n").getBytes());
//              processList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
//              synchronized (this)
//              {
//                connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] received line!\nVT>");
//                connection.getResultWriter().flush();
//                finished = true;
//              }
//            }
//            catch (ArrayIndexOutOfBoundsException e)
//            {
//              synchronized (this)
//              {
//                try
//                {
//                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
//                  connection.getResultWriter().flush();
//                }
//                catch (Throwable e1)
//                {
//                  
//                }
//                finished = true;
//              }
//            }
//            catch (NumberFormatException e)
//            {
//              synchronized (this)
//              {
//                try
//                {
//                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
//                  connection.getResultWriter().flush();
//                }
//                catch (Throwable e1)
//                {
//                  
//                }
//                finished = true;
//              }
//            }
//            catch (Throwable e)
//            {
//              
//            }
//          }
//          
//          if (process_scope == PROCESS_SCOPE_COMMAND)
//          {
//            try
//            {
//              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
//              found = false;
//              // VTTerminal.println(splitCommand[1]);
//              for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
//              {
//                if (process.getCommand().contains(splitCommand[2]))
//                {
//                  found = true;
//                  process.getOut().write((command + "\n").getBytes());
//                  process.getOut().flush();
//                }
//              }
//              if (!found)
//              {
//                synchronized (this)
//                {
//                  try
//                  {
//                    connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
//                    connection.getResultWriter().flush();
//                  }
//                  catch (Throwable e)
//                  {
//                    
//                  }
//                  finished = true;
//                }
//              }
//              else
//              {
//                try
//                {
//                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] received line!\nVT>");
//                  connection.getResultWriter().flush();
//                }
//                catch (Throwable e1)
//                {
//                  
//                }
//                finished = true;
//              }
//            }
//            catch (Throwable e)
//            {
//              
//            }
//            finished = true;
//          }
//          
//          return;
//        }
        
        if (process_command == PROCESS_COMMAND_BASE64)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
                byte[] data = null;
                try
                {
                  data = org.apache.commons.codec.binary.Base64.decodeBase64(command);
                }
                catch (Throwable e)
                {
                  synchronized (this)
                  {
                    connection.getResultWriter().write("\nVT>Detected invalid base64 data!\nVT>");
                    connection.getResultWriter().flush();
                    finished = true;
                  }
                }
                if (data != null)
                {
                  process.getOut().write(data);
                  process.getOut().flush();
                }
              }
              catch (Throwable e)
              {
                
              }
            }
            synchronized (this)
            {
              connection.getResultWriter().write("\nVT>All managed processes received base64!\nVT>");
              connection.getResultWriter().flush();
              finished = true;
            }
          }
          
          if (process_scope == PROCESS_SCOPE_NUMBER)
          {
            try
            {
              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              byte[] data = null;
              try
              {
                data = org.apache.commons.codec.binary.Base64.decodeBase64(command);
              }
              catch (Throwable e)
              {
                synchronized (this)
                {
                  connection.getResultWriter().write("\nVT>Detected invalid base64 data!\nVT>");
                  connection.getResultWriter().flush();
                  finished = true;
                }
              }
              if (data != null)
              {
                processList.get(Integer.parseInt(splitCommand[2])).getOut().write(data);
                processList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
                synchronized (this)
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] received base64!\nVT>");
                  connection.getResultWriter().flush();
                  finished = true;
                }
              }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (NumberFormatException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
          }
          
          if (process_scope == PROCESS_SCOPE_COMMAND)
          {
            try
            {
              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              byte[] data = null;
              try
              {
                data = org.apache.commons.codec.binary.Base64.decodeBase64(command);
              }
              catch (Throwable e)
              {
                synchronized (this)
                {
                  connection.getResultWriter().write("\nVT>Detected invalid base64 data!\nVT>");
                  connection.getResultWriter().flush();
                  finished = true;
                }
              }
              if (data != null)
              {
                found = false;
                // VTTerminal.println(splitCommand[1]);
                for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
                {
                  if (process.getCommand().contains(splitCommand[2]))
                  {
                    found = true;
                    process.getOut().write(data);
                    process.getOut().flush();
                  }
                }
                if (!found)
                {
                  synchronized (this)
                  {
                    try
                    {
                      connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
                      connection.getResultWriter().flush();
                    }
                    catch (Throwable e)
                    {
                      
                    }
                    finished = true;
                  }
                }
                else
                {
                  try
                  {
                    connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] received base64!\nVT>");
                    connection.getResultWriter().flush();
                  }
                  catch (Throwable e1)
                  {
                    
                  }
                  finished = true;
                }
              }
              
            }
            catch (Throwable e)
            {
              
            }
            finished = true;
          }
          
          return;
        }
        
        if (process_command == PROCESS_COMMAND_UTF8)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
                byte[] data = null;
                try
                {
                  data = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(command).getBytes("UTF-8");
                }
                catch (Throwable e)
                {
                  synchronized (this)
                  {
                    connection.getResultWriter().write("\nVT>Detected invalid unicode UTF-8 data!\nVT>");
                    connection.getResultWriter().flush();
                    finished = true;
                  }
                }
                if (data != null)
                {
                  process.getOut().write(data);
                  process.getOut().flush();
                }
              }
              catch (Throwable e)
              {
                
              }
            }
            synchronized (this)
            {
              connection.getResultWriter().write("\nVT>All managed processes received unicode!\nVT>");
              connection.getResultWriter().flush();
              finished = true;
            }
          }
          
          if (process_scope == PROCESS_SCOPE_NUMBER)
          {
            try
            {
              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              byte[] data = null;
              try
              {
                data = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(command).getBytes("UTF-8");
              }
              catch (Throwable e)
              {
                synchronized (this)
                {
                  connection.getResultWriter().write("\nVT>Detected invalid unicode UTF-8 data!\nVT>");
                  connection.getResultWriter().flush();
                  finished = true;
                }
              }
              if (data != null)
              {
                processList.get(Integer.parseInt(splitCommand[2])).getOut().write(data);
                processList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
                synchronized (this)
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] received unicode!\nVT>");
                  connection.getResultWriter().flush();
                  finished = true;
                }
              }
            }
            catch (ArrayIndexOutOfBoundsException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] not found on session list!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (NumberFormatException e)
            {
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Process number [" + splitCommand[2] + "] is not valid!\nVT>");
                  connection.getResultWriter().flush();
                }
                catch (Throwable e1)
                {
                  
                }
                finished = true;
              }
            }
            catch (Throwable e)
            {
              
            }
          }
          
          if (process_scope == PROCESS_SCOPE_COMMAND)
          {
            try
            {
              command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              byte[] data = null;
              try
              {
                data = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(command).getBytes("UTF-8");
              }
              catch (Throwable e)
              {
                synchronized (this)
                {
                  connection.getResultWriter().write("\nVT>Detected invalid unicode UTF-8 data!\nVT>");
                  connection.getResultWriter().flush();
                  finished = true;
                }
              }
              if (data != null)
              {
                found = false;
                // VTTerminal.println(splitCommand[1]);
                for (VTRuntimeProcess process : processList.toArray(new VTRuntimeProcess[] {}))
                {
                  if (process.getCommand().contains(splitCommand[2]))
                  {
                    found = true;
                    process.getOut().write(data);
                    process.getOut().flush();
                  }
                }
                if (!found)
                {
                  synchronized (this)
                  {
                    try
                    {
                      connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] not found on session list!\nVT>");
                      connection.getResultWriter().flush();
                    }
                    catch (Throwable e)
                    {
                      
                    }
                    finished = true;
                  }
                }
                else
                {
                  try
                  {
                    connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] received unicode!\nVT>");
                    connection.getResultWriter().flush();
                  }
                  catch (Throwable e1)
                  {
                    
                  }
                  finished = true;
                }
              }
              
            }
            catch (Throwable e)
            {
              
            }
            finished = true;
          }
          
          return;
        }
      }
      else
      {
        synchronized (this)
        {
          try
          {
            connection.getResultWriter().write("\nVT>Invalid command syntax!" + VTHelpManager.getHelpForClientCommand(splitCommand[0]));
            connection.getResultWriter().flush();
          }
          catch (Throwable e)
          {
            
          }
          finished = true;
          return;
        }
      }
      
      finished = true;
    }
    catch (Throwable e)
    {
      
    }
  }
}