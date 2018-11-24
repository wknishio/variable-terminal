package org.vate.console.graphical.menu;

// import java.awt.CheckboxMenuItem;
import java.awt.Component;
import java.awt.MenuItem;
import java.awt.PopupMenu;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.console.graphical.VTGraphicalConsole;
import org.vate.console.graphical.listener.VTGraphicalConsoleCopyActionListener;
import org.vate.console.graphical.listener.VTGraphicalConsolePasteActionListener;

public class VTGraphicalConsolePopupMenu extends PopupMenu
{
	private static final long serialVersionUID = 1L;
	private MenuItem copy;
	private MenuItem paste;
	private MenuItem scroll;
	private MenuItem insert;
	//private MenuItem expand;
	//private MenuItem reduce;
	
	// private CheckboxMenuItem scroll;
	// private VTGraphicalConsoleKeyListener keyListener;
	private VTGraphicalConsoleCopyActionListener copyActionListener;
	private VTGraphicalConsolePasteActionListener pasteActionListener;
	
	public VTGraphicalConsolePopupMenu()
	{
		// this.keyListener = keyListener;
		copy = new MenuItem("Copy");
		paste = new MenuItem("Paste");
		scroll = new MenuItem("Break");
		insert = new MenuItem("Insert");
		//expand = new MenuItem("Expand");
		//reduce = new MenuItem("Reduce");
		// scroll = new CheckboxMenuItem("Scroll", false);
		copyActionListener = new VTGraphicalConsoleCopyActionListener();
		pasteActionListener = new VTGraphicalConsolePasteActionListener();
		copy.addActionListener(copyActionListener);
		paste.addActionListener(pasteActionListener);
		scroll.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGraphicalConsole.toggleScrollMode();
			}
		});
		insert.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				VTGraphicalConsole.alternateInputMode();
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
		this.insert(copy, 0);
		this.insert(paste, 1);
		this.insert(insert, 2);
		this.insert(scroll, 3);
		//this.insert(expand, 4);
		//this.insert(reduce, 5);
		// this.insert(scroll, 2);
		VTGraphicalConsole.getFrame().add(this);
	}
	
	/* public boolean getScroll() { return scroll.getState(); } */
	
	public void show(Component origin, int x, int y)
	{
		VTGraphicalConsole.getFrame().remove(this);
		VTGraphicalConsole.getFrame().add(this);
		// scroll.setState(b)
		super.show(origin, x, y);
	}
}