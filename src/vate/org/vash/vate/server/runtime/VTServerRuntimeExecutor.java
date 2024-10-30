package org.vash.vate.server.runtime;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
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
  private boolean finished;
  private boolean found;
  private int i;
  /* private int x; private int y; private int z; */
  private String command;
  private String[] splitCommand;
  private StringBuilder message;
  // private VTServer server;
  private VTServerSession session;
  private VTServerConnection connection;
  private List<VTRuntimeProcess> managedProcessList;
  private List<VTRuntimeProcess> removedProcessStack;
  private static List<VTRuntimeProcess> freeProcessList = new ArrayList<VTRuntimeProcess>();
  private File runtimeDirectory;
  
  private static int PROCESS_COMMAND_UNKNOWN = -1; // ?
  private static int PROCESS_COMMAND_MANAGED = 1; // M
  private static int PROCESS_COMMAND_FREE = 2; // F
  private static int PROCESS_COMMAND_LIST = 3; // L
  private static int PROCESS_COMMAND_STOP = 4; // S
  private static int PROCESS_COMMAND_DROP = 5; // D
  private static int PROCESS_COMMAND_PATH = 6; // P
  private static int PROCESS_COMMAND_ENTER = 7; // E
  private static int PROCESS_COMMAND_BASE64 = 8; // B
  private static int PROCESS_COMMAND_UTF8 = 9; // U
  
  private static int PROCESS_SCOPE_NOT_FOUND = -1; // ?
  private static int PROCESS_SCOPE_ALL = 1; // A
  private static int PROCESS_SCOPE_COMMAND = 2; // C
  private static int PROCESS_SCOPE_NUMBER = 3; // N
  
  // private static int PROCESS_OUTPUT_OFF = 1; //?
  // private static int PROCESS_OUTPUT_ON = 2; //O
  
  // private static int PROCESS_RESTART_OFF = 1; //?
  // private static int PROCESS_RESTART_ON = 2; //R
  
  public VTServerRuntimeExecutor(VTServerSession session)
  {
    super(session.getExecutorService());
    // this.server = session.getServer();
    this.session = session;
    this.connection = session.getConnection();
    this.finished = true;
//    this.processList = Collections.synchronizedList(new ArrayList<VTRuntimeProcess>());
//    this.removedProcessStack = Collections.synchronizedList(new ArrayList<VTRuntimeProcess>());
    this.managedProcessList = new ArrayList<VTRuntimeProcess>();
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
  
  public static void free()
  {
    for (VTRuntimeProcess process : freeProcessList.toArray(new VTRuntimeProcess[] {}))
    {
      process.destroy();
    }
    freeProcessList.clear();
  }
  
  public void clear()
  {
    for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
    {
      process.destroy();
    }
    managedProcessList.clear();
  }
  
  /*
   * private void split() { y = 0; z = 0; for (x = 0; x < command.length(); x++)
   * { if (command.charAt(x) == '|') { y++; } } splitCommand = new String[++y];
   * y = 0; for (x = 0; x <= command.length(); x++) { if ((x ==
   * command.length()) || (command.charAt(x) == '|')) { splitCommand[y++] =
   * command.substring(z, x); z = ++x; } } return; }
   */
  
  public void task()
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
      boolean process_input = false;
      boolean process_output = false;
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
        //&& !main_command_string.contains("I")
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
        //&& !main_command_string.contains("I")
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
        //&& !main_command_string.contains("I")
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
        //&& !main_command_string.contains("I")
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
        //&& !main_command_string.contains("I")
        && !main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_DROP;
          need_managed_scope = true;
        }
        //else if (!main_command_string.contains("M")
        //&& !main_command_string.contains("F")
        //&& !main_command_string.contains("L")
        //&& !main_command_string.contains("S")
        //&& !main_command_string.contains("D")
        //&& main_command_string.contains("I")
        //&& !main_command_string.contains("E")
        //&& !main_command_string.contains("P")
        //&& !main_command_string.contains("B")
        //&& !main_command_string.contains("U"))
        //{
          //process_command = PROCESS_COMMAND_INPUT;
          //need_managed_scope = true;
          //parameter_amount += 1;
        //}
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        //&& !main_command_string.contains("I")
        && main_command_string.contains("E")
        && !main_command_string.contains("P")
        && !main_command_string.contains("B")
        && !main_command_string.contains("U"))
        {
          process_command = PROCESS_COMMAND_ENTER;
          need_managed_scope = true;
          parameter_amount += 1;
        }
        else if (!main_command_string.contains("M")
        && !main_command_string.contains("F")
        && !main_command_string.contains("L")
        && !main_command_string.contains("S")
        && !main_command_string.contains("D")
        //&& !main_command_string.contains("I")
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
        //&& !main_command_string.contains("I")
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
        //&& !main_command_string.contains("I")
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
          && !main_command_string.contains("N"))
          {
            process_scope = PROCESS_SCOPE_ALL;
          }
          else if (!main_command_string.contains("A")
          && main_command_string.contains("C")
          && !main_command_string.contains("N"))
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
        
        if (main_command_string.contains("I"))
        {
          process_input = true;
        }
        
        if (main_command_string.contains("O"))
        {
          process_output = true;
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
          
          if (managedProcessList.size() <= 0)
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
        
        if (process_command == PROCESS_COMMAND_MANAGED || process_command == PROCESS_COMMAND_FREE)
        {
          try
          {
            boolean hasInputOutputParameters = false;
            boolean closeInputRedirect = false;
            boolean closeOutputRedirect = false;
            InputStream inputRedirect = null;
            OutputStream outputRedirect = null;
            int parameterIndex = 0;
            
            if (process_input && !process_output && splitCommand.length >= 4 && splitCommand[2].startsWith("@"))
            {
              hasInputOutputParameters = true;
              String inputRedirectPath = splitCommand[2].substring(1);
              try
              {
                inputRedirect = new FileInputStream(inputRedirectPath);
              }
              catch (Throwable t)
              {
                
              }
              parameterIndex = 3;
            }
            else if (!process_input && process_output && splitCommand.length >= 4 && splitCommand[2].startsWith("&"))
            {
              hasInputOutputParameters = true;
              String outputRedirectFilePath = splitCommand[2].substring(1);
              closeOutputRedirect = true;
              try
              {
                outputRedirect = new FileOutputStream(outputRedirectFilePath, true);
              }
              catch (Throwable t)
              {
                
              }
              parameterIndex = 3;
            }
            else if (process_input && process_output && splitCommand.length >= 4)
            {
              parameterIndex = 2;
              if (splitCommand[parameterIndex].startsWith("@"))
              {
                hasInputOutputParameters = true;
                String inputRedirectFilePath = splitCommand[parameterIndex].substring(1);
                try
                {
                  inputRedirect = new FileInputStream(inputRedirectFilePath);
                }
                catch (Throwable t)
                {
                  
                }
                parameterIndex++;
                if (splitCommand[parameterIndex].startsWith("&") && splitCommand.length >= 5)
                {
                  String outputRedirectFilePath = splitCommand[parameterIndex].substring(1);
                  closeOutputRedirect = true;
                  try
                  {
                    outputRedirect = new FileOutputStream(outputRedirectFilePath, true);
                  }
                  catch (Throwable t)
                  {
                    
                  }
                  parameterIndex++;
                }
              }
              else if (splitCommand[parameterIndex].startsWith("&"))
              {
                hasInputOutputParameters = true;
                String outputRedirectFilePath = splitCommand[parameterIndex].substring(1);
                closeOutputRedirect = true;
                try
                {
                  outputRedirect = new FileOutputStream(outputRedirectFilePath, true);
                }
                catch (Throwable t)
                {
                  
                }
                parameterIndex++;
                if (splitCommand[parameterIndex].startsWith("@") && splitCommand.length >= 5)
                {
                  String inputRedirectFilePath = splitCommand[parameterIndex].substring(1);
                  try
                  {
                    inputRedirect = new FileInputStream(inputRedirectFilePath);
                  }
                  catch (Throwable t)
                  {
                    
                  }
                  parameterIndex++;
                }
              }
            }
            
            if (!hasInputOutputParameters)
            {
              parameterIndex = 2;
            }
            
            if (process_output && outputRedirect == null)
            {
              outputRedirect = session.getConnection().getShellDataOutputStream();
            }
            
            String processCommand = parseCommandParameter(command, parameterIndex, false);
            String parsedCommand = parseCommandParameter(command, parameterIndex, true);
            
            String[] processCommands = CommandLineTokenizer.tokenize(processCommand);
            
            ProcessBuilder processBuilder = new ProcessBuilder(processCommands);
            //ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.directory(getRuntimeBuilderWorkingDirectory());
            processBuilder.environment().clear();
            processBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
            // processBuilder.environment().putAll(System.getenv());
            processBuilder.redirectErrorStream(true);
            
            VTRuntimeProcess process = new VTRuntimeProcess(parsedCommand, processBuilder, session.getExecutorService(), inputRedirect, outputRedirect, closeInputRedirect, closeOutputRedirect, process_restart, timeout_value);
            process.start();
            
            if (process_command == PROCESS_COMMAND_MANAGED)
            {
              managedProcessList.add(process);
              synchronized (this)
              {
                connection.getResultWriter().write("\nVT>Managed process with command [" + parsedCommand + "] created!\nVT>");
                connection.getResultWriter().flush();
                finished = true;
              }
            }
            
            if (process_command == PROCESS_COMMAND_FREE)
            {
              freeProcessList.add(process);
              synchronized (this)
              {
                connection.getResultWriter().write("\nVT>Free process with command [" + parsedCommand + "] executed!\nVT>");
                connection.getResultWriter().flush();
                finished = true;
              }
            }
          }
          catch (Throwable e)
          {
            //e.printStackTrace();
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
        
        if (process_command == PROCESS_COMMAND_LIST)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            message.setLength(0);
            message.append("\nVT>List of managed processes on session list:\nVT>");
            i = 0;
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                Integer exitValue = process.getExitValue();
                message.append("\nVT>Number: [" + (i++) + "]" + "\nVT>Command: [" + process.getCommand() + "]" + "\nVT>Restart: [" + (process.isRestart() ? "Enabled" : "Disabled") + "]" + "\nVT>Timeout: [" + (process.getTimeout() > 0 ? process.getTimeout() : "Disabled") + "]" + "\nVT>State: [" + (exitValue == null ? "Running" : "Terminated]\nVT>Return Code: [" + exitValue) + "]" + "\nVT>");
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
              VTRuntimeProcess process = managedProcessList.get(Integer.parseInt(splitCommand[2]));
              if (!found)
              {
                message.setLength(0);
                message.append("\nVT>List of managed processes on session list with number [" + splitCommand[2] + "]:\nVT>");
              }
              found = true;
              Integer exitValue = process.getExitValue();
              message.append("\nVT>Number: [" + splitCommand[2] + "]" + "\nVT>Command: [" + process.getCommand() + "]" + "\nVT>Restart: [" + (process.isRestart() ? "Enabled" : "Disabled") + "]" + "\nVT>Timeout: [" + (process.getTimeout() > 0 ? process.getTimeout() : "Disabled") + "]" + "\nVT>State: [" + (exitValue == null ? "Running" : "Terminated]\nVT>Return Code: [" + exitValue) + "]" + "\nVT>");
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
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
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
                  message.append("\nVT>Number: [" + (i++) + "]" + "\nVT>Command: [" + process.getCommand() + "]" + "\nVT>Restart: [" + (process.isRestart() ? "Enabled" : "Disabled") + "]" + "\nVT>Timeout: [" + (process.getTimeout() > 0 ? process.getTimeout() : "Disabled") + "]" + "\nVT>State: [" + (exitValue == null ? "Running" : "Terminated]\nVT>Return Code: [" + exitValue) + "]" + "\nVT>");
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
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
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
              managedProcessList.get(Integer.parseInt(splitCommand[2])).destroy();
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
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
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
        
        if (process_command == PROCESS_COMMAND_DROP)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            clear();
            synchronized (this)
            {
              try
              {
                connection.getResultWriter().write("\nVT>Dropped all managed processes on session list!\nVT>");
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
              managedProcessList.remove(Integer.parseInt(splitCommand[2])).setRestart(false);
              synchronized (this)
              {
                connection.getResultWriter().write("\nVT>Process with number [" + splitCommand[2] + "] dropped from list!\nVT>");
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
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
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
              managedProcessList.removeAll(removedProcessStack);
              removedProcessStack.clear();
              synchronized (this)
              {
                try
                {
                  connection.getResultWriter().write("\nVT>Processes with command [" + splitCommand[2] + "] dropped from list!\nVT>");
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
        
        if (process_command == PROCESS_COMMAND_ENTER)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            String commandData = parseCommandParameter(command, 2, true);
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
                process.getOut().write((commandData + "\n").getBytes());
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
              //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              String processData = parseCommandParameter(command, 3, true);
              managedProcessList.get(Integer.parseInt(splitCommand[2])).getOut().write((processData + "\n").getBytes());
              managedProcessList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
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
              //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              String commandData = parseCommandParameter(command, 3, true);
              found = false;
              // VTTerminal.println(splitCommand[1]);
              for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
              {
                if (process.getCommand().contains(splitCommand[2]))
                {
                  found = true;
                  process.getOut().write((commandData + "\n").getBytes());
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
        
        if (process_command == PROCESS_COMMAND_BASE64)
        {
          if (process_scope == PROCESS_SCOPE_ALL)
          {
            String commandData = parseCommandParameter(command, 2, true);
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
                //command = command.substring(command.indexOf(splitCommand[2]));
                
                byte[] data = null;
                try
                {
                  data = org.apache.commons.codec.binary.Base64.decodeBase64(commandData);
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
              //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              //command = command.substring(command.indexOf(splitCommand[3]));
              String commandData = parseCommandParameter(command, 3, true);
              
              byte[] data = null;
              try
              {
                data = org.apache.commons.codec.binary.Base64.decodeBase64(commandData);
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
                managedProcessList.get(Integer.parseInt(splitCommand[2])).getOut().write(data);
                managedProcessList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
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
              //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              //command = command.substring(command.indexOf(splitCommand[3]));
              String commandData = parseCommandParameter(command, 3, true);
              
              byte[] data = null;
              try
              {
                data = org.apache.commons.codec.binary.Base64.decodeBase64(commandData);
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
                for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
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
            String commandData = parseCommandParameter(command, 2, true);
            for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
            {
              try
              {
                //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + 2);
                //command = command.substring(command.indexOf(splitCommand[2]));
                
                byte[] data = null;
                try
                {
                  data = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(commandData).getBytes("UTF-8");
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
              //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              //command = command.substring(command.indexOf(splitCommand[3]));
              String commandData = parseCommandParameter(command, 3, true);
              
              byte[] data = null;
              try
              {
                data = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(commandData).getBytes("UTF-8");
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
                managedProcessList.get(Integer.parseInt(splitCommand[2])).getOut().write(data);
                managedProcessList.get(Integer.parseInt(splitCommand[2])).getOut().flush();
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
              //command = command.substring(splitCommand[0].length() + splitCommand[1].length() + splitCommand[2].length() + 3);
              //command = command.substring(command.indexOf(splitCommand[3]));
              String commandData = parseCommandParameter(command, 3, true);
              
              byte[] data = null;
              try
              {
                data = org.apache.commons.lang3.StringEscapeUtils.unescapeJava(commandData).getBytes("UTF-8");
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
                for (VTRuntimeProcess process : managedProcessList.toArray(new VTRuntimeProcess[] {}))
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
  
  private static int findParameterStart(String commandLine, int parameterNumber)
  {
    //List resultBuffer = new java.util.ArrayList();
    int currentParameterCount = -1;
    int currentParameterSize = 0;
    if (commandLine != null)
    {
      int z = commandLine.length();
      boolean insideQuotes = false;
      //StringBuffer buf = new StringBuffer();
      char q = 'q';
      char c = 'c';
      char l = ' ';
      char n = ' ';
      for (int i = 0; i < z; i++)
      {
        c = commandLine.charAt(i);
        n = z > i + 1 ? commandLine.charAt(i + 1) : ' ';
        if (c == '"' || c == '\'')
        {
          if ((q == 'q' && Character.isWhitespace(l)) || (c == q && Character.isWhitespace(n)))
          {
            insideQuotes = !insideQuotes;
            if (insideQuotes)
            {
              q = c;
              //started argument if currentArgumentSize == 0
              if (currentParameterSize == 0)
              {
                currentParameterCount++;
                if (currentParameterCount == parameterNumber)
                {
                  return i;
                }
              }
              currentParameterSize++;
            }
            else
            {
              q = 'q';
              //terminated argument
              currentParameterSize = 0;
            }
          }
          else
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
          }
        }
        else if (insideQuotes && c == '\\')
        {
          if (n == q)
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
            i++;
          }
          else
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
          }
        }
        else
        {
          if (insideQuotes)
          {
            //started argument if currentArgumentSize == 0
            if (currentParameterSize == 0)
            {
              currentParameterCount++;
              if (currentParameterCount == parameterNumber)
              {
                return i;
              }
            }
            currentParameterSize++;
          }
          else
          {
            if (Character.isWhitespace(c))
            {
              //terminated argument
              currentParameterSize = 0;
            }
            else
            {
              //started argument if currentArgumentSize == 0
              if (currentParameterSize == 0)
              {
                currentParameterCount++;
                if (currentParameterCount == parameterNumber)
                {
                  return i;
                }
              }
              currentParameterSize++;
            }
          }
        }
        l = c;
      }
      //terminated argument
      currentParameterSize = 0;
    }
    return -1;
  }
  
  private static String parseCommandParameter(String commandLine, int parameterIndex, boolean removeQuotes)
  {
    String result = "";
    int parameterStart = findParameterStart(commandLine, parameterIndex);
    
    result = commandLine.substring(parameterStart);
    
    if (!removeQuotes)
    {
      return result;
    }
    
    boolean startedWithSingleQuote = (parameterStart > 0) && (commandLine.charAt(parameterStart) == '\'') && (commandLine.charAt(parameterStart - 1) != '\\');
    boolean startedWithDoubleQuote = (parameterStart > 0) && (commandLine.charAt(parameterStart) == '\"') && (commandLine.charAt(parameterStart - 1) != '\\');
    
    int singleQuoteIndex = result.lastIndexOf('\'');
    int doubleQuoteIndex = result.lastIndexOf('\"');
    int lastBackslashIndex = result.lastIndexOf('\\');
    
    if (startedWithSingleQuote && (singleQuoteIndex >= 0))
    {
      if (lastBackslashIndex < 0 || (lastBackslashIndex + 1 != singleQuoteIndex))
      {
        result = result.substring(1, singleQuoteIndex);
      }
    }
    else if (startedWithDoubleQuote && (doubleQuoteIndex >= 0))
    {
      if (lastBackslashIndex < 0 || (lastBackslashIndex + 1 != doubleQuoteIndex))
      {
        result = result.substring(1, doubleQuoteIndex);
      }
    }
    
    return result;
  }
}