package org.vash.vate.client.graphicslink.options;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.KeyEvent;

import org.vash.vate.client.graphicslink.VTGraphicsLinkClientWriter;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuCancelClipboardTransferListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuClearLocalClipboardListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuClearRemoteClipboardListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeyIgnoreListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeySupressListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuReceiveRemoteClipboardListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuRemoteControlListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarControlMenuSendLocalClipboardListener;
import org.vash.vate.client.graphicslink.options.listener.VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener;

public class VTGraphicsLinkClientOptionsMenuBarControlMenu extends Menu
{
  private static final long serialVersionUID = 1L;
  private Menu remoteControlMenu;
  private Menu controlStateClauseMenu;
  private Menu lockingKeysControlMenu;
  private Menu systemKeysControlMenu;
  private Menu modifierKeyDownControlMenu;
  private Menu modifierKeyUpControlMenu;
  private Menu clipboardControlMenu;
  private Menu localKeySuppressionControlMenu;
  private Menu localKeyIgnoreControlMenu;
  private CheckboxMenuItem runningOption;
  private CheckboxMenuItem interruptedOption;
  private CheckboxMenuItem needFocusOption;
  private CheckboxMenuItem needVisibleOption;
  private MenuItem pressNumLock;
  private MenuItem pressCapsLock;
  private MenuItem pressScrollLock;
  private MenuItem pressKanaLock;
  private MenuItem pressEscape;
  private MenuItem pressPrintScreen;
  private MenuItem pressPause;
  private MenuItem downControl;
  private MenuItem downShift;
  private MenuItem downAlt;
  private MenuItem downAltGr;
  private MenuItem downWin;
  private MenuItem downMeta;
  private MenuItem upControl;
  private MenuItem upShift;
  private MenuItem upAlt;
  private MenuItem upAltGr;
  private MenuItem upWin;
  private MenuItem upMeta;
  
  private MenuItem clearLocalClipboardContents;
  private MenuItem clearRemoteClipboardContents;
  private MenuItem sendLocalClipboardContents;
  private MenuItem receiveRemoteClipboardContents;
  private MenuItem cancelClipboardContentsTransfer;
  private CheckboxMenuItem disableLocalKeySuppressionOption;
  private CheckboxMenuItem enableLocalKeySuppressionOption;
  private CheckboxMenuItem disableLocalKeyIgnoreOption;
  private CheckboxMenuItem enableLocalKeyIgnoreOption;
  
  private VTGraphicsLinkClientWriter writer;
  
