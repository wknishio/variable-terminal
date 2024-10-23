package org.vash.vate.client.console.remote;

import java.awt.FileDialog;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.VT;
import org.vash.vate.client.dialog.VTClientConfigurationDialog;
import org.vash.vate.console.VTConsole;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuItem;
import org.vash.vate.dialog.VTFileDialog;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;

public class VTClientRemoteGraphicalConsoleMenuBar extends VTGraphicalConsoleMenuBar
{
  private static final long serialVersionUID = 1L;
  
  private Menu dialogMenu;
  private MenuItem clientConnectionDialogMenu;
  private MenuItem loadFileDialogMenu;
  private MenuItem saveFileDialogMenu;
  private Menu clientConsoleCommandsMenu;
  private Menu sessionMenu;
  private Menu consoleMenu;
  private Menu shellMenu;
  private Menu fileSystemMenu;
  // private Menu zipFileMenu;
  // private Menu zipFileLocalMenu;
  // private Menu zipFileRemoteMenu;
  private Menu fileMenu;
  private Menu fileCheckMenu;
  private Menu fileModifyMenu;
  private Menu fileTransferMenu;
  private Menu graphicalSystemsMenu;
  private Menu graphicsModeMenu;
  private Menu screenCaptureMenu;
  private Menu pointerScreenCaptureMenu;
  private Menu cleanScreenCaptureMenu;
  private Menu screenAlertMenu;
  private Menu browseMenu;
  private Menu printApplicationMenu;
  private Menu runtimeMenu;
  private Menu manageRuntimeMenu;
  private Menu networkMenu;
  private Menu performanceMenu;
  private Menu networkTunnelsMenu;
  private Menu networkInterfacesMenu;
  // private Menu remoteSOCKSTunnelsMenu;
  private Menu printMenu;
  private Menu audioSoundMenu;
  private Menu beepSoundMenu;
  private Menu opticalDriveMenu;
  // private Menu settingsMenu;
  private Menu settingsMenu;
  // private Menu remoteAuthenticationSettingsMenu;
  private Menu connectionSettingsMenu;
  private Menu proxySettingsMenu;
  private Menu encryptionSettingsMenu;
  private Menu sessionsSettingsMenu;
  private Menu audioMixersSoundMenu;
  // private Frame frame;
  // private VTConnectionDialog dialog;
  private Menu audioLinkSoundMenu;
  private Menu helpMenu;
  
