package org.vash.vate.console.graphical;

import java.awt.TextArea;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.vash.vate.console.VTConsole;

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
//		this.addFocusListener(new FocusListener()
//		{
//			public void focusGained(FocusEvent e)
//			{
//				VTGraphicalConsole.deiconifyFrame();
//			}
//
//			public void focusLost(FocusEvent e)
//			{
//				VTGraphicalConsole.iconifyFrame();
//			}
//		});
  }
  
  public void copyText()
  {
    try
    {
      StringSelection text = new StringSelection(VTConsole.getSelectedText());
      systemClipboard.setContents(text, null);
      // VTConsole.flush();
//			VTGraphicalConsole.updateCaretPosition();
//			if (VTGraphicalConsole.isFlushInterrupted())
//			{
//				VTGraphicalConsole.clearCaretPosition();
//			}
    }
    catch (Throwable ex)
    {
      
    }
  }
  
  public void copyAllText()
  {
    try
    {
      StringSelection text = new StringSelection(VTConsole.getAllText());
      systemClipboard.setContents(text, null);
      // VTConsole.flush();
//			VTGraphicalConsole.updateCaretPosition();
//			if (VTGraphicalConsole.isFlushInterrupted())
//			{
//				VTGraphicalConsole.clearCaretPosition();
//			}
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
        VTConsole.input(text);
      }
      else if (systemClipboard.isDataFlavorAvailable(DataFlavor.javaFileListFlavor))
      {
        @SuppressWarnings("unchecked")
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