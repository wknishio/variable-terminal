package org.vate.client.console.remote;

import java.awt.FileDialog;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.VT;
import org.vate.client.dialog.VTClientConfigurationDialog;
import org.vate.console.VTConsole;
import org.vate.console.graphical.menu.VTGraphicalConsoleInputMenuItem;
import org.vate.dialog.VTFileDialog;
import org.vate.graphics.font.VTGlobalTextStyleManager;

public class VTClientRemoteGraphicalConsoleInputMenuBar extends MenuBar
{
	private static final long serialVersionUID = 1L;
	
	private Menu textMenu;
	private MenuItem increaseTextMenu;
	private MenuItem decreaseTextMenu;
	private MenuItem defaultTextMenu;
	private MenuItem toggleBoldTextMenu;
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
	private Menu serverSOCKSProxyTunnelsMenu;
	private Menu serverPrintMenu;
	private Menu audioSoundMenu;
	private Menu beepSoundMenu;
	private Menu opticalDriveMenu;
	//private Menu settingsMenu;
	private Menu serverSettingsMenu;
	private Menu serverAuthenticationSettingsMenu;
	private Menu serverConnectionSettingsMenu;
	private Menu serverProxySettingsMenu;
	private Menu serverEncryptionSettingsMenu;
	private Menu serverSessionsSettingsMenu;
	private Menu audioMixersSoundMenu;
	// private Frame frame;
	// private VTConnectionDialog dialog;
	private Menu audioLinkSoundMenu;
	private Menu helpMenu;
	private Menu keyboardShortcutsMenu;
	private Menu textActionsMenu;
	private MenuItem textActionCopyMenu;
	private MenuItem textActionPasteMenu;
	private MenuItem textActionInsertMenu;
	private MenuItem textActionBreakMenu;
	