  public VTGraphicsLinkClientOptionsMenuBarControlMenu(VTGraphicsLinkClientWriter writer, Menu keyboardShortcutsMenu)
  {
    super("Control");
    this.writer = writer;
    this.localKeyIgnoreControlMenu = new Menu("Shortcut Support");
    this.clipboardControlMenu = new Menu("Clipboard Control ");
    this.remoteControlMenu = new Menu("Remote Control ");
    this.controlStateClauseMenu = new Menu("Control Clause");
    this.localKeySuppressionControlMenu = new Menu("Combination Inhibition ");
    this.modifierKeyDownControlMenu = new Menu("Press Modifier Key ");
    this.modifierKeyUpControlMenu = new Menu("Release Modifier Key ");
    this.lockingKeysControlMenu = new Menu("Enter Lock Key ");
    this.systemKeysControlMenu = new Menu("Enter System Key ");
    
    this.runningOption = new CheckboxMenuItem("Running", true);
    this.runningOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuRemoteControlListener(writer, runningOption));
    this.interruptedOption = new CheckboxMenuItem("Interrupted", false);
    this.interruptedOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuRemoteControlListener(writer, interruptedOption));
    this.pressNumLock = new MenuItem("Num Lock");
    this.pressNumLock.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_NUM_LOCK));
    this.pressCapsLock = new MenuItem("Caps Lock");
    this.pressCapsLock.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_CAPS_LOCK));
    this.pressScrollLock = new MenuItem("Scroll Lock");
    this.pressScrollLock.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_SCROLL_LOCK));
    this.pressKanaLock = new MenuItem("Kana Lock");
    this.pressKanaLock.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_KANA_LOCK));
    this.pressEscape = new MenuItem("Esc");
    this.pressEscape.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_ESCAPE));
    this.pressPrintScreen = new MenuItem("PrtScr / SysRq");
    this.pressPrintScreen.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_PRINTSCREEN));
    this.pressPause = new MenuItem("Pause / Break");
    this.pressPause.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_PAUSE));
    
    this.downControl = new MenuItem("Control");
    this.downControl.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_CONTROL));
    this.downShift = new MenuItem("Shift");
    this.downShift.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_SHIFT));
    this.downAlt = new MenuItem("Alt");
    this.downAlt.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_ALT));
    this.downAltGr = new MenuItem("AltGr");
    this.downAltGr.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_ALT_GRAPH));
    this.downWin = new MenuItem("Win");
    this.downWin.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_WINDOWS));
    this.downMeta = new MenuItem("Meta");
    this.downMeta.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_META));
    
    this.upControl = new MenuItem("Control");
    this.upControl.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_CONTROL));
    this.upShift = new MenuItem("Shift");
    this.upShift.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_SHIFT));
    this.upAlt = new MenuItem("Alt");
    this.upAlt.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_ALT));
    this.upAltGr = new MenuItem("AltGr");
    this.upAltGr.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_ALT_GRAPH));
    this.upWin = new MenuItem("Win");
    this.upWin.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_WINDOWS));
    this.upMeta = new MenuItem("Meta");
    this.upMeta.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_META));
    
    this.clearLocalClipboardContents = new MenuItem("Clear Local Clipboard");
    this.clearLocalClipboardContents.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuClearLocalClipboardListener(writer));
    this.clearRemoteClipboardContents = new MenuItem("Clear Remote Clipboard");
    this.clearRemoteClipboardContents.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuClearRemoteClipboardListener(writer));
    this.sendLocalClipboardContents = new MenuItem("Send Local Clipboard");
    this.sendLocalClipboardContents.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuSendLocalClipboardListener(writer));
    this.receiveRemoteClipboardContents = new MenuItem("Receive Remote Clipboard");
    this.receiveRemoteClipboardContents.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuReceiveRemoteClipboardListener(writer));
    this.cancelClipboardContentsTransfer = new MenuItem("Cancel Clipboard Transfer");
    this.cancelClipboardContentsTransfer.setEnabled(false);
    this.cancelClipboardContentsTransfer.addActionListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuCancelClipboardTransferListener(writer));
    this.disableLocalKeySuppressionOption = new CheckboxMenuItem("Disabled", true);
    this.disableLocalKeySuppressionOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeySupressListener(writer, disableLocalKeySuppressionOption));
    this.enableLocalKeySuppressionOption = new CheckboxMenuItem("Enabled", false);
    this.enableLocalKeySuppressionOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeySupressListener(writer, enableLocalKeySuppressionOption));
    this.disableLocalKeyIgnoreOption = new CheckboxMenuItem("Enabled", true);
    this.disableLocalKeyIgnoreOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeyIgnoreListener(writer, disableLocalKeyIgnoreOption));
    this.enableLocalKeyIgnoreOption = new CheckboxMenuItem("Disabled", false);
    this.enableLocalKeyIgnoreOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarControlMenuLocalKeyIgnoreListener(writer, enableLocalKeyIgnoreOption));
    this.needFocusOption = new CheckboxMenuItem("Focused", true);
    this.needFocusOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener(writer, needFocusOption, VTGraphicsLinkClientWriter.TERMINAL_STATE_FOCUSED));
    this.needVisibleOption = new CheckboxMenuItem("Visible", false);
    this.needVisibleOption.addItemListener(new VTGraphicsLinkClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener(writer, needVisibleOption, VTGraphicsLinkClientWriter.TERMINAL_STATE_VISIBLE));
    this.remoteControlMenu.add(runningOption);
    this.remoteControlMenu.add(interruptedOption);
    this.controlStateClauseMenu.add(needFocusOption);
    this.controlStateClauseMenu.add(needVisibleOption);
    this.localKeySuppressionControlMenu.add(disableLocalKeySuppressionOption);
    this.localKeySuppressionControlMenu.add(enableLocalKeySuppressionOption);
    this.localKeyIgnoreControlMenu.add(disableLocalKeyIgnoreOption);
    this.localKeyIgnoreControlMenu.add(enableLocalKeyIgnoreOption);
    
    this.lockingKeysControlMenu.add(pressNumLock);
    this.lockingKeysControlMenu.add(pressCapsLock);
    this.lockingKeysControlMenu.add(pressScrollLock);
    this.lockingKeysControlMenu.add(pressKanaLock);
    this.systemKeysControlMenu.add(pressPrintScreen);
    this.systemKeysControlMenu.add(pressPause);
    this.systemKeysControlMenu.add(pressEscape);
    this.modifierKeyDownControlMenu.add(downControl);
    this.modifierKeyDownControlMenu.add(downShift);
    this.modifierKeyDownControlMenu.add(downAlt);
    this.modifierKeyDownControlMenu.add(downAltGr);
    this.modifierKeyDownControlMenu.add(downWin);
    this.modifierKeyDownControlMenu.add(downMeta);
    this.modifierKeyUpControlMenu.add(upControl);
    this.modifierKeyUpControlMenu.add(upShift);
    this.modifierKeyUpControlMenu.add(upAlt);
    this.modifierKeyUpControlMenu.add(upAltGr);
    this.modifierKeyUpControlMenu.add(upWin);
    this.modifierKeyUpControlMenu.add(upMeta);
    this.clipboardControlMenu.add(receiveRemoteClipboardContents);
    this.clipboardControlMenu.add(sendLocalClipboardContents);
    this.clipboardControlMenu.add(clearRemoteClipboardContents);
    this.clipboardControlMenu.add(clearLocalClipboardContents);
    this.clipboardControlMenu.add(cancelClipboardContentsTransfer);
    
    this.add(localKeyIgnoreControlMenu);
    this.add(keyboardShortcutsMenu);
    this.add(clipboardControlMenu);
    this.add(remoteControlMenu);
    this.add(controlStateClauseMenu);
    this.add(localKeySuppressionControlMenu);
    this.add(modifierKeyDownControlMenu);
    this.add(modifierKeyUpControlMenu);
    this.add(lockingKeysControlMenu);
    this.add(systemKeysControlMenu);
    
  }
  
  public void sendLocalClipboardContents()
  {
    clearLocalClipboardContents.setEnabled(false);
    clearRemoteClipboardContents.setEnabled(false);
    sendLocalClipboardContents.setEnabled(false);
    receiveRemoteClipboardContents.setEnabled(false);
    cancelClipboardContentsTransfer.setEnabled(true);
  }
  
  public void receiveRemoteClipboardContents()
  {
    clearLocalClipboardContents.setEnabled(false);
    clearRemoteClipboardContents.setEnabled(false);
    sendLocalClipboardContents.setEnabled(false);
    receiveRemoteClipboardContents.setEnabled(false);
    cancelClipboardContentsTransfer.setEnabled(true);
  }
  
  public void finishClipboardContentsTransfer()
  {
    clearLocalClipboardContents.setEnabled(true);
    clearRemoteClipboardContents.setEnabled(true);
    sendLocalClipboardContents.setEnabled(true);
    receiveRemoteClipboardContents.setEnabled(true);
    cancelClipboardContentsTransfer.setEnabled(false);
  }
  
  public void interruptControl()
  {
    runningOption.setState(false);
    interruptedOption.setState(true);
    controlStateClauseMenu.setEnabled(false);
    lockingKeysControlMenu.setEnabled(false);
    systemKeysControlMenu.setEnabled(false);
    modifierKeyDownControlMenu.setEnabled(false);
    modifierKeyUpControlMenu.setEnabled(false);
    //clipboardControlMenu.setEnabled(false);
    localKeySuppressionControlMenu.setEnabled(false);
    writer.clearAllPressedKeys();
  }
  
  public void restabilishControl()
  {
    runningOption.setState(true);
    interruptedOption.setState(false);
    controlStateClauseMenu.setEnabled(true);
    lockingKeysControlMenu.setEnabled(true);
    systemKeysControlMenu.setEnabled(true);
    modifierKeyDownControlMenu.setEnabled(true);
    modifierKeyUpControlMenu.setEnabled(true);
    //clipboardControlMenu.setEnabled(true);
    localKeySuppressionControlMenu.setEnabled(true);
  }
  
  public void setSuppressLocalKeyCombinations(boolean suppressLocalKeyCombinations)
  {
    if (suppressLocalKeyCombinations)
    {
      disableLocalKeySuppressionOption.setState(false);
      enableLocalKeySuppressionOption.setState(true);
    }
    else
    {
      disableLocalKeySuppressionOption.setState(true);
      enableLocalKeySuppressionOption.setState(false);
      writer.clearAllPressedKeys();
    }
  }
  
  public void setIgnoreLocalKeyCombinations(boolean ignoreLocalKeyCombinations)
  {
    if (ignoreLocalKeyCombinations)
    {
      disableLocalKeyIgnoreOption.setState(false);
      enableLocalKeyIgnoreOption.setState(true);
      writer.setKeyboardShortcutsMenuEnabled(false);
      
    }
    else
    {
      disableLocalKeyIgnoreOption.setState(true);
      enableLocalKeyIgnoreOption.setState(false);
      writer.setKeyboardShortcutsMenuEnabled(true);
      // writer.clearAllPressedKeys();
    }
  }
  
  public void setTerminalControlPolicy(int state)
  {
    if (state == VTGraphicsLinkClientWriter.TERMINAL_STATE_FOCUSED)
    {
      needFocusOption.setState(true);
      needVisibleOption.setState(false);
    }
    else if (state == VTGraphicsLinkClientWriter.TERMINAL_STATE_VISIBLE)
    {
      needFocusOption.setState(false);
      needVisibleOption.setState(true);
    }
  }
  
  public boolean isClipboardControlEnabled()
  {
    return clearLocalClipboardContents.isEnabled();
  }
}