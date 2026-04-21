package org.vash.vate.shell.adapter;

import java.io.File;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;

import org.vash.vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;
import org.vash.vate.nativeutils.VTMainNativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.runtime.VTRuntimeProcess;

public class VTShellAdapter
{
  private static final String GROOVYSHELL_MAIN_CLASS = "org.codehaus.groovy.tools.shell.Main";
  private static final String BEANSHELL_MAIN_CLASS = "bsh.Interpreter";
  //private static final String DISABLE_COMMONS_LOGGING = "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog";
  //private static final String DISABLE_UTIL_LOGGING_CLASS = "-Djava.util.logging.config.class=org.vash.vate.shell.adapter.VTSilenceJavaUtilLogging";
  
  private int shellType = VTShellProcessor.SHELL_TYPE_PROCESS;
  private volatile boolean attachPTY = false;
  
  private VTShellProcessor shellProcessor;
  
  private ProcessBuilder shellBuilder;
  private ProcessBuilder javaBuilder;
  private Map<String, String> shellEnvironment;
  private File shellDirectory;
  
  private ExecutorService executorService;
  
  public VTShellAdapter(ExecutorService executorService)
  {
    this.executorService = executorService;
    this.shellProcessor = new VTShellProcessor(executorService);
  }
  
  public void setShellType(int shellType)
  {
    this.shellType = shellType;
    String cp = System.getProperty("java.class.path");
    
    String[] command;
    if (shellType == VTShellProcessor.SHELL_TYPE_GROOVYSH)
    {
      command = new String[] {"java", "-cp", cp, GROOVYSHELL_MAIN_CLASS};
      javaBuilder = new ProcessBuilder(command);
    }
    else if (shellType == VTShellProcessor.SHELL_TYPE_BEANSHELL)
    {
      command = new String[] {"java", "-cp", cp, BEANSHELL_MAIN_CLASS};
      javaBuilder = new ProcessBuilder(command);
    }
    else
    {
      //javaBuilder = null;
    }
  }
  
  public void setAttachPTY(boolean attach)
  {
    attachPTY = attach;
  }
  
  public VTShellProcessor getShellProcessor()
  {
    return shellProcessor;
  }
  
  public InputStream getShellInputStream()
  {
    return shellProcessor.getShellInputStream();
  }
  
  public OutputStream getShellOutputStream()
  {
    return shellProcessor.getShellOutputStream();
  }
  
  public Map<String, String> getShellEnvironment()
  {
    return shellEnvironment;
  }
  
  public File getShellDirectory()
  {
    return shellDirectory;
  }
  
  public boolean setShellDirectory(File shellDirectory)
  {
    if (shellDirectory == null)
    {
      this.shellDirectory = shellDirectory;
      return true;
    }
    if (shellDirectory != null && shellDirectory.exists() && shellDirectory.isDirectory())
    {
      this.shellDirectory = shellDirectory;
      return true;
    }
    return false;
  }
  
