package org.vash.vate.client.graphicsmode.options;

import java.awt.CheckboxMenuItem;
import java.awt.Menu;
import java.awt.MenuItem;
import java.awt.event.KeyEvent;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuCancelClipboardTransferListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuClearLocalClipboardListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuClearRemoteClipboardListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeyIgnoreListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeySupressListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuReceiveRemoteClipboardListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuRemoteControlListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarControlMenuSendLocalClipboardListener;
import org.vash.vate.client.graphicsmode.options.listener.VTGraphicsModeClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener;

public class VTGraphicsModeClientOptionsMenuBarControlMenu extends Menu
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
  
  private VTGraphicsModeClientWriter writer;
  
  public VTGraphicsModeClientOptionsMenuBarControlMenu(VTGraphicsModeClientWriter writer)
  {
    super("Control");
    this.writer = writer;
    this.remoteControlMenu = new Menu("Remote Control ");
    this.localKeyIgnoreControlMenu = new Menu("Shortcut Support ");
    this.controlStateClauseMenu = new Menu("Control Clause ");
    this.localKeySuppressionControlMenu = new Menu("Combination Inhibition ");
    this.modifierKeyDownControlMenu = new Menu("Press Modifier Key ");
    this.modifierKeyUpControlMenu = new Menu("Release Modifier Key ");
    this.lockingKeysControlMenu = new Menu("Enter Lock Key ");
    this.systemKeysControlMenu = new Menu("Enter System Key ");
    this.clipboardControlMenu = new Menu("Clipboard Control ");
    
    this.runningOption = new CheckboxMenuItem("Running", true);
    this.runningOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarControlMenuRemoteControlListener(writer, runningOption));
    this.interruptedOption = new CheckboxMenuItem("Interrupted", false);
    this.interruptedOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarControlMenuRemoteControlListener(writer, interruptedOption));
    this.pressNumLock = new MenuItem("Num Lock");
    this.pressNumLock.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_NUM_LOCK));
    this.pressCapsLock = new MenuItem("Caps Lock");
    this.pressCapsLock.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_CAPS_LOCK));
    this.pressScrollLock = new MenuItem("Scroll Lock");
    this.pressScrollLock.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_SCROLL_LOCK));
    this.pressKanaLock = new MenuItem("Kana Lock");
    this.pressKanaLock.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_KANA_LOCK));
    this.pressEscape = new MenuItem("Esc");
    this.pressEscape.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_ESCAPE));
    this.pressPrintScreen = new MenuItem("PrtScr / SysRq");
    this.pressPrintScreen.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_PRINTSCREEN));
    this.pressPause = new MenuItem("Pause / Break");
    this.pressPause.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyPressListener(writer, KeyEvent.VK_PAUSE));
    
    this.downControl = new MenuItem("Control");
    this.downControl.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_CONTROL));
    this.downShift = new MenuItem("Shift");
    this.downShift.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_SHIFT));
    this.downAlt = new MenuItem("Alt");
    this.downAlt.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_ALT));
    this.downAltGr = new MenuItem("AltGr");
    this.downAltGr.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_ALT_GRAPH));
    this.downWin = new MenuItem("Win");
    this.downWin.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_WINDOWS));
    this.downMeta = new MenuItem("Meta");
    this.downMeta.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyDownListener(writer, KeyEvent.VK_META));
    
    this.upControl = new MenuItem("Control");
    this.upControl.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_CONTROL));
    this.upShift = new MenuItem("Shift");
    this.upShift.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_SHIFT));
    this.upAlt = new MenuItem("Alt");
    this.upAlt.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_ALT));
    this.upAltGr = new MenuItem("AltGr");
    this.upAltGr.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_ALT_GRAPH));
    this.upWin = new MenuItem("Win");
    this.upWin.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_WINDOWS));
    this.upMeta = new MenuItem("Meta");
    this.upMeta.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuKeyUpListener(writer, KeyEvent.VK_META));
    
    this.clearLocalClipboardContents = new MenuItem("Clear Local Clipboard");
    this.clearLocalClipboardContents.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuClearLocalClipboardListener(writer));
    this.clearRemoteClipboardContents = new MenuItem("Clear Remote Clipboard");
    this.clearRemoteClipboardContents.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuClearRemoteClipboardListener(writer));
    this.sendLocalClipboardContents = new MenuItem("Send Local Clipboard");
    this.sendLocalClipboardContents.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuSendLocalClipboardListener(writer));
    this.receiveRemoteClipboardContents = new MenuItem("Receive Remote Clipboard");
    this.receiveRemoteClipboardContents.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuReceiveRemoteClipboardListener(writer));
    this.cancelClipboardContentsTransfer = new MenuItem("Cancel Clipboard Transfer");
    this.cancelClipboardContentsTransfer.setEnabled(false);
    this.cancelClipboardContentsTransfer.addActionListener(new VTGraphicsModeClientOptionsMenuBarControlMenuCancelClipboardTransferListener(writer));
    this.disableLocalKeySuppressionOption = new CheckboxMenuItem("Disabled", true);
    this.disableLocalKeySuppressionOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeySupressListener(writer, disableLocalKeySuppressionOption));
    this.enableLocalKeySuppressionOption = new CheckboxMenuItem("Enabled", false);
    this.enableLocalKeySuppressionOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeySupressListener(writer, enableLocalKeySuppressionOption));
    this.disableLocalKeyIgnoreOption = new CheckboxMenuItem("Enabled", true);
    this.disableLocalKeyIgnoreOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeyIgnoreListener(writer, disableLocalKeyIgnoreOption));
    this.enableLocalKeyIgnoreOption = new CheckboxMenuItem("Disabled", false);
    this.enableLocalKeyIgnoreOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeyIgnoreListener(writer, enableLocalKeyIgnoreOption));
    this.needFocusOption = new CheckboxMenuItem("Focused", true);
    this.needFocusOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener(writer, needFocusOption, VTGraphicsModeClientWriter.TERMINAL_STATE_FOCUSED));
    this.needVisibleOption = new CheckboxMenuItem("Visible", false);
    this.needVisibleOption.addItemListener(new VTGraphicsModeClientOptionsMenuBarViewMenuTerminalControlPolicyOptionsListener(writer, needVisibleOption, VTGraphicsModeClientWriter.TERMINAL_STATE_VISIBLE));
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
    
    this.add(remoteControlMenu);
    this.add(localKeyIgnoreControlMenu);
    this.add(controlStateClauseMenu);
    this.add(localKeySuppressionControlMenu);
    this.add(modifierKeyDownControlMenu);
    this.add(modifierKeyUpControlMenu);
    this.add(lockingKeysControlMenu);
    this.add(systemKeysControlMenu);
    this.add(clipboardControlMenu);
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
    clipboardControlMenu.setEnabled(false);
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
    clipboardControlMenu.setEnabled(true);
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
    if (state == VTGraphicsModeClientWriter.TERMINAL_STATE_FOCUSED)
    {
      needFocusOption.setState(true);
      needVisibleOption.setState(false);
    }
    else if (state == VTGraphicsModeClientWriter.TERMINAL_STATE_VISIBLE)
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