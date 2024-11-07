package org.vash.vate.shell.adapter;

import java.io.File;
import java.io.Reader;
import java.io.Writer;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import org.vash.vate.nativeutils.VTNativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.runtime.VTRuntimeProcess;

public class VTShellAdapter
{
  private static final String GROOVYSHELL_MAIN_CLASS = "org.codehaus.groovy.tools.shell.Main";
  private static final String BEANSHELL_MAIN_CLASS = "bsh.Interpreter";
  private static final String DISABLE_COMMONS_LOGGING = "-Dorg.apache.commons.logging.Log=org.apache.commons.logging.impl.NoOpLog";
  private static final String DISABLE_UTIL_LOGGING_CLASS = "-Djava.util.logging.config.class=org.vash.vate.shell.adapter.VTSilenceJavaUtilLogging";
  
  private int shellType = VTShellProcessor.SHELL_TYPE_PROCESS;
  // private boolean supressEchoShell = true;
  
  private VTShellProcessor shellProcessor;
  
  private ProcessBuilder shellBuilder;
  private ProcessBuilder javaBuilder;
  private Map<String, String> shellEnvironment;
  private File shellDirectory;
  
  private ExecutorService executorService;
  
  public VTShellAdapter(ExecutorService executorService)
  {
    this.executorService = executorService;
    shellProcessor = new VTShellProcessor(executorService);
  }
  
  public void setShellType(int shellType)
  {
    this.shellType = shellType;
    String cp = System.getProperty("java.class.path");
    
    //String command = "";
    String[] command;
    if (shellType == VTShellProcessor.SHELL_TYPE_GROOVYSH)
    {
      //command = "java -cp " + '"' + cp + '"' + " " + DISABLE_COMMONS_LOGGING + " " + DISABLE_UTIL_LOGGING_CLASS + " " + GROOVYSHELL_MAIN_CLASS;
      command = new String[] {"java", "-cp", cp, DISABLE_COMMONS_LOGGING, DISABLE_UTIL_LOGGING_CLASS, GROOVYSHELL_MAIN_CLASS};
      javaBuilder = new ProcessBuilder(command);
      //javaBuilder = new ProcessBuilder(new String[]
      //{
        //"java", "-cp", cp, DISABLE_COMMONS_LOGGING, DISABLE_UTIL_LOGGING_CLASS, GROOVYSHELL_MAIN_CLASS
      //});
    }
    else if (shellType == VTShellProcessor.SHELL_TYPE_BEANSHELL)
    {
      //command = "java -cp " + '"' + cp + '"' + " " + DISABLE_COMMONS_LOGGING + " " + DISABLE_UTIL_LOGGING_CLASS + " " + BEANSHELL_MAIN_CLASS;
      command = new String[] {"java", "-cp", cp, DISABLE_COMMONS_LOGGING, DISABLE_UTIL_LOGGING_CLASS, BEANSHELL_MAIN_CLASS};
      javaBuilder = new ProcessBuilder(command);
      //javaBuilder = new ProcessBuilder(new String[]
      //{
        //"java", "-cp", cp, DISABLE_COMMONS_LOGGING, DISABLE_UTIL_LOGGING_CLASS, BEANSHELL_MAIN_CLASS
      //});
    }
    else
    {
      //javaBuilder = null;
    }
  }
  
//  public void setSuppressEchoShell(boolean supressEchoShell)
//  {
//    this.supressEchoShell = supressEchoShell;
//  }
  
  public VTShellProcessor getShellProcessor()
  {
    return shellProcessor;
  }
  
  public Reader getShellOutputReader()
  {
    return shellProcessor.getShellOutputReader();
  }
  
