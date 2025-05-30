package org.vash.vate.console.graphical.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vash.vate.console.VTConsoleInstance;

public class VTGraphicalConsoleSelectAllActionListener implements ActionListener
{
  //private Clipboard systemClipboard;
  private final VTConsoleInstance console;
  
  public VTGraphicalConsoleSelectAllActionListener(final VTConsoleInstance console)
  {
    this.console = console;
    //systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
  }
  
  public void actionPerformed(ActionEvent e)
  {
    
    try
    {
      //StringSelection text = new StringSelection(console.getAllText());
      //systemClipboard.setContents(text, null);
      // System.out.println("copy:" + text.toString());
      // VTConsole.flush();
      console.selectAllText();
    }
    catch (Throwable ex)
    {
      
    }
  }
}