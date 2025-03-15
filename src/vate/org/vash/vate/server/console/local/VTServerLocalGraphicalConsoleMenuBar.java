package org.vash.vate.server.console.local;

import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.console.VTConsoleInstance;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuBar;
import org.vash.vate.console.graphical.menu.VTGraphicalConsoleMenuItem;
import org.vash.vate.server.dialog.VTServerSettingsDialog;

public class VTServerLocalGraphicalConsoleMenuBar extends VTGraphicalConsoleMenuBar
{
  private static final long serialVersionUID = 1L;
  
  //private Menu dialogMenu;
  private MenuItem serverSettingsDialogMenu;
  private Menu serverConsoleCommandsMenu;
  // private Menu serverBasedCommandsMenu;
  private Menu sessionMenu;
  private Menu trafficMenu;
  private Menu consoleMenu;
  private Menu filesMenu;
  private Menu runtimeMenu;
  private Menu graphicalMenu;
  private Menu audioMenu;
  private Menu networkMenu;
  private Menu printMenu;
  // private Menu settingsMenu;
  private Menu settingsMenu;
  private Menu connectionSettingsMenu;
  // private Menu serverAuthenticationSettingsMenu;
  private Menu proxySettingsMenu;
  private Menu encryptionSettingsMenu;
  private Menu sessionsSettingsMenu;
  private Menu pingSettingsMenu;
  private Menu helpMenu;
  private Menu monitorMenu;
  
  public VTServerLocalGraphicalConsoleMenuBar(final VTConsoleInstance console, final VTServerSettingsDialog connectionDialog)
  {
    super(console);
    removeAllMenus();
    // this.frame = frame;
    serverConsoleCommandsMenu = new Menu("Command");
    // serverConsoleCommandsMenu.setShortcut(new MenuShortcut(KeyEvent.VK_C,
    // true));
    
    sessionMenu = new Menu("Session ");
    sessionMenu.add(new VTGraphicalConsoleMenuItem(console, "Close Server Application", "*VTSTOP\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem(console, "Toggle Server Cover", "*VTCOVER\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem(console, "List Connected Clients", "*VTUSER\n"));
    sessionMenu.add(new VTGraphicalConsoleMenuItem(console, "Send Message To Clients", "*VTTEXT "));
    sessionMenu.add(new VTGraphicalConsoleMenuItem(console, "Disconnect Client", "*VTKICK "));
    
    trafficMenu = new Menu("Traffic ");
    trafficMenu.add(new VTGraphicalConsoleMenuItem(console, "Check Connection Latencies", "*VTPING\n"));
    
    consoleMenu = new Menu("Console ");
    consoleMenu.add(new VTGraphicalConsoleMenuItem(console, "Clear Local Console", "*VTCLEAR\n"));
    consoleMenu.add(new VTGraphicalConsoleMenuItem(console, "Show Local System Time", "*VTDATE\n"));
    
    settingsMenu = new Menu("Setting ");
    connectionSettingsMenu = new Menu("Connection ");
    // serverAuthenticationSettingsMenu = new Menu("Authentication ");
    proxySettingsMenu = new Menu("Proxy ");
    encryptionSettingsMenu = new Menu("Encryption ");
    sessionsSettingsMenu = new Menu("Session ");
    pingSettingsMenu = new Menu("Ping ");
    settingsMenu.add(new VTGraphicalConsoleMenuItem(console, "List Server Settings", "*VTSETTING\n"));
    settingsMenu.add(connectionSettingsMenu);
    // serverSettingsMenu.add(serverAuthenticationSettingsMenu);
    settingsMenu.add(proxySettingsMenu);
    settingsMenu.add(encryptionSettingsMenu);
    settingsMenu.add(pingSettingsMenu);
    settingsMenu.add(sessionsSettingsMenu);
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Connection Mode", "*VTSETTING CM "));
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Connection Host", "*VTSETTING CH "));
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Connection Port", "*VTSETTING CP "));
    connectionSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Connection NAT Port", "*VTSETTING CN "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Proxy Type", "*VTSETTING PT "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Proxy Host", "*VTSETTING PH "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Proxy Port", "*VTSETTING PP "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Proxy Authentication", "*VTSETTING PA "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Proxy User", "*VTSETTING PU "));
    proxySettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Proxy Password", "*VTSETTING PK "));
    encryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Encryption Type", "*VTSETTING ET "));
    encryptionSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Encryption Password", "*VTSETTING EK "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Session Shell", "*VTSETTING SS "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Session Maximum", "*VTSETTING SM "));
    sessionsSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Session Accounts", "*VTSETTING SA "));
    pingSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Ping Limit", "*VTSETTING PL "));
    pingSettingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Ping Interval", "*VTSETTING PI "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Save Settings File", "*VTSETTING SF "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Load Settings File", "*VTSETTING LF "));
    settingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Reconfigure Settings", "*VTCONFIGURE\n"));
    settingsMenu.add(new VTGraphicalConsoleMenuItem(console, "Command Usage", "*VTHELP *VTSETTING\n"));
    // settingsMenu.add(serverSettingsMenu);
    // serverUtilitiesMenu = new Menu("Local System Utilities ");
    runtimeMenu = new Menu("Runtime ");
    runtimeMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Local Environment Variables", "*VTVARIABLE "));
    runtimeMenu.add(new VTGraphicalConsoleMenuItem(console, "Set Local Java Properties", "*VTPROPERTY "));
    
