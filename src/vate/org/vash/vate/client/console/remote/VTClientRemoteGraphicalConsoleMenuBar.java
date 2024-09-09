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
  private Menu remoteFileMenu;
  private Menu remoteFileCheckMenu;
  private Menu remoteFileModifyMenu;
  private Menu fileTransferMenu;
  private Menu serverGraphicalSystemsMenu;
  private Menu serverGraphicsModeMenu;
  private Menu serverScreenCaptureMenu;
  private Menu serverPointerScreenCaptureMenu;
  private Menu serverCleanScreenCaptureMenu;
  private Menu serverScreenAlertMenu;
  private Menu serverBrowseMenu;
  private Menu serverPrintApplicationMenu;
  private Menu serverRuntimeMenu;
  private Menu serverManageRuntimeMenu;
  private Menu networkMenu;
  private Menu performanceMenu;
  private Menu networkTunnelsMenu;
  private Menu networkInterfacesMenu;
  // private Menu serverSOCKSTunnelsMenu;
  private Menu serverPrintMenu;
  private Menu audioSoundMenu;
  private Menu beepSoundMenu;
  private Menu opticalDriveMenu;
  // private Menu settingsMenu;
  private Menu serverSettingsMenu;
  // private Menu serverAuthenticationSettingsMenu;
  private Menu serverConnectionSettingsMenu;
  private Menu serverProxySettingsMenu;
  private Menu serverEncryptionSettingsMenu;
  private Menu serverSessionsSettingsMenu;
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
    
    serverGraphicalSystemsMenu = new Menu("Graphical ");
    serverGraphicalSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Remote Display Devices", "*VTDISPLAY\n"));
    serverGraphicsModeMenu = new Menu("Remote Graphics Link ");
    serverScreenCaptureMenu = new Menu("Remote Screen Capture ");
    serverScreenAlertMenu = new Menu("Remote Screen Alert ");
    serverBrowseMenu = new Menu("Remote Browser Program ");
    serverPrintApplicationMenu = new Menu("Remote Mail Program ");
    serverPointerScreenCaptureMenu = new Menu("Show Cursor ");
    serverCleanScreenCaptureMenu = new Menu("Hide Cursor ");
    serverGraphicalSystemsMenu.add(serverScreenCaptureMenu);
    serverGraphicalSystemsMenu.add(serverScreenAlertMenu);
    serverGraphicalSystemsMenu.add(serverGraphicsModeMenu);
    serverGraphicalSystemsMenu.add(serverBrowseMenu);
    serverGraphicalSystemsMenu.add(serverPrintApplicationMenu);
    serverScreenCaptureMenu.add(serverCleanScreenCaptureMenu);
    serverScreenCaptureMenu.add(serverPointerScreenCaptureMenu);
    serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Toggle Graphics Link", "*VTGRAPHICSLINK\n"));
    serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("View Graphics Link", "*VTGRAPHICSLINK V\n"));
    serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Control Graphics Link", "*VTGRAPHICSLINK C\n"));
    serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTGRAPHICSLINK\n"));
    
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT PW "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT PL "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Simple Color Quality", "*VTSCREENSHOT PS "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Decreased Color Quality", "*VTSCREENSHOT PD "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT PM "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Increased Color Quality", "*VTSCREENSHOT PI "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Extra Color Quality", "*VTSCREENSHOT PE "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT PH "));
//		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Best Color Quality", "*VTSCREENSHOT PB "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT CW "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT CL "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Simple Color Quality", "*VTSCREENSHOT CS "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Decreased Color Quality", "*VTSCREENSHOT CD "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT CM "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Increased Color Quality", "*VTSCREENSHOT CI "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Extra Color Quality", "*VTSCREENSHOT CE "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT CH "));
//		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Best Color Quality", "*VTSCREENSHOT CB "));
    
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("True Color Quality", "*VTSCREENSHOT CT "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Ultra Color Quality", "*VTSCREENSHOT CU "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Vast Color Quality", "*VTSCREENSHOT CV "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT CH "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Extra Color Quality", "*VTSCREENSHOT CE "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Next Color Quality", "*VTSCREENSHOT CN "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT CM "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Simple Color Quality", "*VTSCREENSHOT CS "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Few Color Quality", "*VTSCREENSHOT CF "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT CL "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Gray Color Quality", "*VTSCREENSHOT CG "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Dull Color Quality", "*VTSCREENSHOT CD "));
    serverPointerScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT CW "));
    
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("True Color Quality", "*VTSCREENSHOT OT "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Ultra Color Quality", "*VTSCREENSHOT OU "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Vast Color Quality", "*VTSCREENSHOT OV "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT OH "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Extra Color Quality", "*VTSCREENSHOT OE "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Next Color Quality", "*VTSCREENSHOT ON "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT OM "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Simple Color Quality", "*VTSCREENSHOT OS "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Few Color Quality", "*VTSCREENSHOT OF "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT OL "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Gray Color Quality", "*VTSCREENSHOT OG "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Dull Color Quality", "*VTSCREENSHOT OD "));
    serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT OW "));
    
    serverScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSCREENSHOT\n"));
    serverScreenAlertMenu.add(new VTGraphicalConsoleMenuItem("Show Alert", "*VTSCREENALERT "));
    serverScreenAlertMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSCREENALERT\n"));
    serverBrowseMenu.add(new VTGraphicalConsoleMenuItem("Browse URI", "*VTBROWSE "));
    serverBrowseMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTBROWSE\n"));
    serverPrintApplicationMenu.add(new VTGraphicalConsoleMenuItem("Compose Email", "*VTMAIL "));
    serverPrintApplicationMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTMAIL\n"));
    
    fileSystemMenu = new Menu("File ");
    fileTransferMenu = new Menu("File Transfer ");
    remoteFileMenu = new Menu("Remote Files ");
    remoteFileCheckMenu = new Menu("Check Files ");
    remoteFileModifyMenu = new Menu("Alter Files ");
    // zipFileMenu = new Menu("Zip Files ");
    // zipFileLocalMenu = new Menu("Local Zip Files ");
    // zipFileRemoteMenu = new Menu("Remote Zip Files ");
    
    remoteFileMenu.add(remoteFileCheckMenu);
    remoteFileMenu.add(remoteFileModifyMenu);
    
    // zipFileMenu.add(zipFileLocalMenu);
    // zipFileMenu.add(zipFileRemoteMenu);
    // zipFileMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP
    // *VTZIP\n"));
    
    fileSystemMenu.add(fileTransferMenu);
    fileSystemMenu.add(remoteFileMenu);
    // fileSystemMenu.add(zipFileMenu);
    
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Send File To Server", "*VTFILETRANSFER P"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Receive File From Server", "*VTFILETRANSFER G"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Report File Transfer", "*VTFILETRANSFER\n"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Stop File Transfer", "*VTFILETRANSFER S\n"));
    fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILETRANSFER\n"));
    // clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
    // Working
    // Directory", "*VTWORKDIRECTORY L "));
    // serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
    // Working
    // Directory", "*VTWORKDIRECTORY R "));
    remoteFileCheckMenu.add(new VTGraphicalConsoleMenuItem("List File System Roots", "*VTFILESEEK L\n"));
    remoteFileCheckMenu.add(new VTGraphicalConsoleMenuItem("Show File Information", "*VTFILESEEK I "));
    remoteFileCheckMenu.add(new VTGraphicalConsoleMenuItem("List Directory Contents", "*VTFILESEEK L "));
    remoteFileCheckMenu.add(new VTGraphicalConsoleMenuItem("Report File Seek", "*VTFILESEEK\n"));
    remoteFileCheckMenu.add(new VTGraphicalConsoleMenuItem("Stop File Seek", "*VTFILESEEK S\n"));
    remoteFileCheckMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILESEEK\n"));
    
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Create Empty Directory", "*VTFILEALTER D "));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Create Empty File", "*VTFILEALTER F "));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Remove File", "*VTFILEALTER R "));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Move File", "*VTFILEALTER M "));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Copy File", "*VTFILEALTER C "));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Report File Change", "*VTFILEALTER\n"));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Stop File Change", "*VTFILEALTER S\n"));
    remoteFileModifyMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILEALTER\n"));
    
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
    serverSettingsMenu = new Menu("Setting ");
    serverConnectionSettingsMenu = new Menu("Connection ");
    // serverAuthenticationSettingsMenu = new Menu("Authentication ");
    serverProxySettingsMenu = new Menu("Proxy ");
    serverEncryptionSettingsMenu = new Menu("Encryption ");
    serverSessionsSettingsMenu = new Menu("Session ");
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("List Server Settings", "*VTSETTING\n"));
    serverSettingsMenu.add(serverConnectionSettingsMenu);
    // serverSettingsMenu.add(serverAuthenticationSettingsMenu);
    serverSettingsMenu.add(serverProxySettingsMenu);
    serverSettingsMenu.add(serverEncryptionSettingsMenu);
    serverSettingsMenu.add(serverSessionsSettingsMenu);
    serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Mode", "*VTSETTING CM "));
    serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Host", "*VTSETTING CH "));
    serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Port", "*VTSETTING CP "));
    serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection NAT Port", "*VTSETTING CN "));
    serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Type", "*VTSETTING PT "));
    serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Host", "*VTSETTING PH "));
    serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Port", "*VTSETTING PP "));
    serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Authentication", "*VTSETTING PA "));
    serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy User", "*VTSETTING PU "));
    serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Password", "*VTSETTING PK "));
    serverEncryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Type", "*VTSETTING ET "));
    serverEncryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Password", "*VTSETTING EK "));
    serverSessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Maximum", "*VTSETTING SM "));
    serverSessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Shell", "*VTSETTING SS "));
    serverSessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Accounts", "*VTSETTING SA "));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Save Settings File", "*VTSETTING SF "));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Load Settings File", "*VTSETTING LF "));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSETTING\n"));
    // settingsMenu.add(serverSettingsMenu);
    
    serverRuntimeMenu = new Menu("Runtime ");
    serverManageRuntimeMenu = new Menu("Remote Process Control ");
    serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Environment Variables", "*VTVARIABLE "));
    serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Java Properties", "*VTPROPERTY "));
    serverRuntimeMenu.add(serverManageRuntimeMenu);
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Add Managed Process", "*VTRUNTIME M"));
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Add Free Process", "*VTRUNTIME F"));
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("List Managed Processes", "*VTRUNTIME LA\n"));
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Stop Managed Processes", "*VTRUNTIME SA\n"));
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Delete Managed Processes", "*VTRUNTIME DA\n"));
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Runtime Path", "*VTRUNTIME P "));
    serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTRUNTIME\n"));
    
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
    // serverSOCKSTunnelsMenu = new Menu("Connection SOCKS Tunnels ");
    
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("List Any Tunnels", "*VTTUNNEL\n"));
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Local To Remote", "*VTTUNNEL L "));
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Remote To Local", "*VTTUNNEL R "));
    networkTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTTUNNEL\n"));
    
    networkMenu.add(networkInterfacesMenu);
    networkMenu.add(new VTGraphicalConsoleMenuItem("Resolve Remote Network Host", "*VTHOST "));
    networkMenu.add(networkTunnelsMenu);
    
    serverPrintMenu = new Menu("Print ");
    serverPrintMenu.add(new VTGraphicalConsoleMenuItem("List Remote Printers", "*VTPRINTER\n"));
    serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Detail Remote Printer", "*VTPRINTER "));
    serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Print Text In Remote Printer", "*VTPRINTDATA T "));
    serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Print File In Remote Printer", "*VTPRINTDATA F "));
    serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Commands Usage", "*VTHELP *VTPRINTER\n*VTHELP *VTPRINTDATA\n"));
    
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
    clientConsoleCommandsMenu.add(serverRuntimeMenu);
    clientConsoleCommandsMenu.add(fileSystemMenu);
    clientConsoleCommandsMenu.add(serverGraphicalSystemsMenu);
    clientConsoleCommandsMenu.add(audioSoundMenu);
    clientConsoleCommandsMenu.add(networkMenu);
    // clientConsoleCommandsMenu.add(serverTunnelsMenu);
    clientConsoleCommandsMenu.add(serverPrintMenu);
    clientConsoleCommandsMenu.add(opticalDriveMenu);
    clientConsoleCommandsMenu.add(serverSettingsMenu);
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