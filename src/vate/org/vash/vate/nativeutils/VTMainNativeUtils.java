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
  
  private static final int SAMPLE_RATE_HERTZ = 16000;
  private static final int SAMPLE_SIZE_BITS = 8;
  
  private static final String WIN32_EJECT_DISC_TRAY_VBS = "On Error Resume Next\r\n" + 
  "For Each d in CreateObject(\"Scripting.FileSystemObject\").Drives\r\n" + 
  "    If d.DriveType = 4 Then\r\n" + 
  "        CreateObject(\"Shell.Application\").Namespace(17).ParseName(d.DriveLetter & \":\\\").InvokeVerb(\"Eject\")\r\n" + 
  "        Exit For\r\n" + 
  "    End If\r\n" + 
  "Next\r\n" + 
  "WScript.Quit";
  
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
        return VTAudioBeeper.beep(SAMPLE_RATE_HERTZ, SAMPLE_SIZE_BITS, freq, dur, block, executorService);
      }
      else
      {
        return true;
      }
    }
    else
    {
      return VTAudioBeeper.beep(SAMPLE_RATE_HERTZ, SAMPLE_SIZE_BITS, freq, dur, block, executorService);
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
            int status = Runtime.getRuntime().exec(new String[]{"eject", "-r"}).waitFor();
            return status == 0;
          }
          catch (Throwable e)
          {
            
          }
        }
        else
        {
          // try to use cscript windows script host vbs file
          File tmpedtvbsFile = null;
          int status = -1;
          try
          {
            tmpedtvbsFile = File.createTempFile("vate_w32_tedt", ".vbs");
            FileOutputStream output = new FileOutputStream(tmpedtvbsFile);
            output.write(WIN32_EJECT_DISC_TRAY_VBS.getBytes());
            output.flush();
            output.close();
            status = Runtime.getRuntime().exec(new String[]{"cscript", tmpedtvbsFile.getAbsolutePath()}).waitFor();
          }
          catch (Throwable e)
          {
            
          }
          if (tmpedtvbsFile != null)
          {
            VTFileUtils.truncateDeleteQuietly(tmpedtvbsFile);
            //tmpedtvbsFile.delete();
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
          int status = Runtime.getRuntime().exec(new String[]{"eject", "-r"}).waitFor();
          return status == 0;
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        // try to use cscript windows script host vbs file
        File tmpedtvbsFile = null;
        int status = -1;
        try
        {
          tmpedtvbsFile = File.createTempFile("vate_w32_tedt", ".vbs");
          FileOutputStream output = new FileOutputStream(tmpedtvbsFile);
          output.write(WIN32_EJECT_DISC_TRAY_VBS.getBytes());
          output.flush();
          output.close();
          status = Runtime.getRuntime().exec(new String[]{"cscript", tmpedtvbsFile.getAbsolutePath()}).waitFor();
        }
        catch (Throwable e)
        {
          
        }
        if (tmpedtvbsFile != null)
        {
          VTFileUtils.truncateDeleteQuietly(tmpedtvbsFile);
          //tmpedtvbsFile.delete();
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
            int status = Runtime.getRuntime().exec(new String[]{"eject", "-t"}).waitFor();
            return status == 0;
          }
          catch (Throwable e)
          {
            
          }
        }
        else
        {
          // try to use cscript windows script host vbs file
          File tmpedtvbsFile = null;
          int status = -1;
          try
          {
            tmpedtvbsFile = File.createTempFile("vate_w32_tedt", ".vbs");
            FileOutputStream output = new FileOutputStream(tmpedtvbsFile);
            output.write(WIN32_EJECT_DISC_TRAY_VBS.getBytes());
            output.flush();
            output.close();
            status = Runtime.getRuntime().exec(new String[]{"cscript", tmpedtvbsFile.getAbsolutePath()}).waitFor();
          }
          catch (Throwable e)
          {
            
          }
          if (tmpedtvbsFile != null)
          {
            VTFileUtils.truncateDeleteQuietly(tmpedtvbsFile);
            //tmpedtvbsFile.delete();
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
          int status = Runtime.getRuntime().exec(new String[]{"eject", "-t"}).waitFor();
          return status == 0;
        }
        catch (Throwable e)
        {
          
        }
      }
      else
      {
        // try to use cscript windows script host vbs file
        File tmpedtvbsFile = null;
        int status = -1;
        try
        {
          tmpedtvbsFile = File.createTempFile("vate_w32_tedt", ".vbs");
          FileOutputStream output = new FileOutputStream(tmpedtvbsFile);
          output.write(WIN32_EJECT_DISC_TRAY_VBS.getBytes());
          output.flush();
          output.close();
          status = Runtime.getRuntime().exec(new String[]{"cscript", tmpedtvbsFile.getAbsolutePath()}).waitFor();
        }
        catch (Throwable e)
        {
          
        }
        if (tmpedtvbsFile != null)
        {
          VTFileUtils.truncateDeleteQuietly(tmpedtvbsFile);
          //tmpedtvbsFile.delete();
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
  
  public static void echo(boolean enabled)
  {
    if (checkNativeUtils())
    {
      nativeUtils.echo(enabled);
    }
  }
  
  public static void icanon(boolean enabled)
  {
    if (checkNativeUtils())
    {
      nativeUtils.icanon(enabled);
    }
  }
  
  public static void raw()
  {
    if (checkNativeUtils())
    {
      nativeUtils.raw();
    }
  }
  
  public static void sane()
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
        command.redirectErrorStream(true);
        Process process = command.start();
        if (input != null)
        {
          new Thread(new VTRuntimeProcessDataRedirector(input, process.getOutputStream(), false)).start();
        }
        if (output != null)
        {
          new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), output, false)).start();
        }
        else
        {
          new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), new VTNullOutputStream(), false)).start();
        }
        status = process.waitFor();
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
  
  public static int executeRuntime(String command)
  {
    int status = -1;
    try
    {
      Process process = Runtime.getRuntime().exec(command);
      new Thread(new VTRuntimeProcessDataRedirector(process.getInputStream(), new VTNullOutputStream(), false)).start();
      status = process.waitFor();
    }
    catch (Throwable t)
    {
      
    }
    return status;
  }
  
  private static final Thread restoreTerminalNativeHook = new Thread()
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
  
  private static final Thread restoreTerminalSystemHook = new Thread()
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
  
  public static void disableTerminalProcessing()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      Runtime.getRuntime().addShutdownHook(restoreTerminalSystemHook);
      executeSystem("stty raw -echo");
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        Runtime.getRuntime().addShutdownHook(restoreTerminalNativeHook);
        raw();
      }
    }
  }
  
  public static void restoreTerminalProcessing()
  {
    if ((executeRuntime("tty -s") != -1) && (executeSystem("tty -s") == 0))
    {
      Runtime.getRuntime().removeShutdownHook(restoreTerminalSystemHook);
      executeSystem("stty sane");
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        Runtime.getRuntime().removeShutdownHook(restoreTerminalNativeHook);
        sane();
      }
    }
  }
}