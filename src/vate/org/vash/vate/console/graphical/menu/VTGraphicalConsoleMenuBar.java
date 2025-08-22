package org.vash.vate.console.graphical.menu;

import java.awt.Menu;
import java.awt.MenuBar;
import java.awt.MenuItem;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.console.VTConsoleBooleanToggleNotify;
import org.vash.vate.console.VTConsole;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;

public class VTGraphicalConsoleMenuBar extends MenuBar
{
  private static final long serialVersionUID = 1L;
  
  private Menu editMenu;
  private MenuItem editCopyMenu;
  private MenuItem editPasteMenu;
  private MenuItem editAllMenu;
  
  private Menu viewMenu;
  private MenuItem viewExpandMenu;
  private MenuItem viewReduceMenu;
  private MenuItem viewRepackMenu;
  private MenuItem viewDefaultMenu;
  private MenuItem viewBold;
  
  private Menu keyboardShortcutsMenu;
  
  private Menu flushStatusMenu;
  private MenuItem flushToggleMenu;
  
  private Menu inputStatusMenu;
  private MenuItem inputToggleMenu;
  
  private boolean flushInterrupted = false;
  private boolean replaceInput = false;
  
  private final VTConsole console;
  
  public VTGraphicalConsoleMenuBar(final VTConsole console)
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
    keyboardShortcutsMenu = new Menu("Shortcuts ");
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
    
    editMenu = new Menu("Edit");
    
    editCopyMenu = new MenuItem("Copy ");
    editCopyMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.copyText();
      }
    });
    
    editPasteMenu = new MenuItem("Paste ");
    editPasteMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.pasteText();
      }
    });
    
    editAllMenu = new MenuItem("Select All");
    editAllMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        console.selectAllText();
        //console.copyAllText();
      }
    });
    
    editMenu.add(editCopyMenu);
    editMenu.add(editPasteMenu);
    editMenu.add(editAllMenu);
    
    viewMenu = new Menu("View");    
    viewExpandMenu = new MenuItem("Expand ");
    viewReduceMenu = new MenuItem("Reduce ");
    viewRepackMenu = new MenuItem("Pack ");
    viewBold = new MenuItem("Bold ");
    viewDefaultMenu = new MenuItem("Reset ");
    
    viewExpandMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.increaseFontSize();
      }
    });
    
    viewReduceMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.decreaseFontSize();
      }
    });
    
    viewBold.addActionListener(new ActionListener()
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
      }
    });
    
    viewRepackMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.packComponentSize();
      }
    });
    
    viewDefaultMenu.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.requestFocus();
        VTGlobalTextStyleManager.defaultFontSize();
      }
    });
    
    viewMenu.add(keyboardShortcutsMenu);
    viewMenu.add(viewExpandMenu);
    viewMenu.add(viewReduceMenu);
    viewMenu.add(viewBold);
    viewMenu.add(viewRepackMenu);
    viewMenu.add(viewDefaultMenu);
    viewMenu.setEnabled(true);
    
    inputStatusMenu = new Menu("Insert");
    inputStatusMenu.setEnabled(true);
    inputStatusMenu.add(inputToggleMenu);
    
    flushStatusMenu = new Menu("Resume");
    flushStatusMenu.setEnabled(true);
    flushStatusMenu.add(flushToggleMenu);
    
    this.add(viewMenu);
    this.add(editMenu);
    this.add(inputStatusMenu);
    this.add(flushStatusMenu);
    //this.add(keyboardShortcutsMenu);
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
  
  public Menu getKeyboardShortcutsMenu()
  {
    return keyboardShortcutsMenu;
  }
}
