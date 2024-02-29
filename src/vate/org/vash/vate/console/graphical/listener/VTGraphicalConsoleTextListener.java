package org.vash.vate.console.graphical.listener;

// import org.vash.vate.terminal.graphical.VTGraphicalConsole;

import java.awt.event.TextEvent;
import java.awt.event.TextListener;

public class VTGraphicalConsoleTextListener implements TextListener
{
  // private VTGraphicalConsole terminal;
  
  public VTGraphicalConsoleTextListener(/* VTGraphicalConsole terminal */)
  {
    // this.listeningTextEvents = false;
    // this.terminal = terminal;
  }
  
  public void textValueChanged(TextEvent e)
  {
    // VTGraphicalConsole.appendExtraInput();
    // System.out.println("textEvent!");
  }
}
