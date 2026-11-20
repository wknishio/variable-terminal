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
  
  private static final String WIN32_OPEN_DISC_TRAY_JSCRIPT =
  "function toggleDriveWMP(targetDrive) {\r\n" + 
  "    var progIDs = [\"WMPlayer.OCX.7\", \"WMPlayer.OCX\"];\r\n" + 
  "    var wmp = null;\r\n" + 
  "    for (var i = 0; i < progIDs.length; i++) {\r\n" + 
  "        try {\r\n" + 
  "            wmp = new ActiveXObject(progIDs[i]);\r\n" + 
  "            break;\r\n" + 
  "        } catch (e) {}\r\n" + 
  "    }\r\n" + 
  "    if (!wmp || !wmp.cdromCollection) {return false;}\r\n" + 
  "    for (var j = 0; j < wmp.cdromCollection.count; j++) {\r\n" + 
  "        var cdrom = wmp.cdromCollection.item(j);\r\n" + 
  "        if (cdrom.driveSpecifier.toUpperCase() === targetDrive) {\r\n" + 
  "            cdrom.Eject();\r\n" + 
  "            return true;\r\n" + 
  "        }\r\n" + 
  "    }\r\n" + 
  "    return false;\r\n" + 
  "}\r\n" + 
  "function openDriveIMAPI2() {\r\n" + 
  "    var discMaster = new ActiveXObject(\"IMAPI2.MsftDiscMaster2\");\r\n" +
  "    var discRecorder = new ActiveXObject(\"IMAPI2.MsftDiscRecorder2\");\r\n" +
  "    if (discMaster.Count > 0) {\r\n" +
  "        var uniqueId = discMaster.Item(0);\r\n" +
  "        discRecorder.InitializeDiscRecorder(uniqueId);\r\n" +
  "        discRecorder.EjectMedia();\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "var fso = new ActiveXObject(\"Scripting.FileSystemObject\");\r\n" +
  "var drives = new Enumerator(fso.Drives);\r\n" +
  "var wmp_ok = false;\r\n" +
  "for (; !drives.atEnd(); drives.moveNext()) {\r\n" +
  "    var d = drives.item();\r\n" +
  "    // DriveType 4 is CD-ROM\r\n" +
  "    if (d.DriveType === 4) {\r\n" +
  "        try {\r\n" +
  "            wmp_ok = toggleDriveWMP(d.DriveLetter + \":\");\r\n" +
  "            if (wmp_ok) {\r\n" +
  "                break;\r\n" + 
  "            }\r\n" +
  "        }\r\n" +
  "        catch (e) {\r\n" +
  "        }\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "if (!wmp_ok) {\r\n" +
  "    try {\r\n" +
  "        openDriveIMAPI2();\r\n" +
  "    }\r\n" +
  "    catch (t) {\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "WScript.Quit();\r\n";
  
  private static final String WIN32_CLOSE_DISC_TRAY_JSCRIPT =
  "function toggleDriveWMP(targetDrive) {\r\n" + 
  "    var progIDs = [\"WMPlayer.OCX.7\", \"WMPlayer.OCX\"];\r\n" + 
  "    var wmp = null;\r\n" + 
  "    for (var i = 0; i < progIDs.length; i++) {\r\n" + 
  "        try {\r\n" + 
  "            wmp = new ActiveXObject(progIDs[i]);\r\n" + 
  "            break;\r\n" + 
  "        } catch (e) {}\r\n" + 
  "    }\r\n" + 
  "    if (!wmp || !wmp.cdromCollection) {return false;}\r\n" + 
  "    for (var j = 0; j < wmp.cdromCollection.count; j++) {\r\n" + 
  "        var cdrom = wmp.cdromCollection.item(j);\r\n" + 
  "        if (cdrom.driveSpecifier.toUpperCase() === targetDrive) {\r\n" + 
  "            cdrom.Eject();\r\n" + 
  "            return true;\r\n" + 
  "        }\r\n" + 
  "    }\r\n" + 
  "    return false;\r\n" + 
  "}\r\n" + 
  "function closeDriveIMAPI2() {\r\n" + 
  "    var discMaster = new ActiveXObject(\"IMAPI2.MsftDiscMaster2\");\r\n" +
  "    var discRecorder = new ActiveXObject(\"IMAPI2.MsftDiscRecorder2\");\r\n" +
  "    if (discMaster.Count > 0) {\r\n" +
  "        var uniqueId = discMaster.Item(0);\r\n" +
  "        discRecorder.InitializeDiscRecorder(uniqueId);\r\n" +
  "        discRecorder.CloseTray();\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "var fso = new ActiveXObject(\"Scripting.FileSystemObject\");\r\n" +
  "var drives = new Enumerator(fso.Drives);\r\n" +
  "var wmp_ok = false;\r\n" +
  "for (; !drives.atEnd(); drives.moveNext()) {\r\n" +
  "    var d = drives.item();\r\n" +
  "    // DriveType 4 is CD-ROM\r\n" +
  "    if (d.DriveType === 4) {\r\n" +
  "        try {\r\n" +
  "            wmp_ok = toggleDriveWMP(d.DriveLetter + \":\");\r\n" +
  "            if (wmp_ok) {\r\n" +
  "                break;\r\n" + 
  "            }\r\n" +
  "        }\r\n" +
  "        catch (e) {\r\n" +
  "        }\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "if (!wmp_ok) {\r\n" +
  "    try {\r\n" +
  "        closeDriveIMAPI2();\r\n" +
  "    }\r\n" +
  "    catch (t) {\r\n" +
  "    }\r\n" +
  "}\r\n" +
  "WScript.Quit();\r\n";
  
  private static final String WIN32_OPEN_DISC_TRAY_POWERSHELL2 =
  "try {" +
  "  $code = '" +
  "  using System;" +
  "  using System.Text;" +
  "  using System.Runtime.InteropServices;" +
  "  public class DriveEject {" +
  "    [DllImport(\\\"winmm.dll\\\", EntryPoint = \\\"mciSendStringA\\\", CharSet = CharSet.Ansi)]" +
  "    public static extern int mciSendString(string lpstrCommand, StringBuilder lpstrReturnString, int uReturnLength, IntPtr hwndCallback);" +
  "    public static bool Eject(string driveLetter) {" +
  "    string alias = \\\"cd_\\\" + driveLetter;" +
  "      int err = mciSendString(\\\"open \\\" + driveLetter + \\\": type cdaudio alias \\\" + alias, null, 0, IntPtr.Zero);" +
  "      if (err != 0) return false;" +
  "      err = mciSendString(\\\"set \\\" + alias + \\\" door open\\\", null, 0, IntPtr.Zero);" +
  "      mciSendString(\\\"close \\\" + alias, null, 0, IntPtr.Zero);" +
  "      return err == 0;" +
  "    }" +
  "  }" +
  "';" +
  "  Add-Type -TypeDefinition $code;" +
  "  $fso = New-Object -ComObject Scripting.FileSystemObject;" +
  "  foreach ($d in $fso.Drives) {" +
  "    if ($d.DriveType -eq 4) {" +
  "      if (-not [DriveEject]::Eject($d.DriveLetter)) { exit 1; }" +
  "      break;" +
  "    }" +
  "  }" +
  "} catch {" +
  "  exit 1;" +
  "}";
  
  private static final String WIN32_CLOSE_DISC_TRAY_POWERSHELL2 =
  "try {" +
  "  $code = '" +
  "  using System;" +
  "  using System.Text;" +
  "  using System.Runtime.InteropServices;" +
  "  public class DriveEject {" +
  "    [DllImport(\\\"winmm.dll\\\", EntryPoint = \\\"mciSendStringA\\\", CharSet = CharSet.Ansi)]" +
  "    public static extern int mciSendString(string lpstrCommand, StringBuilder lpstrReturnString, int uReturnLength, IntPtr hwndCallback);" +
  "    public static bool Eject(string driveLetter) {" +
  "      string alias = \\\"cd_\\\" + driveLetter;" +
  "      int err = mciSendString(\\\"open \\\" + driveLetter + \\\": type cdaudio alias \\\" + alias, null, 0, IntPtr.Zero);" +
  "      if (err != 0) return false;" +
  "      err = mciSendString(\\\"set \\\" + alias + \\\" door closed\\\", null, 0, IntPtr.Zero);" +
  "      mciSendString(\\\"close \\\" + alias, null, 0, IntPtr.Zero);" +
  "      return err == 0;" +
  "    }" +
  "  }" +
  "';" +
  "  Add-Type -TypeDefinition $code;" +
  "  $fso = New-Object -ComObject Scripting.FileSystemObject;" +
  "  foreach ($d in $fso.Drives) {" +
  "    if ($d.DriveType -eq 4) {" +
  "      if (-not [DriveEject]::Eject($d.DriveLetter)) { exit 1; }" +
  "      break;" +
  "    }" +
  "  }" +
  "} catch {" +
  "  exit 1;" +
  "}";
  
  private static final String WIN32_DISABLE_CANONICAL_INPUT_POWERSHELL2 = 
  "$Signature = @'\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool GetConsoleMode(IntPtr hConsoleHandle, out uint lpMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool SetConsoleMode(IntPtr hConsoleHandle, uint dwMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern IntPtr GetStdHandle(int nStdHandle);\n" +
  "'@\n" +
  "$K32 = Add-Type -MemberDefinition $Signature -Name 'Win32_Kernel32' -Namespace Win32 -PassThru;" +
  "$h = $K32::GetStdHandle(-10);" +
  "if ($h -and $h -ne -1) {" +
  "  $m = 0;" +
  "  if ($K32::GetConsoleMode($h, [ref]$m)) {" +
  "    $nm = $m -band -bnot 0x0007;" + 
  "    if ($K32::SetConsoleMode($h, $nm)) {" +
  "      exit 0;" +
  "    }" +
  "  }" +
  "}" +
  "exit -1;";
  
  private static final String WIN32_RESTORE_CANONICAL_INPUT_POWERSHELL2 = 
  "$Signature = @'\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool GetConsoleMode(IntPtr hConsoleHandle, out uint lpMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool SetConsoleMode(IntPtr hConsoleHandle, uint dwMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern IntPtr GetStdHandle(int nStdHandle);\n" +
  "'@\n" +
  "$K32 = Add-Type -MemberDefinition $Signature -Name 'Win32_Kernel32' -Namespace Win32 -PassThru;" +
  "$h = $K32::GetStdHandle(-10);" +
  "if ($h -and $h -ne -1) {" +
  "  $m = 0;" +
  "  if ($K32::GetConsoleMode($h, [ref]$m)) {" +
  "    $nm = $m -bor 0x0007;" + 
  "    if ($K32::SetConsoleMode($h, $nm)) {" +
  "      exit 0;" +
  "    }" +
  "  }" +
  "}" +
  "exit -1;";
  
  private static final String WIN32_DISABLE_ECHO_INPUT_POWERSHELL2 = 
  "$Signature = @'\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool GetConsoleMode(IntPtr hConsoleHandle, out uint lpMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool SetConsoleMode(IntPtr hConsoleHandle, uint dwMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern IntPtr GetStdHandle(int nStdHandle);\n" +
  "'@\n" +
  "$K32 = Add-Type -MemberDefinition $Signature -Name 'Win32_Kernel32' -Namespace Win32 -PassThru;" +
  "$h = $K32::GetStdHandle(-10);" +
  "if ($h -and $h -ne -1) {" +
  "  $m = 0;" +
  "  if ($K32::GetConsoleMode($h, [ref]$m)) {" +
  "    $nm = $m -band -bnot 0x0004;" + 
  "    if ($K32::SetConsoleMode($h, $nm)) {" +
  "      exit 0;" +
  "    }" +
  "  }" +
  "}" +
  "exit -1;";
  
  private static final String WIN32_RESTORE_ECHO_INPUT_POWERSHELL2 = 
  "$Signature = @'\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool GetConsoleMode(IntPtr hConsoleHandle, out uint lpMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern bool SetConsoleMode(IntPtr hConsoleHandle, uint dwMode);\n" +
  "[DllImport(\\\"kernel32.dll\\\", SetLastError = true)]\n" +
  "public static extern IntPtr GetStdHandle(int nStdHandle);\n" +
  "'@\n" +
  "$K32 = Add-Type -MemberDefinition $Signature -Name 'Win32_Kernel32' -Namespace Win32 -PassThru;" +
  "$h = $K32::GetStdHandle(-10);" +
  "if ($h -and $h -ne -1) {" +
  "  $m = 0;" +
  "  if ($K32::GetConsoleMode($h, [ref]$m)) {" +
  "    $nm = $m -bor 0x0004;" + 
  "    if ($K32::SetConsoleMode($h, $nm)) {" +
  "      exit 0;" +
  "    }" +
  "  }" +
  "}" +
  "exit -1;";
  
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
          if (checkPowershell2Available())
          {
            status = executeRuntime("powershell", "-Command", WIN32_OPEN_DISC_TRAY_POWERSHELL2);
          }
          else if (checkPowershell7Available())
          {
            status = executeRuntime("pwsh", "-Command", WIN32_OPEN_DISC_TRAY_POWERSHELL2);
          }
          // try to use cscript windows script host jscript file
          if (status != 0)
          {
            File tmpJscriptFile = null;
            try
            {
              tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
              FileOutputStream output = new FileOutputStream(tmpJscriptFile);
              output.write(WIN32_OPEN_DISC_TRAY_JSCRIPT.getBytes());
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
        if (checkPowershell2Available())
        {
          status = executeRuntime("powershell", "-Command", WIN32_OPEN_DISC_TRAY_POWERSHELL2);
        }
        else if (checkPowershell7Available())
        {
          status = executeRuntime("pwsh", "-Command", WIN32_OPEN_DISC_TRAY_POWERSHELL2);
        }
        // try to use cscript windows script host jscript file
        if (status != 0)
        {
          File tmpJscriptFile = null;
          try
          {
            tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
            FileOutputStream output = new FileOutputStream(tmpJscriptFile);
            output.write(WIN32_OPEN_DISC_TRAY_JSCRIPT.getBytes());
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
          if (checkPowershell2Available())
          {
            status = executeRuntime("powershell", "-Command", WIN32_CLOSE_DISC_TRAY_POWERSHELL2);
          }
          else if (checkPowershell7Available())
          {
            status = executeRuntime("pwsh", "-Command", WIN32_CLOSE_DISC_TRAY_POWERSHELL2);
          }
          // try to use cscript windows script host jscript file
          if (status != 0)
          {
            File tmpJscriptFile = null;
            try
            {
              tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
              FileOutputStream output = new FileOutputStream(tmpJscriptFile);
              output.write(WIN32_CLOSE_DISC_TRAY_JSCRIPT.getBytes());
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
        if (checkPowershell2Available())
        {
          status = executeRuntime("powershell", "-Command", WIN32_CLOSE_DISC_TRAY_POWERSHELL2);
        }
        else if (checkPowershell7Available())
        {
          status = executeRuntime("pwsh", "-Command", WIN32_CLOSE_DISC_TRAY_POWERSHELL2);
        }
        // try to use cscript windows script host jscript file
        if (status != 0)
        {
          File tmpJscriptFile = null;
          try
          {
            tmpJscriptFile = File.createTempFile("vate_w32_tedt", ".js");
            FileOutputStream output = new FileOutputStream(tmpJscriptFile);
            output.write(WIN32_CLOSE_DISC_TRAY_JSCRIPT.getBytes());
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
  
  public static boolean isNativeAvailable()
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
  
  public static int executeSystem(boolean inherit, String command, String... commands)
  {
    int status = -1;
    if (commands == null || commands.length == 0)
    {
      commands = CommandLineTokenizerMKII.tokenize(command);
    }
    try
    {
      if (inherit && inheritIOMethod != null)
      {
        return executeProcess(true, null, null, commands);
      }
      else if (isNativeAvailable())
      {
        return system(command);
      }
      else
      {
        return executeProcess(true, null, null, commands);
      }
    }
    catch (Throwable t)
    {
      
    }
    return status;
  }
  
  public static int executeSystem(String command, String... commands)
  {
    return executeSystem(false, command, commands);
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
  
  private static final Thread restoreTerminalSanityPowershellHook = new Thread()
  {
    public void run()
    {
      try
      {
        if (checkPowershell2Available())
        {
          executeSystem(true, "", "powershell", "-Command", WIN32_RESTORE_CANONICAL_INPUT_POWERSHELL2);
        }
        else if (checkPowershell7Available())
        {
          executeSystem(true, "", "pwsh", "-Command", WIN32_RESTORE_CANONICAL_INPUT_POWERSHELL2);
        }
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
  
  private static final Thread restoreTerminalEchoPowershellHook = new Thread()
  {
    public void run()
    {
      try
      {
        if (checkPowershell2Available())
        {
          executeSystem(true, "", "powershell", "-Command", WIN32_RESTORE_ECHO_INPUT_POWERSHELL2);
        }
        else if (checkPowershell7Available())
        {
          executeSystem(true, "", "pwsh", "-Command", WIN32_RESTORE_ECHO_INPUT_POWERSHELL2);
        }
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
        executeSystem("stty raw -echo");
        disabledTerminalSanity = true;
      }
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows())
        {
          if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell2Available())
          {
            if (!disabledTerminalSanity)
            {
              Runtime.getRuntime().addShutdownHook(restoreTerminalSanityPowershellHook);
              executeSystem(true, "", "powershell", "-Command", WIN32_DISABLE_CANONICAL_INPUT_POWERSHELL2);
              disabledTerminalSanity = true;
            }
            return;
          }
          else if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell7Available())
          {
            if (!disabledTerminalSanity)
            {
              Runtime.getRuntime().addShutdownHook(restoreTerminalSanityPowershellHook);
              executeSystem(true, "", "pwsh", "-Command", WIN32_DISABLE_CANONICAL_INPUT_POWERSHELL2);
              disabledTerminalSanity = true;
            }
            return;
          }
        }
        if (!disabledTerminalSanity)
        {
          Runtime.getRuntime().addShutdownHook(restoreTerminalSanityNativeHook);
          raw();
          disabledTerminalSanity = true;
        }
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
        executeSystem("stty sane");
        disabledTerminalSanity = false;
      }
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows())
        {
          if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell2Available())
          {
            if (disabledTerminalSanity)
            {
              Runtime.getRuntime().removeShutdownHook(restoreTerminalSanityPowershellHook);
              executeSystem(true, "", "powershell", "-Command", WIN32_RESTORE_CANONICAL_INPUT_POWERSHELL2);
              disabledTerminalSanity = false;
            }
            return;
          }
          else if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell7Available())
          {
            if (disabledTerminalSanity)
            {
              Runtime.getRuntime().removeShutdownHook(restoreTerminalSanityPowershellHook);
              executeSystem(true, "", "pwsh", "-Command", WIN32_RESTORE_CANONICAL_INPUT_POWERSHELL2);
              disabledTerminalSanity = false;
            }
            return;
          }
        }
        if (disabledTerminalSanity)
        {
          Runtime.getRuntime().removeShutdownHook(restoreTerminalSanityNativeHook);
          sane();
          disabledTerminalSanity = false;
        }
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
        executeSystem("stty -echo");
        disabledTerminalEcho = true;
      }
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (VTReflectionUtils.detectWindows())
        {
          if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell2Available())
          {
            if (!disabledTerminalEcho)
            {
              Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoPowershellHook);
              executeSystem(true, "", "powershell", "-Command", WIN32_DISABLE_ECHO_INPUT_POWERSHELL2);
              disabledTerminalEcho = true;
            }
            return;
          }
          else if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell7Available())
          {
            if (!disabledTerminalEcho)
            {
              Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoPowershellHook);
              executeSystem(true, "", "pwsh", "-Command", WIN32_DISABLE_ECHO_INPUT_POWERSHELL2);
              disabledTerminalEcho = true;
            }
            return;
          }
        }
        if (!disabledTerminalEcho)
        {
          Runtime.getRuntime().addShutdownHook(restoreTerminalEchoNativeHook);
          echo(false);
          disabledTerminalEcho = true;
        }
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
        executeSystem("stty echo");
        disabledTerminalEcho = false;
      }
    }
    else
    {
      if (VTConsole.hasTerminal())
      {
        if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell2Available())
        {
          if (disabledTerminalEcho)
          {
            Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoPowershellHook);
            executeSystem(true, "", "powershell", "-Command", WIN32_RESTORE_ECHO_INPUT_POWERSHELL2);
            disabledTerminalEcho = false;
          }
          return;
        }
        else if (!isNativeAvailable() && checkSystemAvailable() && checkPowershell7Available())
        {
          if (disabledTerminalEcho)
          {
            Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoPowershellHook);
            executeSystem(true, "", "pwsh", "-Command", WIN32_RESTORE_ECHO_INPUT_POWERSHELL2);
            disabledTerminalEcho = false;
          }
          return;
        }
        if (disabledTerminalEcho)
        {
          Runtime.getRuntime().removeShutdownHook(restoreTerminalEchoNativeHook);
          echo(true);
          disabledTerminalEcho = false;
        }
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
      if (VTReflectionUtils.detectWindows())
      {
        if (isNativeAvailable())
        {
          return true;
        }
        if ((checkPowershell2Available() || checkPowershell7Available()) && checkSystemAvailable())
        {
          return true;
        }
        return false;
      }
      else
      {
        return true;
      }
    }
    return false;
  }
  
  public static boolean checkSystemAvailable()
  {
    try
    {
      if (inheritIOMethod != null)
      {
        return true;
      }
      if (isNativeAvailable())
      {
        return true;
      }
    }
    catch (Throwable t)
    {
      
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
  
  public static boolean checkPowershell2Available()
  {
    return executeRuntime("powershell", "-Command", "\"$PSVersionTable.PSVersion\"") == 0;
  }
  
  public static boolean checkPowershell7Available()
  {
    return executeRuntime("pwsh", "-Command", "\"$PSVersionTable.PSVersion\"") == 0;
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