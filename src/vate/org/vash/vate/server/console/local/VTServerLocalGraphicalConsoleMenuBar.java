package org.vash.vate.server.console.local;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuItem;
import org.vash.vate.server.dialog.VTServerSettingsDialog;

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
  // private Menu settingsMenu;
  private Menu serverSettingsMenu;
  private Menu serverConnectionMenu;
  // private Menu serverAuthenticationSettingsMenu;
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
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Close Server Application", "*VTSTOP\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Toggle Server Cover", "*VTCOVER\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("List Connected Clients", "*VTUSERS\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Send Text Message To Clients", "*VTTEXT "));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Disconnect Client From Server", "*VTKICK "));
    
    performanceMenu = new Menu("Rates ");
    performanceMenu.add(new VTGraphicalConsoleMenuItem("Check Connection Latencies", "*VTPING\n"));
    
    consoleMenu = new Menu("Console ");
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Clear Local Console", "*VTCLEAR\n"));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Show Local System Time", "*VTTIME\n"));
    
    serverSettingsMenu = new Menu("Settings ");
    serverConnectionMenu = new Menu("Connection ");
    // serverAuthenticationSettingsMenu = new Menu("Authentication ");
    serverProxyMenu = new Menu("Proxy ");
    serverEncryptionMenu = new Menu("Encryption ");
    serverSessionsMenu = new Menu("Session ");
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("List All Settings", "*VTSETTING\n"));
    serverSettingsMenu.add(serverConnectionMenu);
    // serverSettingsMenu.add(serverAuthenticationSettingsMenu);
    serverSettingsMenu.add(serverProxyMenu);
    serverSettingsMenu.add(serverEncryptionMenu);
    serverSettingsMenu.add(serverSessionsMenu);
    serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Mode", "*VTSETTING CM "));
    serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Host", "*VTSETTING CH "));
    serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection Port", "*VTSETTING CP "));
    serverConnectionMenu.add(new VTGraphicalConsoleMenuItem("Set Connection NAT Port", "*VTSETTING CN "));
    serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Type", "*VTSETTING PT "));
    serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Host", "*VTSETTING PH "));
    serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Port", "*VTSETTING PP "));
    serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Authentication", "*VTSETTING PA "));
    serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy User", "*VTSETTING PU "));
    serverProxyMenu.add(new VTGraphicalConsoleMenuItem("Set Proxy Password", "*VTSETTING PK "));
    serverEncryptionMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Type", "*VTSETTING ET "));
    serverEncryptionMenu.add(new VTGraphicalConsoleMenuItem("Set Encryption Password", "*VTSETTING EK "));
    serverSessionsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Maximum", "*VTSETTING SM "));
    serverSessionsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Shell", "*VTSETTING SS "));
    serverSessionsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Credential", "*VTLOCK "));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Save Settings File", "*VTSETTING SP "));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Load Settings File", "*VTSETTING LP "));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Reconfigure Settings", "*VTCONFIGURE\n"));
    serverSettingsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSETTING\n"));
    // settingsMenu.add(serverSettingsMenu);
    // serverUtilitiesMenu = new Menu("Local System Utilities ");
    serverRuntimeMenu = new Menu("Runtime ");
    serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Local Environment Variables", "*VTVARIABLE "));
    serverRuntimeMenu.add(new VTGraphicalConsoleMenuItem("Set Local Java Properties", "*VTPROPERTY "));
    
    fileSystemMenu = new Menu("File ");
    fileSystemMenu.add(new VTGraphicalConsoleMenuItem("List Local File System Roots", "*VTFILEROOTS\n"));
    
    serverNetworkMenu = new Menu("Network ");
    serverNetworkMenu.add(new VTGraphicalConsoleMenuItem("List Local Network Interfaces", "*VTNETWORKS\n"));
    
    serverGraphicalSystemsMenu = new Menu("Graphical ");
    serverGraphicalSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Local Display Devices", "*VTDISPLAYS\n"));
    
    serverAudioSystemsMenu = new Menu("Audio ");
    serverAudioSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Local Audio Mixers", "*VTMIXERS\n"));
    
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
    // serverConsoleCommandsMenu.add(settingsMenu);
    serverConsoleCommandsMenu.add(consoleMenu);
    serverConsoleCommandsMenu.add(performanceMenu);
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
    
    // Menu helpMenu = new Menu("Resume/Insert");
    // helpMenu.setEnabled(false);
    // this.setHelpMenu(helpMenu);
    // VTGlobalFontManager.registerMenu(this);
  }
  
  public void setEnabled(boolean enabled)
  {
    /*
     * for (int i = 0;i < this.getMenuCount();i++) {
     * this.getMenu(i).setEnabled(enabled); }
     */
    serverConsoleCommandsMenu.setEnabled(enabled);
  }
  
  public void setEnabledDialogMenu(boolean enabled)
  {
    dialogMenu.setEnabled(enabled);
  }
}