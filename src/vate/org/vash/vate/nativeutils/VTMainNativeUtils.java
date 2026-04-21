package org.vash.vate.nativeutils;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.Map.Entry;
import org.vash.vate.audio.VTAudioBeeper;
import org.vash.vate.com.martiansoftware.jsap.CommandLineTokenizerMKII;
import org.vash.vate.console.VTConsole;
import org.vash.vate.filesystem.VTFileUtils;
import org.vash.vate.nativeutils.bsd.VTBSDNativeUtils;
import org.vash.vate.nativeutils.linux.VTLinuxNativeUtils;
import org.vash.vate.nativeutils.mac.VTMacNativeUtils;
import org.vash.vate.nativeutils.sunos.VTSunOSNativeUtils;
import org.vash.vate.nativeutils.win32.VTWin32NativeUtils;
import org.vash.vate.reflection.VTReflectionUtils;
import org.vash.vate.runtime.VTRuntimeProcessDataRedirector;
import org.vash.vate.stream.limit.VTNullOutputStream;

import com.sun.jna.Platform;

public class VTMainNativeUtils
{
  private static String[] virtualEnvironmentVariablesBackup;
  private static String[] virtualEnvironmentVariables;
  private static Properties systemPropertiesBackup;
  private static VTNativeUtils nativeUtils;
  
  private static boolean disabledTerminalSanity = false;
  private static boolean disabledTerminalEcho = false;
  
  private static final String WIN32_EJECT_DISC_TRAY_JSCRIPT = 
  "var shell = new ActiveXObject(\"Shell.Application\");\r\n" +
  "var fso = new ActiveXObject(\"Scripting.FileSystemObject\");\r\n" +
  "var drives = new Enumerator(fso.Drives);\r\n" +
  "\r\n" +
  "for (; !drives.atEnd(); drives.moveNext()) {\r\n" +
  "    var d = drives.item();\r\n" +
  "    try {\r\n" +
  "        // DriveType 4 is CD-ROM\r\n" +
  "        if (d.DriveType === 4) {\r\n" +
  "            shell.Namespace(17).ParseName(d.DriveLetter + \":\\\").InvokeVerb(\"Eject\");\r\n" +
  "            break;\r\n" +
  "        }\r\n" +
  "    } catch (e) {\r\n" +
  "       // Equivalent to 'On Error Resume Next' for the loop body\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "\r\n" +
  "WScript.Quit();\r\n";
  
  private static final String WIN32_EJECT_DISC_TRAY_POWERSHELL = 
  "try" +
  "{" +
  "  $drives = Get-WmiObject Win32_LogicalDisk -Filter \"DriveType=4\";" +
  "  foreach ($d in $drives)" +
  "  {" +
  "    $driveLetter = $d.DeviceID;" +
  "    try" +
  "    {" +
  "      (New-Object -ComObject Shell.Application).Namespace(17).ParseName(\"$driveLetter\\\").InvokeVerb('Eject');" +
  "    }" +
  "    catch" +
  "    {" +
  "    }" +
  "  }" +
  "}" + 
  "catch" +
  "{" +
  "}";
  
//  private static final String WIN32_EJECT_DISC_TRAY_VBS = "On Error Resume Next\r\n" + 
//  "For Each d in CreateObject(\"Scripting.FileSystemObject\").Drives\r\n" + 
//  "    If d.DriveType = 4 Then\r\n" + 
//  "        CreateObject(\"Shell.Application\").Namespace(17).ParseName(d.DriveLetter & \":\\\").InvokeVerb(\"Eject\")\r\n" + 
//  "        Exit For\r\n" + 
//  "    End If\r\n" + 
//  "Next\r\n" + 
//  "WScript.Quit";
  
  public synchronized static void initialize()
  {
    if (nativeUtils != null)
    {
      return;
    }
    try
    {
      if (Platform.isWindows())
      {
        nativeUtils = new VTWin32NativeUtils();
      }
      else if (Platform.isLinux() || Platform.isAndroid())
      {
        nativeUtils = new VTLinuxNativeUtils();
      }
      else if (Platform.isFreeBSD() || Platform.isOpenBSD() || Platform.isNetBSD() || Platform.iskFreeBSD())
      {
        nativeUtils = new VTBSDNativeUtils();
      }
      else if (Platform.isSolaris())
      {
        nativeUtils = new VTSunOSNativeUtils();
      }
      else if (Platform.isMac())
      {
        nativeUtils = new VTMacNativeUtils();
      }
      else
      {
        nativeUtils = new VTMinimalNativeUtils();
      }
    }
    catch (Throwable e)
    {
      // e.printStackTrace();
      nativeUtils = new VTNullNativeUtils();
    }
  }
  