    filesMenu = new Menu("File ");
    filesMenu.add(new VTGraphicalConsoleMenuItem(console, "List Local File Roots", "*VTFILEROOT\n"));
    
    networkMenu = new Menu("Network ");
    networkMenu.add(new VTGraphicalConsoleMenuItem(console, "List Local Network Interfaces", "*VTNETWORK\n"));
    
    graphicalMenu = new Menu("Graphical ");
    graphicalMenu.add(new VTGraphicalConsoleMenuItem(console, "List Local Display Devices", "*VTDISPLAY\n"));
    
    audioMenu = new Menu("Audio ");
    audioMenu.add(new VTGraphicalConsoleMenuItem(console, "List Local Audio Mixers", "*VTMIXER\n"));
    
    printMenu = new Menu("Print ");
    printMenu.add(new VTGraphicalConsoleMenuItem(console, "List Local Printers", "*VTPRINTER\n"));
    // serverUtilitiesMenu.add(new VTGraphicalConsoleInputMenuItem("Show
    // Default
    // Print Service ", "*VTDEFAULTPRINTSERVICE\n"));
    helpMenu = new Menu("Help ");
    helpMenu.add(new VTGraphicalConsoleMenuItem(console, "Complete Commands", "*VTHELP\n"));
    helpMenu.add(new VTGraphicalConsoleMenuItem(console, "Abbreviated Commands", "*VTHL\n"));
    helpMenu.add(new VTGraphicalConsoleMenuItem(console, "Specific Command", "*VTHELP "));
    
    serverSettingsDialogMenu = new MenuItem("Connection");
    // dialogMenu.setShortcut(new MenuShortcut(KeyEvent.VK_D, true));
    //dialogMenu.add(serverSettingsDialogMenu);
    
    serverSettingsDialogMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        connectionDialog.open();
      }
    });
    
    serverConsoleCommandsMenu.add(serverSettingsDialogMenu);
    serverConsoleCommandsMenu.add(sessionMenu);
    serverConsoleCommandsMenu.add(consoleMenu);
    serverConsoleCommandsMenu.add(trafficMenu);
    serverConsoleCommandsMenu.add(runtimeMenu);
    serverConsoleCommandsMenu.add(filesMenu);
    serverConsoleCommandsMenu.add(graphicalMenu);
    serverConsoleCommandsMenu.add(audioMenu);
    serverConsoleCommandsMenu.add(networkMenu);
    serverConsoleCommandsMenu.add(printMenu);
    serverConsoleCommandsMenu.add(settingsMenu);
    serverConsoleCommandsMenu.add(helpMenu);
    
    this.add(serverConsoleCommandsMenu);
    serverConsoleCommandsMenu.setEnabled(true);
    sessionMenu.setEnabled(false);
    consoleMenu.setEnabled(false);
    trafficMenu.setEnabled(false);
    runtimeMenu.setEnabled(false);
    filesMenu.setEnabled(false);
    graphicalMenu.setEnabled(false);
    audioMenu.setEnabled(false);
    networkMenu.setEnabled(false);
    printMenu.setEnabled(false);
    settingsMenu.setEnabled(false);
    helpMenu.setEnabled(false);
    
    //dialogMenu = new Menu("Dialog");
    
    //this.add(dialogMenu);
    //dialogMenu.setEnabled(true);
    
    super.addBaseMenus();
    
    monitorMenu = new Menu("");
    this.add(monitorMenu);
    // Menu helpMenu = new Menu("Resume/Insert");
    // helpMenu.setEnabled(false);
    // this.setHelpMenu(helpMenu);
    // VTGlobalFontManager.registerMenu(this);
  }
  
  public void setEnabled(boolean enabled)
  {
    sessionMenu.setEnabled(enabled);
    consoleMenu.setEnabled(enabled);
    trafficMenu.setEnabled(enabled);
    runtimeMenu.setEnabled(enabled);
    filesMenu.setEnabled(enabled);
    graphicalMenu.setEnabled(enabled);
    audioMenu.setEnabled(enabled);
    networkMenu.setEnabled(enabled);
    printMenu.setEnabled(enabled);
    settingsMenu.setEnabled(enabled);
    helpMenu.setEnabled(enabled);
  }
  
  public void setEnabledDialogMenu(boolean enabled)
  {
    serverSettingsDialogMenu.setEnabled(enabled);
  }
  
  public Menu getMonitorMenu()
  {
    return monitorMenu;
  }
}