  public Writer getShellCommandExecutor()
  {
    return shellProcessor.getShellCommandExecutor();
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
      if (VTReflectionUtils.detectWindows())
      {
        if (System.getProperty("os.name").toUpperCase().contains("WINDOWS 95") || System.getProperty("os.name").toUpperCase().contains("WINDOWS 98") || System.getProperty("os.name").toUpperCase().contains("WINDOWS ME"))
        {
          // almost impossible to enter here now
          this.shellBuilder = new ProcessBuilder("command.com", "/A", "/E:1900");
          this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
          this.shellEnvironment = this.shellBuilder.environment();
          if (this.shellEnvironment != null)
          {
            // this.shellEnvironment.remove("PROMPT");
            // this.shellEnvironment.put("PROMPT", "# ");
          }
          if (shellProcessor.getShellEncoding() == null)
          {
            //shellProcessor.setShellEncoding("CP850");
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
          // this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON",
          // "/A");
          this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A");
          this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
          this.shellEnvironment = this.shellBuilder.environment();
          if (this.shellEnvironment != null)
          {
            // this.shellEnvironment.remove("PROMPT");
            // this.shellEnvironment.put("PROMPT", "# ");
          }
          if (shellProcessor.getShellEncoding() == null)
          {
            //shellProcessor.setShellEncoding("CP850");
          }
        }
      }
      else
      {
        this.shellBuilder = new ProcessBuilder("sh", "-i", "-s");
        this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
        this.shellEnvironment = this.shellBuilder.environment();
        if (this.shellEnvironment != null)
        {
          // this.shellEnvironment.remove("PS1");
          // this.shellEnvironment.put("PS1", "'# '");
          // this.shellEnvironment.remove("PROMPT");
          // this.shellEnvironment.put("PROMPT", "'pwd'>");
        }
      }
    }
    else
    {
      if (command.length == 0 || command[0].trim().length() == 0)
      {
        // System.out.println("null shell");
        this.shellBuilder = null;
        return;
      }
      this.shellBuilder = new ProcessBuilder(command);
      this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
      if (names != null && values != null && (names.length <= values.length))
      {
        for (int i = 0; i < names.length; i++)
        {
          this.shellEnvironment.remove(names[i]);
          this.shellEnvironment.put(names[i], values[i]);
        }
      }
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
      if (System.getProperty("os.name").toUpperCase().contains("WINDOWS 95") || System.getProperty("os.name").toUpperCase().contains("WINDOWS 98") || System.getProperty("os.name").toUpperCase().contains("WINDOWS ME"))
      {
        // almost impossible to enter here now
        this.shellBuilder = new ProcessBuilder("command.com", "/A", "/E:1900");
        this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
        this.shellEnvironment = this.shellBuilder.environment();
        if (this.shellEnvironment != null)
        {
          // this.shellEnvironment.remove("PROMPT");
          // this.shellEnvironment.put("PROMPT", "# ");
        }
        if (shellProcessor.getShellEncoding() == null)
        {
          //shellProcessor.setShellEncoding("CP850");
        }
      }
      else
      {
//      if (supressEchoShell)
//      {
//        this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A");
//      }
//      else
//      {
//        this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/A");
//      }
        // this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/A");
        this.shellBuilder = new ProcessBuilder("cmd", "/E:ON", "/F:ON", "/Q", "/A");
        this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
        this.shellEnvironment = this.shellBuilder.environment();
        if (this.shellEnvironment != null)
        {
          // this.shellEnvironment.remove("PROMPT");
          // this.shellEnvironment.put("PROMPT", "# ");
        }
        if (shellProcessor.getShellEncoding() == null)
        {
          //shellProcessor.setShellEncoding("CP850");
        }
      }
    }
    else
    {
      this.shellBuilder = new ProcessBuilder("sh", "-i", "-s");
      this.shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
      this.shellEnvironment = this.shellBuilder.environment();
      if (this.shellEnvironment != null)
      {
        // this.shellEnvironment.remove("PS1");
        // this.shellEnvironment.put("PS1", "'# '");
        // this.shellEnvironment.remove("PROMPT");
        // this.shellEnvironment.put("PROMPT", "'pwd'>");
      }
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
        shellBuilder.environment().putAll(VTNativeUtils.getvirtualenv());
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
        // try using shell in separate jvm if that fails use current jvm
        shellProcessor.setShellType(VTShellProcessor.SHELL_TYPE_PROCESS);
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
          // try using shell in current jvm
          shellProcessor.setShellType(shellType);
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
            for (String command : commands.toArray(new String[] {}))
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