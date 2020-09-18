package org.vate.client.console.remote;

import java.awt.FileDialog;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.VT;
import org.vate.client.dialog.VTClientConfigurationDialog;
import org.vate.console.VTConsole;
import org.vate.console.graphical.menu.VTGraphicalConsoleMenuItem;
import org.vate.dialog.VTFileDialog;
import org.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;

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
	private Menu fileSystemMenu;
	private Menu clientFileSystemMenu;
	private Menu serverFileSystemMenu;
	private Menu fileTransferMenu;
	private Menu serverGraphicalSystemsMenu;
	private Menu serverGraphicsModeMenu;
	private Menu serverScreenCaptureMenu;
	private Menu serverStandarScreenCaptureMenu;
	private Menu serverCleanScreenCaptureMenu;
	private Menu serverGraphicsAlertMenu;
	private Menu serverBrowseMenu;
	private Menu serverPrintApplicationMenu;
	private Menu serverRuntimeMenu;
	private Menu serverManageRuntimeMenu;
	private Menu serverNetworkMenu;
	private Menu performanceMenu;
	private Menu serverTCPTunnelsMenu;
	private Menu serverSOCKSTunnelsMenu;
	private Menu serverPrintMenu;
	private Menu audioSoundMenu;
	private Menu beepSoundMenu;
	private Menu opticalDriveMenu;
	//private Menu settingsMenu;
	private Menu serverSettingsMenu;
	//private Menu serverAuthenticationSettingsMenu;
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
		final VTFileDialog loadFileDialog = new VTFileDialog(VTConsole.getFrame(), "Variable-Terminal Client " + VT.VT_VERSION + " - Load File", FileDialog.LOAD);
		final VTFileDialog saveFileDialog = new VTFileDialog(VTConsole.getFrame(), "Variable-Terminal Client " + VT.VT_VERSION + " - Save File", FileDialog.SAVE);
		clientConsoleCommandsMenu = new Menu("Command");
		//clientConsoleCommandsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_C, true));
		
		serverGraphicalSystemsMenu = new Menu("Graphical ");
		serverGraphicalSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Remote Display Devices", "*VTDISPLAYS\n"));
		serverGraphicsModeMenu = new Menu("Remote Graphics Link ");
		serverScreenCaptureMenu = new Menu("Remote Screen Capture ");
		serverGraphicsAlertMenu = new Menu("Remote Screen Alert ");
		serverBrowseMenu = new Menu("Remote Browser Application ");
		serverPrintApplicationMenu = new Menu("Remote Print Application ");
		serverStandarScreenCaptureMenu = new Menu("Show Pointer ");
		serverCleanScreenCaptureMenu = new Menu("Hide Pointer ");
		serverGraphicalSystemsMenu.add(serverScreenCaptureMenu);
		serverGraphicalSystemsMenu.add(serverGraphicsModeMenu);
		serverGraphicalSystemsMenu.add(serverGraphicsAlertMenu);
		serverGraphicalSystemsMenu.add(serverBrowseMenu);
		serverGraphicalSystemsMenu.add(serverPrintApplicationMenu);
		serverScreenCaptureMenu.add(serverStandarScreenCaptureMenu);
		serverScreenCaptureMenu.add(serverCleanScreenCaptureMenu);
		serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Toggle Remote Graphics Link", "*VTGRAPHICSLINK\n"));
		serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("View Remote Graphics Link", "*VTGRAPHICSLINK V\n"));
		serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Control Remote Graphics Link", "*VTGRAPHICSLINK C\n"));
		serverGraphicsModeMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTGRAPHICSLINK\n"));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT PW "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT PL "));
		//serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Simple Color Quality", "*VTSCREENSHOT PS "));
		//serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Normal Color Quality", "*VTSCREENSHOT PN "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT PM "));
		//serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Good Color Quality", "*VTSCREENSHOT PG "));
		//serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Extra Color Quality", "*VTSCREENSHOT PE "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT PH "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Best Color Quality", "*VTSCREENSHOT PB "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Worst Color Quality", "*VTSCREENSHOT CW "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Low Color Quality", "*VTSCREENSHOT CL "));
		//serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Simple Color Quality", "*VTSCREENSHOT CS "));
		//serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Normal Color Quality", "*VTSCREENSHOT CN "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Medium Color Quality", "*VTSCREENSHOT CM "));
		//serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Good Color Quality", "*VTSCREENSHOT CG "));
		//serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Extra Color Quality", "*VTSCREENSHOT CE "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("High Color Quality", "*VTSCREENSHOT CH "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Best Color Quality", "*VTSCREENSHOT CB "));
		serverScreenCaptureMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSCREENSHOT\n"));
		serverGraphicsAlertMenu.add(new VTGraphicalConsoleMenuItem("Show Alert Message", "*VTSCREENALERT "));
		serverGraphicsAlertMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSCREENALERT\n"));
		serverBrowseMenu.add(new VTGraphicalConsoleMenuItem("Browse URI", "*VTBROWSE "));
		serverBrowseMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTBROWSE\n"));
		serverPrintApplicationMenu.add(new VTGraphicalConsoleMenuItem("Print File", "*VTRUNPRINT "));
		serverPrintApplicationMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTRUNPRINT\n"));
		
		fileSystemMenu = new Menu("File ");
		fileTransferMenu = new Menu("File Transfer ");
		clientFileSystemMenu = new Menu("Local Files ");
		serverFileSystemMenu = new Menu("Remote Files ");
		fileSystemMenu.add(fileTransferMenu);
		fileSystemMenu.add(clientFileSystemMenu);
		fileSystemMenu.add(serverFileSystemMenu);
		fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Send File To Server", "*VTFILETRANSFER P"));
		fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Receive File From Server", "*VTFILETRANSFER G"));
		fileTransferMenu.add(new VTGraphicalConsoleMenuItem("File Transfer Status", "*VTFILETRANSFER\n"));
		fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Stop File Transfer", "*VTFILETRANSFER S\n"));
		fileTransferMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTFILETRANSFER\n"));
		// clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
		// Working
		// Directory", "*VTWORKDIRECTORY L "));
		clientFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Compress Zip File", "*VTZIP L C "));
		clientFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Uncompress Zip File", "*VTZIP L U "));
		clientFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Decompress Zip File", "*VTZIP L D "));
		clientFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Stop Zip Operation", "*VTZIP L S\n"));
		// serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
		// Working
		// Directory", "*VTWORKDIRECTORY R "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("List File System Roots", "*VTFILEINSPECT L\n"));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Show File Information", "*VTFILEINSPECT I "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("List Directory Contents", "*VTFILEINSPECT L "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Stop File Inspection", "*VTFILEINSPECT S\n"));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Create Empty Directory", "*VTFILEMODIFY D "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Create Empty File", "*VTFILEMODIFY F "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Remove File", "*VTFILEMODIFY R "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Move File", "*VTFILEMODIFY M "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Copy File", "*VTFILEMODIFY C "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Stop File Modification", "*VTFILEMODIFY S\n"));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Compress Zip File", "*VTZIP R C "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Uncompress Zip File", "*VTZIP R U "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Decompress Zip File", "*VTZIP R D "));
		serverFileSystemMenu.add(new VTGraphicalConsoleMenuItem("Stop Zip Operation", "*VTZIP R S\n"));
		
		sessionMenu = new Menu("Session ");
		sessionMenu.add(new VTGraphicalConsoleMenuItem("List Connected Clients", "*VTSESSIONS\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Send Message To Server", "*VTMESSAGE "));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Disconnect From Server", "*VTDISCONNECT\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Close Client Application", "*VTQUIT\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Close Server Application", "*VTSTOP\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Toggle Server Cover", "*VTCOVER\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Save Client Connection Settings", "*VTSAVE "));
		
		consoleMenu = new Menu("Console ");
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Show Remote System Time", "*VTTIME\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Clear Remote Console", "*VTCLEAR\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Toggle Remote Console Echo", "*VTECHO\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Detect Chained Instances", "*VTCHAINS\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Open Remote Shell", "*VTSHELL O\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Close Remote Shell", "*VTSHELL C\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Define Remote Shell", "*VTSHELL D "));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Change Remote Shell Encoding", "*VTSHELL E "));
		
		//settingsMenu = new Menu("Settings ");
		serverSettingsMenu = new Menu("Access ");
		serverConnectionSettingsMenu = new Menu("Connection ");
		//serverAuthenticationSettingsMenu = new Menu("Authentication ");
		serverProxySettingsMenu = new Menu("Proxy ");
		serverEncryptionSettingsMenu = new Menu("Encryption ");
		serverSessionsSettingsMenu = new Menu("Session ");
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("List All Settings", "*VTACCESS\n"));
		serverSettingsMenu.add(serverConnectionSettingsMenu);
		//serverSettingsMenu.add(serverAuthenticationSettingsMenu);
		serverSettingsMenu.add(serverProxySettingsMenu);
		serverSettingsMenu.add(serverEncryptionSettingsMenu);
		serverSettingsMenu.add(serverSessionsSettingsMenu);
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Mode", "*VTACCESS CM "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Host", "*VTACCESS CH "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Port", "*VTACCESS CP "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection NAT Port", "*VTACCESS NP "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Login", "*VTLOCK "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Type", "*VTACCESS PT "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Host", "*VTACCESS PH "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Port", "*VTACCESS PP "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Authentication", "*VTACCESS PA "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy User", "*VTACCESS PU "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Password", "*VTACCESS PS "));
		serverEncryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Type", "*VTACCESS ET "));
		serverEncryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Password", "*VTACCESS ES "));
		//serverAuthenticationSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Single Login", "*VTLOCK "));
		serverSessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Sessions Limit", "*VTACCESS SL "));
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Save Settings File", "*VTACCESS SF "));
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Load Settings File", "*VTACCESS LF "));
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTACCESS\n"));
		//settingsMenu.add(serverSettingsMenu);
		
		serverRuntimeMenu = new Menu("Runtime ");
		serverManageRuntimeMenu = new Menu("Remote Process Control ");
		serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Remote Environment Variables", "*VTVARIABLE "));
		serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Remote JVM Properties", "*VTPROPERTY "));
		serverRuntimeMenu.add(serverManageRuntimeMenu);
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Add Managed Process", "*VTRUNTIME M"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Add Free Process", "*VTRUNTIME F"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("List All Managed Processes", "*VTRUNTIME LA\n"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Stop All Managed Processes", "*VTRUNTIME SA\n"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Delete All Managed Processes", "*VTRUNTIME DA\n"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Runtime Path", "*VTRUNTIME P "));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTRUNTIME\n"));
		
		performanceMenu = new Menu("Rating ");
		performanceMenu.add(new VTGraphicalConsoleMenuItem("Calculate Connection Latency", "*VTPING\n"));
		performanceMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Rate Limits", "*VTRATELIMIT "));
		
		serverNetworkMenu = new Menu("Network ");
		serverNetworkMenu.add(new VTGraphicalConsoleMenuItem("List Remote Network Interfaces", "*VTNETWORKS\n"));
		serverNetworkMenu.add(new VTGraphicalConsoleMenuItem("Resolve Remote Network Host", "*VTHOSTRESOLVE "));
		
		//serverNetworkMenu.add(serverNetworkTunnelsMenu);
		//serverNetworkMenu.add(serverSocksProxyTunnelsMenu);
		//serverTunnelsMenu = new Menu("Tunnels ");
		serverTCPTunnelsMenu = new Menu("Connection TCP Tunnels ");
		serverSOCKSTunnelsMenu = new Menu("Connection SOCKS Tunnels ");
		
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleMenuItem("List All TCP Tunnels", "*VTTCPTUNNEL\n"));
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Local To Remote", "*VTTCPTUNNEL L "));
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Remote To Local", "*VTTCPTUNNEL R "));
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTTCPTUNNEL\n"));
		
		serverSOCKSTunnelsMenu.add(new VTGraphicalConsoleMenuItem("List All SOCKS Tunnels", "*VTSOCKSTUNNEL\n"));
		serverSOCKSTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Local To Remote", "*VTSOCKSTUNNEL L "));
		serverSOCKSTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Set Remote To Local", "*VTSOCKSTUNNEL R "));
		serverSOCKSTunnelsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSOCKSTUNNEL\n"));
		
		serverNetworkMenu.add(serverTCPTunnelsMenu);
		serverNetworkMenu.add(serverSOCKSTunnelsMenu);
		
		serverPrintMenu = new Menu("Printing ");
		serverPrintMenu.add(new VTGraphicalConsoleMenuItem("List Remote Printers", "*VTPRINTERS\n"));
		serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Detail Specific Remote Printer", "*VTPRINTERS "));
		serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Print Text In Remote Printer", "*VTDATAPRINT T "));
		serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Print File In Remote Printer", "*VTDATAPRINT F "));
		serverPrintMenu.add(new VTGraphicalConsoleMenuItem("Commands Usage", "*VTHELP *VTPRINTERS\n*VTHELP *VTDATAPRINT\n"));
		
		audioSoundMenu = new Menu("Audio ");
		
		audioMixersSoundMenu = new Menu("List Available Audio Mixers ");
		audioMixersSoundMenu.add(new VTGraphicalConsoleMenuItem("All Audio Mixers", "*VTMIXERS\n"));
		audioMixersSoundMenu.add(new VTGraphicalConsoleMenuItem("Local Audio Mixers", "*VTMIXERS L\n"));
		audioMixersSoundMenu.add(new VTGraphicalConsoleMenuItem("Remote Audio Mixers", "*VTMIXERS R\n"));
		audioSoundMenu.add(audioMixersSoundMenu);
		
		audioLinkSoundMenu = new Menu("Toggle Remote Audio Link ");
		audioLinkSoundMenu.add(new VTGraphicalConsoleMenuItem("Use Default Audio Mixers", "*VTAUDIOLINK\n"));
		audioLinkSoundMenu.add(new VTGraphicalConsoleMenuItem("Use Custom Audio Mixers", "*VTAUDIOLINK "));
		audioLinkSoundMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTAUDIOLINK\n"));
		audioSoundMenu.add(audioLinkSoundMenu);
		
		audioSoundMenu.add(new VTGraphicalConsoleMenuItem("Ring Remote Terminal Bell", "*VTBELL\n"));
		beepSoundMenu = new Menu("Remote Internal Speakers ");
		beepSoundMenu.add(new VTGraphicalConsoleMenuItem("Make Beep", "*VTBEEP "));
		beepSoundMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTBEEP\n"));
		audioSoundMenu.add(beepSoundMenu);
		
		opticalDriveMenu = new Menu("Drive ");
		opticalDriveMenu.add(new VTGraphicalConsoleMenuItem("Open Remote Optical Drive", "*VTOPTICALDRIVE O\n"));
		opticalDriveMenu.add(new VTGraphicalConsoleMenuItem("Close Remote Optical Drive", "*VTOPTICALDRIVE C\n"));
		
		helpMenu = new Menu("Help ");
		helpMenu.add(new VTGraphicalConsoleMenuItem("Complete Commands", "*VTHELP\n"));
		helpMenu.add(new VTGraphicalConsoleMenuItem("Abbreviated Commands", "*VTHLP\n"));
		helpMenu.add(new VTGraphicalConsoleMenuItem("Specific Command", "*VTHELP "));
		
		clientConsoleCommandsMenu.add(sessionMenu);
		//clientConsoleCommandsMenu.add(settingsMenu);
		clientConsoleCommandsMenu.add(performanceMenu);
		clientConsoleCommandsMenu.add(consoleMenu);
		clientConsoleCommandsMenu.add(serverRuntimeMenu);
		clientConsoleCommandsMenu.add(fileSystemMenu);
		clientConsoleCommandsMenu.add(serverGraphicalSystemsMenu);
		clientConsoleCommandsMenu.add(audioSoundMenu);
		clientConsoleCommandsMenu.add(serverNetworkMenu);
		//clientConsoleCommandsMenu.add(serverTunnelsMenu);
		clientConsoleCommandsMenu.add(serverPrintMenu);
		clientConsoleCommandsMenu.add(opticalDriveMenu);
		clientConsoleCommandsMenu.add(serverSettingsMenu);
		clientConsoleCommandsMenu.add(helpMenu);
		
		this.add(clientConsoleCommandsMenu);
		clientConsoleCommandsMenu.setEnabled(false);
		
		dialogMenu = new Menu("Dialog");
		//dialogMenu.setShortcut(new MenuShortcut(KeyEvent.VK_D, true));
		
		clientConnectionDialogMenu = new MenuItem("Connection");
		loadFileDialogMenu = new MenuItem("Input File");
		saveFileDialogMenu = new MenuItem("Output File");
		
		clientConnectionDialogMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				connectionDialog.open();
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

		//Menu helpMenu = new Menu("Resume/Insert");
		//helpMenu.setEnabled(false);
		//this.setHelpMenu(helpMenu);
		// VTGlobalFontManager.registerMenu(this);
	}
	
	public void setEnabled(boolean enabled)
	{
		clientConsoleCommandsMenu.setEnabled(enabled);
		loadFileDialogMenu.setEnabled(enabled);
		saveFileDialogMenu.setEnabled(enabled);
		/* for (int i = 0;i < this.getMenuCount();i++) {
		 * this.getMenu(i).setEnabled(enabled); } */
	}
	
	public void setEnabledDialogMenu(boolean enabled)
	{
		dialogMenu.setEnabled(enabled);
	}
}