package org.vash.vate.console.graphical.listener;

import java.awt.PopupMenu;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;
import java.util.List;
import org.vash.vate.console.VTConsole;
import org.vash.vate.console.graphical.VTGraphicalConsole;
import org.vash.vate.graphics.font.VTGlobalTextStyleManager;
import org.vash.vate.runtime.VTRuntimeExit;

public class VTGraphicalConsoleKeyListener implements KeyListener
{
  private boolean scrolled;
  private Clipboard systemClipboard;
  private PopupMenu popupMenu;
  
  public VTGraphicalConsoleKeyListener(PopupMenu popupMenu)
  {
    this.popupMenu = popupMenu;
    this.scrolled = false;
    this.systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }
  
  public void toggleFlushMode()
  {
    scrolled = !scrolled;
    if (scrolled)
    {
      VTGraphicalConsole.interruptOutputFlush();
    }
    else
    {
      VTGraphicalConsole.resumeOutputFlush();
    }
  }
  
  @SuppressWarnings("unchecked")
  public void keyPressed(KeyEvent e)
  {
    // System.out.println(e.toString());
    // System.out.println("e.getKeyChar():[" + e.getKeyChar() + "] [" +
    // (int)
    // e.getKeyChar() + "]");
    /*
     * if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU) {
     * popupMenu.show(e.getComponent(), 0, 0); e.consume(); }
     */
    if (VTGlobalTextStyleManager.processKeyEvent(e))
    {
      return;
    }
    if (e.isShiftDown() && !e.isControlDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_INSERT)
    {
      e.consume();
      try
      {
        if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
        {
          String text = systemClipboard.getData(DataFlavor.stringFlavor).toString();
          VTConsole.input(text);
        }
        else if (systemClipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
        {
          List<File> files = (List<File>) systemClipboard.getData(DataFlavor.javaFileListFlavor);
          if (files.size() > 0)
          {
            StringBuilder fileList = new StringBuilder();
            String fileListString = "";
            for (File file : files)
            {
              fileList.append(" " + file.getAbsolutePath());
            }
            fileListString = fileList.substring(1);
            VTConsole.input(fileListString);
          }
        }
      }
      catch (Throwable e1)
      {
        
      }
      return;
    }
    if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_INSERT)
    {
      e.consume();
      String selectedText = VTConsole.getSelectedText();
      StringSelection text = null;
      if (selectedText != null)
      {
        text = new StringSelection(selectedText);
      }
      else
      {
        text = new StringSelection("");
      }
      systemClipboard.setContents(text, null);
      return;
    }
    if (e.isControlDown() && !e.isShiftDown() && !e.isAltDown() && e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
    {
      e.consume();
      String selectedText = VTConsole.getAllText();
      StringSelection text = null;
      if (selectedText != null)
      {
        text = new StringSelection(selectedText);
      }
      else
      {
        text = new StringSelection("");
      }
      systemClipboard.setContents(text, null);
      return;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_DELETE)
    {
      e.consume();
      VTGlobalTextStyleManager.defaultFontSize();
      return;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_HOME)
    {
      e.consume();
      VTGlobalTextStyleManager.defaultComponentSize();
      return;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_END)
    {
      e.consume();
      if (VTGlobalTextStyleManager.isFontStyleBold())
      {
        VTGlobalTextStyleManager.disableFontStyleBold();
      }
      else
      {
        VTGlobalTextStyleManager.enableFontStyleBold();
      }
      return;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_X)
    {
      e.consume();
      VTConsole.toggleInputMode();
      return;
    }
    if (e.isControlDown() && e.getKeyCode() == KeyEvent.VK_Z)
    {
      e.consume();
      VTConsole.toggleFlushMode();
      return;
    }
    if (e.getKeyCode() == KeyEvent.VK_INSERT)
    {
      e.consume();
      VTConsole.toggleInputMode();
      return;
    }
    if (e.getKeyCode() == KeyEvent.VK_PAUSE)
    {
      e.consume();
      VTConsole.toggleFlushMode();
      return;
    }
    
    if (!scrolled)
    {
      if (e.getKeyCode() == KeyEvent.VK_LEFT)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_LEFT);
        e.consume();
        return;
      }
      else if (e.getKeyCode() == KeyEvent.VK_UP)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_UP);
        e.consume();
        return;
      }
      else if (e.getKeyCode() == KeyEvent.VK_DOWN)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_DOWN);
        e.consume();
        return;
      }
      else if (e.getKeyCode() == KeyEvent.VK_HOME)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_HOME);
        e.consume();
        return;
      }
      else if (e.getKeyCode() == KeyEvent.VK_END)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_END);
        e.consume();
        return;
      }
      else if (e.getKeyCode() == KeyEvent.VK_RIGHT)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_RIGHT);
        e.consume();
        return;
      }
      else if (e.getKeyCode() == KeyEvent.VK_DELETE)
      {
        VTGraphicalConsole.inputSpecial(VTGraphicalConsole.VT_VK_DELETE);
        e.consume();
        return;
      }
    }
    else
    {
      /*
       * if (!e.isShiftDown()) { VTGraphicalConsole.refreshCaretPosition(); }
       */
    }
    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
    {
      e.consume();
    }
    // if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0)
    // {
    // e.consume();
    // }
    // System.out.println(e.toString());
    if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
    {
      // System.out.println("VK_CONTEXT_MENU");
      popupMenu.show(e.getComponent(), 0, 0);
      // KeyEvent alt = new KeyEvent(VTGraphicalConsole.getTextArea(),
      // e.getID(),
      // e.getWhen(), e.getModifiersEx(), KeyEvent.VK_ALT, '\0');
      e.consume();
      return;
      // VTGraphicalConsole.getTextArea().dispatchEvent(alt);
    }
    
    // e.consume();
    // return;
  }
  
  public void keyReleased(KeyEvent e)
  {
    if (!scrolled)
    {
      if (e.getKeyCode() == KeyEvent.VK_LEFT || e.getKeyCode() == KeyEvent.VK_RIGHT || e.getKeyCode() == KeyEvent.VK_UP || e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_HOME || e.getKeyCode() == KeyEvent.VK_END)
      {
        e.consume();
      }
    }
    else
    {
      /*
       * if (!e.isShiftDown()) { VTGraphicalConsole.refreshCaretPosition(); }
       */
    }
    if (e.getKeyCode() == KeyEvent.VK_ENTER || e.getKeyCode() == KeyEvent.VK_DELETE || e.getKeyCode() == KeyEvent.VK_BACK_SPACE)
    {
      e.consume();
    }
    /*
     * if ((e.getKeyCode() != KeyEvent.VK_LEFT) && (e.getKeyCode() !=
     * KeyEvent.VK_UP) && (e.getKeyCode() != KeyEvent.VK_DOWN) &&
     * (e.getKeyCode() != KeyEvent.VK_HOME) && (e.getKeyCode() !=
     * KeyEvent.VK_END) && (e.getKeyCode() != KeyEvent.VK_RIGHT) &&
     * (e.getKeyCode() != KeyEvent.VK_PAGE_DOWN) && (e.getKeyCode() !=
     * KeyEvent.VK_PAGE_UP) && (!e.isActionKey())) { e.consume();
     * System.out.println("consumed!"); }
     */
    // if ((e.getModifiersEx() & KeyEvent.CTRL_DOWN_MASK) > 0)
    // {
    // e.consume();
    // }
    if (e.getKeyCode() == KeyEvent.VK_CONTEXT_MENU)
    {
      e.consume();
    }
    
    // System.out.println(e.toString());
  }
  
  public void keyTyped(KeyEvent e)
  {
    // System.out.println(e.toString());
    // System.out.println("e.getKeyChar():[" + e.getKeyChar() + "] [" +
    // (int)
    // e.getKeyChar() + "]");
    e.consume();
    if (e.getKeyChar() == '\t' && e.isShiftDown())
    {
      VTGraphicalConsole.inputSpecial('\b');
    }
    else if (e.getKeyChar() == '\u007f')
    {
      
    }
    else if (e.getKeyChar() == '\u0003')
    {
      if (!VTGraphicalConsole.ignoreClose)
      {
        VTRuntimeExit.exit(0);
      }
    }
    else if (e.getKeyChar() == '\u001A')
    {
      VTConsole.toggleFlushMode();
    }
    else if (e.getKeyChar() == '\u0018')
    {
      VTConsole.toggleInputMode();
    }
    else
    {
      VTGraphicalConsole.input(e.getKeyChar());
    }
    
  }
}