  public VTClientRemoteGraphicalConsoleMenuBar(final VTClientConfigurationDialog connectionDialog)
  {
    removeAllMenus();
    // this.dialog = dialog;
    // this.frame = frame;
    final VTFileDialog loadFileDialog = new VTFileDialog(VTConsole.getFrame(), "Variable-Terminal " + VT.VT_VERSION + " - Client - Load File", FileDialog.LOAD);
    final VTFileDialog saveFileDialog = new VTFileDialog(VTConsole.getFrame(), "Variable-Terminal " + VT.VT_VERSION + " - Client - Save File", FileDialog.SAVE);
    clientConsoleCommandsMenu = new Menu("Command");
    // clientConsoleCommandsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_C,
    // true));
    
    graphicalSystemsMenu = new Menu("Graphical ");
    graphicalSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Remote Display Devices", "*VTDISPLAY\n"));
    graphicsModeMenu = new Menu("Remote Graphics Link ");
    screenCaptureMenu = new Menu("Remote Screen Capture ");
    screenAlertMenu = new Menu("Remote Screen Alert ");
    browseMenu = new Menu("Remote Browser Program ");
    printApplicationMenu = new Menu("Remote Mail Program ");
    pointerScreenCaptureMenu = new Menu("Show Cursor ");
    cleanScreenCaptureMenu = new Menu("Hide Cursor ");
    graphicalSystemsMenu.add(screenCaptureMenu);
    graphicalSystemsMenu.add(screenAlertMenu);
    graphicalSystemsMenu.add(graphicsModeMenu);
    graphicalSystemsMenu.add(browseMenu);
    graphicalSystemsMenu.add(printApplicationMenu);
    screenCaptureMenu.add(cleanScreenCaptureMenu);
    screenCaptureMenu.add(pointerScreenCaptureMenu);
    graphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Toggle Graphics Link", "*VTGRAPHICSLINK\n"));
    graphicsModeMenu.add(new VTGraphicalConsoleMenuItem("View Graphics Link", "*VTGRAPHICSLINK V\n"));
    graphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Control Graphics Link", "*VTGRAPHICSLINK C\n"));
    graphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTGRAPHICSLINK\n"));
    
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("True Color Quality", "*VTSCREENSHOT CT "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Ultra Color Quality", "*VTSCREENSHOT CU "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Vast Color Quality", "*VTSCREENSHOT CV "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT CH "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Extra Color Quality", "*VTSCREENSHOT CE "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Next Color Quality", "*VTSCREENSHOT CN "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT CM "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Simple Color Quality", "*VTSCREENSHOT CS "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Few Color Quality", "*VTSCREENSHOT CF "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT CL "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Gray Color Quality", "*VTSCREENSHOT CG "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Dull Color Quality", "*VTSCREENSHOT CD "));
    pointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT CW "));
    
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("True Color Quality", "*VTSCREENSHOT OT "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Ultra Color Quality", "*VTSCREENSHOT OU "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Vast Color Quality", "*VTSCREENSHOT OV "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT OH "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Extra Color Quality", "*VTSCREENSHOT OE "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Next Color Quality", "*VTSCREENSHOT ON "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT OM "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Simple Color Quality", "*VTSCREENSHOT OS "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Few Color Quality", "*VTSCREENSHOT OF "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT OL "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Gray Color Quality", "*VTSCREENSHOT OG "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Dull Color Quality", "*VTSCREENSHOT OD "));
    cleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT OW "));
    
    screenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSCREENSHOT\n"));
    screenAlertMenu.add(new VTGraphicalConsoleMenuItem("Show Alert", "*VTSCREENALERT "));
    screenAlertMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSCREENALERT\n"));
    browseMenu.add(new VTGraphicalConsoleMenuItem("Browse URI", "*VTBROWSE "));
    browseMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTBROWSE\n"));
    printApplicationMenu.add(new VTGraphicalConsoleMenuItem("Compose Email", "*VTMAIL "));
    printApplicationMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTMAIL\n"));
    
    fileSystemMenu = new Menu("File ");
    fileTransferMenu = new Menu("File Transfer ");
    fileMenu = new Menu("Remote Files ");
    fileCheckMenu = new Menu("Check Files ");
    fileModifyMenu = new Menu("Alter Files ");
    // zipFileMenu = new Menu("Zip Files ");
    // zipFileLocalMenu = new Menu("Local Zip Files ");
    // zipFileRemoteMenu = new Menu("Remote Zip Files ");
    
    fileMenu.add(fileCheckMenu);
    fileMenu.add(fileModifyMenu);
    
    // zipFileMenu.add(zipFileLocalMenu);
    // zipFileMenu.add(zipFileRemoteMenu);
    // zipFileMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP
    // *VTZIP\n"));
    
    fileSystemMenu.add(fileTransferMenu);
    fileSystemMenu.add(fileMenu);
    // fileSystemMenu.add(zipFileMenu);
    
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Send File To Server", "*VTFILETRANSFER P"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Receive File From Server", "*VTFILETRANSFER G"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Report File Transfer", "*VTFILETRANSFER\n"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Stop File Transfer", "*VTFILETRANSFER S\n"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Wait File Transfer", "*VTFILETRANSFER W"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILETRANSFER\n"));
    // clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
    // Working
    // Directory", "*VTWORKDIRECTORY L "));
    // remoteFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
    // Working
    // Directory", "*VTWORKDIRECTORY R "));
    fileCheckMenu.add(new VTGraphicalConsoleMenuItem("List File System Roots", "*VTFILESEEK L\n"));
    fileCheckMenu.add(new VTGraphicalConsoleMenuItem("Show File Information", "*VTFILESEEK I "));
    fileCheckMenu.add(new VTGraphicalConsoleMenuItem("List Directory Contents", "*VTFILESEEK L "));
    fileCheckMenu.add(new VTGraphicalConsoleMenuItem("Report File Seek", "*VTFILESEEK\n"));
    fileCheckMenu.add(new VTGraphicalConsoleMenuItem("Stop File Seek", "*VTFILESEEK S\n"));
    fileCheckMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILESEEK\n"));
    
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Create Empty Directory", "*VTFILEALTER D "));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Create Empty File", "*VTFILEALTER F "));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Remove File", "*VTFILEALTER R "));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Move File", "*VTFILEALTER M "));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Copy File", "*VTFILEALTER C "));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Report File Change", "*VTFILEALTER\n"));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Stop File Change", "*VTFILEALTER S\n"));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Wait File Change", "*VTFILEALTER W"));
    fileModifyMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILEALTER\n"));
    
    // zipFileLocalMenu.add(new VTGraphicalConsoleMenuItem("Compress Zip File",
    // "*VTZIP L C "));
    // zipFileLocalMenu.add(new VTGraphicalConsoleMenuItem("Uncompress Zip
    // File", "*VTZIP L U "));
    // zipFileLocalMenu.add(new VTGraphicalConsoleMenuItem("Decompress Zip
    // File", "*VTZIP L D "));
    // zipFileLocalMenu.add(new VTGraphicalConsoleMenuItem("Report Zip
    // Operation", "*VTZIP L\n"));
    // zipFileLocalMenu.add(new VTGraphicalConsoleMenuItem("Stop Zip Operation",
    // "*VTZIP L S\n"));
    
    // zipFileRemoteMenu.add(new VTGraphicalConsoleMenuItem("Compress Zip File",
    // "*VTZIP R C "));
    // zipFileRemoteMenu.add(new VTGraphicalConsoleMenuItem("Uncompress Zip
    // File", "*VTZIP R U "));
    // zipFileRemoteMenu.add(new VTGraphicalConsoleMenuItem("Decompress Zip
    // File", "*VTZIP R D "));
    // zipFileRemoteMenu.add(new VTGraphicalConsoleMenuItem("Report Zip
    // Operation", "*VTZIP R\n"));
    // zipFileRemoteMenu.add(new VTGraphicalConsoleMenuItem("Stop Zip
    // Operation", "*VTZIP R S\n"));
    
    sessionMenu = new Menu("Session ");
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Disconnect From Server", "*VTEXIT\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Close Client Application", "*VTQUIT\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Close Server Application", "*VTSTOP\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Toggle Server Cover", "*VTCOVER\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Save Client Settings File", "*VTSAVE "));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Detect Chained Sessions", "*VTCHAIN\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("List Connected Clients", "*VTUSER\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Send Message To Server", "*VTTEXT "));
    
    consoleMenu = new Menu("Console ");
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Clear Remote Console", "*VTCLEAR\n"));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Toggle Console Echo", "*VTECHO\n"));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Show Remote System Time", "*VTDATE\n"));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Pause Local Console", "*VTPAUSE "));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Execute Commands In Files", "*VTREAD "));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Record Commands To File", "*VTLOG "));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Record Console To File", "*VTOUT "));
    // consoleMenu.add(new VTGraphicalConsoleMenuItem("Toggle Remote Console
    // Echo",
    // "*VTECHO\n"));
    shellMenu = new Menu("Shell ");
    shellMenu.add(new VTGraphicalConsoleMenuItem("Run Remote Shell", "*VTSHELL R\n"));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Stop Remote Shell", "*VTSHELL S\n"));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Set Custom Remote Shell", "*VTSHELL C "));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Set Default Remote Shell", "*VTSHELL D\n"));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Set BeanShell Remote Shell", "*VTSHELL B\n"));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Disable Remote Shell", "*VTSHELL N\n"));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Shell Directory", "*VTSHELL P "));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Shell Encoding", "*VTSHELL E "));
    shellMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSHELL\n"));
    
    // settingsMenu = new Menu("Settings ");
    settingsMenu = new Menu("Setting ");
    connectionSettingsMenu = new Menu("Connection ");
    // remoteAuthenticationSettingsMenu = new Menu("Authentication ");
    proxySettingsMenu = new Menu("Proxy ");
    encryptionSettingsMenu = new Menu("Encryption ");
    sessionsSettingsMenu = new Menu("Session ");
    settingsMenu.add(new VTGraphicalConsoleMenuItem("List Server Settings", "*VTSETTING\n"));
    settingsMenu.add(connectionSettingsMenu);
    // remoteSettingsMenu.add(remoteAuthenticationSettingsMenu);
    settingsMenu.add(proxySettingsMenu);
    settingsMenu.add(encryptionSettingsMenu);
    settingsMenu.add(sessionsSettingsMenu);
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Mode", "*VTSETTING CM "));
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Host", "*VTSETTING CH "));
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Port", "*VTSETTING CP "));
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection NAT Port", "*VTSETTING CN "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Type", "*VTSETTING PT "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Host", "*VTSETTING PH "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Port", "*VTSETTING PP "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Authentication", "*VTSETTING PA "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy User", "*VTSETTING PU "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Password", "*VTSETTING PK "));
    encryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Type", "*VTSETTING ET "));
    encryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Password", "*VTSETTING EK "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Maximum", "*VTSETTING SM "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Shell", "*VTSETTING SS "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Accounts", "*VTSETTING SA "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Save Settings File", "*VTSETTING SF "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Load Settings File", "*VTSETTING LF "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSETTING\n"));
    // settingsMenu.add(remoteSettingsMenu);
    
    runtimeMenu = new Menu("Runtime ");
    manageRuntimeMenu = new Menu("Remote Process Control ");
    runtimeMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Environment Variables", "*VTVARIABLE "));
    runtimeMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Java Properties", "*VTPROPERTY "));
    runtimeMenu.add(manageRuntimeMenu);
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Create Managed Process", "*VTRUNTIME M"));
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Create Free Process", "*VTRUNTIME F"));
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("List Managed Processes", "*VTRUNTIME LA\n"));
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Stop Managed Processes", "*VTRUNTIME SA\n"));
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Drop Managed Processes", "*VTRUNTIME DA\n"));
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Runtime Path", "*VTRUNTIME P "));
    manageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTRUNTIME\n"));
    
    performanceMenu = new Menu("Rate ");
    performanceMenu.add(new VTGraphicalConsoleMenuItem("Check Connection Latency", "*VTPING\n"));
    performanceMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Rate Limits", "*VTLIMIT "));
    performanceMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTPING\n*VTHELP *VTLIMIT\n"));
    
    networkMenu = new Menu("Network ");
    
    networkInterfacesMenu = new Menu("List Valid Network Interfaces ");
    networkInterfacesMenu.add(new VTGraphicalConsoleMenuItem("Remote Network Interfaces", "*VTNETWORK R\n"));
    networkInterfacesMenu.add(new VTGraphicalConsoleMenuItem("Local Network Interfaces", "*VTNETWORK L\n"));
    networkInterfacesMenu.add(new VTGraphicalConsoleMenuItem("Any Network Interfaces", "*VTNETWORK\n"));
    
    networkTunnelsMenu = new Menu("Connection Network Tunnels ");
    // remoteSOCKSTunnelsMenu = new Menu("Connection SOCKS Tunnels ");
    
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("List Any Tunnels", "*VTTUNNEL\n"));
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Local To Remote", "*VTTUNNEL L "));
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Remote To Local", "*VTTUNNEL R "));
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTTUNNEL\n"));
    
    networkMenu.add(networkInterfacesMenu);
    networkMenu.add(new VTGraphicalConsoleMenuItem("Resolve Remote Network Host", "*VTHOST "));
    networkMenu.add(networkTunnelsMenu);
    
    printMenu = new Menu("Print ");
    printMenu.add(new VTGraphicalConsoleMenuItem("List Remote Printers", "*VTPRINTER\n"));
    printMenu.add(new VTGraphicalConsoleMenuItem("Detail Remote Printer", "*VTPRINTER "));
    printMenu.add(new VTGraphicalConsoleMenuItem("Print Text In Remote Printer", "*VTPRINTDATA T "));
    printMenu.add(new VTGraphicalConsoleMenuItem("Print File In Remote Printer", "*VTPRINTDATA F "));
    printMenu.add(new VTGraphicalConsoleMenuItem("Stop Remote Printer Task", "*VTPRINTDATA S\n"));
    printMenu.add(new VTGraphicalConsoleMenuItem("Wait Remote Printer Task", "*VTPRINTDATA W"));
    printMenu.add(new VTGraphicalConsoleMenuItem("Commands Usage", "*VTHELP *VTPRINTER\n*VTHELP *VTPRINTDATA\n"));
    
    audioSoundMenu = new Menu("Audio ");
    
    audioMixersSoundMenu = new Menu("List Valid Audio Mixers ");
    audioMixersSoundMenu.add(new VTGraphicalConsoleMenuItem("Remote Audio Mixers", "*VTMIXER R\n"));
    audioMixersSoundMenu.add(new VTGraphicalConsoleMenuItem("Local Audio Mixers", "*VTMIXER L\n"));
    audioMixersSoundMenu.add(new VTGraphicalConsoleMenuItem("Any Audio Mixers", "*VTMIXER\n"));
    audioSoundMenu.add(audioMixersSoundMenu);
    
    //audioSoundMenu.add(new VTGraphicalConsoleMenuItem("Ring Remote Terminal Bell", "*VTBELL\n"));
    beepSoundMenu = new Menu("Remote Audio Beep ");
    beepSoundMenu.add(new VTGraphicalConsoleMenuItem("Play Default Beep", "*VTBEEP\n"));
    beepSoundMenu.add(new VTGraphicalConsoleMenuItem("Play Custom Beep", "*VTBEEP "));
    beepSoundMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTBEEP\n"));
    audioSoundMenu.add(beepSoundMenu);
    
    audioLinkSoundMenu = new Menu("Remote Audio Link ");
    audioLinkSoundMenu.add(new VTGraphicalConsoleMenuItem("Toggle Audio Link", "*VTAUDIOLINK\n"));
    audioLinkSoundMenu.add(new VTGraphicalConsoleMenuItem("Configure Audio Link", "*VTAUDIOLINK "));
    audioLinkSoundMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTAUDIOLINK\n"));
    audioSoundMenu.add(audioLinkSoundMenu);
    
    opticalDriveMenu = new Menu("Disc ");
    opticalDriveMenu.add(new VTGraphicalConsoleMenuItem("Open Remote Disc Drive", "*VTDISCTRAY O\n"));
    opticalDriveMenu.add(new VTGraphicalConsoleMenuItem("Close Remote Disc Drive", "*VTDISCTRAY C\n"));
    
    helpMenu = new Menu("Help ");
    helpMenu.add(new VTGraphicalConsoleMenuItem("Complete Commands", "*VTHELP\n"));
    helpMenu.add(new VTGraphicalConsoleMenuItem("Abbreviated Commands", "*VTHL\n"));
    helpMenu.add(new VTGraphicalConsoleMenuItem("Specific Command", "*VTHELP "));
    
    clientConsoleCommandsMenu.add(sessionMenu);
    // clientConsoleCommandsMenu.add(settingsMenu);
    clientConsoleCommandsMenu.add(consoleMenu);
    clientConsoleCommandsMenu.add(performanceMenu);
    clientConsoleCommandsMenu.add(shellMenu);
    clientConsoleCommandsMenu.add(runtimeMenu);
    clientConsoleCommandsMenu.add(fileSystemMenu);
    clientConsoleCommandsMenu.add(graphicalSystemsMenu);
    clientConsoleCommandsMenu.add(audioSoundMenu);
    clientConsoleCommandsMenu.add(networkMenu);
    // clientConsoleCommandsMenu.add(remoteTunnelsMenu);
    clientConsoleCommandsMenu.add(printMenu);
    clientConsoleCommandsMenu.add(opticalDriveMenu);
    clientConsoleCommandsMenu.add(settingsMenu);
    clientConsoleCommandsMenu.add(helpMenu);
    
    this.add(clientConsoleCommandsMenu);
    clientConsoleCommandsMenu.setEnabled(false);
    
    dialogMenu = new Menu("Dialog");
    // dialogMenu.setShortcut(new MenuShortcut(KeyEvent.VK_D, true));
    
    clientConnectionDialogMenu = new MenuItem("Connection");
    loadFileDialogMenu = new MenuItem("Load File");
    saveFileDialogMenu = new MenuItem("Save File");
    
    clientConnectionDialogMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        if (!connectionDialog.isVisible())
        {
          connectionDialog.open();
        }
      }
    });
    
    loadFileDialogMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        loadFileDialog.setVisible(true);
        if (loadFileDialog.getFile() != null)
        {
          VTConsole.input(loadFileDialog.getDirectory() + loadFileDialog.getFile());
        }
      }
    });
    
    saveFileDialogMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        saveFileDialog.setVisible(true);
        if (saveFileDialog.getFile() != null)
        {
          VTConsole.input(saveFileDialog.getDirectory() + saveFileDialog.getFile());
        }
      }
    });
    
    dialogMenu.add(clientConnectionDialogMenu);
    // dialogMenu.add(loadFileDialogMenu);
    // dialogMenu.add(saveFileDialogMenu);
    loadFileDialogMenu.setEnabled(false);
    saveFileDialogMenu.setEnabled(false);
    
    this.add(dialogMenu);
    dialogMenu.setEnabled(true);
    
    super.addBaseMenus();
    
    // Menu helpMenu = new Menu("Resume/Insert");
    // helpMenu.setEnabled(false);
    // this.setHelpMenu(helpMenu);
    // VTGlobalFontManager.registerMenu(this);
  }
  
  public void setEnabled(boolean enabled)
  {
    clientConsoleCommandsMenu.setEnabled(enabled);
    // dialogMenu.setEnabled(enabled);
    loadFileDialogMenu.setEnabled(enabled);
    saveFileDialogMenu.setEnabled(enabled);
    /*
     * for (int i = 0;i < this.getMenuCount();i++) {
     * this.getMenu(i).setEnabled(enabled); }
     */
  }
  
  public void setEnabledDialogMenu(boolean enabled)
  {
    dialogMenu.setEnabled(enabled);
  }
}