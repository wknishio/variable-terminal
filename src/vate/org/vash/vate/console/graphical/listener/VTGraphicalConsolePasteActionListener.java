package org.vash.vate.console.graphical.listener;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.util.List;
import org.vash.vate.console.VTConsole;

public class VTGraphicalConsolePasteActionListener implements ActionListener
{
  private Clipboard systemClipboard;
  
  public VTGraphicalConsolePasteActionListener()
  {
    systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }
  
  @SuppressWarnings("unchecked")
  public void actionPerformed(ActionEvent e)
  {
    
    try
    {
      if (systemClipboard.isDataFlavorAvailable(DataFlavor.stringFlavor))
      {
        String text = systemClipboard.getData(DataFlavor.stringFlavor).toString();
        VTConsole.input(text);
        // System.out.println("paste:" + text);
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
          // System.out.println("paste:" + fileList.toString());
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
}
