package org.vash.vate.client.graphicsmode.options;

import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;

import org.vash.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBar extends MenuBar
{
  private static final long serialVersionUID = 1L;
  private VTGraphicsModeClientOptionsMenuBarViewMenu viewMenu;
  private VTGraphicsModeClientOptionsMenuBarControlMenu controlMenu;
  private Menu keyboardShortcutsMenu;
  private Menu monitorMenu;
  // private Menu refreshStatusMenu;
  // private Menu controlStatusMenu;
  private Frame frame;
  
  public VTGraphicsModeClientOptionsMenuBar(VTGraphicsModeClientWriter writer, Frame frame)
  {
    // this.setFont(new Font("Dialog", Font.PLAIN, 12));
    keyboardShortcutsMenu = new Menu("Key Shortcuts ");
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Backspace: Toggle Menu Bar"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Alt: Toggle Remote Control"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Space: Toggle Auto Scroll"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Enter: Toggle Full Screen"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+PageDown: Decrease Image Scale"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+PageUp: Increase Image Scale"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Insert: Normalize Image Scale"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Home: Previous Display Device"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+End: Next Display Device"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+1: Toggle Image Refresh"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+2: Reset Image Data"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+3: Decrease Refresh Rate"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+4: Increase Refresh Rate"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+5: Decrease Color Quality"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+6: Increase Color Quality"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+7: Decrease Remote Pointer"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+8: Increase Remote Pointer"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+9: Receive Remote Clipboard"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+0: Send Local Clipboard"));
    
    this.viewMenu = new VTGraphicsModeClientOptionsMenuBarViewMenu(writer);
    this.controlMenu = new VTGraphicsModeClientOptionsMenuBarControlMenu(writer, keyboardShortcutsMenu);
    // this.refreshStatusMenu = new Menu("Refresh");
    // this.refreshStatusMenu.setEnabled(false);
    // this.controlStatusMenu = new Menu("Command");
    // this.controlStatusMenu.setEnabled(false);
    this.add(viewMenu);
    this.add(controlMenu);
    
    //this.add(keyboardShortcutsMenu);
    keyboardShortcutsMenu.setEnabled(true);
    
    monitorMenu = new Menu("");
    this.add(monitorMenu);
    
    this.frame = frame;
    frame.setMenuBar(this);
    // VTGlobalFontManager.registerMenu(this);
  }
  
  public boolean enabled()
  {
    return frame.getMenuBar() != null;
  }
  
  public void disable()
  {
    frame.setMenuBar(null);
  }
  
  public void enable()
  {
    frame.setMenuBar(this);
  }
  
  /*
   * public void setReadOnly(boolean readOnly) { if (readOnly) {
   * remove(controlMenu); } else { add(controlMenu); } }
   */
  
  public void interruptControl()
  {
    controlMenu.interruptControl();
    // controlStatusMenu.setLabel("Control");
    // controlStatusMenu.setEnabled(false);
  }
  
  public void restabilishControl()
  {
    controlMenu.restabilishControl();
    // controlStatusMenu.setLabel("Control");
    // controlStatusMenu.setEnabled(true);
  }
  
  public void sendLocalClipboardContents()
  {
    controlMenu.sendLocalClipboardContents();
  }
  
  public void receiveRemoteClipboardContents()
  {
    controlMenu.receiveRemoteClipboardContents();
  }
  
  public void finishClipboardContentsTransfer()
  {
    controlMenu.finishClipboardContentsTransfer();
  }
  
  public void setColorQuality(int colorQuality)
  {
    viewMenu.setColorQuality(colorQuality);
  }
  
  public void setDrawPointer(boolean drawPointer)
  {
    viewMenu.setDrawPointer(drawPointer);
  }
  
  public void setScreenCaptureInterval(int screenCaptureInterval)
  {
    viewMenu.setScreenCaptureInterval(screenCaptureInterval);
  }
  
  public void setScreenCaptureMode(int mode)
  {
    viewMenu.setScreenCaptureMode(mode);
  }
  
  public void setSynchronousRefresh(boolean completeRefresh)
  {
    viewMenu.setSynchronousRefresh(completeRefresh);
  }
  
  public void setTerminalRefreshPolicy(int state)
  {
    viewMenu.setTerminalRefreshPolicy(state);
  }
  
  public void setTerminalControlPolicy(int state)
  {
    controlMenu.setTerminalControlPolicy(state);
  }
  
  /*
   * public void setIgnoreFocus(boolean ignoreFocus) {
   * viewMenu.setIgnoreFocus(ignoreFocus); } public void
   * setIgnoreIconification(boolean ignoreIconification) {
   * viewMenu.setIgnoreIconification(ignoreIconification); }
   */
  
  public void setSuppressLocalKeyCombinations(boolean suppressLocalKeyCombinations)
  {
    controlMenu.setSuppressLocalKeyCombinations(suppressLocalKeyCombinations);
  }
  
  public void setIgnoreLocalKeyCombinations(boolean ignoreLocalKeyCombinations)
  {
    controlMenu.setIgnoreLocalKeyCombinations(ignoreLocalKeyCombinations);
  }
  
  public void interruptRefresh()
  {
    viewMenu.interruptRefresh();
    // refreshStatusMenu.setLabel("Refresh: Off");
    // refreshStatusMenu.setEnabled(false);
  }
  
  public void resumeRefresh()
  {
    viewMenu.resumeRefresh();
    // refreshStatusMenu.setLabel("Refresh: On");
    // refreshStatusMenu.setEnabled(true);
  }
  
//  public void setDynamicCoding(boolean dynamicCoding)
//  {
//    viewMenu.setDynamicCoding(dynamicCoding);
//  }
  
//  public void setInterleavedCoding(boolean interleavedCoding)
//  {
//    viewMenu.setSeparatedCoding(interleavedCoding);
//  }
  
  public void setImageCoding(int imageCoding)
  {
    viewMenu.setImageCoding(imageCoding);
  }
  
  public void increaseCaptureInterval()
  {
    viewMenu.increaseCaptureInterval();
  }
  
  public void decreaseCaptureInterval()
  {
    viewMenu.decreaseCaptureInterval();
  }
  
  public void decreaseColorQuality()
  {
    viewMenu.decreaseColorQuality();
  }
  
  public void increaseColorQuality()
  {
    viewMenu.increaseColorQuality();
  }
  
  public boolean isClipBoardControlEnabled()
  {
    return controlMenu.isClipboardControlEnabled();
  }
  
  public void setKeyboardShortcutsMenuEnabled(boolean enabled)
  {
    keyboardShortcutsMenu.setEnabled(enabled);
  }
  
  public Menu getMonitorMenu()
  {
    return monitorMenu;
  }
}