package org.vate.server.console.local;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vate.console.VTConsole;
import org.vate.console.graphical.menu.VTGraphicalConsoleInputMenuItem;
import org.vate.graphics.font.VTGlobalTextStyleManager;
import org.vate.server.dialog.VTServerSettingsDialog;

public class VTServerLocalGraphicalConsoleInputMenuBar extends MenuBar
{
	private static final long serialVersionUID = 1L;
	
	private Menu textMenu;
	private MenuItem increaseTextMenu;
	private MenuItem decreaseTextMenu;
	private MenuItem defaultTextMenu;
	private MenuItem toggleBoldTextMenu;
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
	private Menu serverAuthenticationSettingsMenu;
	private Menu serverProxyMenu;
	private Menu serverEncryptionMenu;
	private Menu serverSessionsMenu;
	private Menu helpMenu;
	// private Frame frame;
	private Menu keyboardShortcutsMenu;
	private Menu textActionsMenu;
	private MenuItem textActionCopyMenu;
	private MenuItem textActionPasteMenu;
	private MenuItem textActionAllMenu;
	private MenuItem textActionInsertMenu;
	private MenuItem textActionBreakMenu;
	
	public VTServerLocalGraphicalConsoleInputMenuBar(final VTServerSettingsDialog connectionDialog)
	{
		// this.frame = frame;
		serverConsoleCommandsMenu = new Menu("Commands");
		// serverConsoleCommandsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_C,
		// true));
		
		sessionMenu = new Menu("Session ");
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("List Connected Clients", "*VTSESSIONS\n"));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Send Message To Clients", "*VTMESSAGE "));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Disconnect Client From Server", "*VTDISCONNECT "));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Close Server Application", "*VTSTOP\n"));
		sessionMenu.add(new VTGraphicalConsoleInputMenuItem("Toggle Server Cover", "*VTCOVER\n"));
		
		performanceMenu = new Menu("Rates ");
		performanceMenu.add(new VTGraphicalConsoleInputMenuItem("Calculate Connection Latencies", "*VTPING\n"));
		
		consoleMenu = new Menu("Console ");
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Show Local System Time", "*VTTIME\n"));
		consoleMenu.add(new VTGraphicalConsoleInputMenuItem("Clear Local Console", "*VTCLEAR\n"));
		
		serverSettingsMenu = new Menu("Access ");
		serverConnectionMenu = new Menu("Connection ");
		serverAuthenticationSettingsMenu = new Menu("Authentication ");
		serverProxyMenu = new Menu("Proxy ");
		serverEncryptionMenu = new Menu("Encryption ");
		serverSessionsMenu = new Menu("Sessions ");
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("List All Settings", "*VTACCESS\n"));
		serverSettingsMenu.add(serverConnectionMenu);
		serverSettingsMenu.add(serverProxyMenu);
		serverSettingsMenu.add(serverEncryptionMenu);
		serverSettingsMenu.add(serverAuthenticationSettingsMenu);
		serverSettingsMenu.add(serverSessionsMenu);
		serverConnectionMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Mode", "*VTACCESS CM "));
		serverConnectionMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Host", "*VTACCESS CH "));
		serverConnectionMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection Port", "*VTACCESS CP "));
		serverConnectionMenu.add(new VTGraphicalConsoleInputMenuItem("Set Connection NAT Port", "*VTACCESS NP "));
		serverProxyMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Type", "*VTACCESS PT "));
		serverProxyMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Host", "*VTACCESS PH "));
		serverProxyMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Port", "*VTACCESS PP "));
		serverProxyMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Authentication", "*VTACCESS PA "));
		serverProxyMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy User", "*VTACCESS PU "));
		serverProxyMenu.add(new VTGraphicalConsoleInputMenuItem("Set Proxy Password", "*VTACCESS PK "));
		serverEncryptionMenu.add(new VTGraphicalConsoleInputMenuItem("Set Encryption Type", "*VTACCESS ET "));
		serverEncryptionMenu.add(new VTGraphicalConsoleInputMenuItem("Set Encryption Password", "*VTACCESS EK "));
		serverAuthenticationSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Single Login", "*VTLOCK "));
		serverSessionsMenu.add(new VTGraphicalConsoleInputMenuItem("Set Sessions Limit", "*VTACCESS SL "));
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Save Settings File", "*VTACCESS SF "));
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Load Settings File", "*VTACCESS LF "));
		serverSettingsMenu.add(new VTGraphicalConsoleInputMenuItem("Command Usage", "*VTHELP *VTACCESS\n"));
		//settingsMenu.add(serverSettingsMenu);
		
		// serverUtilitiesMenu = new Menu("Local System Utilities ");
		serverRuntimeMenu = new Menu("Runtime ");
		serverRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Set Local Environment Variables", "*VTVARIABLE "));
		serverRuntimeMenu.add(new VTGraphicalConsoleInputMenuItem("Set Local JVM Properties", "*VTPROPERTY "));
		
		fileSystemMenu = new Menu("Files ");
		fileSystemMenu.add(new VTGraphicalConsoleInputMenuItem("List Local File System Roots", "*VTFILEROOTS\n"));
		
		serverNetworkMenu = new Menu("Networks ");
		serverNetworkMenu.add(new VTGraphicalConsoleInputMenuItem("List Local Network Interfaces", "*VTNETWORKS\n"));
		
		serverGraphicalSystemsMenu = new Menu("Graphics ");
		serverGraphicalSystemsMenu.add(new VTGraphicalConsoleInputMenuItem("List Local Display Devices", "*VTDISPLAYS\n"));
		
		serverAudioSystemsMenu = new Menu("Audio ");
		serverAudioSystemsMenu.add(new VTGraphicalConsoleInputMenuItem("List Local Audio Mixers", "*VTAUDIOMIXERS\n"));
		
		serverPrintMenu = new Menu("Printing ");
		serverPrintMenu.add(new VTGraphicalConsoleInputMenuItem("List Local Printers", "*VTPRINTERS\n"));
		// serverUtilitiesMenu.add(new VTGraphicalConsoleInputMenuItem("Show
		// Default
		// Print Service ", "*VTDEFAULTPRINTSERVICE\n"));
		helpMenu = new Menu("Help ");
		helpMenu.add(new VTGraphicalConsoleInputMenuItem("Complete Commands", "*VTHELP\n"));
		helpMenu.add(new VTGraphicalConsoleInputMenuItem("Abbreviated Commands", "*VTHLP\n"));
		helpMenu.add(new VTGraphicalConsoleInputMenuItem("Specific Command", "*VTHELP "));
		
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
		
		dialogMenu = new Menu("Dialogs");
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
		
		textActionAllMenu = new MenuItem("All ");
		textActionAllMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTConsole.copyAllText();
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
		textActionsMenu.add(textActionAllMenu);
		textActionsMenu.add(textActionBreakMenu);
		textActionsMenu.add(textActionInsertMenu);
		
		this.add(textActionsMenu);
		
		textMenu = new Menu("Fonts");
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
		textMenu.add(toggleBoldTextMenu);
		textMenu.add(defaultTextMenu);
		
		this.add(textMenu);
		textMenu.setEnabled(true);
		
		keyboardShortcutsMenu = new Menu("Shortcuts");
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+C : Quit Application"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Z : Interrupt Text"));
		keyboardShortcutsMenu.add(new MenuItem("Break/Pause : Interrupt Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Insert : Copy Text"));
		keyboardShortcutsMenu.add(new MenuItem("Shift+Insert : Paste Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Backspace : Copy All Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Delete : Toggle Insert/Replace"));
		keyboardShortcutsMenu.add(new MenuItem("Insert : Toggle Insert/Replace"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgDown : Decrease Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgUp : Increase Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+End : Intensitize Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Home : Normalize Font"));
		
		this.add(keyboardShortcutsMenu);
		keyboardShortcutsMenu.setEnabled(true);
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
		serverSettingsDialogMenu.setEnabled(enabled);
	}
}