	public VTClientRemoteGraphicalConsoleInputMenuBar(final VTClientConfigurationDialog connectionDialog)
	{
		// this.dialog = dialog;
		// this.frame = frame;
		final VTFileDialog loadFileDialog = new VTFileDialog(VTConsole.getFrame(), "Variable-Terminal Client " + VT.VT_VERSION + " - Load File", FileDialog.LOAD);
		final VTFileDialog saveFileDialog = new VTFileDialog(VTConsole.getFrame(), "Variable-Terminal Client " + VT.VT_VERSION + " - Save File", FileDialog.SAVE);
		clientConsoleCommandsMenu = new Menu("Commands");
		//clientConsoleCommandsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_C, true));
		
		serverGraphicalSystemsMenu = new Menu("Graphics ");
		serverGraphicalSystemsMenu.add(new VTGraphicalConsoleInputMenuItem("List Remote Display Devices", "*VTDISPLAYDEVICES\n"));
		serverGraphicsModeMenu = new Menu("Remote Graphics Link ");
		serverScreenCaptureMenu = new Menu("Remote Screen Capture ");
		serverGraphicsAlertMenu = new Menu("Remote Screen Alert ");
		serverBrowseMenu = new Menu("Remote Default Browser ");
		serverPrintApplicationMenu = new Menu("Remote Default Printing ");
		serverStandarScreenCaptureMenu = new Menu("Show Pointer ");
		serverCleanScreenCaptureMenu = new Menu("Hide Pointer ");
		serverGraphicalSystemsMenu.add(serverScreenCaptureMenu);
		serverGraphicalSystemsMenu.add(serverGraphicsModeMenu);
		serverGraphicalSystemsMenu.add(serverGraphicsAlertMenu);
		serverGraphicalSystemsMenu.add(serverBrowseMenu);
		serverGraphicalSystemsMenu.add(serverPrintApplicationMenu);
		serverScreenCaptureMenu.add(serverStandarScreenCaptureMenu);
		serverScreenCaptureMenu.add(serverCleanScreenCaptureMenu);
		serverGraphicsModeMenu.add(new VTGraphicalConsoleInputMenuItem("Toggle Remote Graphics Link", "*VTGRAPHICSLINK\n"));
		serverGraphicsModeMenu.add(new VTGraphicalConsoleInputMenuItem("View Remote Graphics Link", "*VTGRAPHICSLINK V\n"));
		serverGraphicsModeMenu.add(new VTGraphicalConsoleInputMenuItem("Control Remote Graphics Link", "*VTGRAPHICSLINK C\n"));
		serverGraphicsModeMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTGRAPHICSLINK\n"));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Worst Color Quality", "*VTSCREENSHOT SW "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Low Color Quality", "*VTSCREENSHOT SL "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Medium Color Quality", "*VTSCREENSHOT SM "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("High Color Quality", "*VTSCREENSHOT SH "));
		serverStandarScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Best Color Quality", "*VTSCREENSHOT SB "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Worst Color Quality", "*VTSCREENSHOT CW "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Low Color Quality", "*VTSCREENSHOT CL "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Medium Color Quality", "*VTSCREENSHOT CM "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("High Color Quality", "*VTSCREENSHOT CH "));
		serverCleanScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Best Color Quality", "*VTSCREENSHOT CB "));
		serverScreenCaptureMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTSCREENSHOT\n"));
		serverGraphicsAlertMenu.add(new VTGraphicalConsoleInputMenuItem("Show Alert Message", "*VTSCREENALERT "));
		serverGraphicsAlertMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTSCREENALERT\n"));
		serverBrowseMenu.add(new VTGraphicalConsoleInputMenuItem("Browse URI", "*VTBROWSE "));
		serverBrowseMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTBROWSE\n"));
		serverPrintApplicationMenu.add(new VTGraphicalConsoleInputMenuItem("Print File", "*VTPRINTRUN "));
		serverPrintApplicationMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTPRINTRUN\n"));
		
		fileSystemMenu = new Menu("Files ");
		fileTransferMenu = new Menu("File Transfer Function ");
		clientFileSystemMenu = new Menu("Local File System ");
		serverFileSystemMenu = new Menu("Remote File System ");
		fileSystemMenu.add(fileTransferMenu);
		fileSystemMenu.add(clientFileSystemMenu);
		fileSystemMenu.add(serverFileSystemMenu);
		fileTransferMenu.add(new VTGraphicalConsoleInputMenuItem("Send File To Server", "*VTFILETRANSFER P"));
		fileTransferMenu.add(new VTGraphicalConsoleInputMenuItem("Receive File From Server", "*VTFILETRANSFER G"));
		fileTransferMenu.add(new VTGraphicalConsoleInputMenuItem("File Transfer Status", "*VTFILETRANSFER\n"));
		fileTransferMenu.add(new VTGraphicalConsoleInputMenuItem("Stop File Transfer", "*VTFILETRANSFER S\n"));
		fileTransferMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTFILETRANSFER\n"));
		// clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
		// Working
		// Directory", "*VTWORKDIRECTORY L "));
		clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Compress Zip File", "*VTZIP L C "));
		clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Uncompress Zip File", "*VTZIP L U "));
		clientFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Decompress Zip File", "*VTZIP L D "));
		// serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Set
		// Working
		// Directory", "*VTWORKDIRECTORY R "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("List File System Roots", "*VTFILEINSPECT L\n"));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Show File Information", "*VTFILEINSPECT I "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("List Directory Contents", "*VTFILEINSPECT L "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Stop File Inspection", "*VTFILEINSPECT S\n"));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Create Empty Directory", "*VTFILEMODIFY D "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Create Empty File", "*VTFILEMODIFY F "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Remove File", "*VTFILEMODIFY R "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Move File", "*VTFILEMODIFY M "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Copy File", "*VTFILEMODIFY C "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Stop File Modification", "*VTFILEMODIFY S\n"));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Compress Zip File", "*VTZIP R C "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Uncompress Zip File", "*VTZIP R U "));
		serverFileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("Decompress Zip File", "*VTZIP R D "));
		
		sessionMenu = new Menu("Session ");
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("List Connected Clients", "*VTSESSIONS\n"));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Send Message To Server", "*VTMESSAGE "));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Disconnect From Server", "*VTDISCONNECT\n"));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Close Client Application", "*VTQUIT\n"));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Close Server Application", "*VTSTOP\n"));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Save Client Connection Settings", "*VTSAVE "));
		
		consoleMenu = new Menu("Console ");
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Show Remote System Time", "*VTTIME\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Clear Remote Console", "*VTCLEAR\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Toggle Remote Console Echo", "*VTECHO\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Toggle Remote Console Cover", "*VTCOVER\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Detect Chained Instances", "*VTCHAINS\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Open Remote Shell", "*VTSHELL O\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Close Remote Shell", "*VTSHELL C\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Define Remote Shell", "*VTSHELL D "));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Change Remote Shell Encoding", "*VTSHELL E "));
		
		//settingsMenu = new Menu("Settings ");
		serverSettingsMenu = new Menu("Access ");
		serverConnectionSettingsMenu = new Menu("Connection ");
		serverAuthenticationSettingsMenu = new Menu("Authentication ");
		serverProxySettingsMenu = new Menu("Proxy ");
		serverEncryptionSettingsMenu = new Menu("Encryption ");
		serverSessionsSettingsMenu = new Menu("Sessions ");
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("List All Settings", "*VTACCESS\n"));
		serverSettingsMenu.add(serverConnectionSettingsMenu);
		serverSettingsMenu.add(serverProxySettingsMenu);
		serverSettingsMenu.add(serverEncryptionSettingsMenu);
		serverSettingsMenu.add(serverAuthenticationSettingsMenu);
		serverSettingsMenu.add(serverSessionsSettingsMenu);
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Mode", "*VTACCESS CM "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Host", "*VTACCESS CH "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Port", "*VTACCESS CP "));
		serverConnectionSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection NAT Port", "*VTACCESS NP "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Type", "*VTACCESS PT "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Host", "*VTACCESS PH "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Port", "*VTACCESS PP "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Authentication", "*VTACCESS PA "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy User", "*VTACCESS PU "));
		serverProxySettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Password", "*VTACCESS PK "));
		serverEncryptionSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Encryption Type", "*VTACCESS ET "));
		serverEncryptionSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Encryption Password", "*VTACCESS EK "));
		serverAuthenticationSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Single Login", "*VTLOCK "));
		serverSessionsSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Sessions Limit", "*VTACCESS SL "));
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Save Settings File", "*VTACCESS SF "));
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Load Settings File", "*VTACCESS LF "));
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTACCESS\n"));
		//settingsMenu.add(serverSettingsMenu);
		
		serverRuntimeMenu = new Menu("Runtime ");
		serverManageRuntimeMenu = new Menu("Remote Process Control ");
		serverRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Set Remote Environment Variables", "*VTVARIABLE "));
		serverRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Set Remote JVM Properties", "*VTPROPERTY "));
		serverRuntimeMenu.add(serverManageRuntimeMenu);
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Add Managed Process", "*VTRUNTIME M "));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Add Free Process", "*VTRUNTIME F "));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("List All Managed Processes", "*VTRUNTIME LA\n"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Stop All Managed Processes", "*VTRUNTIME SA\n"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Delete All Managed Processes", "*VTRUNTIME DA\n"));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Set Runtime Path", "*VTRUNTIME P "));
		serverManageRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTRUNTIME\n"));
		
		performanceMenu = new Menu("Rates ");
		performanceMenu.add(new VTGraphicalConsoleInputMenuItem("Calculate Connection Latency", "*VTPING\n"));
		performanceMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Rate Limits", "*VTRATELIMIT "));
		
		serverNetworkMenu = new Menu("Networks ");
		serverNetworkMenu.add(new VTGraphicalConsoleInputMenuItem("List Remote Network Interfaces", "*VTNETWORKINTERFACES\n"));
		serverNetworkMenu.add(new VTGraphicalConsoleInputMenuItem("Resolve Remote Network Host", "*VTRESOLVEHOST "));
		
		//serverNetworkMenu.add(serverNetworkTunnelsMenu);
		//serverNetworkMenu.add(serverSocksProxyTunnelsMenu);
		//serverTunnelsMenu = new Menu("Tunnels ");
		serverTCPTunnelsMenu = new Menu("Connection TCP Tunnels ");
		serverSOCKSProxyTunnelsMenu = new Menu("Connection SOCKS Tunnels ");
		
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("List All TCP Tunnels", "*VTTCPTUNNEL\n"));
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Local To Remote", "*VTTCPTUNNEL L "));
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Remote To Local", "*VTTCPTUNNEL R "));
		serverTCPTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTTCPTUNNEL\n"));
		
		serverSOCKSProxyTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("List All SOCKS Tunnels", "*VTSOCKSTUNNEL\n"));
		serverSOCKSProxyTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Local To Remote", "*VTSOCKSTUNNEL L "));
		serverSOCKSProxyTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Remote To Local", "*VTSOCKSTUNNEL R "));
		serverSOCKSProxyTunnelsMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTSOCKSTUNNEL\n"));
		
		serverNetworkMenu.add(serverTCPTunnelsMenu);
		serverNetworkMenu.add(serverSOCKSProxyTunnelsMenu);
		
		serverPrintMenu = new Menu("Printing ");
		serverPrintMenu.add(new VTGraphicalConsoleInputMenuItem("List Remote Printers", "*VTPRINTERS\n"));
		serverPrintMenu.add(new VTGraphicalConsoleInputMenuItem("Detail Specific Remote Printer", "*VTPRINTERS "));
		serverPrintMenu.add(new VTGraphicalConsoleInputMenuItem("Print Text In Remote Printer", "*VTPRINTDATA T "));
		serverPrintMenu.add(new VTGraphicalConsoleInputMenuItem("Print File In Remote Printer", "*VTPRINTDATA F "));
		serverPrintMenu.add(new VTGraphicalConsoleInputMenuItem("Commands Usage", "*VTHELP *VTPRINTERS\n*VTHELP *VTPRINTDATA\n"));
		
		audioSoundMenu = new Menu("Audio ");
		
		audioMixersSoundMenu = new Menu("List Available Audio Mixers ");
		audioMixersSoundMenu.add(new VTGraphicalConsoleInputMenuItem("All Audio Mixers", "*VTAUDIOMIXERS\n"));
		audioMixersSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Local Audio Mixers", "*VTAUDIOMIXERS L\n"));
		audioMixersSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Remote Audio Mixers", "*VTAUDIOMIXERS R\n"));
		audioSoundMenu.add(audioMixersSoundMenu);
		
		audioLinkSoundMenu = new Menu("Toggle Remote Audio Link ");
		audioLinkSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Use Default Audio Mixers", "*VTAUDIOLINK\n"));
		audioLinkSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Use Custom Audio Mixers", "*VTAUDIOLINK "));
		audioLinkSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTAUDIOLINK\n"));
		audioSoundMenu.add(audioLinkSoundMenu);
		
		audioSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Ring Remote Terminal Bell", "*VTBELL\n"));
		beepSoundMenu = new Menu("Remote Internal Speakers ");
		beepSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Make Beep", "*VTBEEP "));
		beepSoundMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTBEEP\n"));
		audioSoundMenu.add(beepSoundMenu);
		
		opticalDriveMenu = new Menu("Drives ");
		opticalDriveMenu.add(new VTGraphicalConsoleInputMenuItem("Open Remote Optical Drive", "*VTOPTICALDRIVE O\n"));
		opticalDriveMenu.add(new VTGraphicalConsoleInputMenuItem("Close Remote Optical Drive", "*VTOPTICALDRIVE C\n"));
		
		helpMenu = new Menu("Help ");
		helpMenu.add(new VTGraphicalConsoleInputMenuItem("Complete Commands", "*VTHELP\n"));
		helpMenu.add(new VTGraphicalConsoleInputMenuItem("Abbreviated Commands", "*VTHLP\n"));
		helpMenu.add(new VTGraphicalConsoleInputMenuItem("Specific Command", "*VTHELP "));
		
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
		
		dialogMenu = new Menu("Dialogs");
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
		
		textActionsMenu = new Menu("Texts");
		
		textActionCopyMenu = new MenuItem("Copy ");
		textActionCopyMenu.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				VTConsole.copyText();
			}
		});
		
		textActionPasteMenu = new MenuItem("Paste ");
		textActionPasteMenu.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				VTConsole.pasteText();
			}
		});
		
		textActionInsertMenu = new MenuItem("Insert ");
		textActionInsertMenu.addActionListener(new ActionListener()
		{

			public void actionPerformed(ActionEvent e)
			{
				VTConsole.toggleInputMode();
			}
		});
		
		textActionBreakMenu = new MenuItem("Break ");
		textActionBreakMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTConsole.toggleScrollMode();
			}
		});
		
		textActionsMenu.add(textActionCopyMenu);
		textActionsMenu.add(textActionPasteMenu);
		textActionsMenu.add(textActionInsertMenu);
		textActionsMenu.add(textActionBreakMenu);
		
		this.add(textActionsMenu);
		
		textMenu = new Menu("Fonts");
		//textMenu.setShortcut(new MenuShortcut(KeyEvent.VK_F, true));
		
		increaseTextMenu = new MenuItem("Increase ");
		decreaseTextMenu = new MenuItem("Decrease ");
		defaultTextMenu = new MenuItem("Normalize ");
		toggleBoldTextMenu = new MenuItem("Intensitize ");
		
		increaseTextMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGlobalTextStyleManager.increaseFontSize();
			}
		});
		
		decreaseTextMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGlobalTextStyleManager.decreaseFontSize();
			}
		});
		
		defaultTextMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGlobalTextStyleManager.defaultFontSize();
			}
		});
		
		toggleBoldTextMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (VTGlobalTextStyleManager.isFontStyleBold())
				{
					VTGlobalTextStyleManager.disableFontStyleBold();
				}
				else
				{
					VTGlobalTextStyleManager.enableFontStyleBold();
				}
//				VTGlobalTextStyleManager.packComponents();
			}
		});
		
		textMenu.add(increaseTextMenu);
		textMenu.add(decreaseTextMenu);
		textMenu.add(defaultTextMenu);
		textMenu.add(toggleBoldTextMenu);
		
		this.add(textMenu);
		textMenu.setEnabled(true);
		
		keyboardShortcutsMenu = new Menu("Shortcuts");
		//keyboardShortcutsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_S, true));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+C : Quit Application"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Z : Interrupt Text"));
		keyboardShortcutsMenu.add(new MenuItem("Break/Pause : Interrupt Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Insert : Copy Text"));
		keyboardShortcutsMenu.add(new MenuItem("Shift+Insert : Paste Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Delete : Toggle Insert/Replace"));
		keyboardShortcutsMenu.add(new MenuItem("Insert : Toggle Insert/Replace"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgDown : Decrease Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgUp : Increase Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Home : Normalize Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+End : Intensitize Font"));
		
		this.add(keyboardShortcutsMenu);
		keyboardShortcutsMenu.setEnabled(true);
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