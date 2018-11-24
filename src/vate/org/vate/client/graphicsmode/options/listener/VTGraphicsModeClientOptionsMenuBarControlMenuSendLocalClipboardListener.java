package org.vate.client.graphicsmode.options.listener;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarControlMenuSendLocalClipboardListener implements ActionListener
{
	private VTGraphicsModeClientWriter writer;
	
	public VTGraphicsModeClientOptionsMenuBarControlMenuSendLocalClipboardListener(VTGraphicsModeClientWriter writer)
	{
		this.writer = writer;
	}
	
	public void actionPerformed(ActionEvent e)
	{
		writer.sendClipboardContents();
	}
}