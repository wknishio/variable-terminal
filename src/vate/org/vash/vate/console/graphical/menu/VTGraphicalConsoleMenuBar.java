package org.vash.vate.console.graphical.menu;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.console.VTConsoleBooleanToggleNotify;
import org.vash.vate.console.VTConsoleInstance;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

public class VTGraphicalConsoleMenuBar extends MenuBar
{
  private static final long serialVersionUID = 1L;
  
  private Menu textActionsMenu;
  private MenuItem textActionCopyMenu;
  private MenuItem textActionPasteMenu;
  private MenuItem textActionAllMenu;
  
  private Menu sizesMenu;
  private MenuItem sizesExpandMenu;
  private MenuItem sizesReduceMenu;
  private MenuItem sizesRepackMenu;
  private MenuItem sizesDefaultMenu;
  private MenuItem sizesBold;
  
  private Menu keyboardShortcutsMenu;
  
  private Menu flushStatusMenu;
  private MenuItem flushToggleMenu;
  
  private Menu inputStatusMenu;
  private MenuItem inputToggleMenu;
  
  private boolean flushInterrupted = false;
  private boolean replaceInput = false;
  
  private final VTConsoleInstance console;
  
  public VTGraphicalConsoleMenuBar(final VTConsoleInstance console)
  {
    this.console = console;
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
    inputToggleMenu = new MenuItem("Replace");
    inputToggleMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.toggleInputMode();
      }
    });
    
    flushToggleMenu = new MenuItem("Pause");
    flushToggleMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.toggleFlushMode();
      }
    });
    
    textActionsMenu = new Menu("Edit");
    
    textActionCopyMenu = new MenuItem("Copy ");
    textActionCopyMenu.addActionListener(new ActionListener()
    {
      
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.copyText();
      }
    });
    
    textActionPasteMenu = new MenuItem("Paste ");
    textActionPasteMenu.addActionListener(new ActionListener()
    {
      
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.pasteText();
      }
    });
    
    textActionAllMenu = new MenuItem("All ");
    textActionAllMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.selectAllText();
        //console.copyAllText();
      }
    });
    
    textActionsMenu.add(textActionCopyMenu);
    textActionsMenu.add(textActionPasteMenu);
    textActionsMenu.add(textActionAllMenu);
    
    sizesMenu = new Menu("View");    
    sizesExpandMenu = new MenuItem("Expand ");
    sizesReduceMenu = new MenuItem("Reduce ");
    sizesRepackMenu = new MenuItem("Pack ");
    sizesBold = new MenuItem("Bold ");
    sizesDefaultMenu = new MenuItem("Reset ");
    
    sizesExpandMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.increaseFontSize();
      }
    });
    
    sizesReduceMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.decreaseFontSize();
      }
    });
    
    sizesBold.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
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
    
    sizesRepackMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.defaultComponentSize();
      }
    });
    
    sizesDefaultMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.defaultFontSize();
      }
    });
    
    sizesMenu.add(sizesExpandMenu);
    sizesMenu.add(sizesReduceMenu);
    sizesMenu.add(sizesBold);
    sizesMenu.add(sizesRepackMenu);
    sizesMenu.add(sizesDefaultMenu);
    sizesMenu.setEnabled(true);
    
    inputStatusMenu = new Menu("Insert");
    inputStatusMenu.setEnabled(true);
    inputStatusMenu.add(inputToggleMenu);
    
    flushStatusMenu = new Menu("Resume");
    flushStatusMenu.setEnabled(true);
    flushStatusMenu.add(flushToggleMenu);
    
    keyboardShortcutsMenu = new Menu("Shortcut");
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+C: Quit Application"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+X: Toggle Insert/Replace"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Z: Toggle Resume/Pause"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Insert: Copy Selected"));
    keyboardShortcutsMenu.add(new MenuItem("Shift+Insert: Paste Selected"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Backspace: Select All"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgUp: Expand View"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+PgDown: Reduce View"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+End: Bold View"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Home: Pack View"));
    keyboardShortcutsMenu.add(new MenuItem("Ctrl+Delete: Reset View"));
    
    this.add(textActionsMenu);
    this.add(sizesMenu);
    this.add(inputStatusMenu);
    this.add(flushStatusMenu);
    this.add(keyboardShortcutsMenu);
    keyboardShortcutsMenu.setEnabled(true);
    
    updateStatusMenu();
  }
  
  private class VTGraphicalConsoleFlushInterruptedNotify implements VTConsoleBooleanToggleNotify
  {
    public void notify(boolean state)
    {
      flushInterrupted = state;
      updateStatusMenu();
    }
  }
  
  private class VTGraphicalConsoleReplaceInputNotify implements VTConsoleBooleanToggleNotify
  {
    public void notify(boolean state)
    {
      replaceInput = state;
      updateStatusMenu();
    }
  }
  
  public void addStatusNotify()
  {
    console.addToggleFlushModePauseNotify(new VTGraphicalConsoleFlushInterruptedNotify());
    console.addToggleInputModeReplaceNotify(new VTGraphicalConsoleReplaceInputNotify());
  }
  
  public void updateStatusMenu()
  {
    console.requestFocus();
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
