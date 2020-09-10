package org.vate.server.console.local;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;
import org.vate.console.graphical.menu.VTGraphicalConsoleMenuItem;
import org.vate.server.dialog.VTServerSettingsDialog;

public class VTServerLocalGraphicalConsoleMenuBar extends VTGraphicalConsoleMenuBar
{
	private static final long serialVersionUID = 1L;
	
	private Menu dialogMenu;
	private MenuItem serverSettingsDialogMenu;
	private Menu serverConsoleCommandsMenu;
	// private Menu serverBasedCommandsMenu;
	private Menu sessionMenu;
	private Menu performanceMenu;
	private Menu consoleMenu;
	private Menu fileSystemMenu;
	private Menu serverRuntimeMenu;
	private Menu serverGraphicalSystemsMenu;
	private Menu serverAudioSystemsMenu;
	private Menu serverNetworkMenu;
	private Menu serverPrintMenu;
	//private Menu settingsMenu;
	private Menu serverSettingsMenu;
	private Menu serverConnectionMenu;
	//private Menu serverAuthenticationSettingsMenu;
	private Menu serverProxyMenu;
	private Menu serverEncryptionMenu;
	private Menu serverSessionsMenu;
	private Menu helpMenu;
			
	public VTServerLocalGraphicalConsoleMenuBar(final VTServerSettingsDialog connectionDialog)
	{
		removeAllMenus();
		// this.frame = frame;
		serverConsoleCommandsMenu = new Menu("Command");
		// serverConsoleCommandsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_C,
		// true));
		
		sessionMenu = new Menu("Session ");
		sessionMenu.add(new VTGraphicalConsoleMenuItem("List Connected Clients", "*VTSESSIONS\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Send Message To Clients", "*VTMESSAGE "));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Disconnect Client From Server", "*VTDISCONNECT "));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Close Server Application", "*VTSTOP\n"));
		sessionMenu.add(new VTGraphicalConsoleMenuItem("Toggle Server Cover", "*VTCOVER\n"));
		
		performanceMenu = new Menu("Rating ");
		performanceMenu.add(new VTGraphicalConsoleMenuItem("Calculate Connection Latencies", "*VTPING\n"));
		
		consoleMenu = new Menu("Console ");
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Show Local System Time", "*VTTIME\n"));
		consoleMenu.add(new VTGraphicalConsoleMenuItem("Clear Local Console", "*VTCLEAR\n"));
		
		serverSettingsMenu = new Menu("Access ");
		serverConnectionMenu = new Menu("Connection ");
		//serverAuthenticationSettingsMenu = new Menu("Authentication ");
		serverProxyMenu = new Menu("Proxy ");
		serverEncryptionMenu = new Menu("Encryption ");
		serverSessionsMenu = new Menu("Session ");
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("List All Settings", "*VTACCESS\n"));
		serverSettingsMenu.add(serverConnectionMenu);
		//serverSettingsMenu.add(serverAuthenticationSettingsMenu);
		serverSettingsMenu.add(serverProxyMenu);
		serverSettingsMenu.add(serverEncryptionMenu);
		serverSettingsMenu.add(serverSessionsMenu);
		serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Mode", "*VTACCESS CM "));
		serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Host", "*VTACCESS CH "));
		serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Port", "*VTACCESS CP "));
		serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection NAT Port", "*VTACCESS NP "));
		serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Login", "*VTLOCK "));
		serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Type", "*VTACCESS PT "));
		serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Host", "*VTACCESS PH "));
		serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Port", "*VTACCESS PP "));
		serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Authentication", "*VTACCESS PA "));
		serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy User", "*VTACCESS PU "));
		serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Password", "*VTACCESS PS "));
		serverEncryptionMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Type", "*VTACCESS ET "));
		serverEncryptionMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Password", "*VTACCESS ES "));
		//serverAuthenticationSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Single Login", "*VTLOCK "));
		serverSessionsMenu.add(new VTGraphicalConsoleMenuItem("Set Sessions Limit", "*VTACCESS SL "));
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Save Settings File", "*VTACCESS SF "));
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Load Settings File", "*VTACCESS LF "));
		serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTACCESS\n"));
		//settingsMenu.add(serverSettingsMenu);
		
		// serverUtilitiesMenu = new Menu("Local System Utilities ");
		serverRuntimeMenu = new Menu("Runtime ");
		serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Local Environment Variables", "*VTVARIABLE "));
		serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Local JVM Properties", "*VTPROPERTY "));
		
		fileSystemMenu = new Menu("File ");
		fileSystemMenu.add(new VTGraphicalConsoleMenuItem("List Local File System Roots", "*VTFILEROOTS\n"));
		
		serverNetworkMenu = new Menu("Network ");
		serverNetworkMenu.add(new VTGraphicalConsoleMenuItem("List Local Network Interfaces", "*VTNETWORKS\n"));
		
		serverGraphicalSystemsMenu = new Menu("Graphical ");
		serverGraphicalSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Local Display Devices", "*VTDISPLAYS\n"));
		
		serverAudioSystemsMenu = new Menu("Audio ");
		serverAudioSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Local Audio Mixers", "*VTAUDIOMIXERS\n"));
		
		serverPrintMenu = new Menu("Printing ");
		serverPrintMenu.add(new VTGraphicalConsoleMenuItem("List Local Printers", "*VTPRINTERS\n"));
		// serverUtilitiesMenu.add(new VTGraphicalConsoleInputMenuItem("Show
		// Default
		// Print Service ", "*VTDEFAULTPRINTSERVICE\n"));
		helpMenu = new Menu("Help ");
		helpMenu.add(new VTGraphicalConsoleMenuItem("Complete Commands", "*VTHELP\n"));
		helpMenu.add(new VTGraphicalConsoleMenuItem("Abbreviated Commands", "*VTHLP\n"));
		helpMenu.add(new VTGraphicalConsoleMenuItem("Specific Command", "*VTHELP "));
		
		serverConsoleCommandsMenu.add(sessionMenu);
		//serverConsoleCommandsMenu.add(settingsMenu);
		serverConsoleCommandsMenu.add(performanceMenu);
		serverConsoleCommandsMenu.add(consoleMenu);
		serverConsoleCommandsMenu.add(serverRuntimeMenu);
		serverConsoleCommandsMenu.add(fileSystemMenu);
		serverConsoleCommandsMenu.add(serverGraphicalSystemsMenu);
		serverConsoleCommandsMenu.add(serverAudioSystemsMenu);
		serverConsoleCommandsMenu.add(serverNetworkMenu);
		serverConsoleCommandsMenu.add(serverPrintMenu);
		serverConsoleCommandsMenu.add(serverSettingsMenu);
		serverConsoleCommandsMenu.add(helpMenu);
		
		this.add(serverConsoleCommandsMenu);
		serverConsoleCommandsMenu.setEnabled(false);
		
		dialogMenu = new Menu("Dialog");
		serverSettingsDialogMenu = new MenuItem("Connection");
		// dialogMenu.setShortcut(new MenuShortcut(KeyEvent.VK_D, true));
		dialogMenu.add(serverSettingsDialogMenu);
		
		serverSettingsDialogMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				connectionDialog.open();
			}
		});
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
		/* for (int i = 0;i < this.getMenuCount();i++) {
		 * this.getMenu(i).setEnabled(enabled); } */
		serverConsoleCommandsMenu.setEnabled(enabled);
	}
	
	public void setEnabledDialogMenu(boolean enabled)
	{
		dialogMenu.setEnabled(enabled);
	}
}