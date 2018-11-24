package org.vate.client.graphicsmode.options.listener;

import java.awt.CheckboxMenuItem;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import org.vate.client.graphicsmode.VTGraphicsModeClientWriter;

public class VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeyCombinationsListener implements ItemListener
{
	private CheckboxMenuItem option;
	private VTGraphicsModeClientWriter writer;
	
	public VTGraphicsModeClientOptionsMenuBarControlMenuLocalKeyCombinationsListener(VTGraphicsModeClientWriter writer, CheckboxMenuItem option)
	{
		this.writer = writer;
		this.option = option;
	}
	
	public void itemStateChanged(ItemEvent e)
	{
		if (e.getStateChange() == ItemEvent.SELECTED)
		{
			writer.toggleSuppressLocalKeyCombinations();
		}
		else
		{
			option.setState(true);
		}
	}
}