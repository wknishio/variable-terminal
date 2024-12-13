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
  private Menu runtimeMenu;
  private Menu graphicalSystemsMenu;
  private Menu audioSystemsMenu;
  private Menu networkMenu;
  private Menu printMenu;
  // private Menu settingsMenu;
  private Menu settingsMenu;
  private Menu connectionSettingsMenu;
  // private Menu serverAuthenticationSettingsMenu;
  private Menu proxySettingsMenu;
  private Menu encryptionSettingsMenu;
  private Menu sessionsSettingsMenu;
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
    sessionMenu.add(new VTGraphicalConsoleMenuItem("List Connected Clients", "*VTUSER\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Send Message To Clients", "*VTTEXT "));
    sessionMenu.add(new VTGraphicalConsoleMenuItem("Disconnect Client", "*VTKICK "));
    
    performanceMenu = new Menu("Rate ");
    performanceMenu.add(new VTGraphicalConsoleMenuItem("Check Connection Latencies", "*VTPING\n"));
    
    consoleMenu = new Menu("Console ");
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Clear Local Console", "*VTCLEAR\n"));
    consoleMenu.add(new VTGraphicalConsoleMenuItem("Show Local System Time", "*VTDATE\n"));
    
    settingsMenu = new Menu("Setting ");
    connectionSettingsMenu = new Menu("Connection ");
    // serverAuthenticationSettingsMenu = new Menu("Authentication ");
    proxySettingsMenu = new Menu("Proxy ");
    encryptionSettingsMenu = new Menu("Encryption ");
    sessionsSettingsMenu = new Menu("Session ");
    settingsMenu.add(new VTGraphicalConsoleMenuItem("List Server Settings", "*VTSETTING\n"));
    settingsMenu.add(connectionSettingsMenu);
    // serverSettingsMenu.add(serverAuthenticationSettingsMenu);
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
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Shell", "*VTSETTING SS "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Maximum", "*VTSETTING SM "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem("Set Session Accounts", "*VTSETTING SA "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Save Settings File", "*VTSETTING SF "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Load Settings File", "*VTSETTING LF "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Reconfigure Settings", "*VTCONFIGURE\n"));
    settingsMenu.add(new VTGraphicalConsoleMenuItem("Command Usage", "*VTHELP *VTSETTING\n"));
    // settingsMenu.add(serverSettingsMenu);
    // serverUtilitiesMenu = new Menu("Local System Utilities ");
    runtimeMenu = new Menu("Runtime ");
    runtimeMenu.add(new VTGraphicalConsoleMenuItem("Set Local Environment Variables", "*VTVARIABLE "));
    runtimeMenu.add(new VTGraphicalConsoleMenuItem("Set Local Java Properties", "*VTPROPERTY "));
    
    fileSystemMenu = new Menu("File ");
    fileSystemMenu.add(new VTGraphicalConsoleMenuItem("List Local File System Roots", "*VTFILEROOT\n"));
    
    networkMenu = new Menu("Network ");
    networkMenu.add(new VTGraphicalConsoleMenuItem("List Local Network Interfaces", "*VTNETWORK\n"));
    
    graphicalSystemsMenu = new Menu("Graphical ");
    graphicalSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Local Display Devices", "*VTDISPLAY\n"));
    
    audioSystemsMenu = new Menu("Audio ");
    audioSystemsMenu.add(new VTGraphicalConsoleMenuItem("List Local Audio Mixers", "*VTMIXER\n"));
    
    printMenu = new Menu("Print ");
    printMenu.add(new VTGraphicalConsoleMenuItem("List Local Printers", "*VTPRINTER\n"));
    // serverUtilitiesMenu.add(new VTGraphicalConsoleInputMenuItem("Show
    // Default
    // Print Service ", "*VTDEFAULTPRINTSERVICE\n"));
    helpMenu = new Menu("Help ");
    helpMenu.add(new VTGraphicalConsoleMenuItem("Complete Commands", "*VTHELP\n"));
    helpMenu.add(new VTGraphicalConsoleMenuItem("Abbreviated Commands", "*VTHL\n"));
    helpMenu.add(new VTGraphicalConsoleMenuItem("Specific Command", "*VTHELP "));
    
    serverConsoleCommandsMenu.add(sessionMenu);
    // serverConsoleCommandsMenu.add(settingsMenu);
    serverConsoleCommandsMenu.add(consoleMenu);
    serverConsoleCommandsMenu.add(performanceMenu);
    serverConsoleCommandsMenu.add(runtimeMenu);
    serverConsoleCommandsMenu.add(fileSystemMenu);
    serverConsoleCommandsMenu.add(graphicalSystemsMenu);
    serverConsoleCommandsMenu.add(audioSystemsMenu);
    serverConsoleCommandsMenu.add(networkMenu);
    serverConsoleCommandsMenu.add(printMenu);
    serverConsoleCommandsMenu.add(settingsMenu);
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