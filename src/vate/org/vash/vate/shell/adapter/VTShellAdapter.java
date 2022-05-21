package org.vash.vate.shell.adapter;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.vash.vate.nativeutils.VTNativeUtils;
import org.vash.vate.runtime.VTRuntimeProcess;
import com.sun.jna.Platform;

public class VTShellAdapter
{
  private static final String GROOVYSHELL_MAIN_CLASS = "org.codehaus.groovy.tools.shell.Main";
  private static final String BEANSHELL_MAIN_CLASS = "bsh.Interpreter";
  private static final String DISABLE_COMMONS_LOGGING = "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog";
  private static final String DISABLE_UTIL_LOGGING_CLASS = "-Djava.util.logging.config.class=org.vash.vate.shell.adapter.VTSilenceJavaUtilLogging";

  private int shellType = VTShellProcess.SHELL_TYPE_PROCESS;
  //private boolean supressEchoShell = true;
  
  private VTShellProcess shellProcess;
  
  private ProcessBuilder shellBuilder;
  private ProcessBuilder javaBuilder;
  private Map<String, String> shellEnvironment;
  private File shellDirectory;
    
  private ExecutorService threads;
  
  public VTShellAdapter()
  {
    shellProcess = new VTShellProcess();
  }
  
  public void setShellType(int shellType)
  {
    this.shellType = shellType;
    if (shellType == VTShellProcess.SHELL_TYPE_GROOVYSH)
    {
      javaBuilder = new ProcessBuilder(new String[] {"java", "-cp", "\"" + System.getProperty("java.class.path") + "\"", DISABLE_COMMONS_LOGGING, DISABLE_UTIL_LOGGING_CLASS, GROOVYSHELL_MAIN_CLASS});
    }
    if (shellType == VTShellProcess.SHELL_TYPE_BEANSHELL)
    {
      javaBuilder = new ProcessBuilder(new String[] {"java", "-cp", "\"" + System.getProperty("java.class.path") + "\"", DISABLE_COMMONS_LOGGING, DISABLE_UTIL_LOGGING_CLASS, BEANSHELL_MAIN_CLASS});
    }
  }
  
//  public void setSuppressEchoShell(boolean supressEchoShell)
//  {
//    this.supressEchoShell = supressEchoShell;
//  }
  
  public void setThreads(ExecutorService threads)
  {
    this.threads = threads;
    //shellProcess.setThreads(threads);
  }
  
  public VTShellProcess getShell()
  {
    return shellProcess;
  }
  
  public Reader getShellOutputReader()
  {
    return shellProcess.getShellOutputReader();
  }

  public Reader getShellErrorReader()
  {
    return shellProcess.getShellErrorReader();
  }

  public Writer getShellCommandExecutor()
  {
    return shellProcess.getShellCommandExecutor();
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
    if (shellDirectory.exists() && shellDirectory.isDirectory())
    {
      this.shellDirectory = shellDirectory;
      return true;
    }
    return false;
  }
  
  public void setShellBuilder(String[] command, String[] names, String[] values)
  {
    if (command == null)
    {
      if (Platform.isWindows())
      {
        if (System.getProperty("os.name").toUpperCase().startsWith("WINDOWS 95") || System.getProperty("os.name").toUpperCase().startsWith("WINDOWS 98"))
        {
          // almost impossible to enter here now
          this.shellBuilder = new ProcessBuilder("command.com", "/A", "/E:1900");
          this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
          this.shellEnvironment = this.shellBuilder.environment();
          if (this.shellEnvironment != null)
          {
            //this.shellEnvironment.remove("PROMPT");
            //this.shellEnvironment.put("PROMPT", "# ");
          }
        }
        else
        {
//          if (supressEchoShell)
//          {
//            this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A");
//          }
//          else
//          {
//            this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/A");
//          }
          //this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/A");
          this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A");
          this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
          this.shellEnvironment = this.shellBuilder.environment();
          if (this.shellEnvironment != null)
          {
            //this.shellEnvironment.remove("PROMPT");
            //this.shellEnvironment.put("PROMPT", "# ");
          }
        }
      }
      else
      {
        this.shellBuilder = new ProcessBuilder("/bin/sh", "-l", "-s");
        this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
        this.shellEnvironment = this.shellBuilder.environment();
        if (this.shellEnvironment != null)
        {
          //this.shellEnvironment.remove("PS1");
          //this.shellEnvironment.put("PS1", "'# '");
          //this.shellEnvironment.remove("PROMPT");
          //this.shellEnvironment.put("PROMPT", "'pwd'>");
        }
      }
    }
    else
    {
      this.shellBuilder = new ProcessBuilder(command);
      this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
      if (names != null && values != null && (names.length == values.length))
      {
        for (int i = 0; i < names.length; i++)
        {
          this.shellEnvironment.remove(names[i]);
          this.shellEnvironment.put(names[i], values[i]);
        }
      }
    }
    this.shellBuilder.redirectErrorStream(true);
  }
  
  public void stopShell()
  {
    shellProcess.stopShell();
  }

  public void waitShell()
  {
    try
    {
      shellProcess.waitFor();
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
      if (shellDirectory != null)
      {
        shellBuilder.directory(shellDirectory);
      }
      else
      {
        shellBuilder.directory(null);
      }
      shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
      shellEnvironment = shellBuilder.environment();
      ProcessBuilder commandBuilder = null;
      if (shellType == VTShellProcess.SHELL_TYPE_PROCESS)
      {
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
      VTRuntimeProcess runtimeProcess = new VTRuntimeProcess(null, commandBuilder, threads, null, false, false, 0);
      shellProcess.setRuntimeProcess(runtimeProcess);
      if (shellType != VTShellProcess.SHELL_TYPE_PROCESS)
      {
        //try using shell in separate jvm if that fails use current jvm
        shellProcess.setShellType(VTShellProcess.SHELL_TYPE_PROCESS);
        boolean ok = false;
        try
        {
          ok = shellProcess.startShell();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
        }
        if (!ok)
        {
          //try using shell in current jvm
          shellProcess.setShellType(shellType);
          try
          {
            ok = shellProcess.startShell();
          }
          catch (Throwable t)
          {
            //t.printStackTrace();
          }
        }
      }
      else
      {
        shellProcess.setShellType(shellType);
        try
        {
          shellProcess.startShell();
        }
        catch (Throwable t)
        {
          //t.printStackTrace();
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
    return shellProcess.setShellEncoding(shellEncoding);
  }
  
  public String getShellEncoding()
  {
    return shellProcess.getShellEncoding();
  }
}