  public void setShellBuilder(String command, String[] names, String[] values)
  {
    String[] parsed = CommandLineTokenizerMKII.tokenize(command);
    List<String> commandList = null;
    if (command == null)
    {
      if (VTReflectionUtils.detectWindows())
      {
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS 95") || System.getProperty("os.name").toUpperCase().contains("WINDOWS 98") || System.getProperty("os.name").toUpperCase().contains("WINDOWS ME"))
        {
          // almost impossible to enter here now
          commandList = Arrays.asList(new String[] {"command.com", "/A", "/E:1900"});
        }
        else
        {
          if (attachPTY)
          {
            if (VTMainNativeUtils.checkShAvailable())
            {
              if (VTMainNativeUtils.checkWinptyAvailable())
              {
                commandList = Arrays.asList(new String[] {"winpty", "-Xallow-non-tty", "sh", "-i"});
              }
              else if (VTMainNativeUtils.checkScriptAvailable())
              {
                commandList = Arrays.asList(new String[] {"script", "/dev/null", "-q", "-c", "sh", "-i"});
              }
              else
              {
                commandList = Arrays.asList(new String[] {"sh", "-i", "-s"});
              }
            }
            else if (VTMainNativeUtils.checkWSLShAvailable() && VTMainNativeUtils.checkWSLScriptAvailable())
            {
              commandList = Arrays.asList(new String[] {"wsl", "script", "/dev/null", "-q", "-c", "sh", "-i"});
            }
            else
            {
              commandList = Arrays.asList(new String[] {"cmd", "/k"});
            }
          }
          else
          {
            commandList = Arrays.asList(new String[] {"cmd", "/k"});
          }
        }
      }
      else
      {
        if (attachPTY)
        {
          commandList = Arrays.asList(new String[] {"script", "/dev/null", "-q", "-c", "sh", "-i"});
        }
        else
        {
          commandList = Arrays.asList(new String[] {"sh", "-i", "-s"});
        }
      }
      this.shellBuilder = new ProcessBuilder(commandList);
      this.shellBuilder.environment().putAll(VTMainNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
    }
    else
    {
      if (parsed.length == 0 || parsed[0].trim().length() == 0)
      {
        this.shellBuilder = null;
        return;
      }
      
      if (VTReflectionUtils.detectWindows())
      {
        if (attachPTY)
        {
          if (VTMainNativeUtils.checkShAvailable())
          {
            if (VTMainNativeUtils.checkWinptyAvailable())
            {
              commandList = new ArrayList<String>();
              commandList.addAll(Arrays.asList(new String[] {"winpty", "-Xallow-non-tty"}));
              commandList.addAll(Arrays.asList(parsed));
            }
            else if (VTMainNativeUtils.checkScriptAvailable())
            {
              commandList = new ArrayList<String>();
              commandList.addAll(Arrays.asList(new String[] {"script", "/dev/null", "-q", "-c"}));
              commandList.addAll(Arrays.asList(parsed));
            }
            else
            {
              commandList = Arrays.asList(parsed);
//              commandList = new ArrayList<String>();
//              commandList.addAll(Arrays.asList(new String[] {"cmd", "/c"}));
//              commandList.addAll(Arrays.asList(parsed));
            }
          }
          else if (VTMainNativeUtils.checkWSLShAvailable() && VTMainNativeUtils.checkWSLScriptAvailable())
          {
            commandList = new ArrayList<String>();
            commandList.addAll(Arrays.asList(new String[] {"wsl", "script", "/dev/null", "-q", "-c"}));
            commandList.addAll(Arrays.asList(parsed));
          }
          else
          {
            commandList = Arrays.asList(parsed);
//            commandList = new ArrayList<String>();
//            commandList.addAll(Arrays.asList(new String[] {"cmd", "/c"}));
//            commandList.addAll(Arrays.asList(parsed));
          }
        }
        else
        {
          commandList = Arrays.asList(parsed);
//          commandList = new ArrayList<String>();
//          commandList.addAll(Arrays.asList(new String[] {"cmd", "/c"}));
//          commandList.addAll(Arrays.asList(parsed));
        }
      }
      else
      {
        if (attachPTY)
        {
          commandList = new ArrayList<String>();
          commandList.addAll(Arrays.asList(new String[] {"script", "/dev/null", "-q", "-c"}));
          commandList.addAll(Arrays.asList(parsed));
        }
        else
        {
          commandList = Arrays.asList(parsed);
//          commandList = new ArrayList<String>();
//          commandList.addAll(Arrays.asList(new String[] {"sh", "-i", "-c"}));
//          commandList.addAll(Arrays.asList(parsed));
        }
      }
      this.shellBuilder = new ProcessBuilder(commandList);
      this.shellBuilder.environment().putAll(VTMainNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
    }
    if (shellBuilder != null)
    {
      this.shellBuilder.redirectErrorStream(true);
    }
  }
  
  private void revertShellBuilder()
  {
    if (VTReflectionUtils.detectWindows())
    {
      this.shellBuilder = new ProcessBuilder("cmd", "/k");
      this.shellBuilder.environment().putAll(VTMainNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
    }
    else
    {
      this.shellBuilder = new ProcessBuilder("sh", "-i", "-s");
      this.shellBuilder.environment().putAll(VTMainNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
    }
  }
  
  public void stopShell()
  {
    shellProcessor.stopShell();
  }
  
  public void waitShell()
  {
    try
    {
      shellProcessor.waitFor();
    }
    catch (Throwable e)
    {
      return;
    }
  }
  
  public boolean startShell() throws Throwable
  {
    try
    {
      ProcessBuilder commandBuilder = null;
      // shellProcessor = new VTShellProcessor();
      if (shellType == VTShellProcessor.SHELL_TYPE_PROCESS)
      {
        if (shellBuilder == null)
        {
          return false;
        }
        if (shellDirectory != null)
        {
          shellBuilder.directory(shellDirectory);
        }
        else
        {
          shellBuilder.directory(null);
        }
        shellBuilder.environment().putAll(VTMainNativeUtils.getvirtualenv());
        shellEnvironment = shellBuilder.environment();
        commandBuilder = shellBuilder;
      }
      else
      {
        javaBuilder.redirectErrorStream(true);
        if (shellDirectory != null)
        {
          javaBuilder.directory(shellDirectory);
        }
        else
        {
          javaBuilder.directory(null);
        }
        javaBuilder.environment().clear();
        javaBuilder.environment().putAll(shellBuilder.environment());
        commandBuilder = javaBuilder;
      }
      VTRuntimeProcess runtimeProcess = new VTRuntimeProcess(null, commandBuilder, executorService, null, null, false, false, false, 0);
      shellProcessor.setRuntimeProcess(runtimeProcess);
      if (shellType != VTShellProcessor.SHELL_TYPE_PROCESS)
      {
        // try using shell in current jvm if that fails use separate jvm
        shellProcessor.setShellType(shellType);
        boolean ok = false;
        try
        {
          ok = shellProcessor.startShell();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        if (!ok)
        {
          // try using shell in separate jvm
          shellProcessor.setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
          try
          {
            ok = shellProcessor.startShell();
          }
          catch (Throwable t)
          {
            //t.printStackTrace();
          }
        }
      }
      else
      {
        shellProcessor.setShellType(shellType);
        try
        {
          shellProcessor.startShell();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
          if (VTReflectionUtils.detectWindows())
          {
            // try again with cmd.exe if cannot start old DOS command.com shell
            List<String> commands = commandBuilder.command();
            boolean dosLegacyShellFound = false;
            for (String command : commands)
            {
              if (command.toUpperCase().contains("COMMAND.COM"))
              {
                dosLegacyShellFound = true;
              }
            }
            if (dosLegacyShellFound)
            {
              revertShellBuilder();
              return startShell();
            }
          }
        }
      }
    }
    catch (Throwable t)
    {
      throw t;
    }
    return true;
  }
  
  public boolean setShellEncoding(String shellEncoding)
  {
    return shellProcessor.setShellEncoding(shellEncoding);
  }
  
  public String getShellEncoding()
  {
    return shellProcessor.getShellEncoding();
  }
}