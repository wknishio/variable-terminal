package org.vate.console.graphical;

import java.awt.Graphics;
import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;

public class VTGraphicalConsoleTextArea extends TextArea
{
	// private static volatile Object synchronizer;
	// private volatile int trueCaretPosition;
	private static final long serialVersionUID = 1L;
	private Clipboard systemClipboard;
	
	public VTGraphicalConsoleTextArea(String text, int rows, int columns, int scrollbars)
	{
		super(text, rows, columns, scrollbars);
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		setIgnoreRepaint(true);
	}
	
	public void copyText()
	{
		try
		{
			StringSelection text = new StringSelection(VTGraphicalConsole.getSelectedText());
			systemClipboard.setContents(text, null);
			VTGraphicalConsole.updateCaretPosition();
			if (VTGraphicalConsole.isFlushInterrupted())
			{
				VTGraphicalConsole.clearCaretPosition();
			}
		}
		catch (Throwable ex)
		{
			
		}
	}
	
	public void pasteText()
	{
		try
		{
			if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
			{
				String text = systemClipboard.getData(DataFlavor.stringFlavor).toString();
				VTGraphicalConsole.input(text);
			}
			else if (systemClipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
			{
				@SuppressWarnings("unchecked")
				List<File> files = (List<File>) systemClipboard.getData(DataFlavor.javaFileListFlavor);
				if (files.size() > 0)
				{
					StringBuilder fileList = new StringBuilder();
					for (File file : files)
					{
						fileList.append(file.getAbsolutePath() + ";");
					}
					fileList.deleteCharAt(fileList.length() - 1);
					VTGraphicalConsole.input(fileList.toString());
				}
			}
		}
		catch (UnsupportedFlavorException e1)
		{
			
		}
		catch (IOException e1)
		{
			
		}
		catch (Throwable e1)
		{
			
		}
	}
	
//	public void repaint()
//	{
//		
//	}
//	
//	public void update(Graphics g)
//	{
//		this.paint
//	}
//	
//	public void paint()
//	{
//		
//	}
//	
//	public void paint(Graphics g)
//	{
//		
//    }
}