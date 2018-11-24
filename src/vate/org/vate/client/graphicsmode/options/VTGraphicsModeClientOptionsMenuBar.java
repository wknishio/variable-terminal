package org.vate.client.graphicsmode.options;

import java.awt.Frame;
import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;

import org.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBar extends MenuBar
{
	private static final long serialVersionUID = 1L;
	private VTGraphicsModeClientOptionsMenuBarViewMenu viewMenu;
	private VTGraphicsModeClientOptionsMenuBarControlMenu controlMenu;
	private Menu keyboardShortcutsMenu;
	// private Menu refreshStatusMenu;
	// private Menu controlStatusMenu;
	private Frame frame;
	
	
	public VTGraphicsModeClientOptionsMenuBar(VTGraphicsModeClientWriter writer, Frame frame)
	{
		// this.setFont(new Font("Dialog", Font.PLAIN, 12));
		this.viewMenu = new VTGraphicsModeClientOptionsMenuBarViewMenu(writer);
		this.controlMenu = new VTGraphicsModeClientOptionsMenuBarControlMenu(writer);
		// this.refreshStatusMenu = new Menu("Refresh");
		// this.refreshStatusMenu.setEnabled(false);
		// this.controlStatusMenu = new Menu("Command");
		// this.controlStatusMenu.setEnabled(false);
		this.add(viewMenu);
		this.add(controlMenu);
		
		keyboardShortcutsMenu = new Menu("Shortcuts");
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Alt : Toggle remote control"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Space : Toggle interface refresh"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Backspace : Toggle menu bar"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Enter : Toggle full screen mode"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+PageDown : Decrease image scale"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+PageUp : Increase image scale"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Insert : Reset image scale"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+Home : Use previous display device"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Shift+End : Use next display device"));
		
		this.add(keyboardShortcutsMenu);
		keyboardShortcutsMenu.setEnabled(true);
		// this.add(refreshStatusMenu);
		// this.add(controlStatusMenu);
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
	
	/* public void setReadOnly(boolean readOnly) { if (readOnly) {
	 * remove(controlMenu); } else { add(controlMenu); } } */
	
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
	
	/* public void setIgnoreFocus(boolean ignoreFocus) {
	 * viewMenu.setIgnoreFocus(ignoreFocus); }
	 * public void setIgnoreIconification(boolean ignoreIconification) {
	 * viewMenu.setIgnoreIconification(ignoreIconification); } */
	
	public void setSuppressLocalKeyCombinations(boolean suppressLocalKeyCombinations)
	{
		controlMenu.setSuppressLocalKeyCombinations(suppressLocalKeyCombinations);
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
	
	public void setDynamicCoding(boolean dynamicCoding)
	{
		viewMenu.setDynamicCoding(dynamicCoding);
	}
	
	public void setInterleavedCoding(boolean interleavedCoding)
	{
		viewMenu.setSeparatedCoding(interleavedCoding);
	}
	
	public void setImageCoding(int imageCoding)
	{
		viewMenu.setImageCoding(imageCoding);
	}
}