  public static int system(String command)
  {
    if (checkNativeUtils())
    {
      return nativeUtils.system(command);
    }
    return -1;
  }
  
  public static int getchar()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.getchar();
    }
    return -1;
  }
  
  public static int getch()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.getch();
    }
    return -1;
  }
  
  public static void printf(String format, Object... args)
  {
    if (checkNativeUtils())
    {
      nativeUtils.printf(format, args);
    }
  }
  
  public static boolean beep(int freq, int dur, boolean block, ExecutorService executorService)
  {
    if (checkNativeUtils())
    {
      boolean nativeBeep = nativeUtils.beep(freq, dur, block);
      if (!nativeBeep)
      {
        return VTAudioBeeper.beep(16000, 8, freq, dur, block, executorService);
      }
      else
      {
        return true;
      }
    }
    else
    {
      return VTAudioBeeper.beep(16000, 8, freq, dur, block, executorService);
    }
  }
  
  public static boolean openDiscDrive()
  {
    if (checkNativeUtils())
    {
      // needs root user on linux
      boolean nativeCD = nativeUtils.openDiscDrive();
      if (!nativeCD)
      {
        if (!VTReflectionUtils.detectWindows())
        {
          // try to call eject on non-windows
          try
          {
            int status = executeRuntime("eject", "-r");
            return status == 0;
          }
          catch (Throwable e)
          {
            
          }
        }
        else
        {
          int status = -1;
          status = executeRuntime("powershell", "-Command", WIN32_EJECT_DISC_TRAY_POWERSHELL);
          // try to use cscript windows script host jscript file
          if (status == -1)
          {
            File tmpJscriptFile = null;
            try
            {
              tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
              FileOutputStream output = new FileOutputStream(tmpJscriptFile);
              output.write(WIN32_EJECT_DISC_TRAY_JSCRIPT.getBytes());
              output.flush();
              output.close();
              status = executeRuntime("cscript", "//e:jscript", tmpJscriptFile.getAbsolutePath());
            }
            catch (Throwable e)
            {
              
            }
            if (tmpJscriptFile != null)
            {
              VTFileUtils.truncateDeleteQuietly(tmpJscriptFile);
            }
          }
          return status == 0;
        }
      }
      else
      {
        return true;
      }
    }
    else
    {
      if (!VTReflectionUtils.detectWindows())
      {
        // try to call eject on non-windows
        try
        {
          int status = executeRuntime("eject", "-r");
          return status == 0;
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        int status = -1;
        status = executeRuntime("powershell", "-Command", WIN32_EJECT_DISC_TRAY_POWERSHELL);
        // try to use cscript windows script host jscript file
        if (status == -1)
        {
          File tmpJscriptFile = null;
          try
          {
            tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
            FileOutputStream output = new FileOutputStream(tmpJscriptFile);
            output.write(WIN32_EJECT_DISC_TRAY_JSCRIPT.getBytes());
            output.flush();
            output.close();
            status = executeRuntime("cscript", "//e:jscript", tmpJscriptFile.getAbsolutePath());
          }
          catch (Throwable e)
          {
            
          }
          if (tmpJscriptFile != null)
          {
            VTFileUtils.truncateDeleteQuietly(tmpJscriptFile);
          }
        }
        return status == 0;
      }
    }
    return false;
  }
  
  public static boolean closeDiscDrive()
  {
    if (checkNativeUtils())
    {
      // needs root user on linux
      boolean nativeCD = nativeUtils.closeDiscDrive();
      if (!nativeCD)
      {
        if (!VTReflectionUtils.detectWindows())
        {
          // try to call eject on non-windows
          try
          {
            int status = executeRuntime("eject", "-r");
            return status == 0;
          }
          catch (Throwable e)
          {
            
          }
        }
        else
        {
          int status = -1;
          status = executeRuntime("powershell", "-Command", WIN32_EJECT_DISC_TRAY_POWERSHELL);
          // try to use cscript windows script host jscript file
          if (status == -1)
          {
            File tmpJscriptFile = null;
            try
            {
              tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
              FileOutputStream output = new FileOutputStream(tmpJscriptFile);
              output.write(WIN32_EJECT_DISC_TRAY_JSCRIPT.getBytes());
              output.flush();
              output.close();
              status = executeRuntime("cscript", "//e:jscript", tmpJscriptFile.getAbsolutePath());
            }
            catch (Throwable e)
            {
              
            }
            if (tmpJscriptFile != null)
            {
              VTFileUtils.truncateDeleteQuietly(tmpJscriptFile);
            }
          }
          return status == 0;
        }
      }
      else
      {
        return true;
      }
    }
    else
    {
      if (!VTReflectionUtils.detectWindows())
      {
        // try to call eject on non-windows
        try
        {
          int status = executeRuntime("eject", "-r");
          return status == 0;
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        int status = -1;
        status = executeRuntime("powershell", "-Command", WIN32_EJECT_DISC_TRAY_POWERSHELL);
        // try to use cscript windows script host jscript file
        if (status == -1)
        {
          File tmpJscriptFile = null;
          try
          {
            tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
            FileOutputStream output = new FileOutputStream(tmpJscriptFile);
            output.write(WIN32_EJECT_DISC_TRAY_JSCRIPT.getBytes());
            output.flush();
            output.close();
            status = executeRuntime("cscript", "//e:jscript", tmpJscriptFile.getAbsolutePath());
          }
          catch (Throwable e)
          {
            
          }
          if (tmpJscriptFile != null)
          {
            VTFileUtils.truncateDeleteQuietly(tmpJscriptFile);
          }
        }
        return status == 0;
      }
    }
    return false;
  }
  
  public static int putvirtualenv(String name, String newValue)
  {
    initvirtualenvs();
    virtualEnvironmentVariables = changeEnvArray(virtualEnvironmentVariables, name, newValue);
    return 0;
  }
  
  public static String[] getvirtualenvs()
  {
    initvirtualenvs();
    return virtualEnvironmentVariables;
  }
  
  public static Map<String, String> getvirtualenv()
  {
    initvirtualenvs();
    return getEnvMapAsMap(virtualEnvironmentVariables);
  }
  
  public static String getvirtualenv(String name)
  {
    initvirtualenvs();
    return getEnv(virtualEnvironmentVariables, name);
  }
  
  public static boolean backupvirtualenvs()
  {
    try
    {
      initvirtualenvs();
      virtualEnvironmentVariablesBackup = cloneArray(virtualEnvironmentVariables);
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  public static boolean restorevirtualenvs()
  {
    try
    {
      initvirtualenvs();
      if (virtualEnvironmentVariablesBackup != null)
      {
        virtualEnvironmentVariables = cloneArray(virtualEnvironmentVariablesBackup);
      }
      else
      {
        virtualEnvironmentVariables = getEnvMapAsArray(System.getenv());
      }
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  public static boolean backupsystemproperties()
  {
    try
    {
      systemPropertiesBackup = new Properties();
      systemPropertiesBackup.putAll(System.getProperties());
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  public static boolean restoresystemproperties()
  {
    try
    {
      // System.getProperties().putAll(systemPropertiesBackup);
      System.setProperties(systemPropertiesBackup);
      return true;
    }
    catch (Throwable e)
    {
      return false;
    }
  }
  
  private static void initvirtualenvs()
  {
    if (virtualEnvironmentVariables == null)
    {
      virtualEnvironmentVariables = getEnvMapAsArray(System.getenv());
    }
  }
  
  private static String[] cloneArray(String[] array)
  {
    String[] clone = new String[array.length];
    System.arraycopy(array, 0, clone, 0, array.length);
    return clone;
  }
  
  private static String[] getEnvMapAsArray(Map<String, String> envMap)
  {
    String[] envs = new String[envMap.size()];
    int count = 0;
    for (Entry<String, String> variable : envMap.entrySet())
    {
      envs[count++] = variable.getKey() + "=" + variable.getValue();
    }
    return envs;
  }
  
  private static Map<String, String> getEnvMapAsMap(String[] envArray)
  {
    Map<String, String> envs = new LinkedHashMap<String, String>();
    for (String variable : envArray)
    {
      if (variable.indexOf('=') != -1)
      {
        envs.put(variable.substring(0, variable.indexOf('=')), variable.substring(variable.indexOf('=') + 1));
      }
    }
    return envs;
  }
  
  private static String getEnv(String[] envs, String name)
  {
    int i = 0;
    for (i = 0; i < envs.length; i++)
    {
      if (envs[i].indexOf('=') != -1)
      {
        if (envs[i].substring(0, envs[i].indexOf('=')).equalsIgnoreCase(name))
        {
          return envs[i].substring(envs[i].indexOf('=') + 1);
        }
      }
    }
    return null;
  }
  
  private static String[] changeEnvArray(String[] envs, String name, String newValue)
  {
    boolean found = false;
    int i = 0;
    for (i = 0; i < envs.length; i++)
    {
      if (envs[i].indexOf('=') != -1)
      {
        if (envs[i].substring(0, envs[i].indexOf('=')).equalsIgnoreCase(name))
        {
          found = true;
          envs[i] = envs[i].substring(0, envs[i].indexOf('=')) + "=" + newValue;
        }
      }
    }
    if (!found)
    {
      String[] newEnvs = new String[envs.length + 1];
      System.arraycopy(envs, 0, newEnvs, 0, envs.length);
      newEnvs[envs.length] = name + "=" + newValue;
      return newEnvs;
    }
    else
    {
      return envs;
    }
  }
  
  public static int getpid()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.getpid();
    }
    return -1;
  }
  
  public static int isatty(int fd)
  {
    if (checkNativeUtils())
    {
      return nativeUtils.isatty(fd);
    }
    return -1;
  }
  
  public static boolean hideConsole()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.hideConsole();
    }
    return false;
  }
  
  public static boolean detachConsole()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.detachConsole();
    }
    return false;
  }
  
  public static boolean attachConsole()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.attachConsole();
    }
    return false;
  }
  
  private static boolean checkNativeUtils()
  {
    if (nativeUtils == null)
    {
      initialize();
    }
    return nativeUtils != null;
  }
  
  public static boolean checkANSI()
  {
    if (checkNativeUtils())
    {
      return nativeUtils.checkANSI();
    }
    return false;
  }
  
  public static boolean isAvailable()
  {
    if (checkNativeUtils())
    {
      return !(nativeUtils instanceof VTNullNativeUtils);
    }
    return false;
  }
  
  private static void echo(boolean enabled)
  {
    if (checkNativeUtils())
    {
      nativeUtils.echo(enabled);
    }
  }
  
  private static void raw()
  {
    if (checkNativeUtils())
    {
      nativeUtils.raw();
    }
  }
  
  private static void sane()
  {
    if (checkNativeUtils())
    {
      nativeUtils.sane();
    }
  }
  
  private static Method inheritIOMethod;
  
  static
  {
    try
    {
      inheritIOMethod = Class.forName("java.lang.ProcessBuilder").getDeclaredMethod("inheritIO");
    }
    catch (Throwable t)
    {
      inheritIOMethod = null;
    }
  }
  
  public static ProcessBuilder createProcessBuilder(boolean inheritIO, boolean redirectError, File directory, Map<String, String> environment, String... commands)
  {
    ProcessBuilder command = null;
    try
    {
      if (commands.length == 1)
      {
        command = new ProcessBuilder(CommandLineTokenizerMKII.tokenize(commands[0]));
      }
      else
      {
        command = new ProcessBuilder(commands);
      }
      if (directory != null)
      {
        command.directory(directory);
      }
      if (environment != null)
      {
        command.environment().clear();
        command.environment().putAll(environment);
      }
      if (inheritIO && inheritIOMethod != null)
      {
        command = (ProcessBuilder) inheritIOMethod.invoke(command);
      }
      command.redirectErrorStream(redirectError);
    }
    catch (Throwable t)
    {
      
    }
    return command;
  }
  
  public static Process createProcess(boolean inheritIO, boolean redirectError, File directory, Map<String, String> environment, String... commands)
  {
    Process process = null;
    try
    {
      ProcessBuilder command = null;
      if (commands.length == 1)
      {
        command = new ProcessBuilder(CommandLineTokenizerMKII.tokenize(commands[0]));
      }
      else
      {
        command = new ProcessBuilder(commands);
      }
      if (directory != null)
      {
        command.directory(directory);
      }
      if (environment != null)
      {
        command.environment().clear();
        command.environment().putAll(environment);
      }
      if (inheritIO && inheritIOMethod != null)
      {
        command = (ProcessBuilder) inheritIOMethod.invoke(command);
      }
      command.redirectErrorStream(redirectError);
      process = command.start();
    }
    catch (Throwable t)
    {
      
    }
    return process;
  }
  
  public static int executeProcess(boolean inheritIO, InputStream input, OutputStream output, String... commands)
  {
    int status = -1;
    try
    {
      ProcessBuilder command = null;
      if (commands.length == 1)
      {
        command = new ProcessBuilder(CommandLineTokenizerMKII.tokenize(commands[0]));
      }
      else
      {
        command = new ProcessBuilder(commands);
      }
      if (inheritIOMethod != null && inheritIO)
      {
        command = (ProcessBuilder) inheritIOMethod.invoke(command);
        status = command.start().waitFor();
      }
      else
      {
        Thread outputConsumer = null;
        Thread inputConsumer = null;
        command.redirectErrorStream(true);
        Process process = command.start();
        if (input != null)
        {
          inputConsumer = new Thread(new VTRuntimeProcessDataRedirector(input, process.getOutputStream(), false));
        }
        if (output != null)
        {
          outputConsumer = new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), output, false));
        }
        else
        {
          outputConsumer = new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), new VTNullOutputStream(), false));
        }
        outputConsumer.setDaemon(true);
        outputConsumer.start();
        if (inputConsumer != null)
        {
          inputConsumer.setDaemon(true);
          inputConsumer.start();
        }
        status = process.waitFor();
        outputConsumer.join();
        if (inputConsumer != null)
        {
          try
          {
            inputConsumer.interrupt();
          }
          catch (Throwable t)
          {
            
          }
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    return status;
  }
  
  public static int executeRuntime(String... commands)
  {
    int status = -1;
    try
    {
      if (commands.length == 1)
      {
        commands = CommandLineTokenizerMKII.tokenize(commands[0]);
      }
      Process process = Runtime.getRuntime().exec(commands);
      Thread outputConsumer = new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), new VTNullOutputStream(), false));
      Thread errorConsumer = new Thread(new VTRuntimeProcessDataRedirector(process.getErrorStream(), new VTNullOutputStream(), false));
      outputConsumer.setDaemon(true);
      errorConsumer.setDaemon(true);
      outputConsumer.start();
      errorConsumer.start();
      status = process.waitFor();
      outputConsumer.join();
      errorConsumer.join();
    }
    catch (Throwable t)
    {
      
    }
    return status;
  }
  
  public static int executeRuntime(InputStream input, OutputStream output, OutputStream error, String... commands)
  {
    int status = -1;
    try
    {
      if (commands.length == 1)
      {
        commands = CommandLineTokenizerMKII.tokenize(commands[0]);
      }
      if (output == null)
      {
        output = new VTNullOutputStream();
      }
      if (error == null)
      {
        error = new VTNullOutputStream();
      }
      Thread inputConsumer = null;
      Process process = Runtime.getRuntime().exec(commands);
      if (input != null)
      {
        inputConsumer = new Thread(new VTRuntimeProcessDataRedirector(input, process.getOutputStream(), false));
      }
      Thread outputConsumer = new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), output, false));
      Thread errorConsumer = new Thread(new VTRuntimeProcessDataRedirector(process.getErrorStream(), error, false));
      outputConsumer.setDaemon(true);
      errorConsumer.setDaemon(true);
      outputConsumer.start();
      errorConsumer.start();
      if (inputConsumer != null)
      {
        inputConsumer.setDaemon(true);
        inputConsumer.start();
      }
      status = process.waitFor();
      outputConsumer.join();
      errorConsumer.join();
      if (inputConsumer != null)
      {
        try
        {
          inputConsumer.interrupt();
        }
        catch (Throwable t)
        {
          
        }
      }
    }
    catch (Throwable t)
    {
      
    }
    return status;
  }
  
  public static int executeSystem(String command)
  {
    int status = -1;
    try
    {
      if (isAvailable())
      {
        return system(command);
      }
      else
      {
        return executeProcess(true, null, null, command);
      }
    }
    catch (Throwable t)
    {
      
    }
    return status;
  }
  
  private static final Thread restoreTerminalSanityNativeHook = new Thread()
  {
    public void run()
    {
      try
      {
        sane();
      }
      catch (Throwable t)
      {
        
      }
    }
  };
  
  private static final Thread restoreTerminalSanitySystemHook = new Thread()
  {
    public void run()
    {
      try
      {
        executeSystem("stty sane");
      }
      catch (Throwable t)
      {
        
      }
    }
  };
  
  private static final Thread restoreTerminalEchoNativeHook = new Thread()
  {
    public void run()
    {
      try
      {
        echo(false);
      }
      catch (Throwable t)
      {
        
      }
    }
  };
  
  private static final Thread restoreTerminalEchoSystemHook = new Thread()
  {
    public void run()
    {
      try
      {
        executeSystem("stty echo");
      }
      catch (Throwable t)
      {
        
      }
    }
  };
  
  public static void disableTerminalSanity()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      if (!disabledTerminalSanity)
      {
        Runtime.getRuntime().addShutdownHook(restoreTerminalSanitySystemHook);
      }
      executeSystem("stty raw -echo");
      disabledTerminalSanity = true;
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows() && !isAvailable())
        {
          return;
        }
        if (!disabledTerminalSanity)
        {
          Runtime.getRuntime().addShutdownHook(restoreTerminalSanityNativeHook);
        }
        raw();
        disabledTerminalSanity = true;
      }
    }
  }
  
  public static void restoreTerminalSanity()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      if (disabledTerminalSanity)
      {
        Runtime.getRuntime().removeShutdownHook(restoreTerminalSanitySystemHook);
      }
      executeSystem("stty sane");
      disabledTerminalSanity = false;
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows() && !isAvailable())
        {
          return;
        }
        if (disabledTerminalSanity)
        {
          Runtime.getRuntime().removeShutdownHook(restoreTerminalSanityNativeHook);
        }
        sane();
        disabledTerminalSanity = false;
      }
    }
  }
  
  public static void disableTerminalEcho()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      if (!disabledTerminalEcho)
      {
        Runtime.getRuntime().addShutdownHook(restoreTerminalEchoSystemHook);
      }
      executeSystem("stty -echo");
      disabledTerminalEcho = true;
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows() && !isAvailable())
        {
          return;
        }
        if (!disabledTerminalEcho)
        {
          Runtime.getRuntime().addShutdownHook(restoreTerminalEchoNativeHook);
        }
        echo(false);
        disabledTerminalEcho = true;
      }
    }
  }
  
  public static void restoreTerminalEcho()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      if (disabledTerminalEcho)
      {
        Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoSystemHook);
      }
      executeSystem("stty echo");
      disabledTerminalEcho = false;
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows() && !isAvailable())
        {
          return;
        }
        if (disabledTerminalEcho)
        {
          Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoNativeHook);
        }
        echo(true);
        disabledTerminalEcho = false;
      }
    }
  }
  
  public static boolean checkTerminalAvailable()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      return true;
    }
    else if (VTConsole.hasTerminal())
    {
      if (VTReflectionUtils.detectWindows() && !isAvailable())
      {
        return false;
      }
      else
      {
        return true;
      }
    }
    return false;
  }
  
  public static boolean checkShAvailable()
  {
    return executeRuntime("sh", "-c", "exit") != -1;
  }
  
  public static boolean checkScriptAvailable()
  {
    return executeRuntime("script", "/dev/null", "-q", "-c", "sh", "-c", "exit") != -1;
  }
  
  public static boolean checkWinptyAvailable()
  {
    return executeRuntime("winpty", "--help") != -1;
  }
  
  public static boolean checkPowershellAvailable()
  {
    return executeRuntime("powershell", "-Command", "exit") != -1;
  }
  
  public static boolean checkWindowsTerminalAvailable()
  {
    return executeRuntime("cmd", "/c", "start", "/b", "/min", "wt", "cmd", "/c", "exit") != -1;
  }
  
  public static boolean checkWSLShAvailable()
  {
    return executeRuntime("wsl", "sh", "-c", "exit") == 0;
  }
  
  public static boolean checkWSLScriptAvailable()
  {
    return executeRuntime("wsl", "script", "/dev/null", "-q", "-c", "sh", "-c", "exit") == 0;
  }
}