package org.vash.vate.console.graphical.listener;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTConsoleImplementation;

public class VTGraphicalConsoleCopyActionListener implements ActionListener
{
  private Clipboard systemClipboard;
  private final VTConsoleImplementation console;
  
  public VTGraphicalConsoleCopyActionListener(final VTConsoleImplementation console)
  {
    this.console = console;
    systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }
  
  public void actionPerformed(ActionEvent e)
  {
    
    try
    {
      StringSelection text = new StringSelection(console.getSelectedText());
      systemClipboard.setContents(text, null);
      // System.out.println("copy:" + text.toString());
      // VTConsole.flush();
    }
    catch (Throwable ex)
    {
      
    }
  }
}