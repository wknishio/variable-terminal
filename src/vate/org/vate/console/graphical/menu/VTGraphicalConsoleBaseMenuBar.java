package org.vate.console.graphical.menu;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.console.VTConsole;
import org.vate.console.VTConsoleBooleanToggleNotify;
import org.vate.graphics.font.VTGlobalTextStyleManager;

public class VTGraphicalConsoleBaseMenuBar extends MenuBar
{
	private static final long serialVersionUID = 1L;
	
	private Menu textActionsMenu;
	private MenuItem textActionCopyMenu;
	private MenuItem textActionPasteMenu;
	private MenuItem textActionAllMenu;
	private MenuItem textActionInsertMenu;
	private MenuItem textActionBreakMenu;
	
	private Menu textFontMenu;
	private MenuItem textFontIncreaseMenu;
	private MenuItem textFontDecreaseMenu;
	private MenuItem textFontDefaultMenu;
	private MenuItem textFontToggleBoldMenu;
	
	private Menu keyboardShortcutsMenu;
	
	private Menu flushStatusMenu;
	private MenuItem flushToggleMenu;
	
	private Menu inputStatusMenu;
	private MenuItem inputToggleMenu;
	
	private volatile boolean flushInterrupted = false;
	private volatile boolean replaceInput = false;
	
	public VTGraphicalConsoleBaseMenuBar()
	{
		addStatusNotify();
		removeAllMenus();
		addBaseMenus();
	}
	
	public void removeAllMenus()
	{
		int count = this.getMenuCount();
		for (int i = 0; i < count; i++)
		{
			this.remove(0);
		}
	}
	
	public void addBaseMenus()
	{
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
		
		textFontMenu = new Menu("Fonts");
		//textMenu.setShortcut(new MenuShortcut(KeyEvent.VK_F, true));
		
		textFontIncreaseMenu = new MenuItem("Increase ");
		textFontDecreaseMenu = new MenuItem("Decrease ");
		textFontDefaultMenu = new MenuItem("Normalize ");
		textFontToggleBoldMenu = new MenuItem("Intensitize ");
		
		textFontIncreaseMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGlobalTextStyleManager.increaseFontSize();
			}
		});
		
		textFontDecreaseMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGlobalTextStyleManager.decreaseFontSize();
			}
		});
		
		textFontDefaultMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGlobalTextStyleManager.defaultFontSize();
			}
		});
		
		textFontToggleBoldMenu.addActionListener(new ActionListener()
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
		
		textFontMenu.add(textFontIncreaseMenu);
		textFontMenu.add(textFontDecreaseMenu);
		textFontMenu.add(textFontToggleBoldMenu);
		textFontMenu.add(textFontDefaultMenu);
		
		this.add(textFontMenu);
		textFontMenu.setEnabled(true);
		
		keyboardShortcutsMenu = new Menu("Shortcuts");
		keyboardShortcutsMenu.add(new MenuItem("Break/Pause : Toggle Resume / Pause"));
		keyboardShortcutsMenu.add(new MenuItem("Insert : Toggle Insert / Replace"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+C : Quit Application"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Z : Toggle Resume / Pause"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+X : Toggle Insert / Replace"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Insert : Copy Selected Text"));
		keyboardShortcutsMenu.add(new MenuItem("Shift+Insert : Paste Selected Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Backspace : Copy All Text"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgDown : Decrease Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgUp : Increase Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+End : Intensitize Font"));
		keyboardShortcutsMenu.add(new MenuItem("Ctrl+Home : Normalize Font"));
		
		this.add(keyboardShortcutsMenu);
		keyboardShortcutsMenu.setEnabled(true);
		
		flushStatusMenu = new Menu("Resume");
		flushStatusMenu.setEnabled(true);
		flushToggleMenu = new MenuItem("Pause");
		flushToggleMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTConsole.toggleScrollMode();
			}
		});
		flushStatusMenu.add(flushToggleMenu);
		this.add(flushStatusMenu);
		
		inputStatusMenu = new Menu("Insert");
		inputStatusMenu.setEnabled(true);
		inputToggleMenu = new MenuItem("Replace");
		inputToggleMenu.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTConsole.toggleInputMode();
			}
		});
		inputStatusMenu.add(inputToggleMenu);
		this.add(inputStatusMenu);
		
		updateStatusMenu();
	}
	
	private class VTGraphicalConsoleFlushInterruptedNotify implements VTConsoleBooleanToggleNotify
	{
		public void notify(boolean state)
		{
			flushInterrupted = state;
			//System.out.println("flushInterrupted:" + flushInterrupted);
			updateStatusMenu();
		}
	}
	
	private class VTGraphicalConsoleReplaceInputNotify implements VTConsoleBooleanToggleNotify
	{
		public void notify(boolean state)
		{
			replaceInput = state;
			//System.out.println("replaceInput:" + replaceInput);
			updateStatusMenu();
		}
	}
	
	public void addStatusNotify()
	{
		VTConsole.addToggleFlushInterruptNotify(new VTGraphicalConsoleFlushInterruptedNotify());
		VTConsole.addToggleReplaceInputNotify(new VTGraphicalConsoleReplaceInputNotify());
	}
	
	public void updateStatusMenu()
	{
		if (flushInterrupted)
		{
			flushStatusMenu.setLabel("Pause");
			flushToggleMenu.setLabel("Resume");
		}
		else
		{
			flushStatusMenu.setLabel("Resume");
			flushToggleMenu.setLabel("Pause");
		}
		if (replaceInput)
		{
			inputStatusMenu.setLabel("Replace");
			inputToggleMenu.setLabel("Insert");
		}
		else
		{
			inputStatusMenu.setLabel("Insert");
			inputToggleMenu.setLabel("Replace");
		}
	}
}
