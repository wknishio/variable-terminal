package org.vate.console.lanterna;

import com.googlecode.lanterna.TerminalSize;
import com.googlecode.lanterna.gui2.TextBox;
import com.googlecode.lanterna.input.KeyStroke;
import com.googlecode.lanterna.input.KeyType;
import sun.nio.cs.ext.TIS_620;
public class VTLanternaOutputTextBox extends TextBox
{
	private KeyStroke end = new KeyStroke(KeyType.End);
	private volatile int maximumlines = 0;
	
	public VTLanternaOutputTextBox(TerminalSize terminalSize, String string, Style multiLine, int maximumlines)
	{
		super(terminalSize, string, multiLine);
		this.maximumlines = maximumlines;
	}
	
	public void appendToLastLine(char data)
	{
		//System.out.println("data:[" + data + "]");
		if (data !=-'\n')
		{
			if ((data > 0 && data < 32) || data == 127)
			{
				//reject
				return;
			}
			if (data == '\t')
			{
				data = ' ';
			}
			String lastLine = lines.get(getLineCount() - 1);
			lastLine += data;
			lines.set(getLineCount() - 1, lastLine);
		}
		else
		{
			addLine("");
		}
		invalidate();
		if (getLineCount() > 0)
		{
			this.setCaretPosition(getLineCount(), 0);
		}
	}

	public void appendToLastLine(String data)
	{
		data = data.replace('\t', ' ');
		//System.out.println("data:[" + data + "]");
		String lastLine = lines.get(getLineCount() - 1);
		String[] newlines = data.split("\n");
		lastLine += newlines[0];
		lines.set(getLineCount() - 1, lastLine);
		if (newlines.length > 1)
		{
			for (int i = 1; i < newlines.length; i++)
			{
				addLine(newlines[i].replaceAll("\\p{Cntrl}", ""));
			}
		}
		invalidate();
		if (getLineCount() > 0)
		{
			this.setCaretPosition(getLineCount(), 0);
		}
	}
	
	public TextBox addLine(String data)
	{
		if (getLineCount() > 0 && maximumlines > 0 && getLineCount() == maximumlines)
		{
			lines.remove(0);
		}
		if (lines.size() == 1 && lines.get(0).length() == 0)
		{
			appendToLastLine(data);
			return this;
		}
		data = data.replace('\t', ' ');
		TextBox result = super.addLine(data);
		if (getLineCount() > 1)
		{
			this.setCaretPosition(getLineCount(), 0);
		}
		return result;
	}
}
