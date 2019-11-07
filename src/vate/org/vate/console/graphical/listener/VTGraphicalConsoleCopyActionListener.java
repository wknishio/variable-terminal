package org.vate.console.graphical.listener;

import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.vate.console.VTConsole;
import org.vate.console.graphical.VTGraphicalConsole;

public class VTGraphicalConsoleCopyActionListener implements ActionListener
{
	private Clipboard systemClipboard;
	
	public VTGraphicalConsoleCopyActionListener()
	{
		systemClipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
	}
	
	public void actionPerformed(ActionEvent e)
	{
		try
		{
			StringSelection text = new StringSelection(VTConsole.getSelectedText());
			systemClipboard.setContents(text, null);
			VTConsole.flush();
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
}