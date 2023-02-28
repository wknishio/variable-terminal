package org.vash.vate.console.graphical.menu;

// import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.Frame;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vash.vate.console.VTConsole;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleCopyActionListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsoleCopyAllActionListener;
import org.vash.vate.console.graphical.listener.VTGraphicalConsolePasteActionListener;

public class VTGraphicalConsolePopupMenu extends PopupMenu
{
  private static final long serialVersionUID = 1L;
  private Frame frame;
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
  
  public VTGraphicalConsolePopupMenu(Frame frame)
  {
    this.frame = frame;
    // this.keyListener = keyListener;
    copy = new MenuItem("Copy ");
    paste = new MenuItem("Paste ");
    all = new MenuItem("All ");
    insert = new MenuItem("Insert ");
    scroll = new MenuItem("Break ");
    // expand = new MenuItem("Expand");
    // reduce = new MenuItem("Reduce");
    // scroll = new CheckboxMenuItem("Scroll", false);
    copyActionListener = new VTGraphicalConsoleCopyActionListener();
    allActionListener = new VTGraphicalConsoleCopyAllActionListener();
    pasteActionListener = new VTGraphicalConsolePasteActionListener();
    copy.addActionListener(copyActionListener);
    paste.addActionListener(pasteActionListener);
    all.addActionListener(allActionListener);
    scroll.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VTConsole.toggleScrollMode();
      }
    });
    insert.addActionListener(new ActionListener()
    {
      public void actionPerformed(ActionEvent e)
      {
        VTConsole.toggleInputMode();
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
    frame.remove(this);
    frame.add(this);
    // scroll.setState(b)
    super.show(origin, x, y);
  }
}