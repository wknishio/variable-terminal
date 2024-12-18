package org.vash.vate.console.graphical.menu;

// import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.console.VTConsoleInstance;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleCopyActionListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleCopyAllActionListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsolePasteActionListener;

public class VTGraphicalConsolePopupMenu extends PopupMenu
{
  private static final long serialVersionUID = 1L;
  
  private MenuItem copy;
  private MenuItem paste;
  private MenuItem all;
  private MenuItem scroll;
  private MenuItem insert;
  // private MenuItem expand;
  // private MenuItem reduce;
  
  // private CheckboxMenuItem scroll;
  // private VTGraphicalConsoleKeyListener keyListener;
  private VTGraphicalConsoleCopyActionListener copyActionListener;
  private VTGraphicalConsoleCopyAllActionListener allActionListener;
  private VTGraphicalConsolePasteActionListener pasteActionListener;
  
  private final VTConsoleInstance console;
  private final Frame frame;
  
  public VTGraphicalConsolePopupMenu(final VTConsoleInstance console, final Frame frame)
  {
    this.console = console;
    this.frame = frame;
    // this.keyListener = keyListener;
    copy = new MenuItem("Copy ");
    paste = new MenuItem("Paste ");
    all = new MenuItem("All ");
    insert = new MenuItem("Replace ");
    scroll = new MenuItem("Pause ");
    // expand = new MenuItem("Expand");
    // reduce = new MenuItem("Reduce");
    // scroll = new CheckboxMenuItem("Scroll", false);
    copyActionListener = new VTGraphicalConsoleCopyActionListener(console);
    allActionListener = new VTGraphicalConsoleCopyAllActionListener(console);
    pasteActionListener = new VTGraphicalConsolePasteActionListener(console);
    copy.addActionListener(copyActionListener);
    paste.addActionListener(pasteActionListener);
    all.addActionListener(allActionListener);
    scroll.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.toggleFlushMode();
      }
    });
    insert.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        console.toggleInputMode();
      }
    });
//		expand.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				VTGlobalFontManager.increaseFontSize();
//			}
//		});
//		reduce.addActionListener(new ActionListener()
//		{
//			public void actionPerformed(ActionEvent e)
//			{
//				VTGlobalFontManager.decreaseFontSize();
//			}
//		});
    // int dpi = Toolkit.getDefaultToolkit().getScreenResolution();
    // double factor = dpi / 96;
    // this.setFont(new Font("Dialog", Font.PLAIN, 12));
    this.add(copy);
    this.add(paste);
    this.add(all);
    this.add(scroll);
    this.add(insert);
    // this.insert(expand, 4);
    // this.insert(reduce, 5);
    // this.insert(scroll, 2);
    frame.add(this);
  }
  
  /* public boolean getScroll() { return scroll.getState(); } */
  
  public void show(Component origin, int x, int y)
  {
    if (console.isFlushModePause())
    {
      scroll.setLabel("Resume ");
    }
    else
    {
      scroll.setLabel("Pause ");
    }
    
    if (console.isInputModeReplace())
    {
      insert.setLabel("Insert ");
    }
    else
    {
      insert.setLabel("Replace ");
    }
    frame.remove(this);
    frame.add(this);
    // scroll.setState(b)
    super.show(origin, x, y);
  }
}